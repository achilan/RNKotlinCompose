package com.rnkotlincompose.core

data class User(
    val id: Long? = null,
    val name: String,
    val email: String,
    val gender: String,
    val status: String
)

sealed interface PageState<out T> {
    object Loading : PageState<Nothing>
    object Empty : PageState<Nothing>
    data class Error(val message: String) : PageState<Nothing>
    data class Data<T>(val items: List<T>, val page: Int, val totalPages: Int) : PageState<T>
}

interface UsersRepository {
    suspend fun getUsers(page: Int, perPage: Int): Result<Pair<List<User>, Int>>
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(id: Long): Result<Unit>
}
