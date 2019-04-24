package net.shaiya.http.methods

/**
 * @author Triston Plummer ("Cups")
 *
 * An annotation used to indicate that a method should be handled as a HTTP POST route.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Post