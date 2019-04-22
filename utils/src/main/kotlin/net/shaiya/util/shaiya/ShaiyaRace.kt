package net.shaiya.util.shaiya

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents a race in Shaiya
 */
enum class ShaiyaRace {
    HUMAN,
    ELF,
    VAIL,
    DEATHEATER;

    /**
     * The id of this [ShaiyaRace]
     */
    val id = this.ordinal

    /**
     * The companion object for this enum, which allows for searching
     * values
     */
    companion object {

        /**
         * An array of the [ShaiyaRace] values
         */
        private val values = enumValues<ShaiyaRace>()

        /**
         * Gets the [ShaiyaRace] for a specified id
         *
         * @param id    The class id
         */
        fun byId(id: Int): ShaiyaRace? = values.find { it.id == id }
    }
}