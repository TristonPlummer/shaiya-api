package net.shaiya.database.userdata

import net.shaiya.database.Databases
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents the PS_UserData.dbo.Users_Master table. Use this with the [Databases.userDataDatabase] connection
 */
object UsersMaster: Table("Users_Master") {
    val userId: Column<Int> = integer("UserUID")
    val status: Column<Int> = integer("Status")
}