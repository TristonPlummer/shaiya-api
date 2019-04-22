package net.shaiya.discord.models

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents a position in Shaiya
 *
 * @param map       The map id
 * @param x         The x coordinate
 * @param z         The z coordinate
 * @param height    The height (y) coordinate
 */
data class ShaiyaPosition(val map: Int, val x: Float, val z: Float, val height: Float)