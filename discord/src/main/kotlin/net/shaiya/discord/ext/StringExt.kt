package net.shaiya.discord.ext

import com.github.binarywang.java.emoji.EmojiConverter
import org.apache.commons.text.WordUtils
import java.util.regex.Pattern

/**
 * A [Regex] used for matching the tail end of a Discord emoji string
 */
val emojiTailRegex = Regex("[0-9]+>")

/**
 * A [Regex] used for matching the start of a Discord emoji string
 */
val emojiHeadRegex = Regex("^(<[a]?:)")

/**
 * A [Regex] used for matching multiple whitespace characters in a row
 */
val whitespaceRegex = Regex("\\s+/g")

/**
 * A [Regex] used for seperating command arguments by whitespace and quotes
 */
val commandRegex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'")!!

/**
 * The [EmojiConverter] instance
 */
private val emoji = EmojiConverter.getInstance()

/**
 * Converts a [String] to title case, ie: "sHaiya" to "Shaiya"
 *
 * @return  The new [String] instance
 */
fun String.toTitleCase() : String {
    return WordUtils.capitalize(this.toLowerCase())
}

/**
 * "Cleans" a string by replacing emojis with their text aliases, and stripping
 * Discord emoji code.
 */
fun String.clean() : String = emoji.toAlias(
        this.replace(emojiTailRegex, "")
        .replace(emojiHeadRegex, ":")
        .replace(whitespaceRegex, " ")
)

/**
 * Splits a command by spaces, unless they are surrounded by quotes
 *
 * @return  The list of arguments
 */
fun String.commandSplit() : List<String> {
    val args = mutableListOf<String>()
    val matcher = commandRegex.matcher(this)
    while (matcher.find()) {
        when {
            matcher.group(1) != null -> args.add(matcher.group(1))
            matcher.group(2) != null -> args.add(matcher.group(2))
            else -> args.add(matcher.group())
        }
    }
    return args
}