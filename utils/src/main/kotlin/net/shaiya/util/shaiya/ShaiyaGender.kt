package net.shaiya.util.shaiya

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents a gender in Shaiya
 */
enum class ShaiyaGender {
    MALE,
    FEMALE;

    /**
     * The id of this [ShaiyaGender]
     */
    val id = this.ordinal

    /**
     * The companion object for this enum, which allows for searching
     * values
     */
    companion object {

        /**
         * An array of the [ShaiyaGender] values
         */
        private val values = enumValues<ShaiyaGender>()

        /**
         * Gets the [ShaiyaGender] for a specified id
         *
         * @param id    The class id
         */
        fun byId(id: Int): ShaiyaGender? = values.find { it.id == id }
    }
}