package net.shaiya.http.controller.impl

import io.javalin.Context
import net.shaiya.http.controller.HttpController
import net.shaiya.http.methods.Get
import net.shaiya.repository.UserRepository
import javax.inject.Inject

/**
 * @author Triston Plummer ("Cups")
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
    @Get fun Context.getUserByName(name: String) = json(repository.getUser(name))

}