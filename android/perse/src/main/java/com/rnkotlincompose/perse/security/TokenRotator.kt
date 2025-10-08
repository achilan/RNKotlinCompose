package com.rnkotlincompose.perse.security

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object TokenRotator {
    private val client = OkHttpClient()
    suspend fun rotate(context: Context, rotateUrl: String, currentToken: String): Result<String> {
        if (rotateUrl.isBlank()) return Result.failure(IllegalArgumentException("rotateUrl empty"))
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().put("token", currentToken)
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val req = Request.Builder().url(rotateUrl).post(body).build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext Result.failure(Exception("HTTP ${resp.code}"))
                    val text = resp.body?.string().orEmpty()
                    val parsed = JSONObject(text)
                    val newToken = parsed.optString("token", "")
                    if (newToken.isBlank()) return@withContext Result.failure(Exception("no token in response"))
                    // persist
                    TokenStore.saveToken(context, newToken)
                    return@withContext Result.success(newToken)
                }
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }
    }
    fun rotateBlocking(context: Context, rotateUrl: String, currentToken: String): String? {
        if (rotateUrl.isBlank()) return null
        return try {
            val json = JSONObject().put("token", currentToken)
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val req = Request.Builder().url(rotateUrl).post(body).build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val text = resp.body?.string().orEmpty()
                val parsed = JSONObject(text)
                val newToken = parsed.optString("token", "")
                if (newToken.isBlank()) return null
                TokenStore.saveToken(context, newToken)
                newToken
            }
        } catch (t: Throwable) {
            null
        }
    }
}
