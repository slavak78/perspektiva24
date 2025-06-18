package ru.crmkurgan.main.utils

import android.widget.FrameLayout
import android.view.WindowInsets
import androidx.core.util.ObjectsCompat
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet

@Suppress("DEPRECATION")
class FrameView : FrameLayout {
    private var mLastInsets: Any? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (!ObjectsCompat.equals(mLastInsets, insets)) {
            mLastInsets = insets
            requestLayout()
        }
        return insets.consumeSystemWindowInsets()
    }

    @Deprecated("Deprecated in Java")
    override fun fitSystemWindows(insets: Rect): Boolean {
        if (!ObjectsCompat.equals(mLastInsets, insets)) {
            if (mLastInsets == null) {
                mLastInsets = Rect(insets)
            } else {
                (mLastInsets as Rect).set(insets)
            }
            requestLayout()
        }
        return true
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mLastInsets != null) {
            val wi = mLastInsets as WindowInsets
            val childCount = childCount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child.visibility != GONE) {
                    child.dispatchApplyWindowInsets(wi)
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}