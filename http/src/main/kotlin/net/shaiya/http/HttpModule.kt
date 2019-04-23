package net.shaiya.http

import com.google.inject.Binder
import com.google.inject.Module
import io.javalin.Javalin
import net.shaiya.database.Databases
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config

/**
 * @author Triston Plummer ("Cups")
 *
 * Handles the injection of dependencies for the HTTP module.
 *
 * @param http          The [Javalin] instance
 * @param redisOptions  The [Config] instance
 * @param databases     The [Databases] instance
 */
class HttpModule(private val http: Javalin, private val redisOptions: Config, private val databases: Databases) : Module {

    /**
     * Configures the binding of classes for the login server
     *
     * @param binder    The dependency injector instance
     */
    override fun configure(binder: Binder) {
        binder.bind(Javalin::class.java).toInstance(http)
        binder.bind(RedissonClient::class.java).toInstance(Redisson.create(redisOptions))
        binder.bind(Databases::class.java).toInstance(databases)
    }

}