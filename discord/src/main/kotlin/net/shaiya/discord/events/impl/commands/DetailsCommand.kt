package net.shaiya.discord.events.impl.commands

import com.google.inject.Inject
import mu.KLogging
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel
import net.shaiya.discord.events.impl.DiscordChatCommand
import net.shaiya.discord.ext.toTitleCase
import net.shaiya.discord.models.ShaiyaUser
import net.shaiya.queue.QueueTask
import net.shaiya.repository.UserRepository
import net.shaiya.util.shaiya.ShaiyaClass
import net.shaiya.util.shaiya.ShaiyaFaction
import org.redisson.api.RedissonClient
import java.awt.Color

/**
 * @author Triston Plummer ("Cups")
 *
 * @param client        The [JDA] Discord client instance
 * @param repository    The [UserRepository] instance
 * @param redis         The [RedissonClient] Redis client instance
 */
class DetailsCommand @Inject constructor(client: JDA, private val repository: UserRepository, private val redis: RedissonClient): DiscordChatCommand(client, "details") {

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

        // The user instance
        val user = repository.getUser(name)

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
        private val allianceOfLightColour = Color.decode("#1A75FF")!!

        /**
         * The sidebar colour to use for Union of Fury
         */
        private val unionOfFuryColour = Color.decode("#FF3300")!!
    }
}