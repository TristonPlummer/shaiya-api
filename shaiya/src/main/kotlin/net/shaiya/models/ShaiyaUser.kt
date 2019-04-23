package net.shaiya.discord.models

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents a player in Shaiya
 *
 * @param charId    The character id
 * @param charName  The name of the character
 * @param username  The name of the character's account
 * @param userId    The id of the character's id
 * @param position  The [ShaiyaPosition] instance
 * @param faction   The faction id
 * @param race      The race id
 * @param job       The class id
 * @param level     The level of the character
 * @param kills     The number of kills earned by the character
 * @param deaths    The number of deaths accumulated by the character
 * @param victories The number of victories the character has
 * @param defeats   The number of defeats the character has
 * @param status    The privilege level of the account
 */
data class ShaiyaUser(
        val charId: Int, val charName: String, val username: String, val userId: Int, val position: ShaiyaPosition,
        val faction: Int, val race: Int, val job: Int, val level: Int, val kills: Int, val deaths: Int, val victories: Int,
        val defeats: Int, val status: Int)