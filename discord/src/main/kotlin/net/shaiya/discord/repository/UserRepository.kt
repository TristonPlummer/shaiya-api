package net.shaiya.discord.repository

import com.google.gson.Gson
import com.google.inject.Inject
import net.shaiya.database.Databases
import net.shaiya.database.gamedata.Chars
import net.shaiya.database.gamedata.UserMaxGrow
import net.shaiya.database.userdata.UsersMaster
import net.shaiya.discord.models.ShaiyaPosition
import net.shaiya.discord.models.ShaiyaUser
import net.shaiya.queue.QueueTask
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.json.JSONObject
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

/**
 * @author Triston Plummer ("Cups")
 *
 * Handles the caching and retrieval of [ShaiyaUser] instances.
 *
 * @param databases The [Databases] instance
 * @param redis     The [RedissonClient] client instance
 */
class UserRepository @Inject constructor(private val databases: Databases, private val redis: RedissonClient){

    /**
     * The [Gson] instance
     */
    private val gson = Gson()

    /**
     * Gets a [ShaiyaUser] instance.
     *
     * @param task  The [QueueTask] instance
     * @param name  The name of the user to search for
     *
     * @return      The [ShaiyaUser] instance
     */
    suspend fun getUser(task: QueueTask, name: String): ShaiyaUser? {

        // The name, in lowercase
        val nameLower = name.toLowerCase()

        // The redis data
        val data = redis.getBucket<String>(nameLower)

        // If a cached version exists, return that
        if (data.isExists) return gson.fromJson(data.get(), ShaiyaUser::class.java)

        // Build the JSON request payload
        val json = JSONObject()
        json.put("name", nameLower)

        // Publish a request to the author details topic, so the game can try to cache the live author
        val topic = redis.getTopic("user_details")
        topic.publish(json.toString())

        // The user instance
        val user = getUserFromDatabase(nameLower)

        // If the user was successfully retrieved, cache it for 10 minutes
        if (user != null) {

            // Wait 1 second
            task.wait(1)

            // The Redis data
            val cached = redis.getBucket<String>(nameLower)

            // Cache the result if it doesn't exist
            if (!cached.isExists) cached.set(gson.toJson(user), 10, TimeUnit.MINUTES)
        }

        return user
    }

    /**
     * Gets a [ShaiyaUser] from the database.
     *
     * @param name  The name of the user to search for
     * @return      The [ShaiyaUser] instance
     */
    private fun getUserFromDatabase(name: String): ShaiyaUser? {
        var user: ShaiyaUser? = null
        transaction(databases.gameDataDatabase) {
            val result = Chars.join(UserMaxGrow, JoinType.INNER, Chars.userId, UserMaxGrow.userId).select { (Chars.deleted eq false) and (Chars.name eq name) }.firstOrNull()
            if (result != null) {

                // The position of the user
                val position = ShaiyaPosition(map = result[Chars.map], x = result[Chars.posX], z = result[Chars.posZ], height = result[Chars.posHeight])

                // The status of the user
                val status: Int by lazy {
                    transaction(databases.userDataDatabase) {
                        val statusResult = UsersMaster.select { UsersMaster.userId eq result[Chars.userId] }.first()
                        statusResult[UsersMaster.status]
                    }
                }

                // The user instance
                user = ShaiyaUser(
                        charId = result[Chars.id],
                        charName = result[Chars.name],
                        username = result[Chars.username],
                        userId = result[Chars.userId],
                        position = position,
                        faction = result[UserMaxGrow.faction].id,
                        race = result[Chars.race].id,
                        job = result[Chars.job].id,
                        level = result[Chars.level],
                        kills = result[Chars.kills],
                        deaths = result[Chars.deaths],
                        victories = result[Chars.victories],
                        defeats = result[Chars.defeats],
                        status = status
                )
            }
        }

        return user
    }
}