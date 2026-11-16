package com.example.izikomuseum.data.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * The visitor's progress, saved on the phone. It mirrors the website's `izikoState` object:
 * saved artworks, best quiz scores, daily-challenge streak, points, language and dark mode.
 */
object Store {
    private lateinit var prefs: android.content.SharedPreferences

    var saved: MutableList<Int> = mutableListOf()
    var quiz: MutableMap<String, Int> = mutableMapOf()
    var dailyLast: String = ""
    var dailyStreak: Int = 0
    var dailyCount: Int = 0
    var pts: Int = 0
    var lang: String = "en"
    /** null = follow the phone's light/dark setting until the visitor taps the moon button. */
    var dark: Boolean? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("iziko_state", Context.MODE_PRIVATE)
        try {
            val o = JSONObject(prefs.getString("state", "{}") ?: "{}")
            saved = o.optJSONArray("saved")?.let { a -> (0 until a.length()).map { a.getInt(it) }.toMutableList() } ?: mutableListOf()
            quiz = mutableMapOf()
            o.optJSONObject("quiz")?.let { q -> q.keys().forEach { k -> quiz[k] = q.getInt(k) } }
            o.optJSONObject("daily")?.let { d ->
                dailyLast = d.optString("last", ""); dailyStreak = d.optInt("streak", 0); dailyCount = d.optInt("count", 0)
            }
            pts = o.optInt("pts", 0)
            lang = o.optString("lang", "en")
            dark = if (o.has("dark") && !o.isNull("dark")) o.getBoolean("dark") else null
        } catch (e: Exception) { /* corrupt data: start fresh */ }
    }

    fun save() {
        val o = JSONObject()
        o.put("saved", JSONArray(saved))
        o.put("quiz", JSONObject(quiz as Map<*, *>))
        o.put("daily", JSONObject().put("last", dailyLast).put("streak", dailyStreak).put("count", dailyCount))
        o.put("pts", pts)
        o.put("lang", lang)
        o.put("dark", dark ?: JSONObject.NULL)
        prefs.edit().putString("state", o.toString()).apply()
        schedulePush()
    }

    // ---------- sync with the shared server (same /api/progress the website uses) ----------

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val pushTask = Runnable { push() }

    private fun schedulePush() {
        if (AuthRepository.token == null) return
        handler.removeCallbacks(pushTask)
        handler.postDelayed(pushTask, 400)
    }

    private fun progressJson(): JSONObject = JSONObject()
        .put("saved", JSONArray(saved))
        .put("quiz", JSONObject(quiz as Map<*, *>))
        .put("daily", JSONObject().put("last", dailyLast).put("streak", dailyStreak).put("count", dailyCount))
        .put("pts", pts)

    private fun push() {
        val t = AuthRepository.token ?: return
        Api.call("POST", "/api/progress", JSONObject().put("state", progressJson()), t) { }
    }

    /** Merges the server's progress into this phone's (never loses anything), then saves and uploads the result. */
    fun pull(done: () -> Unit = {}) {
        val t = AuthRepository.token ?: return done()
        Api.call("GET", "/api/progress", null, t) { r ->
            val st = r?.json?.optJSONObject("state")
            if (r != null && r.code < 400) {
                if (st != null) {
                    st.optJSONArray("saved")?.let { a -> for (i in 0 until a.length()) if (!saved.contains(a.getInt(i))) saved.add(a.getInt(i)) }
                    st.optJSONObject("quiz")?.let { q -> q.keys().forEach { k -> quiz[k] = maxOf(quiz[k] ?: 0, q.getInt(k)) } }
                    st.optJSONObject("daily")?.let { d -> if (d.optString("last", "") > dailyLast) { dailyLast = d.optString("last"); dailyStreak = d.optInt("streak"); dailyCount = d.optInt("count") } }
                    pts = maxOf(pts, st.optInt("pts", 0))
                }
                save()
            }
            done()
        }
    }

    fun addPts(n: Int) { pts += n; save() }

    /** Today's date as yyyy-MM-dd in UTC, like the website's `today()`. */
    fun today(offsetDays: Int = 0): String {
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        f.timeZone = TimeZone.getTimeZone("UTC")
        return f.format(Date(System.currentTimeMillis() + offsetDays * 86_400_000L))
    }

    /** Whole days since 1970 (UTC): picks the daily-challenge question. */
    fun dayNumber(): Long = System.currentTimeMillis() / 86_400_000L
}
