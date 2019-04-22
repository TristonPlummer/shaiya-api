package net.shaiya.discord

import com.google.inject.Binder
import com.google.inject.Module
import net.dv8tion.jda.core.JDA
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config

/**
 * @author Triston Plummer ("Cups")
 *
 * Handles the injection of dependencies for the discord module.
 *
 * @param discord       The [JDA] instance
 * @param redisOptions  The [Config] instance
 */
class DiscordModule(private val discord: JDA, private val redisOptions: Config) : Module {

    /**
     * Configures the binding of classes for the login server
     *
     * @param binder    The dependency injector instance
     */
    override fun configure(binder: Binder) {
        binder.bind(JDA::class.java).toInstance(discord)
        binder.bind(RedissonClient::class.java).toInstance(Redisson.create(redisOptions))
    }

}