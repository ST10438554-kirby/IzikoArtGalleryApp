package com.example.izikomuseum.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.izikomuseum.data.Content
import com.example.izikomuseum.data.repository.AuthRepository

private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
internal fun Pages.visitPage(): View = with(ui) {
    val enc = android.net.Uri.encode(Content.MAP_QUERY)

    // ----- title band with the gallery photo -----
    val band = FrameLayout(ctx)
    band.setBackgroundColor(Color.parseColor("#2A1208"))
    band.addView(cover("gallery_event", 0), FrameLayout.LayoutParams(MATCH, MATCH))
    band.addView(View(ctx).apply { setBackgroundColor(Color.argb(166, 25, 8, 2)) }, FrameLayout.LayoutParams(MATCH, MATCH))
    val bandText = vbox()
    bandText.setPadding(dp(24), dp(70), dp(24), dp(70))
    bandText.put(eyebrow("Plan ahead"), b = 8)
    bandText.put(text("Visit the Gallery", (widthDp * 0.05f).coerceIn(34f, 56f), Color.WHITE, 700, serif = true, lineMult = 1.1f))
    band.addView(bandText, FrameLayout.LayoutParams(dp(minOf(widthDp, 1100)), WRAP, Gravity.CENTER_HORIZONTAL))

    // ----- three info boxes -----
    val tickets = box()
    tickets.put(text("🎟️ Ticket Prices", 19f, Pal.ink, 700, serif = true), b = 16)
    Content.TICKETS.forEach { tickets.put(hoursRow(it.first, "R${it.second}", Pal.rust)) }
    tickets.put(text("Free entry for SA pensioners and students on Fridays with valid ID.", 13f, Pal.mute), t = 14)
    tickets.put(btn("Book tickets online") { openUrl(Content.BOOK_URL) }, w = WRAP, t = 14)

    val hours = box()
    hours.put(text("🕘 Opening Hours", 19f, Pal.ink, 700, serif = true), b = 16)
    hours.put(hoursRow("Monday – Sunday", "09:00 – 17:00"))
    hours.put(hoursRow("Christmas Day", "Closed", Pal.rust))
    hours.put(hoursRow("Workers Day (1 May)", "Closed", Pal.rust))

    val loc = box()
    loc.put(text("📍 Location", 19f, Pal.ink, 700, serif = true), b = 16)
    loc.put(text("Government Avenue\nCompany's Garden\nCape Town\nSouth Africa", 16f, Pal.mute, lineMult = 1.5f))
    loc.put(lk("Open in Google Maps ›") { openUrl("https://www.google.com/maps/search/?api=1&query=$enc") }, w = WRAP, t = 6)

    // ----- embedded map -----
    val mapFrame = FrameLayout(ctx)
    mapFrame.background = shape(Pal.card, Pal.line)
    mapFrame.setPadding(dp(1), dp(1), dp(1), dp(1))
    val web = WebView(ctx)
    web.settings.javaScriptEnabled = true
    web.webViewClient = WebViewClient()
    web.setOnTouchListener { v, e ->
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> v.parent?.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.parent?.requestDisallowInterceptTouchEvent(false)
        }
        false
    }
    web.contentDescription = "Map of the gallery"
    web.loadUrl("https://www.google.com/maps?q=$enc&z=16&output=embed")
    mapFrame.addView(web, FrameLayout.LayoutParams(MATCH, MATCH))

    // ----- floor map -----
    val floorCards = Content.FLOORS.map { (title, rooms) ->
        val f = vbox()
        f.setBackgroundColor(Pal.card)
        f.setPadding(dp(16), dp(16), dp(16), dp(16))
        f.put(text(title, 11f, Pal.rust, 700, track = 0.12f), b = 2)
        rooms.forEach { r ->
            f.put(text(r, 12f, Pal.ink).apply { gravity = Gravity.CENTER; setBackgroundColor(Pal.bg2); setPadding(dp(7), dp(7), dp(7), dp(7)) }, t = 8)
        }
        f as View
    }
    val floors = LinearLayout(ctx)
    floors.background = shape(Pal.bg2, Pal.line)
    floors.setPadding(dp(30), dp(30), dp(30), dp(30))
    if (contentDp - 60 >= 3 * 190 + 32) {
        floors.orientation = LinearLayout.HORIZONTAL
        floors.gravity = Gravity.CENTER_HORIZONTAL
        floorCards.forEachIndexed { i, c -> floors.put(c, w = dp(190), r = if (i < 2) 16 else 0) }
    } else {
        floors.orientation = LinearLayout.VERTICAL
        floorCards.forEachIndexed { i, c -> floors.put(c, w = dp(190), t = if (i > 0) 16 else 0, gravity = Gravity.CENTER_HORIZONTAL) }
    }

    // ----- nearby -----
    val near = Content.NEARBY.map { (name, info) ->
        val n = hbox()
        n.gravity = Gravity.TOP
        n.background = shape(Pal.card, Pal.line)
        n.setPadding(dp(16), dp(16), dp(16), dp(16))
        n.put(text("📍", 16f, Pal.amber), w = WRAP, r = 14)
        val col = vbox()
        col.put(text(name, 16f, Pal.ink))
        col.put(text(info, 14f, Pal.mute))
        n.put(col, w = 0, weight = 1f)
        n as View
    }
    val colsN = if (atLeast(860)) 3 else 1

    val p = vbox()
    p.setBackgroundColor(Pal.bg)
    p.put(band)
    p.put(section {
        put(rows(listOf(tickets, hours, loc), colsN, 20))
        put(mapFrame, h = dp(340), t = 20)
        put(h2("Gallery Floor Map"), t = 48)
        put(floors, t = 20)
        put(text("Schematic only, not to scale.", 13f, Pal.mute).apply { gravity = Gravity.CENTER }, t = 8)
        put(h2("Nearby Attractions"), t = 48)
        put(rows(near, colsN, 20), t = 20)
    })
    p
}

