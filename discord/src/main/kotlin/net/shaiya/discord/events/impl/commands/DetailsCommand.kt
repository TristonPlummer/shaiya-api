package net.shaiya.discord.events.impl.commands

import com.google.gson.Gson
import com.google.inject.Inject
import mu.KLogging
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel
import net.shaiya.database.Databases
import net.shaiya.database.gamedata.Chars
import net.shaiya.database.gamedata.UserMaxGrow
import net.shaiya.database.userdata.UsersMaster
import net.shaiya.discord.events.impl.DiscordChatCommand
import net.shaiya.discord.ext.toTitleCase
import net.shaiya.discord.models.ShaiyaPosition
import net.shaiya.discord.models.ShaiyaUser
import net.shaiya.queue.QueueTask
import net.shaiya.util.shaiya.ShaiyaClass
import net.shaiya.util.shaiya.ShaiyaFaction
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.json.JSONObject
import org.redisson.api.RedissonClient
import java.awt.Color
import java.util.concurrent.TimeUnit

/**
 * @author Triston Plummer ("Cups")
 *
 * @param client    The [JDA] Discord client instance
 * @param databases The [Databases] instance
 * @param redis     The [RedissonClient] Redis client instance
 */
class DetailsCommand @Inject constructor(client: JDA, private val databases: Databases, private val redis: RedissonClient): DiscordChatCommand(client, "details") {

    /**
     * The [Gson] instance
     */
    private val gson = Gson()

    /**
     * Executes this [DetailsCommand]
     *
     * @param task      The [QueueTask] instance
     * @param author    The author executing the command
     * @param channel   The channel the message was sent in
     * @param args      The command arguments
     */
    override suspend fun execute(task: QueueTask, author: Member, channel: TextChannel, args: Array<String>) {

        // The name of the character to get details for
        val name = args[0]
        val nameLower = name.toLowerCase()

        // The ShaiyaUser instance
        val user: ShaiyaUser?

        // Attempt
        val dataBucket = redis.getBucket<String>(nameLower)
        if (dataBucket.isExists) {

            // Grab the cached user from Redis
            user = gson.fromJson(dataBucket.get(), ShaiyaUser::class.java)

        } else {

            // Build the JSON request payload
            val json = JSONObject()
            json.put("name", nameLower)

            // Publish a request to the author details topic, so the game can try to cache the live author
            val topic = redis.getTopic("user_details")
            topic.publish(json.toString())

            // Inform the user that we are waiting to get the data
            channel.sendMessage("Attempting to retrieve data for $name...").queue()

            // The user instance
            user = getUserFromDatabase(nameLower)

            // If the user was successfully retrieved, cache it for 10 minutes
            if (user != null) {

                // Wait 1 second
                task.wait(1)

                // The Redis data
                val cached = redis.getBucket<String>(nameLower)

                // Cache the result if it doesn't exist
                if (!cached.isExists) cached.set(gson.toJson(user), 10, TimeUnit.MINUTES)
            }
        }

        // If the author was not retrieved
        if (user == null) {
            channel.sendMessage("${author.asMention} - The user '$name' doesn't exist.").queue()
            return
        }

        // Write the user's details
        writeUserDetails(channel, user)
    }

    /**
     * Writes the [ShaiyaUser] details to the channel
     *
     * @param channel   The channel to respond in
     * @param user      The [ShaiyaUser] instance
     */
    private fun writeUserDetails(channel: TextChannel, user: ShaiyaUser) {

        // The faction and class
        val faction = ShaiyaFaction.byId(user.faction)!!
        val job = ShaiyaClass.byId(user.job)!!

        // The embedded message builder
        val message = EmbedBuilder()
        message.setTitle("Character Details - ${user.charName}")
        message.setColor(if (faction == ShaiyaFaction.LIGHT) allianceOfLightColour else unionOfFuryColour)
        message.addField("Faction", faction.name.toTitleCase(), true)
        message.addField("Class", job.name.toTitleCase(), true)
        message.addField("Level", "${user.level}", true)
        message.addField("Kills", "${user.kills}", true)
        message.addField("Victories", "${user.victories}", true)
        message.addField("Map", "${user.position.map}", true)

        // Send the embedded message
        channel.sendMessage(message.build()).queue()
    }

    /**
     * Gets a [ShaiyaUser] from the database.
     *
     * @param name  The name of the user to search for
     * @return      The [ShaiyaUser] instance
     */
    private fun getUserFromDatabase(name: String) : ShaiyaUser? {
        var user: ShaiyaUser? = null
        transaction(databases.gameDataDatabase) {
            val result = Chars.join(UserMaxGrow, JoinType.INNER, Chars.userId, UserMaxGrow.userId).select { (Chars.deleted eq false) and (Chars.name eq name) }.firstOrNull()
            if (result != null) {

                // The position of the user
                val position = ShaiyaPosition(map = result[Chars.map], x = result[Chars.posX], z = result[Chars.posZ], height = result[Chars.posHeight])

                // The status of the user
                val status: Int by lazy {
                    transaction(databases.userDataDatabase) {
                        val statusResult = UsersMaster.select { UsersMaster.userId eq result[Chars.userId] }.first()
                        statusResult[UsersMaster.status]
                    }
                }

                // The user instance
                user = ShaiyaUser(
                        charId = result[Chars.id],
                        charName = result[Chars.name],
                        username = result[Chars.username],
                        userId = result[Chars.userId],
                        position = position,
                        faction = result[UserMaxGrow.faction].id,
                        race = result[Chars.race].id,
                        job = result[Chars.job].id,
                        level = result[Chars.level],
                        kills = result[Chars.kills],
                        deaths = result[Chars.deaths],
                        victories = result[Chars.victories],
                        defeats = result[Chars.defeats],
                        status = status
                )
            }
        }

        return user
    }


    /**
     * The default suggested usage of the [DiscordChatCommand]
     */
    override fun getUsage(): String = "details CharacterName"

    /**
     * Adds logging functionality to this instance, and defines some
     * constant values.
     */
    companion object: KLogging() {

        /**
         * The sidebar colour to use for Alliance of Light
         */
        val allianceOfLightColour = Color.decode("#1A75FF")

        /**
         * The sidebar colour to use for Union of Fury
         */
        val unionOfFuryColour = Color.decode("#FF3300")
    }
}