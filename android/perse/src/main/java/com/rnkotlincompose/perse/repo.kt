package com.rnkotlincompose.perse.data

import com.rnkotlincompose.core.User
import com.rnkotlincompose.core.UsersRepository

internal class UsersRepositoryImpl(
    private val api: GoRestApi
) : UsersRepository {

    override suspend fun getUsers(page: Int, perPage: Int) = runCatching {
        val items = api.getUsers(page, perPage).map { it.toDomain() }
        items to 50 
    }

    override suspend fun createUser(user: User) = runCatching {
        val dto = api.createUser(
            CreateUserRequest(
                name = user.name,
                email = user.email,
                gender = user.gender,
                status = user.status
            )
        )
        dto.toDomain()
    }
}
