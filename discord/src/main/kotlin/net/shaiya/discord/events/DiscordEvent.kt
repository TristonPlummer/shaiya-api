package net.shaiya.discord.events

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import mu.KLogging
import net.shaiya.queue.QueueTask
import net.shaiya.queue.QueueTaskSet
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * @author Triston Plummer ("Cups")
 *
 * Represents an event that occurs on a Discord server.
 */
abstract class DiscordEvent {

    /**
     * Initialises this [DiscordEvent]
     *
     * @param config    The discord configuration
     */
    abstract fun init(config : Map<String, Any?>)

    /**
     * Dispatches a coroutine to execute on the [dispatcher] instance.
     *
     * @param logic The coroutine logic
     */
    fun dispatch(logic: suspend QueueTask.(CoroutineScope) -> Unit) {
        queue.queue(this, dispatcher, logic)
    }

    /**
     * The companion object, which adds logging functionality to events
     * and houses the coroutine dispatcher threads.
     */
    companion object: KLogging() {

        /**
         * Handles the cycling of the [QueueTaskSet] instance
         */
        private val taskExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

        /**
         * Handles the processing of [QueueTask] coroutines
         */
        private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(ThreadFactoryBuilder().setNameFormat("discord-context").setUncaughtExceptionHandler { t, e -> logger.error("Error with thread $t", e) }.build())

        /**
         * Handles the dispatching of coroutine instance
         */
        private val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

        /**
         * The set of queues that are being processed
         */
        private val queue = QueueTaskSet()

        /**
         * Initialises the executor loop
         */
        fun init() = taskExecutor.scheduleAtFixedRate({ queue.cycle() }, 0, 1, TimeUnit.SECONDS)!!
    }
}