package net.shaiya.database.gamedata

import net.shaiya.database.Databases
import net.shaiya.util.shaiya.ShaiyaFaction
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents the PS_GameData.dbo.UserMaxGrow table. Use this with the [Databases.gameDataDatabase] connection
 */
object UserMaxGrow: Table() {
    val serverId:   Column<Int>             = integer("ServerId")
    val userId:     Column<Int>             = integer("UserUID")
    val faction:    Column<ShaiyaFaction>   = enumeration("Country", ShaiyaFaction::class)
    val deleted:    Column<Boolean>         = bool("Del")
}