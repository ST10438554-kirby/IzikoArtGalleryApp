package com.example.izikomuseum.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.izikomuseum.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import com.example.izikomuseum.data.Content
import com.example.izikomuseum.data.Content.t
import com.example.izikomuseum.data.repository.AuthRepository
import com.example.izikomuseum.data.repository.Store

/**
 * The whole app lives in this one screen
 */
class MainActivity : AppCompatActivity(), Host {

    override lateinit var ui: Ui
    private lateinit var pages: Pages
    private lateinit var root: FrameLayout
    private lateinit var headerHolder: FrameLayout
    private lateinit var scroll: ScrollView
    private lateinit var body: LinearLayout
    private lateinit var menuLayer: FrameLayout
    private lateinit var chatLayer: FrameLayout

    private val stack = ArrayList<String>()
    private var navOpen = false
    private var langOpen = false
    private var chatOpen = false
    private val chatLog = ArrayList<Pair<Boolean, String>>()
    private var chatMessages: LinearLayout? = null
    private var chatScroll: ScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ui = Ui(this)
        pages = Pages(this)
        stack.add(savedInstanceState?.getString("route") ?: "home")

        root = FrameLayout(this)
        setContentView(root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            WindowInsetsCompat.CONSUMED
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    navOpen || langOpen -> { navOpen = false; langOpen = false; refreshMenus() }
                    chatOpen -> { chatOpen = false; refreshChat() }
                    stack.size > 1 -> back()
                    else -> finish()
                }
            }
        })
        buildRoot()
        Store.pull { refreshPage() } // fetch progress saved from the website 
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("route", stack.last())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ui = Ui(this)
        buildRoot()
    }

    // ---------- Host ----------

    override fun go(route: String) {
        navOpen = false; langOpen = false
        if (stack.last() != route) stack.add(route)
        showPage(true)
    }

    override fun back() {
        if (stack.size > 1) { stack.removeAt(stack.lastIndex); showPage(true) } else finish()
    }

    override fun refreshPage() = showPage(false)

    override fun refreshChrome() {
        headerHolder.removeAllViews()
        headerHolder.addView(buildHeader())
    }

    override fun toast(message: String) {
        val tv = ui.text(message, 16f, Pal.bg).apply {
            setPadding(ui.dp(20), ui.dp(12), ui.dp(20), ui.dp(12))
            setBackgroundColor(Pal.ink)
            contentDescription = message
            elevation = ui.dp(6).toFloat()
        }
        root.addView(tv, FrameLayout.LayoutParams(WRAP, WRAP, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply { bottomMargin = ui.dp(28) })
        tv.postDelayed({ root.removeView(tv) }, 2600)
    }

    override fun openUrl(url: String) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        catch (e: ActivityNotFoundException) { toast("No app is available to open this link.") }
    }

    override fun share(text: String) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, "Art Quiz"))
    }

    override fun afterSignIn() {
        refreshChrome()
        toast("Welcome!")
        go("home")
        Store.pull { refreshPage() }
    }

    /**
     * Google sign-in, same as the website: Google account -> Firebase -> ID token -> POST /api/auth/firebase.
     * One Google account therefore gives the same user and the same progress on the website and in the app.
     */
    override fun googleSignIn(done: (String?) -> Unit) {
        val option = GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id)).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        lifecycleScope.launch {
            try {
                val cred = CredentialManager.create(this@MainActivity).getCredential(this@MainActivity, request).credential
                if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
                    FirebaseAuth.getInstance().signInWithCredential(GoogleAuthProvider.getCredential(googleToken, null))
                        .addOnCompleteListener { signIn ->
                            if (!signIn.isSuccessful) { done("Google sign-in failed. Please try again."); return@addOnCompleteListener }
                            val user = signIn.result.user
                            if (user == null) { done("Google sign-in failed. Please try again."); return@addOnCompleteListener }
                            user.getIdToken(false).addOnCompleteListener { idTask ->
                                if (!idTask.isSuccessful) { done("Google sign-in failed. Please try again."); return@addOnCompleteListener }
                                val idToken = idTask.result.token
                                if (idToken == null) done("Google sign-in failed. Please try again.")
                                else AuthRepository.google(idToken) { error -> done(error) }
                            }
                        }
                } else done("Google sign-in failed. Please try again.")
            } catch (e: GetCredentialCancellationException) {
                done("Google sign-in was cancelled.")
            } catch (e: GetCredentialException) {
                done("Google sign-in failed: " + (e.message ?: "unknown error"))
            }
        }
    }

    // ---------- building the screen ----------

    private fun darkNow(): Boolean =
        Store.dark ?: ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)

    @Suppress("DEPRECATION")
    private fun buildRoot() {
        Pal.dark = darkNow()
        val keepY = if (::scroll.isInitialized) scroll.scrollY else 0
        window.setBackgroundDrawable(ColorDrawable(Pal.bg))
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, root).apply {
            isAppearanceLightStatusBars = !Pal.dark
            isAppearanceLightNavigationBars = !Pal.dark
        }
        root.setBackgroundColor(Pal.bg)
        root.removeAllViews()
        chatMessages = null; chatScroll = null

        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        headerHolder = FrameLayout(this)
        column.addView(headerHolder, LinearLayout.LayoutParams(MATCH, WRAP))
        scroll = ScrollView(this).apply { isFillViewport = true; overScrollMode = View.OVER_SCROLL_NEVER; isVerticalScrollBarEnabled = false }
        body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Pal.bg) }
        scroll.addView(body, FrameLayout.LayoutParams(MATCH, WRAP))
        column.addView(scroll, LinearLayout.LayoutParams(MATCH, 0, 1f))
        root.addView(column, FrameLayout.LayoutParams(MATCH, MATCH))

        menuLayer = FrameLayout(this)
        chatLayer = FrameLayout(this)
        root.addView(menuLayer, FrameLayout.LayoutParams(MATCH, MATCH))
        root.addView(chatLayer, FrameLayout.LayoutParams(MATCH, MATCH))

        refreshChrome()
        showPage(false)
        scroll.post { scroll.scrollTo(0, keepY) }
        refreshChat()
    }

    private fun showPage(toTop: Boolean) {
        val keepY = scroll.scrollY
        body.removeAllViews()
        body.addView(pages.build(stack.last()), LinearLayout.LayoutParams(MATCH, WRAP))
        body.addView(pages.footer(), LinearLayout.LayoutParams(MATCH, WRAP))
        if (toTop) scroll.scrollTo(0, 0) else scroll.post { scroll.scrollTo(0, keepY) }
        refreshChrome()
        refreshMenus()
    }

    /** Which top-bar link is "on" for the current page. */
    private fun activeRoute(): String = when (val r = stack.last().substringBefore("/")) {
        "artwork" -> "artworks"
        "artist" -> "artists"
        else -> r
    }

    // ---------- top bar ----------

    private fun buildHeader(): View = with(ui) {
        val wrap = vbox()
        wrap.setBackgroundColor(Pal.bg)
        val bar = hbox()
        val side = if (widthDp < 420) 14 else 24
        bar.setPadding(dp(side), 0, dp(side), 0)

        val brand = hbox()
        brand.put(ImageView(ctx).apply {
            setImageDrawable(image("iziko_logo")); setBackgroundColor(Color.WHITE); scaleType = ImageView.ScaleType.FIT_CENTER
        }, w = dp(36), h = dp(36), r = if (widthDp < 420) 10 else 14)
        brand.put(text("SA National Gallery", 13f, Pal.mute, 700, caps = true, track = 0.07f).apply {
            maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END
        }, w = 0, weight = 1f)
        tap(brand) { go("home") }
        brand.contentDescription = "Home"

        val wideNav = widthDp > 1080
        if (wideNav) {
            bar.put(brand, w = WRAP)
            val nav = hbox()
            nav.gravity = Gravity.CENTER
            Content.NAV.forEachIndexed { i, (route, label) ->
                val on = activeRoute() == route
                val color = if (on) (if (Pal.dark) Pal.amber else Pal.rust) else Pal.mute
                val item = vbox()
                item.put(text(t(label), 14f, color).apply { gravity = Gravity.CENTER; maxLines = 1 }, h = 0, weight = 1f)
                item.put(View(ctx).apply { setBackgroundColor(if (on) color else Color.TRANSPARENT) }, h = dp(2))
                tap(item) { go(route) }
                nav.put(item, w = WRAP, h = MATCH, l = if (i > 0) 22 else 0)
            }
            bar.put(nav, w = 0, h = MATCH, weight = 1f, l = 20, r = 20)
        } else {
            bar.put(brand, w = 0, weight = 1f)
            val burger = text(if (navOpen) "✕" else "☰", 16f, Pal.mute).apply { setPadding(dp(8), dp(8), dp(8), dp(8)); contentDescription = "Menu" }
            tap(burger) { navOpen = !navOpen; langOpen = false; refreshMenus(); refreshChrome() }
            bar.put(burger, w = WRAP, l = 4)
        }

        val user = AuthRepository.currentUser
        val account = text(
            if (user != null) "${user.fullName.trim().substringBefore(' ').ifEmpty { "Account" }} · Log out" else "Log in",
            14f, Pal.mute
        ).apply {
            setPadding(dp(8), dp(8), dp(8), dp(8)); maxLines = 1
            if (user == null) paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
        tap(account) {
            if (user != null) { AuthRepository.logout(); refreshChrome(); toast("You are logged out"); go("home") } else go("login")
        }
        val lang = text("🌐 ${Store.lang.uppercase()}", 14f, Pal.mute).apply { setPadding(dp(8), dp(8), dp(8), dp(8)); contentDescription = "Language" }
        tap(lang) { langOpen = !langOpen; navOpen = false; refreshMenus(); refreshChrome() }
        val moon = text(if (Pal.dark) "☀" else "☾", 14f, Pal.mute).apply { setPadding(dp(8), dp(8), dp(8), dp(8)); contentDescription = "Toggle dark mode" }
        tap(moon) { Store.dark = !Pal.dark; Store.save(); buildRoot() }
        bar.put(account, w = WRAP, l = 2)
        bar.put(lang, w = WRAP)
        bar.put(moon, w = WRAP)

        wrap.put(bar, h = dp(60))
        wrap.put(View(ctx).apply { setBackgroundColor(Pal.line) }, h = 1)
        wrap
    }

    /** The drop-down menu (phones and tablets) and the language menu, drawn over the page. */
    private fun refreshMenus() {
        menuLayer.removeAllViews()
        if (!navOpen && !langOpen) return
        with(ui) {
            val scrim = View(ctx)
            scrim.setOnClickListener { navOpen = false; langOpen = false; refreshMenus(); refreshChrome() }
            menuLayer.addView(scrim, FrameLayout.LayoutParams(MATCH, MATCH))
            val top = dp(61)
            if (navOpen && widthDp <= 1080) {
                val m = vbox()
                m.setBackgroundColor(Pal.bg)
                m.setPadding(dp(24), dp(8), dp(24), dp(8))
                Content.NAV.forEach { (route, label) ->
                    val on = activeRoute() == route
                    val item = text(t(label), 14f, if (on) (if (Pal.dark) Pal.amber else Pal.rust) else Pal.mute).apply {
                        setPadding(0, dp(12), 0, dp(12))
                        if (on) contentDescription = "$label, current page"
                    }
                    tap(item) { go(route) }
                    m.put(item)
                }
                val holder = vbox()
                holder.put(m)
                holder.put(View(ctx).apply { setBackgroundColor(Pal.line) }, h = 1)
                holder.elevation = dp(2).toFloat()
                menuLayer.addView(holder, FrameLayout.LayoutParams(MATCH, WRAP).apply { topMargin = top })
            }
            if (langOpen) {
                val m = vbox()
                m.background = shape(Pal.card, Pal.line)
                m.elevation = dp(8).toFloat()
                Content.LANGS.forEach { (k, v) ->
                    val on = k == Store.lang
                    val row = hbox()
                    row.setPadding(dp(14), dp(10), dp(14), dp(10))
                    if (on) row.setBackgroundColor(Pal.bg2)
                    row.put(text("ZA", 10f, Pal.mute), w = WRAP, r = 10)
                    row.put(text(v, 14f, if (on) Pal.rust else Pal.ink), w = WRAP)
                    tap(row) {
                        Store.lang = k; Store.save(); langOpen = false
                        buildRoot()
                    }
                    m.put(row)
                }
                menuLayer.addView(m, FrameLayout.LayoutParams(dp(170), WRAP, Gravity.TOP or Gravity.END).apply { topMargin = top })
            }
        }
    }

    // ---------- gallery assistant ----------

    private fun refreshChat() {
        chatLayer.removeAllViews()
        chatMessages = null; chatScroll = null
        with(ui) {
            val fab = text("💬", 22f, Color.WHITE).apply {
                gravity = Gravity.CENTER; setBackgroundColor(Pal.rust); elevation = dp(6).toFloat()
                contentDescription = "Open gallery assistant"
            }
            tap(fab) { chatOpen = !chatOpen; refreshChat() }
            chatLayer.addView(fab, FrameLayout.LayoutParams(dp(56), dp(56), Gravity.BOTTOM or Gravity.END).apply { setMargins(0, 0, dp(20), dp(20)) })
            if (!chatOpen) return
            if (chatLog.isEmpty()) chatLog.add(false to "Hi! How can I help you plan your visit?")

            val panelW = minOf(360, widthDp - 40)
            val panelH = minOf(440, heightDp - 150)
            val panel = vbox()
            panel.background = shape(Pal.card, Pal.line)
            panel.elevation = dp(12).toFloat()
            panel.put(text("Gallery assistant", 16f, Color.WHITE, 700).apply { setBackgroundColor(Pal.rust); setPadding(dp(16), dp(14), dp(16), dp(14)) })

            val msgs = vbox()
            msgs.setPadding(dp(14), dp(14), dp(14), dp(14))
            val sv = ScrollView(ctx)
            sv.addView(msgs, FrameLayout.LayoutParams(MATCH, WRAP))
            panel.put(sv, h = 0, weight = 1f)
            chatMessages = msgs; chatScroll = sv
            chatLog.forEach { addBubble(it.first, it.second, panelW) }

            val sug = FlowLayout(ctx, dp(6), dp(6))
            sug.setPadding(dp(14), 0, dp(14), dp(10))
            listOf("Opening hours", "Ticket prices", "Where are you?", "Quiz and badges").forEach { s ->
                sug.addView(text(s, 12f, Pal.ink).apply {
                    setPadding(dp(10), dp(4), dp(10), dp(4)); background = shape(Color.TRANSPARENT, Pal.line)
                    tap(this) { ask(s, panelW) }
                })
            }
            panel.put(sug)

            val form = hbox()
            val input = EditText(ctx).apply {
                hint = "Type a question"; setHintTextColor(Pal.mute); setTextColor(Pal.ink)
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
                typeface = Fonts.get(ctx, false, 400, false)
                background = null; isSingleLine = true; imeOptions = EditorInfo.IME_ACTION_SEND
                setPadding(dp(12), dp(12), dp(12), dp(12))
            }
            val send = text("Send", 16f, Pal.rust, 700).apply { setPadding(dp(14), dp(12), dp(14), dp(12)) }
            fun submit() { val v = input.text.toString().trim(); if (v.isNotEmpty()) { ask(v, panelW); input.setText("") } }
            tap(send) { submit() }
            input.setOnEditorActionListener { _, _, _ -> submit(); true }
            form.put(input, w = 0, weight = 1f)
            form.put(send, w = WRAP)
            panel.put(View(ctx).apply { setBackgroundColor(Pal.line) }, h = 1)
            panel.put(form)
            chatLayer.addView(panel, FrameLayout.LayoutParams(dp(panelW), dp(panelH), Gravity.BOTTOM or Gravity.END).apply { setMargins(0, 0, dp(20), dp(88)) })
        }
    }

    private fun addBubble(mine: Boolean, message: String, panelW: Int) {
        val box = chatMessages ?: return
        with(ui) {
            val b = text(message, 14f, if (mine) Color.WHITE else Pal.ink).apply {
                setPadding(dp(12), dp(9), dp(12), dp(9)); maxWidth = dp((panelW * 0.85f).toInt())
                setBackgroundColor(if (mine) Pal.amber else Pal.bg2)
            }
            box.put(b, w = WRAP, t = 8, gravity = if (mine) Gravity.END else Gravity.START)
        }
        chatScroll?.post { chatScroll?.fullScroll(View.FOCUS_DOWN) }
    }

    /** Keyword matching, no server needed: the first matching rule answers . */
    private fun ask(question: String, panelW: Int) {
        val q = question.lowercase()
        val answer = Content.FAQ.firstOrNull { it.first.containsMatchIn(q) }?.second ?: Content.FAQ_FALLBACK
        chatLog.add(true to question); chatLog.add(false to answer)
        addBubble(true, question, panelW)
        addBubble(false, answer, panelW)
    }
}
