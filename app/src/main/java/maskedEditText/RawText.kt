package maskedEditText

@Suppress("NAME_SHADOWING")
class RawText {
    var text = ""
        private set

    fun subtractFromString(range: Range) {
        var firstPart = ""
        var lastPart = ""
        if (range.start > 0 && range.start <= text.length) {
            firstPart = text.substring(0, range.start)
        }
        if (range.end >= 0 && range.end < text.length) {
            lastPart = text.substring(range.end)
        }
        text = firstPart + lastPart
    }

    fun addToString(newString: String?, start: Int, maxLength: Int): Int {
        var newString = newString
        var firstPart = ""
        var lastPart = ""
        if (newString == null || newString == "") {
            return 0
        } else require(start >= 0) { "Start position must be non-negative" }
        require(start <= text.length) { "Start position must be less than the actual text length" }
        var count = newString.length
        if (start > 0) {
            firstPart = text.substring(0, start)
        }
        if (start < text.length) {
            lastPart = text.substring(start)
        }
        if (text.length + newString.length > maxLength) {
            count = maxLength - text.length
            newString = newString.substring(0, count)
        }
        text = firstPart + newString + lastPart
        return count
    }

    fun length(): Int {
        return text.length
    }

    fun charAt(position: Int): Char {
        return text[position]
    }
}