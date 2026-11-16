package com.example.izikomuseum.ui

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.izikomuseum.data.Content
import com.example.izikomuseum.data.Content.t
import com.example.izikomuseum.data.model.Artwork
import com.example.izikomuseum.data.model.Exhibition
import com.example.izikomuseum.data.repository.AuthRepository
import com.example.izikomuseum.data.repository.MuseumRepository
import com.example.izikomuseum.data.repository.Store

/** What the pages need from the screen that hosts them. */
interface Host {
    val ui: Ui
    fun go(route: String)
    fun back()
    fun toast(message: String)
    /** Rebuilds the page that is showing now (like the website's route()). */
    fun refreshPage()
    /** Rebuilds the top bar (after logging in or out). */
    fun refreshChrome()
    fun openUrl(url: String)
    fun share(text: String)
    /** Starts Google sign-in (Firebase). [done] gets null on success or an error message. */
    fun googleSignIn(done: (String?) -> Unit)
    /** Called after any successful sign-in: refreshes the bar, welcomes the visitor and syncs their progress. */
    fun afterSignIn()
}

/** Builds each page of the website as a native view tree. Routes are the same as the website's "#/page/id" hashes. */
class Pages(val host: Host) {

    val ui: Ui get() = host.ui
    fun go(route: String) = host.go(route)
    fun openUrl(url: String) = host.openUrl(url)

    fun build(route: String): View {
        val parts = route.split("/")
        val name = parts.getOrNull(0) ?: "home"
        val arg = parts.getOrNull(1)
        currentIsCollection = name == "collection"
        return when (name) {
            "artworks" -> artworksPage()
            "artwork" -> artworkPage(arg?.toIntOrNull())
            "artists" -> artistsPage()
            "artist" -> artistPage(arg?.toIntOrNull())
            "exhibitions" -> exhibitionsPage()
            "quiz" -> quizPage()
            "visit" -> visitPage()
            "collection" -> collectionPage()
            "tour" -> tourPage()
            "challenge" -> challengePage()
            "login" -> authPage(false)
            "register" -> authPage(true)
            "info" -> infoPage(arg ?: "")
            else -> homePage()
        }
    }

    // ---------- shared components ----------

    /** Round heart button (.heart). Tapping saves or removes the artwork and shows a toast. */
    fun heart(id: Int): TextView = with(ui) {
        val saved = Store.saved.contains(id)
        val h = text(if (saved) "♥" else "♡", 19f, if (saved) Pal.red else Color.parseColor("#8A7D70")).apply {
            gravity = Gravity.CENTER
            background = shape(Color.argb(235, 255, 252, 247), oval = true)
            includeFontPadding = false
        }
        h.contentDescription = "Save to My Collection"
        tap(h) {
            val was = Store.saved.contains(id)
            if (was) Store.saved.remove(id) else Store.saved.add(id)
            if (!was) Store.addPts(5)
            Store.save()
            host.toast(if (was) "Removed from My Collection" else "Saved to My Collection")
            if (currentIsCollection) host.refreshPage()
            else {
                h.text = if (was) "♡" else "♥"
                h.setTextColor(if (was) Color.parseColor("#8A7D70") else Pal.red)
            }
        }
        h.foreground = null
        h
    }

    var currentIsCollection = false

    /** The artwork card (.art): picture, category, title, artist and date. Tap to open the detail page. */
    fun artCard(w: Artwork): View = with(ui) {
        val c = vbox()
        c.background = shape(Pal.card, Pal.line)
        val im = AspectFrame(ctx, 0.75f)
        im.setBackgroundColor(Pal.bg2)
        val pic = image(w.image)
        if (pic != null) im.addView(cover(w.image, 0).apply { contentDescription = w.title }, FrameLayout.LayoutParams(MATCH, MATCH))
        else im.addView(text("Image not available", 15f, Pal.mute, serif = true, italic = true).apply { gravity = Gravity.CENTER },
            FrameLayout.LayoutParams(MATCH, MATCH, Gravity.CENTER))
        val hp = FrameLayout.LayoutParams(dp(38), dp(38), Gravity.TOP or Gravity.END)
        hp.setMargins(0, dp(10), dp(10), 0)
        im.addView(heart(w.id), hp)
        c.put(im)
        val b = vbox()
        pad(b, 18, 16, 18, 20)
        b.put(text(w.category, 11f, Pal.amber, 700, caps = true, track = 0.12f))
        b.put(text(w.title, 19f, Pal.ink, 700, serif = true, lineMult = 1.2f), t = 4, b = 4)
        b.put(text(w.artistName + if (w.date.isNotEmpty()) ", " + w.date else "", 14f, Pal.mute))
        c.put(b)
        tap(c) { currentIsCollection = false; host.go("artwork/${w.id}") }
        c
    }

