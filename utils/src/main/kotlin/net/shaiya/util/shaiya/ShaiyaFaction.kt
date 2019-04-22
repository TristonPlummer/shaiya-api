package net.shaiya.util.shaiya

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents a faction in Shaiya
 */
enum class ShaiyaFaction {
    LIGHT,
    FURY,
    NOT_SELECTED;

    /**
     * The id of this [ShaiyaClass]
     */
    val id = this.ordinal

    /**
     * The companion object for this enum, which allows for searching
     * values
     */
    companion object {

        /**
         * An array of the [ShaiyaFaction] values
         */
        private val values = enumValues<ShaiyaFaction>()

        /**
         * Gets the [ShaiyaFaction] for a specified id
         *
         * @param id    The class id
         */
        fun byId(id: Int): ShaiyaFaction? = values.find { it.id == id }
    }
}