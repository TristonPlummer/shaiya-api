package net.shaiya.util.shaiya

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents a class in Shaiya
 */
enum class ShaiyaClass {
    FIGHTER,
    DEFENDER,
    RANGER,
    ARCHER,
    MAGE,
    PRIEST;

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
         * An array of the [ShaiyaClass] values
         */
        private val values = enumValues<ShaiyaClass>()

        /**
         * Gets the [ShaiyaClass] for a specified id
         *
         * @param id    The class id
         */
        fun byId(id: Int): ShaiyaClass? = values.find { it.id == id }
    }
}