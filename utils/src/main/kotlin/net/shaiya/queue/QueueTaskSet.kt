package net.shaiya.queue

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.util.*
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

/**
 * @author Triston Plummer ("Cups")
 *
 * A system responsible for task coroutine logic.
 */
class QueueTaskSet {

    /**
     * The internal [LinkedList] containing the [QueueTask]s to process.
     */
    private val queue: LinkedList<QueueTask> = LinkedList()

    /**
     * Queues a block of logic to get processed in the [queue]
     *
     * @param ctx           The context of the [QueueTask]
     * @param dispatcher    The [CoroutineDispatcher] instance to use
     * @param block         The coroutine logic block
     */
    fun queue(ctx: Any, dispatcher: CoroutineDispatcher, block: suspend QueueTask.(CoroutineScope) -> Unit) {
        val task = QueueTask(ctx)
        val suspendBlock = suspend { block(task, CoroutineScope(dispatcher)) }

        task.coroutine = suspendBlock.createCoroutine(completion = task)

        queue.addFirst(task)
    }

    /**
     * Cycles through the [queue] and processes each coroutine step
     */
    fun cycle() {
        val iterator = queue.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()

            if (!task.invoked) {
                task.invoked = true
                task.coroutine.resume(Unit)
            }

            task.cycle()

            if (!task.suspended()) {
                iterator.remove()
            }
        }
    }
}
