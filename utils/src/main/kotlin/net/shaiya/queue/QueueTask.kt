package net.shaiya.queue

import net.shaiya.queue.coroutine.SuspendableStep
import mu.KLogging
import net.shaiya.queue.coroutine.PredicateCondition
import net.shaiya.queue.coroutine.WaitCondition
import kotlin.coroutines.*

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents a queued coroutine task
 *
 * @param ctx   The context of this task.
 */
data class QueueTask(val ctx: Any) : Continuation<Unit> {

    /**
     * The next continuation to execute
     */
    lateinit var coroutine: Continuation<Unit>

    /**
     * If the task's logic has already been invoked.
     */
    var invoked = false

    /**
     * A value that can be requested by a task, such as an input for dialogs.
     */
    var requestReturnValue: Any? = null

    /**
     * Represents an action that should be executed if, and only if, this task
     * was terminated via [terminate].
     */
    var terminateAction: ((QueueTask).() -> Unit)? = null

    /**
     * The next [SuspendableStep], if any, that must be handled once a [SuspendableCondition]
     * returns [SuspendableCondition.resume] as true.
     */
    private var nextStep: SuspendableStep? = null

    /**
     * The [CoroutineContext] implementation for our task.
     */
    override val context: CoroutineContext = EmptyCoroutineContext

    /**
     * When the [nextStep] [SuspendableCondition.resume] returns true, this
     * method is called.
     */
    override fun resumeWith(result: Result<Unit>) {
        nextStep = null
        result.exceptionOrNull()?.let { e -> logger.error("Error with event!", e) }
    }

    /**
     * The logic in each [SuspendableStep] must be game-thread-safe, so we use
     * this method to keep them in-sync.
     */
    internal fun cycle() {
        val next = nextStep ?: return

        if (next.condition.resume()) {
            next.continuation.resume(Unit)
            requestReturnValue = null
        }
    }

    /**
     * Terminate any further execution of this task, during any state,
     * and invoke [terminateAction] if applicable (not null).
     */
    private fun terminate() {
        nextStep = null
        requestReturnValue = null
        terminateAction?.invoke(this)
    }

    /**
     * If the task has been "paused" (aka suspended).
     */
    fun suspended(): Boolean = nextStep != null

    /**
     * Wait for the specified amount of game seconds [seconds] before
     * continuing the logic associated with this task.
     */
    suspend fun wait(seconds: Int): Unit = suspendCoroutine {
        check(seconds > 0) { "Wait seconds must be greater than 0." }
        nextStep = SuspendableStep(WaitCondition(seconds), it)
    }

    /**
     * Wait for [predicate] to return true.
     */
    suspend fun wait(predicate: () -> Boolean): Unit = suspendCoroutine {
        nextStep = SuspendableStep(PredicateCondition { predicate() }, it)
    }

    /**
     * The companion object, which adds logging functionality to this task
     */
    companion object: KLogging()
}