    /** Exhibition card (.ex): picture on the left (top on phones) and the details next to it. */
    fun exCard(e: Exhibition): View = with(ui) {
        val wide = atLeast(700)
        val root = LinearLayout(ctx)
        root.background = shape(Pal.card, Pal.line)
        val info = vbox()
        pad(info, 24, 24, 24, 24)
        info.put(text(e.status, 12f, Pal.rust, 700, caps = true, track = 0.1f))
        info.put(text(e.title, 22f, Pal.ink, 700, serif = true, lineMult = 1.2f), t = 6, b = 6)
        info.put(text(e.dates, 16f, Pal.rust, 500))
        if (!e.curator.isNullOrEmpty()) info.put(text("Curated by ${e.curator}", 14f, Pal.mute))
        info.put(text(e.summary, 16f, Pal.mute, lineMult = 1.4f), t = 10)
        if (wide) {
            root.orientation = LinearLayout.HORIZONTAL
            root.put(cover(e.image, 180).apply { contentDescription = e.title }, w = dp(280), h = MATCH)
            root.put(info, w = 0, weight = 1f)
        } else {
            root.orientation = LinearLayout.VERTICAL
            naturalImage(e.image)?.let { root.put(it) }
            root.put(info)
        }
        root
    }

    // ---------- footer ----------

    fun footer(): View = with(ui) {
        val outer = vbox()
        outer.setBackgroundColor(Pal.bg)
        outer.put(View(ctx).apply { setBackgroundColor(Pal.line) }, h = 1)
        val w = vbox()
        w.setPadding(dp(24), dp(48), dp(24), dp(28))

        val brand = vbox()
        brand.put(android.widget.ImageView(ctx).apply {
            setImageDrawable(image("iziko_logo")); setBackgroundColor(Color.WHITE); scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
        }, w = dp(40), h = dp(40), b = 12)
        brand.put(text("Part of Iziko Museums of South Africa. Government Avenue, Company's Garden, Cape Town.", 14f, Pal.mute))

        val explore = vbox()
        explore.put(text("EXPLORE", 11f, Pal.mute, 700, track = 0.14f), b = 12)
        listOf("artworks", "artists", "exhibitions", "quiz", "visit", "collection").forEach { r ->
            val label = t(Content.NAV.first { it.first == r }.second)
            explore.put(link(label) { currentIsCollection = false; host.go(r) }, w = WRAP, t = 6, b = 6)
        }

        val contact = vbox()
        contact.put(text("CONTACT", 11f, Pal.mute, 700, track = 0.14f), b = 12)
        contact.put(link("+27 21 467 4660") { host.openUrl("tel:+27214674660") }, w = WRAP, t = 6, b = 6)
        contact.put(link("isang@iziko.org.za") { host.openUrl("mailto:isang@iziko.org.za") }, w = WRAP, t = 6, b = 6)
        contact.put(text("Mon–Sun: 09:00 – 17:00", 14f, Pal.mute), t = 6, b = 6)

        if (atLeast(760)) {
            val g = LinearLayout(ctx)
            g.put(brand, w = 0, weight = 1.4f, r = 20)
            g.put(explore, w = 0, weight = 1f, l = 20, r = 20)
            g.put(contact, w = 0, weight = 1f, l = 20)
            w.put(g)
        } else {
            w.put(brand); w.put(explore, t = 40); w.put(contact, t = 40)
        }

        w.put(View(ctx).apply { setBackgroundColor(Pal.line) }, h = 1, t = 30)
        val copy = hbox()
        copy.setPadding(0, dp(20), 0, 0)
        copy.put(text("© 2026 Iziko South African National Gallery", 14f, Pal.mute), w = 0, weight = 1f)
        w.put(copy)
        val links = hbox()
        listOf("about" to "About", "faq" to "FAQ", "accessibility" to "Accessibility").forEachIndexed { i, p ->
            links.put(link(p.second) { host.go("info/${p.first}") }, w = WRAP, r = if (i < 2) 18 else 0)
        }
        w.put(links, t = 10)
        outer.put(w, w = dp(minOf(widthDp, 1100)), gravity = Gravity.CENTER_HORIZONTAL)
        outer
    }

    private fun link(label: String, onClick: () -> Unit): TextView = with(ui) {
        text(label, 14f, Pal.mute).apply { setPadding(0, dp(4), 0, dp(4)); tap(this, onClick) }
    }

    /** Orange small-caps text link (.lk). */
    fun lk(label: String, onClick: () -> Unit): TextView = with(ui) {
        text(label, 13f, Pal.amber, 700, caps = true, track = 0.08f).apply { setPadding(0, dp(8), 0, dp(8)); tap(this, onClick) }
    }

    fun empty(title: String, message: String, glyph: String? = null, button: Pair<String, String>? = null): View = with(ui) {
        val e = vbox()
        e.gravity = Gravity.CENTER_HORIZONTAL
        e.setPadding(0, dp(90), 0, dp(90))
        if (glyph != null) e.put(text(glyph, 56f, Pal.line, serif = true).apply { gravity = Gravity.CENTER }, w = WRAP)
        e.put(text(title, 24f, Pal.ink, 700, serif = true).apply { gravity = Gravity.CENTER }, t = 8, b = 8)
        e.put(text(message, 16f, Pal.mute).apply { gravity = Gravity.CENTER })
        if (button != null) e.put(btn(button.first) { host.go(button.second) }, w = WRAP, t = 16)
        e
    }

    fun gallerySection(content: LinearLayout.() -> Unit): LinearLayout = ui.section(false, build = content)

    fun page(vararg sections: View): LinearLayout = with(ui) {
        val p = vbox()
        p.setBackgroundColor(Pal.bg)
        sections.forEach { p.put(it) }
        p
    }

    val loggedInName: String? get() = AuthRepository.currentUser?.fullName
    val allArtworks get() = MuseumRepository.allArtworks()
}
