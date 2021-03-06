package net.shaiya.http.controller

import com.google.gson.Gson
import io.javalin.Context
import io.javalin.Javalin
import mu.KLogging
import net.shaiya.http.methods.Get
import net.shaiya.http.methods.Post
import org.eclipse.jetty.http.HttpStatus
import java.lang.Exception
import java.lang.StringBuilder
import java.lang.reflect.Method
import javax.inject.Inject

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents a route controller for the HTTP Server. I honestly don't know what I was thinking when
 * I started writing this. I just started writing code and then it spiraled out of control. I wanted to achieve
 * the ease of development of ASP.NET though I think I lost my sanity in the process.
 *
 * @param route The base route path.
 */
abstract class HttpController(private val route: String) {

    /**
     * The [Javalin] instance
     */
    @Inject
    private lateinit var http: Javalin

    /**
     * The HTTP configuration
     */
    private lateinit var config : Map<String, Any?>

    /**
     * The [Gson] instance
     */
    private val gson = Gson()

    /**
     * Initialises this [HttpController]
     *
     * @param config    The HTTP configuration
     */
    fun init(config : Map<String, Any?>) {
        this.config = config
        registerRoutes()
    }

    /**
     * Registers the routes for this [HttpController] instance, using black magic.
     */
    private fun registerRoutes() {

        // The array of methods in this class
        val methods = this.javaClass.methods

        // Register the "get" and "post" methods
        methods.filter { it.isAnnotationPresent(Get::class.java) }.forEach { http.get(it.getRoutePath(), it.createRoute(this)) }
        methods.filter { it.isAnnotationPresent(Post::class.java) }.forEach { http.post(it.getRoutePath(), it.createRoute(this)) }
    }

    /**
     * Gets the named route path for a method.
     *
     * @return  The path
     */
    private fun Method.getRoutePath() : String {

        // If the parameter is 1, bind the route
        if (parameters.size == 1) return ""

        // The route path
        val bldr = StringBuilder(route)
        parameters.drop(1).forEach { p -> bldr.append("/:${p.name}") }
        return bldr.toString()
    }

    /**
     * Creates a route from a [Method] instance
     *
     * @param controller    The [HttpController] instance
     *
     * @return  The
     */
    private fun Method.createRoute(controller: HttpController) : (Context) -> Unit {

        // If the parameter is 1, bind the route
        if (parameters.size == 1) return { ctx -> invoke(controller, ctx)}

        // Drop the first parameter
        val params = parameters.drop(1).associate { p -> p.name to p.type }

        // Bind the route
        return { ctx ->

            // The parameters passed in the path
            val pathParams = ctx.pathParamMap()

            // If all of the path parameters exist
            if (pathParams.all { pathParam -> params.containsKey(pathParam.key) }) {

                // The parameter values
                val paramValues = pathParams.map { p -> map(p.value, params.getValue(p.key))}.toTypedArray()

                // Invoke the method
                invoke(controller, ctx, *paramValues)
            }
        }
    }

    /**
     * Maps a value to it's specified type
     *
     * @param value The value, as a string
     * @param type  The output type
     *
     * @return  The mapped type
     */
    private fun map(value: String, type: Class<*>) : Any = when (type) {
        Long::class.java -> value.toLong()
        Int::class.java -> value.toInt()
        Boolean::class.java -> value.toBoolean()
        else -> {

            // Attempt to cast to a the type first, and fallback to JSON
            try {
                type.cast(value)
            } catch (e: Exception) {
                gson.fromJson(value, type)
            }
        }
    }

    /**
     * Writes a nullable object to the [Context] as JSON.
     * If the object is null, a [HttpStatus.NOT_FOUND_404] will be issued.
     *
     * @param obj   The object
     */
    protected fun Context.json(obj: Any?) {
        if (obj == null) {
            status(HttpStatus.NOT_FOUND_404)
            return
        }
        json(obj)
    }

    /**
     * The companion object, which adds logging functionality to the controller
     */
    companion object: KLogging()
}