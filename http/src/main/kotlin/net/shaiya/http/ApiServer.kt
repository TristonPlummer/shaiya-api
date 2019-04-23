package net.shaiya.http

import com.google.common.reflect.ClassPath
import com.google.inject.Guice
import com.google.inject.Injector
import io.javalin.Javalin
import mu.KLogging
import net.shaiya.database.Databases
import net.shaiya.http.controller.HttpController
import net.shaiya.util.Properties
import org.redisson.client.codec.StringCodec
import org.redisson.config.Config
import java.io.IOException
import java.lang.reflect.Modifier
import java.nio.file.Path

/**
 * @author Triston Plummer ("Cups")
 *
 * The [ApiServer] is responsible for initialising and configuring a JSON API for retrieving data and
 * using it in external applications.
 */
class ApiServer {

    /**
     * Starts this [ApiServer] instance
     *
     * @param config    The path to the configuration file
     */
    fun startServer(config: Path) {

        // Load the discord bot properties
        val properties = Properties()
        properties.loadYaml(config.toFile())
        logger.info { "Loaded properties for the Shaiya HTTP API" }

        // Build the RedisOptions configuration
        val redisConf = properties.get<Map<String, Any?>>("redis") ?: throw IOException("Redis properties not found")
        val redisPassword = redisConf["password"] as String
        val redisConfig = Config()
        val serverConf = redisConfig.useSingleServer().setAddress(redisConf["endpoint"] as String)
        if (redisPassword.isNotEmpty()) serverConf.password = redisPassword
        redisConfig.codec = StringCodec()

        // Connect to the database
        val databaseConf = properties.get<Map<String, Any?>>("database") ?: throw IOException("Database properties not found")
        val databaseHost = databaseConf["host"] as String
        val databaseUser = databaseConf["user"] as String
        val databasePass = databaseConf["pass"] as String

        // The database connector instance
        val database = Databases()
        database.init(databaseHost, 1433, databaseUser, databasePass)

        // The HTTP configuration
        val httpConf = properties.get<Map<String, Any?>>("http") ?: throw IOException("HTTP properties not found")
        val port = httpConf["port"] as Int

        // The Javalin instance
        val http = Javalin.create()
                .enableCorsForAllOrigins()
                .start(port)

        // The dependency injection module
        val injector = Guice.createInjector(HttpModule(http = http, redisOptions = redisConfig, databases = database))

        // Initialises the HTTP controllers
        registerControllers(httpConf, injector)
    }

    /**
     * Registers the [HttpController] instances.
     *
     * @param injector  The dependency injector
     */
    private fun registerControllers(config: Map<String, Any?>, injector: Injector) {

        // The current classpath
        val classPath = ClassPath.from(javaClass.classLoader)

        // The set of classes
        val classes = classPath.getTopLevelClassesRecursive(CONTROLLER_PACKAGE)

        // Loop through the class metadata
        for (metadata in classes) {
            val clazz = metadata.load()
            if (HttpController::class.java.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.modifiers)) {
                val instance = injector.getInstance(clazz) as HttpController
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
         * The package used for [HttpController]s
         */
        const val CONTROLLER_PACKAGE = "net.shaiya.http.controller.impl"
    }
}