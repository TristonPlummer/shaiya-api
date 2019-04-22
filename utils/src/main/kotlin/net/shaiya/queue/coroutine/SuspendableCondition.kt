package net.shaiya.queue.coroutine

import com.google.common.base.MoreObjects
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Triston Plummer ("Cups")
 *
 * A condition that must be met for a suspended coroutine to continue
 */
abstract class SuspendableCondition {
    /**
     * Whether or not the coroutine can continue its logic.
     */
    abstract fun resume(): Boolean
}

/**
 * A [SuspendableCondition] that waits for the given amount of cycles before
 * permitting the coroutine to continue its logic.
 *
 * @param cycles
 * The amount of game cycles that must pass before the coroutine can continue.
 */
class WaitCondition(cycles: Int) : SuspendableCondition() {

    private val cyclesLeft = AtomicInteger(cycles)

    override fun resume(): Boolean = cyclesLeft.decrementAndGet() <= 0

    override fun toString(): String = MoreObjects.toStringHelper(this).add("cycles", cyclesLeft).toString()
}

/**
 * A [SuspendableCondition] that waits for [predicate] to return true before
 * permitting the coroutine to continue its logic.
 */
class PredicateCondition(private val predicate: () -> Boolean) : SuspendableCondition() {

    override fun resume(): Boolean = predicate.invoke()
}