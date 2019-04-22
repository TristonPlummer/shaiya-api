package net.shaiya.discord.events.impl

import com.google.gson.Gson
import com.google.inject.Inject
import mu.KLogging
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.shaiya.discord.events.DiscordEvent
import net.shaiya.discord.ext.clean
import org.redisson.api.RedissonClient
import java.io.IOException

/**
 * @author Triston Plummer ("Cups")
 *
 * Handles the interaction between trade chat channels and the game world.
 *
 * @param client    The [JDA] instance
 * @param redis     The [RedissonClient] instance
 */
class TradeChatEvent @Inject constructor(private val client: JDA, private val redis: RedissonClient) : DiscordEvent() {

    /**
     * The channel id for the Alliance of Light
     */
    private var lightChannel: String = ""

    /**
     * The channel id for the Union of Fury
     */
    private var furyChannel: String = ""

    /**
     * The Redis topic for messages from Discord to the game
     */
    private var discordTopic: String = ""

    /**
     * The Redis topic for messages from the game to Discord
     */
    private var gameTopic: String = ""

    /**
     * The [Gson] instance for converting to and from JSON
     */
    private val gson = Gson()

    /**
     * Initialises this [TradeChatEvent] instance.
     *
     * @param config    The map of Discord configuration
     */
    override fun init(config: Map<String, Any?>) {
        lightChannel    = config["light-channel"]   as String? ?: throw IOException("Light channel name not specified")
        furyChannel     = config["fury-channel"]    as String? ?: throw IOException("Fury channel name not specified")
        discordTopic    = config["discord-topic"]   as String? ?: throw IOException("Discord Redis topic not specified")
        gameTopic       = config["game-topic"]      as String? ?: throw IOException("Game Redis topic not specified")

        // Listen to incoming Discord trade chat message
        client.addEventListener(object : ListenerAdapter() {
            override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
                val channel = event.channel.name
                if (event.author.isBot) return
                if (channel != lightChannel && channel != furyChannel) return

                dispatch { onDiscordChat(event.author, event.channel, event.message) }
            }
        })

        // Listen to trade chat messages from the game server
        val gameTopic = redis.getTopic(gameTopic)
        gameTopic.addListener(String::class.java) { _, msg -> onGameChat(msg) }
    }

    /**
     * Represents a trade chat message
     *
     * @param faction   The faction of the user
     * @param name      The username of the user
     * @param msg       The message
     */
    data class TradeChatMessage(val faction: Int, val name: String, val msg: String)

    /**
     * Handles a trade chat event.
     *
     * @param author    The user who wrote the message
     * @param channel   The channel the message was sent in
     * @param message   The message instance
     */
    private fun onDiscordChat(author: User, channel: MessageChannel, message: Message) {

        // The faction, display name and message content
        val faction = if (channel.name == lightChannel) 0 else 1
        var name    = author.name.clean()
        name = name.substring(0, Math.min(name.length, 30))
        val content = message.strippedContent.clean()

        // If the message is longer than the game can display, inform the user and delete the message
        if (content.length > MAX_MESSAGE_LENGTH) {
            message.delete().queue()

            val direct = author.openPrivateChannel().complete()
            direct.sendMessage("Your message was ${content.length} characters long but the maximum number that can show in-game is $MAX_MESSAGE_LENGTH. Please keep your messages short, as in-game players won't be able to see longer messages. The message has been deleted.\nThe message you tried to send was: ```$content```")
            return
        }

        // Publish the trade message to the game's Redis channel
        val json = gson.toJson(TradeChatMessage(faction, name, "[Discord] $content"))
        val topic = redis.getTopic(discordTopic)
        topic.publishAsync(json)
    }

    /**
     * Handles a message sent from a the game's Redis channel
     *
     * @param message   The [TradeChatMessage] instance, serialized as JSON
     */
    private fun onGameChat(message: String) {

        // The chat message instance
        val data = gson.fromJson(message, TradeChatMessage::class.java)
        val discordChannel = if (data.faction == 0) lightChannel else furyChannel
        val content = data.msg.clean().replace("@everyone", "@ everyone").replace("@here", "@ here")

        // The channel instance
        val channel = client.getTextChannelsByName(discordChannel, true)
        channel.filterNotNull().forEach { it.sendMessage("**${data.name}** $content").queue() }
    }

    /**
     * The companion object for this bot, which is used to add
     * logging functionality
     */
    companion object: KLogging() {

        /**
         * The maximum length of a game message (it's actually 128, but we allow room for the "Discord" tag
         */
        private const val MAX_MESSAGE_LENGTH = 110
    }
}