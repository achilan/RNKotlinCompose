package com.rnkotlincompose.perse.data

import com.rnkotlincompose.core.User
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


internal interface GoRestApi {
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): List<UserDto>

    @Headers("Content-Type: application/json")
    @POST("users")
    suspend fun createUser(@Body body: CreateUserRequest): UserDto
}

data class UserDto(
    val id: Long?,
    val name: String,
    val email: String,
    val gender: String,
    val status: String
) {
    fun toDomain() = User(id = id, name = name, email = email, gender = gender, status = status)
}

data class CreateUserRequest(
    val name: String,
    val email: String,
    val gender: String,
    val status: String
)

internal class BearerInterceptor(private val tokenProvider: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}

internal object ApiClientFactory {
    fun create(baseUrl: String, tokenProvider: () -> String): GoRestApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val client = OkHttpClient.Builder()
            .addInterceptor(BearerInterceptor(tokenProvider))
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GoRestApi::class.java)
    }
}
