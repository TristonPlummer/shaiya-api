package net.shaiya.discord.events.impl

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.shaiya.discord.events.DiscordEvent
import net.shaiya.discord.ext.commandSplit
import net.shaiya.queue.QueueTask
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.IOException

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents a command that can be executed by a user
 *
 * @param client        The [JDA] Discord client instance
 * @param identifier    The command identifier prefix
 * @param requiredRole  The minimum required role
 */
abstract class DiscordChatCommand(private val client: JDA, private val identifier: String, private val requiredRole: String? = null) : DiscordEvent() {

    /**
     * The [DiscordEvent] configuration
     */
    protected lateinit var config: Map<String, Any?>

    /**
     * Handles the execution of the [DiscordChatCommand]
     *
     * @param task      The [QueueTask] instance
     * @param author    The author executing the command
     * @param channel   The channel that the command is being executed in
     * @param args      The command arguments
     */
    abstract suspend fun execute(task: QueueTask, author: Member, channel: TextChannel, args: Array<String>)

    /**
     * Gets the suggested usage for this command
     */
    abstract fun getUsage(): String

    /**
     * Initialises this [DiscordChatCommand] instance
     *
     * @param config    The discord configuration
     */
    override fun init(config: Map<String, Any?>) {
        this.config = config
        val prefix = config["prefix"] as String? ?: throw IOException("No command prefix was found!")

        // Listen to incoming Discord trade chat message
        client.addEventListener(object : ListenerAdapter() {
            override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {

                // The channel, message and author instances
                val channel = event.channel
                val message = event.message
                val author  = event.author
                val member  = event.guild.getMemberById(author.id)
                val mention = author.asMention

                // Don't respond to other bots
                if (author.isBot) return

                // If the required role is less than the user's role
                if (requiredRole != null) {
                    val reqRole = client.getRolesByName(requiredRole, true).firstOrNull()
                    if (reqRole != null && !member.roles.any { it.position >= reqRole.position }) {
                        channel.sendMessage("$mention - You have insufficient permissions to execute the command: $identifier. Your role must be ${reqRole.name} or higher.").queue()
                        return
                    }
                }

                // If the message starts with our command prefix
                if (message.content.indexOf(prefix) != -1) {

                    // The command values
                    val values = message.content.commandSplit()
                    val command = values[0].toLowerCase().substring(1)
                    val args = if (values.size > 1) values.slice(1 until values.size).filter { it.isNotEmpty() }.toTypedArray() else null

                    // If the command doesn't match
                    if (command != identifier) return

                    // Dispatch work to the coroutine dispatcher
                    dispatch {

                        // Wrap in a try-catch, if it fails then we should respond with command usage
                        try {
                            execute(this, member, channel, args ?: emptyArray())
                        } catch (e: Exception) {
                            channel.sendMessage("$mention - Invalid format! Example of proper command: $prefix${getUsage()}").queue()
                            logger.error { ExceptionUtils.getStackTrace(e) }
                        }
                    }
                }
            }
        })
    }
}