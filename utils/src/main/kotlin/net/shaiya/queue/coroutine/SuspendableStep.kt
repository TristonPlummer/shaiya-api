package net.shaiya.queue.coroutine

import kotlin.coroutines.Continuation

/**
 * @author Triston Plummer ("Cups")
 *
 * A step in suspendable logic that can be used to step through event logic
 *
 * @param condition     The condition to suspend execution on
 * @param continuation  The next block to execute
 */
data class SuspendableStep(val condition: SuspendableCondition, val continuation: Continuation<Unit>)