package net.shaiya.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File

/**
 * @author Triston Plummer ("Cups")
 *
 * Holds a map of properties that can be used by the application.
 */
class Properties {

    /**
     * The [HashMap] of property keys to their values
     */
    private val properties = hashMapOf<String, Any?>()

    /**
     * Gets the property associated with the [key]. If it cannot be found, it will
     * return the [default] value instead.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getOrDefault(key: String, default: T): T = properties[key] as? T ?: default

    /**
     * Gets the property associated with the [key]. If it cannot be found, it will
     * return null instead.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? = properties[key] as? T

    /**
     * Checks if [properties] contains a value associated with [key].
     */
    fun has(key: String): Boolean = properties.containsKey(key)

    /**
     * Loads a YAML (.yml) file and puts all the found keys & values
     * into the [properties] map.
     */
    fun loadYaml(file: File): Properties {
        check(properties.isEmpty())

        val mapper = ObjectMapper(YAMLFactory())
        val values = mapper.readValue(file, HashMap<String, Any>().javaClass)

        values.forEach { key, value ->
            if (value is String && value.isEmpty()) {
                properties[key] = null
            } else {
                properties[key] = value
            }
        }
        return this
    }
}