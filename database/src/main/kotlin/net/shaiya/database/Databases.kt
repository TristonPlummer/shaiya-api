package net.shaiya.database

import mu.KLogging
import org.jetbrains.exposed.sql.Database

/**
 * @author Triston Plummer ("Cups")
 *
 * Holds the various databases used through Shaiya
 */
class Databases {

    /**
     * Represents the PS_GameData database
     */
    lateinit var gameDataDatabase: Database
        private set

    /**
     * Represents the PS_UserData database
     */
    lateinit var userDataDatabase: Database
        private set

    /**
     * Initialises the [Database] connections
     *
     * @param host  The database host
     * @param port  The database port
     * @param user  The database user
     * @param pass  The database password
     */
    fun init(host: String, port: Int, user: String, pass: String) {

        // The SQL Driver and JDBC URL
        val driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
        val jdbcUrl = "jdbc:sqlserver://$host:$port;DatabaseName="

        gameDataDatabase = Database.connect("${jdbcUrl}PS_GameData", driver, user, pass)
        userDataDatabase = Database.connect("${jdbcUrl}PS_UserData", driver, user, pass)
    }

    /**
     * The companion object for this class, which is used to add
     * logging functionality
     */
    companion object: KLogging()
}