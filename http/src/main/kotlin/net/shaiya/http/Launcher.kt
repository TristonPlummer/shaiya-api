package net.shaiya.http

import mu.KLogging
import java.nio.file.Paths

/**
 * @author Triston Plummer ("Cups")
 *
 * Handles the initialisation of the Shaiya HTTP API
 */
object Launcher : KLogging() {

    /**
     * The entry point of the Shaiya HTTP API.
     *
     * @param args  The command-line arguments
     */
    @JvmStatic fun main(args: Array<String>) {
        val server = ApiServer()
        server.startServer(config = Paths.get("shaiya.yml"))
    }
}