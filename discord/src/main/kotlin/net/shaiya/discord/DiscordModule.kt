package net.shaiya.discord

import com.google.inject.Binder
import com.google.inject.Module
import net.dv8tion.jda.core.JDA
import net.shaiya.database.Databases
import net.shaiya.discord.repository.UserRepository
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
 * @param databases     The [Databases] instance
 */
class DiscordModule(private val discord: JDA, private val redisOptions: Config, private val databases: Databases) : Module {

    /**
     * The [UserRepository] singleton instance.
     */
    private val userRepository = UserRepository(databases, Redisson.create(redisOptions))

    /**
     * Configures the binding of classes for the login server
     *
     * @param binder    The dependency injector instance
     */
    override fun configure(binder: Binder) {
        binder.bind(JDA::class.java).toInstance(discord)
        binder.bind(RedissonClient::class.java).toInstance(Redisson.create(redisOptions))
        binder.bind(Databases::class.java).toInstance(databases)
        binder.bind(UserRepository::class.java).toInstance(userRepository)
    }

}