internal fun Pages.infoPage(key: String): View = with(ui) {
    val info = Content.INFO[key] ?: ("Not found" to "")
    page(section {
        put(h1(info.first))
        put(lead(info.second), w = WRAP, t = 10)
    })
}

/** Log in / register card (.auth). Accounts are kept by AuthRepository; Google sign-in needs Firebase set up first. */
internal fun Pages.authPage(register: Boolean): View = with(ui) {
    fun field(password: Boolean, type: Int = InputType.TYPE_CLASS_TEXT): EditText = EditText(ctx).apply {
        setTextColor(Pal.ink)
        setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
        typeface = Fonts.get(ctx, false, 400, false)
        isSingleLine = true
        inputType = if (password) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else type
        setPadding(dp(12), dp(12), dp(12), dp(12))
        background = shape(Pal.bg, Pal.line)
    }
    val name = field(false, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME or InputType.TYPE_TEXT_FLAG_CAP_WORDS)
    val email = field(false, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
    val pw = field(true)
    val pw2 = field(true)
    val err = text("", 14f, Pal.red).apply { visibility = View.GONE }

    val card = vbox()
    card.gravity = Gravity.CENTER_HORIZONTAL
    card.background = shape(Pal.card, Pal.line)
    card.setPadding(dp(36), dp(36), dp(36), dp(36))
    card.put(ImageView(ctx).apply {
        setImageDrawable(image("iziko_logo")); setBackgroundColor(Color.WHITE); scaleType = ImageView.ScaleType.FIT_CENTER
    }, w = dp(72), h = dp(72), b = 14)
    card.put(text(if (register) "Create your account" else "Welcome back", 32f, Pal.ink, 700, serif = true).apply { gravity = Gravity.CENTER })
    card.put(lead(if (register) "Save your progress and artworks to your account." else "Log in to save your progress and artworks.").apply { gravity = Gravity.CENTER }, w = WRAP, t = 10, b = 18)
    fun labelled(label: String, f: EditText) {
        card.put(text(label, 13f, Pal.mute), t = 14)
        card.put(f, t = 4)
    }
    if (register) labelled("Full name", name)
    labelled("Email address", email)
    labelled("Password", pw)
    if (register) labelled("Confirm password", pw2)
    card.put(err, t = 12)

    fun fail(m: String, go: TextView) { err.text = m; err.visibility = View.VISIBLE; go.alpha = 1f; go.isClickable = true }
    fun busy(go: TextView) { go.alpha = 0.6f; go.isClickable = false }
    val go = btn(if (register) "Create account" else "Log in") {}
    go.setOnClickListener {
        err.visibility = View.GONE
        val em = email.text.toString().trim()
        val pass = pw.text.toString()
        if (register) {
            if (name.text.toString().trim().isEmpty()) return@setOnClickListener fail("Please enter your full name.", go)
            if (!EMAIL.matches(em)) return@setOnClickListener fail("Please enter a valid email address.", go)
            if (pass.length < 8) return@setOnClickListener fail("Use a password with at least 8 characters.", go)
            if (pass != pw2.text.toString()) return@setOnClickListener fail("The passwords do not match.", go)
        } else if (em.isEmpty() || pass.isEmpty()) return@setOnClickListener fail("Please enter your email and password.", go)

        busy(go)
        val finished: (String?) -> Unit = { error -> if (error == null) host.afterSignIn() else fail(error, go) }
        if (register) AuthRepository.register(name.text.toString(), em, pass, finished)
        else AuthRepository.login(em, pass, finished)
    }
    card.put(go, t = 18)
    card.put(text("or", 13f, Pal.mute).apply { gravity = Gravity.CENTER }, t = 14)
    val gLabel = android.text.SpannableStringBuilder("G  Continue with Google").apply {
        setSpan(android.text.style.ForegroundColorSpan(Color.parseColor("#4285F4")), 0, 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    val google = btn(gLabel, "line") {
        err.visibility = View.GONE
        busy(go)
        host.googleSignIn { error -> if (error == null) host.afterSignIn() else fail(error, go) }
    }
    card.put(google, t = 10)
    card.put(lk(if (register) "Already have an account? Log in" else "Don't have an account? Register") {
        host.go(if (register) "login" else "register")
    }, w = WRAP, t = 10)

    page(section(topDp = 56) {
        put(card, w = dp(minOf(contentDp, 420)), gravity = Gravity.CENTER_HORIZONTAL, t = 20, b = 20)
    })
}
