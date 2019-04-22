package net.shaiya.database.gamedata

import net.shaiya.database.Databases
import net.shaiya.util.shaiya.ShaiyaClass
import net.shaiya.util.shaiya.ShaiyaGender
import net.shaiya.util.shaiya.ShaiyaRace
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents the PS_GameData.dbo.Chars table. Use this with the [Databases.gameDataDatabase] connection
 */
object Chars: Table() {
    val id:             Column<Int>             = integer("CharID").autoIncrement().primaryKey()
    val name:           Column<String>          = varchar("CharName", 30)
    val username:       Column<String>          = varchar("UserID", 12)
    val userId:         Column<Int>             = integer("UserUID")
    val deleted:        Column<Boolean>         = bool("Del")
    val race:           Column<ShaiyaRace>      = enumeration("Family", ShaiyaRace::class)
    val mode:           Column<Int>             = integer("Grow")
    val hair:           Column<Int>             = integer("Hair")
    val face:           Column<Int>             = integer("Face")
    val height:         Column<Int>             = integer("Size")
    val job:            Column<ShaiyaClass>     = enumeration("Job", ShaiyaClass::class)
    val gender:         Column<ShaiyaGender>    = enumeration("Sex", ShaiyaGender::class)
    val level:          Column<Int>             = integer("Level")
    val statpoints:     Column<Int>             = integer("StatPoint")
    val skillpoints:    Column<Int>             = integer("SkillPoint")
    val strength:       Column<Int>             = integer("Str")
    val dexterity:      Column<Int>             = integer("Dex")
    val resistance:     Column<Int>             = integer("Rec")
    val intelligence:   Column<Int>             = integer("Int")
    val luck:           Column<Int>             = integer("Luc")
    val wisdom:         Column<Int>             = integer("Wis")
    val hitpoints:      Column<Int>             = integer("HP")
    val manapoints:     Column<Int>             = integer("MP")
    val staminapoints:  Column<Int>             = integer("SP")
    val map:            Column<Int>             = integer("Map")
    val direction:      Column<Int>             = integer("Dir")
    val experience:     Column<Int>             = integer("Exp")
    val gold:           Column<Int>             = integer("Money")
    val posX:           Column<Float>           = float("PosX")
    val posZ:           Column<Float>           = float("Posz")
    val posHeight:      Column<Float>           = float("PosY")
    val kills:          Column<Int>             = integer("K1")
    val deaths:         Column<Int>             = integer("K2")
    val victories:      Column<Int>             = integer("K3")
    val defeats:        Column<Int>             = integer("K4")
    val pvpRank:        Column<Int>             = integer("KillLevel")
    val deathRank:      Column<Int>             = integer("DeadLevel")
    val creationDate:   Column<DateTime>        = date("RegDate")
    val deletionDate:   Column<DateTime>        = date("DeleteDate")
    val lastLoginDate:  Column<DateTime>        = date("JoinDate")
    val lastLogoutDate: Column<DateTime>        = date("LeaveDate")
    val previousName:   Column<String>          = varchar("OldCharName", 30)
    val online:         Column<Boolean>         = bool("LoginStatus")
}