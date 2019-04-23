package net.shaiya.http.controller.impl

import io.javalin.Context
import net.shaiya.http.controller.HttpController
import net.shaiya.repository.UserRepository
import javax.inject.Inject

/**
 * @author Triston Plummer
 *
 * Handles various Shaiya user related routes.
 *
 * @param repository    The repository used for retrieving [ShaiyaUser]s.
 */
class UserController @Inject constructor(private val repository: UserRepository): HttpController("user") {

    /**
     * Gets the [ShaiyaUser] by name, as JSON.
     *
     * @param   name    The name of the user
     */
    fun Context.get(name: String) {
        val user = repository.getUser(name)
        json(user)
    }
}