package com.example.izikomuseum.data.repository

import android.content.Context
import com.example.izikomuseum.data.model.User
import org.json.JSONObject

/**
 * Accounts live on the shared Python server, exactly like the website: email register/login and Google
 * sign-in (Firebase) all end in the same session token. The token is kept on the phone so the visitor stays signed in.
 * Every function reports back with null on success or an error message to show.
 */
object AuthRepository {
    private lateinit var prefs: android.content.SharedPreferences

    var token: String? = null
        private set
    var currentUser: User? = null
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences("iziko_auth", Context.MODE_PRIVATE)
        token = prefs.getString("token", null)
        val name = prefs.getString("name", null)
        val email = prefs.getString("email", null)
        currentUser = if (token != null && name != null && email != null) User(name, email) else null
    }

    private fun accept(r: Api.Reply?): String? {
        val t = r?.json?.optString("token")
        val u = r?.json?.optJSONObject("user")
        if (r == null || r.code >= 400 || t.isNullOrEmpty() || u == null) return Api.errorOf(r)
        token = t
        currentUser = User(u.optString("fullName"), u.optString("email"))
        prefs.edit().putString("token", t).putString("name", currentUser!!.fullName).putString("email", currentUser!!.email).apply()
        return null
    }

    fun login(email: String, password: String, done: (String?) -> Unit) {
        val body = JSONObject().put("email", email.trim()).put("password", password)
        Api.call("POST", "/api/auth/login", body) { done(accept(it)) }
    }

    fun register(fullName: String, email: String, password: String, done: (String?) -> Unit) {
        val body = JSONObject().put("fullName", fullName.trim()).put("email", email.trim()).put("password", password)
        Api.call("POST", "/api/auth/register", body) { r ->
            if (r == null || r.code >= 400) done(Api.errorOf(r)) else login(email, password, done)
        }
    }

    /** Sends the Firebase ID token from Google sign-in to the same endpoint the website uses. */
    fun google(idToken: String, done: (String?) -> Unit) {
        Api.call("POST", "/api/auth/firebase", JSONObject().put("idToken", idToken)) { done(accept(it)) }
    }

    fun logout() {
        token?.let { Api.call("POST", "/api/auth/logout", JSONObject(), it) { } }
        token = null
        currentUser = null
        prefs.edit().clear().apply()
        try { com.google.firebase.auth.FirebaseAuth.getInstance().signOut() } catch (e: Exception) { }
    }
}
