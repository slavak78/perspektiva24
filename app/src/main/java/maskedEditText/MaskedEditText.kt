package maskedEditText

import androidx.appcompat.widget.AppCompatEditText
import android.text.TextWatcher
import android.widget.TextView.OnEditorActionListener
import android.widget.TextView
import ru.crmkurgan.main.R
import android.os.Parcelable
import android.os.Bundle
import android.content.ContentValues
import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import java.lang.RuntimeException
import java.lang.StringBuilder

@Suppress("NAME_SHADOWING")
class MaskedEditText : AppCompatEditText, TextWatcher {
    private val onEditorActionListener =
        OnEditorActionListener { _: TextView?, _: Int, _: KeyEvent? -> true }
    private var mask: String? = null
    private var charRepresentation = 0.toChar()
    private var keepHint = false
    private lateinit var rawToMask: IntArray
    private var rawText: RawText? = null
    private var editingBefore = false
    private var editingOnChanged = false
    private var editingAfter = false
    private lateinit var maskToRaw: IntArray
    private var selection1 = 0
    private var initialized = false
    private var ignore = false
    private var maxRawLength = 0
    private var lastValidMaskPosition = 0
    private var selectionChanged = false
    private var focusChangeListener: OnFocusChangeListener? = null
    private var allowedChars: String? = null
    private var deniedChars: String? = null
    private var isKeepingText = false

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.MaskedEditText)
        mask = attributes.getString(R.styleable.MaskedEditText_mask)
        allowedChars = attributes.getString(R.styleable.MaskedEditText_allowed_chars)
        deniedChars = attributes.getString(R.styleable.MaskedEditText_denied_chars)
        val enableImeAction =
            attributes.getBoolean(R.styleable.MaskedEditText_enable_ime_action, false)
        val representation = attributes.getString(R.styleable.MaskedEditText_char_representation)
        charRepresentation = if (representation == null) {
            '#'
        } else {
            representation[0]
        }
        keepHint = attributes.getBoolean(R.styleable.MaskedEditText_keep_hint, false)
        cleanUp()

        if (!enableImeAction) {
            setOnEditorActionListener(onEditorActionListener)
        } else {
            setOnEditorActionListener(null)
        }
        attributes.recycle()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superParcelable = super.onSaveInstanceState()
        val state = Bundle()
        state.putParcelable("super", superParcelable)
        state.putString("text", getRawText())
        state.putBoolean("keepHint", isKeepHint())
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        keepHint = bundle.getBoolean("keepHint", false)
        super.onRestoreInstanceState(state.getParcelable("super"))
        val text = bundle.getString("text")
        setText(text)
        Log.d(ContentValues.TAG, "onRestoreInstanceState: $text")
    }

    override fun setText(text: CharSequence, type: BufferType) {
//		if (text == null || text.equals("")) return;
        super.setText(text, type)
    }

    /** @param listener - its onFocusChange() method will be called before performing MaskedEditText operations,
     * related to this event.
     */
    override fun setOnFocusChangeListener(listener: OnFocusChangeListener) {
        focusChangeListener = listener
    }

    private fun cleanUp() {
        initialized = false
        if (mask == null || mask!!.isEmpty()) {
            return
        }
        generatePositionArrays()
        if (!isKeepingText || rawText == null) {
            rawText = RawText()
            selection1 = rawToMask[0]
        }
        editingBefore = true
        editingOnChanged = true
        editingAfter = true
        if (hasHint() && rawText!!.length() == 0) {
            this.setText(makeMaskedTextWithHint())
        } else {
            this.setText(makeMaskedText())
        }
        editingBefore = false
        editingOnChanged = false
        editingAfter = false
        maxRawLength = maskToRaw[previousValidPosition(mask!!.length - 1)] + 1
        lastValidMaskPosition = findLastValidMaskPosition()
        initialized = true
        super.setOnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (focusChangeListener != null) {
                focusChangeListener!!.onFocusChange(v, hasFocus)
            }
            if (hasFocus()) {
                selectionChanged = false
                this@MaskedEditText.setSelection(lastValidPosition())
            }
        }
    }

    private fun findLastValidMaskPosition(): Int {
        for (i in maskToRaw.indices.reversed()) {
            if (maskToRaw[i] != -1) return i
        }
        throw RuntimeException("Mask must contain at least one representation char")
    }

    private fun hasHint(): Boolean {
        return hint != null
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        init()
    }

    fun setShouldKeepText(shouldKeepText: Boolean) {
        isKeepingText = shouldKeepText
    }

    fun setMask(mask: String?) {
        this.mask = mask
        cleanUp()
    }

    fun getMask(): String? {
        return mask
    }

    fun setImeActionEnabled(isEnabled: Boolean) {
        if (isEnabled) setOnEditorActionListener(onEditorActionListener) else setOnEditorActionListener(
            null
        )
    }

    private fun getRawText(): String {
        return rawText!!.text
    }

    fun setCharRepresentation(charRepresentation: Char) {
        this.charRepresentation = charRepresentation
        cleanUp()
    }

    fun getCharRepresentation(): Char {
        return charRepresentation
    }

    private fun generatePositionArrays() {
        val aux = IntArray(mask!!.length)
        maskToRaw = IntArray(mask!!.length)
        var charsInMaskAux = ""
        var charIndex = 0
        for (i in 0 until mask!!.length) {
            val currentChar = mask!![i]
            if (currentChar == charRepresentation) {
                aux[charIndex] = i
                maskToRaw[i] = charIndex++
            } else {
                val charAsString = currentChar.toString()
                if (!charsInMaskAux.contains(charAsString)) {
                    charsInMaskAux += charAsString
                }
                maskToRaw[i] = -1
            }
        }
        if (charsInMaskAux.indexOf(' ') < 0) {
            charsInMaskAux += SPACE
        }
        val charsInMask = charsInMaskAux.toCharArray()
        rawToMask = IntArray(charIndex)
        System.arraycopy(aux, 0, rawToMask, 0, charIndex)
    }

    private fun init() {
        addTextChangedListener(this)
    }

    override fun beforeTextChanged(
        s: CharSequence, start: Int, count: Int,
        after: Int
    ) {
        if (!editingBefore) {
            editingBefore = true
            if (start > lastValidMaskPosition) {
                ignore = true
            }
            var rangeStart = start
            if (after == 0) {
                rangeStart = erasingStart(start)
            }
            val range = calculateRange(rangeStart, start + count)
            if (range.start != -1) {
                rawText!!.subtractFromString(range)
            }
            if (count > 0) {
                selection1 = previousValidPosition(start)
            }
        }
    }

    private fun erasingStart(start: Int): Int {
        var start = start
        while (start > 0 && maskToRaw[start] == -1) {
            start--
        }
        return start
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        var count = count
        if (!editingOnChanged && editingBefore) {
            editingOnChanged = true
            if (ignore) {
                return
            }
            if (count > 0) {
                val startingPosition = maskToRaw[nextValidPosition(start)]
                val addedString = s.subSequence(start, start + count).toString()
                count = rawText!!.addToString(clear(addedString), startingPosition, maxRawLength)
                if (initialized) {
                    val currentPosition: Int =
                        if (startingPosition + count < rawToMask.size) rawToMask[startingPosition + count] else lastValidMaskPosition + 1
                    selection1 = nextValidPosition(currentPosition)
                }
            }
        }
    }

    override fun afterTextChanged(s: Editable) {
        if (!editingAfter && editingBefore && editingOnChanged) {
            editingAfter = true
            if (hasHint() && (keepHint || rawText!!.length() == 0)) {
                setText(makeMaskedTextWithHint())
            } else {
                setText(makeMaskedText())
            }
            selectionChanged = false
            setSelection(selection1)
            editingBefore = false
            editingOnChanged = false
            editingAfter = false
            ignore = false
        }
    }

    private fun isKeepHint(): Boolean {
        return keepHint
    }

    fun setKeepHint(keepHint: Boolean) {
        this.keepHint = keepHint
        setText(getRawText())
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        var selStart = selStart
        var selEnd = selEnd
        if (initialized) {
            if (!selectionChanged) {
                selStart = fixSelection(selStart)
                selEnd = fixSelection(selEnd)

                // exactly in this order. If getText.length() == 0 then selStart will be -1
                if (selStart > text!!.length) selStart = text!!.length
                if (selStart < 0) selStart = 0

                // exactly in this order. If getText.length() == 0 then selEnd will be -1
                if (selEnd > text!!.length) selEnd = text!!.length
                if (selEnd < 0) selEnd = 0
                setSelection(selStart, selEnd)
                selectionChanged = true
            } else {
                //check to see if the current selection is outside the already entered text
                if (selStart > rawText!!.length() - 1) {
                    val start = fixSelection(selStart)
                    val end = fixSelection(selEnd)
                    if (start >= 0 && end < text!!.length) {
                        setSelection(start, end)
                    }
                }
            }
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    private fun fixSelection(selection: Int): Int {
        return if (selection > lastValidPosition()) {
            lastValidPosition()
        } else {
            nextValidPosition(selection)
        }
    }

    private fun nextValidPosition(currentPosition: Int): Int {
        var currentPosition = currentPosition
        while (currentPosition < lastValidMaskPosition && maskToRaw[currentPosition] == -1) {
            currentPosition++
        }
        return if (currentPosition > lastValidMaskPosition) lastValidMaskPosition + 1 else currentPosition
    }

    private fun previousValidPosition(currentPosition: Int): Int {
        var currentPosition = currentPosition
        while (currentPosition >= 0 && maskToRaw[currentPosition] == -1) {
            currentPosition--
            if (currentPosition < 0) {
                return nextValidPosition(0)
            }
        }
        return currentPosition
    }

    private fun lastValidPosition(): Int {
        return if (rawText!!.length() == maxRawLength) {
            rawToMask[rawText!!.length() - 1] + 1
        } else nextValidPosition(rawToMask[rawText!!.length()])
    }

    private fun makeMaskedText(): String {
        val maskedTextLength: Int = if (rawText!!.length() < rawToMask.size) {
            rawToMask[rawText!!.length()]
        } else {
            mask!!.length
        }
        val maskedText =
            CharArray(maskedTextLength) //mask.replace(charRepresentation, ' ').toCharArray();
        for (i in maskedText.indices) {
            val rawIndex = maskToRaw[i]
            if (rawIndex == -1) {
                maskedText[i] = mask!![i]
            } else {
                maskedText[i] = rawText!!.charAt(rawIndex)
            }
        }
        return String(maskedText)
    }

    private fun makeMaskedTextWithHint(): CharSequence {
        val ssb = SpannableStringBuilder()
        var mtrv: Int
        val maskFirstChunkEnd = rawToMask[0]
        for (i in 0 until mask!!.length) {
            mtrv = maskToRaw[i]
            if (mtrv != -1) {
                if (mtrv < rawText!!.length()) {
                    ssb.append(rawText!!.charAt(mtrv))
                } else {
                    ssb.append(hint[maskToRaw[i]])
                }
            } else {
                ssb.append(mask!![i])
            }
            if (keepHint && rawText!!.length() < rawToMask.size && i >= rawToMask[rawText!!.length()]
                || !keepHint && i >= maskFirstChunkEnd
            ) {
                ssb.setSpan(ForegroundColorSpan(currentHintTextColor), i, i + 1, 0)
            }
        }
        return ssb
    }

    private fun calculateRange(start: Int, end: Int): Range {
        val range = Range()
        var i = start
        while (i <= end && i < mask!!.length) {
            if (maskToRaw[i] != -1) {
                if (range.start == -1) {
                    range.start = maskToRaw[i]
                }
                range.end = maskToRaw[i]
            }
            i++
        }
        if (end == mask!!.length) {
            range.end = rawText!!.length()
        }
        if (range.start == range.end && start < end) {
            val newStart = previousValidPosition(range.start - 1)
            if (newStart < range.start) {
                range.start = newStart
            }
        }
        return range
    }

    private fun clear(string: String): String {
        var string = string
        if (deniedChars != null) {
            for (c in deniedChars!!.toCharArray()) {
                string = string.replace(c.toString(), "")
            }
        }
        if (allowedChars != null) {
            val builder = StringBuilder(string.length)
            for (c in string.toCharArray()) {
                if (allowedChars!!.contains(c.toString())) {
                    builder.append(c)
                }
            }
            string = builder.toString()
        }
        return string
    }

    companion object {
        const val SPACE = " "
    }
}