package net.shaiya.discord.ext

import com.github.binarywang.java.emoji.EmojiConverter
import org.apache.commons.text.WordUtils

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
 * The [EmojiConverter] instance
 */
private val emoji = EmojiConverter.getInstance()

/**
 * Converts a [String] to title case, ie: "sHaiya" to "Shaiya"
 *
 * @return  The new [String] instance
 */
fun String.toTitleCase() : String {
    return WordUtils.capitalize(this)
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