package com.example.izikomuseum.data.repository

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Talks to the same Python server as the website (server/app.py), so both use one database.
 *
 * SET [BASE_URL] to your live server (for example the Render address). It must be https://.
 * For testing on the Android emulator against a server on your own computer use
 * "http://10.0.2.2:8000" and add android:usesCleartextTraffic="true" to the <application> tag.
 */
object Api {
    const val BASE_URL = "https://YOUR-SERVER.onrender.com"

    /** [code] is the HTTP status; [json] the parsed body. */
    class Reply(val code: Int, val json: JSONObject?)

    private val main = Handler(Looper.getMainLooper())

    /** Runs the request on a background thread. [done] is called on the main thread; null means the server could not be reached. */
    fun call(method: String, path: String, body: JSONObject? = null, token: String? = null, done: (Reply?) -> Unit) {
        Thread {
            var reply: Reply? = null
            try {
                val c = URL(BASE_URL + path).openConnection() as HttpURLConnection
                c.requestMethod = method
                c.connectTimeout = 20000
                c.readTimeout = 40000 // free hosting can take a while to wake up
                c.setRequestProperty("Content-Type", "application/json")
                if (token != null) c.setRequestProperty("Authorization", "Bearer $token")
                if (body != null) { c.doOutput = true; c.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) } }
                val code = c.responseCode
                val text = (if (code < 400) c.inputStream else c.errorStream)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                reply = Reply(code, try { JSONObject(text) } catch (e: Exception) { null })
                c.disconnect()
            } catch (e: Exception) { reply = null }
            main.post { done(reply) }
        }.start()
    }

    const val OFFLINE = "Can't reach the server. Check your connection and try again."
    fun errorOf(r: Reply?): String = if (r == null) OFFLINE else r.json?.optString("error")?.takeIf { it.isNotEmpty() } ?: "Something went wrong (${r.code})."
}
