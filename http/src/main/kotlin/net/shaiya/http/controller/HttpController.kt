package net.shaiya.http.controller

import io.javalin.Context
import io.javalin.Javalin
import io.javalin.NotFoundResponse
import mu.KLogging
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.websocket.api.StatusCode
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
        methods.filter { it.name == "get" }.forEach { http.get(it.getRoutePath(), it.createRoute(this)) }
        methods.filter { it.name == "post" }.forEach { http.post(it.getRoutePath(), it.createRoute(this)) }
    }

    /**
     * Gets the named route path for a method.
     *
     * @return  The path
     */
    private fun Method.getRoutePath() : String {

        // Get the parameters
        val parameters = parameters

        // If the parameter is 1, bind the route
        if (parameters.size == 1) return ""

        // Drop the first parameter
        val params = parameters.drop(1).associate { p -> p.name to p.type }

        // The route path
        val bldr = StringBuilder(route)
        params.forEach { p -> bldr.append("/:${p.key}") }
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

        // Get the parameters
        val parameters = parameters

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
        Int::class.java -> value.toInt()
        Boolean::class.java -> value.toBoolean()
        else -> type.cast(value)
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