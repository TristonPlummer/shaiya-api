package net.shaiya.discord

import mu.KLogging
import java.nio.file.Paths

/**
 * @author Triston Plummer ("Cups")
 *
 * Handles the initialisation of the Shaiya Discord bot.
 */
object Launcher : KLogging() {

    /**
     * The entry point of the Shaiya Discord bot.
     *
     * @param args  The command-line arguments
     */
    @JvmStatic fun main(args: Array<String>) {
        val bot = Bot()
        bot.startBot(config = Paths.get("shaiya.yml"))
    }
}