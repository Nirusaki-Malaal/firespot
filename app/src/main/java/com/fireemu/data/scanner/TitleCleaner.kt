package com.fireemu.data.scanner

object TitleCleaner {
    // Regex matching (USA), (Japan, USA), [!], [b1], (En,Ja,Fr), (Rev A), etc.
    private val TAG_REGEX = Regex("""(\([^\)]*\)|\[[^\]]*\])""")
    private val MULTI_SPACE_REGEX = Regex("""\s+""")

    fun clean(filename: String): String {
        // Strip extension
        val nameWithoutExt = filename.substringBeforeLast('.')
        // Replace underscores with spaces
        val withSpaces = nameWithoutExt.replace('_', ' ')
        // Remove parenthesized and bracketed tags
        val noTags = TAG_REGEX.replace(withSpaces, "")
        // Normalize whitespace and trim
        val cleaned = MULTI_SPACE_REGEX.replace(noTags, " ").trim()
        return if (cleaned.isNotEmpty()) cleaned else nameWithoutExt
    }
}
