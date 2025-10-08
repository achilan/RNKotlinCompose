package com.rnkotlincompose.perse.data

import com.rnkotlincompose.core.User
import com.rnkotlincompose.core.UsersRepository

internal class UsersRepositoryImpl(
    private val api: GoRestApi
) : UsersRepository {

    override suspend fun getUsers(page: Int, perPage: Int) = runCatching {
        val response = api.getUsers(page, perPage)
        if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code()} getting users")
        val items = (response.body() ?: emptyList()).map { it.toDomain() }
        val totalPages = response.headers()["X-Pagination-Pages"]?.toIntOrNull() ?: 1

        items to totalPages
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

    override suspend fun updateUser(user: User) = runCatching {
        val id = user.id ?: throw IllegalArgumentException("User id is required for update")
        val dto = api.updateUser(
            id,
            UpdateUserRequest(
                name = user.name,
                email = user.email,
                gender = user.gender,
                status = user.status
            )
        )
        dto.toDomain()
    }

    override suspend fun deleteUser(id: Long) = runCatching {
        val resp = api.deleteUser(id)
        if (!resp.isSuccessful) throw IllegalStateException("HTTP ${resp.code()} deleting user")
        Unit
    }
}

private fun parseTotalPagesFromLink(linkHeader: String?, currentPage: Int): Int {
    if (linkHeader.isNullOrBlank()) return currentPage
    val parts = linkHeader.split(',')
    val lastPart = parts.firstOrNull { it.contains("rel=\"last\"") }
    if (lastPart != null) {
        val pageRegex = Regex("[?&]page=([0-9]+)")
        val match = pageRegex.find(lastPart)
        if (match != null) return match.groupValues[1].toInt()
    }
    val nextPart = parts.firstOrNull { it.contains("rel=\"next\"") }
    if (nextPart != null) {
        val pageRegex = Regex("[?&]page=([0-9]+)")
        val match = pageRegex.find(nextPart)
        if (match != null) return maxOf(currentPage, match.groupValues[1].toInt())
    }
    return currentPage
}

fun provideUsersRepository(baseUrl: String, tokenProvider: () -> String): UsersRepository {
    val api = ApiClientFactory.create(baseUrl, tokenProvider)
    return UsersRepositoryImpl(api)
}
