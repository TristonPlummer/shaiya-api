package net.shaiya.discord

import com.google.common.reflect.ClassPath
import com.google.inject.Guice
import com.google.inject.Injector
import mu.KLogging
import net.shaiya.util.Properties
import java.nio.file.Path
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.shaiya.discord.events.DiscordEvent
import org.redisson.client.codec.StringCodec
import org.redisson.config.Config
import java.io.IOException

/**
 * @author Triston Plummer ("Cups")
 *
 * The [Bot] is responsible for initiating the Discord bot service, and configuring
 * the Redis and database connections it requires to function.
 */
class Bot {

    /**
     * Initialises the Discord bot instance.
     *
     * @param config    The path to the configuration file
     */
    fun startBot(config: Path) {

        // Load the discord bot properties
        val properties = Properties()
        properties.loadYaml(config.toFile())
        logger.info { "Loaded properties for the Shaiya discord bot" }

        // Build the Discord bot instance
        val discordConf = properties.get<Map<String, Any?>>("discord") ?: throw IOException("Discord properties not found")
        val builder = JDABuilder(AccountType.BOT).setAutoReconnect(true).setToken(discordConf["token"] as String)
        val client = builder.buildBlocking()

        // Build the RedisOptions configuration
        val redisConf = properties.get<Map<String, Any?>>("redis") ?: throw IOException("Redis properties not found")
        val redisPassword = redisConf["password"] as String
        val redisConfig = Config()
        val serverConf = redisConfig.useSingleServer().setAddress(redisConf["endpoint"] as String)
        if (redisPassword.isNotEmpty()) serverConf.password = redisPassword
        redisConfig.codec = StringCodec()

        // The dependency injection module
        val injector = Guice.createInjector(DiscordModule(discord = client, redisOptions = redisConfig))

        // Initialise the DiscordEvent queue
        DiscordEvent.init()

        // Initialises the bot events
        registerEvents(discordConf, injector)
    }

    /**
     * Registers the [DiscordEvent] instances.
     *
     * @param injector  The dependency injector
     */
    private fun registerEvents(config: Map<String, Any?>, injector: Injector) {

        // The current classpath
        val classPath = ClassPath.from(javaClass.classLoader)

        // The set of classes
        val classes = classPath.getTopLevelClassesRecursive(EVENT_PACKAGE)

        // Loop through the class metadata
        for (metadata in classes) {
            val clazz = metadata.load()
            if (DiscordEvent::class.java.isAssignableFrom(clazz)) {
                val instance = injector.getInstance(clazz) as DiscordEvent
                instance.init(config)
            }
        }
    }

    /**
     * The companion object for this bot, which is used to add
     * logging functionality
     */
    companion object: KLogging() {

        /**
         * The package used for [DiscordEvent]s
         */
        const val EVENT_PACKAGE = "net.shaiya.discord.events.impl"
    }
}