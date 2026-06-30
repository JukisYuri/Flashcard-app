package com.example.mindcard.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class StudySessionRequest(
    val deckId: String,
    val accuracy: Int,
    val xp: Int,
    val timeMin: Int
)

@Serializable
data class PromptRequest(
    val prompt: String
)

internal object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api"

    @PublishedApi
    internal val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    internal suspend inline fun <reified T> get(path: String): T? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL$path")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 30000
            conn.readTimeout = 60000

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                return@withContext json.decodeFromString<T>(text)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn?.disconnect()
        }
        return@withContext null
    }

    internal suspend inline fun <reified Req, reified Resp> post(path: String, body: Req): Resp? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL$path")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 60000

            val jsonBody = json.encodeToString(body)
            OutputStreamWriter(conn.outputStream).use { it.write(jsonBody) }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                return@withContext json.decodeFromString<Resp>(text)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn?.disconnect()
        }
        return@withContext null
    }

    internal suspend inline fun <reified Req, reified Resp> put(path: String, body: Req): Resp? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL$path")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 60000

            val jsonBody = json.encodeToString(body)
            OutputStreamWriter(conn.outputStream).use { it.write(jsonBody) }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                return@withContext json.decodeFromString<Resp>(text)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn?.disconnect()
        }
        return@withContext null
    }

    internal suspend fun delete(path: String): Boolean = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL$path")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "DELETE"
            conn.connectTimeout = 15000
            conn.readTimeout = 60000

            val responseCode = conn.responseCode
            return@withContext responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        } finally {
            conn?.disconnect()
        }
    }
}
