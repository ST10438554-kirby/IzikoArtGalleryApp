package com.example.izikomuseum.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.izikomuseum.data.Content
import com.example.izikomuseum.data.Content.t
import com.example.izikomuseum.data.repository.MuseumRepository

private fun mapsDirections() = "https://www.google.com/maps/dir/?api=1&destination=" + android.net.Uri.encode(Content.MAP_QUERY)

/** Paragraph where the **marked** words are darker and medium weight (the website's <b> inside .hist). */
private fun rich(text: String, ink: Int): CharSequence {
    val out = SpannableStringBuilder()
    text.split("**").forEachIndexed { i, part ->
        val start = out.length
        out.append(part)
        if (i % 2 == 1) {
            out.setSpan(ForegroundColorSpan(ink), start, out.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            out.setSpan(TypefaceSpan("sans-serif-medium"), start, out.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    return out
}

internal fun Pages.homePage(): View = with(ui) {
    val page = vbox()
    page.setBackgroundColor(Pal.bg)

    // ----- hero -----
    val heroH = (heightDp - 84).coerceIn(440, 760)
    val hero = FrameLayout(ctx)
    hero.setBackgroundColor(Color.parseColor("#2A1208"))
    hero.addView(cover("museum_hero", 0).apply { contentDescription = "The gallery building" }, FrameLayout.LayoutParams(MATCH, MATCH))
    hero.addView(View(ctx).apply {
        background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.argb(209, 25, 8, 2), Color.argb(89, 25, 8, 2)))
    }, FrameLayout.LayoutParams(MATCH, MATCH))
    val heroBody = vbox()
    heroBody.gravity = Gravity.CENTER_VERTICAL
    heroBody.setPadding(dp(24), 0, dp(24), 0)
    heroBody.put(text("EST. 1930 · CAPE TOWN", 11f, Color.WHITE, 700, track = 0.12f).apply {
        setPadding(dp(10), dp(5), dp(10), dp(5)); background = shape(Pal.amber)
    }, w = WRAP, b = 22)
    val h1Size = (widthDp * 0.07f).coerceIn(40f, 76f)
    heroBody.put(text("Iziko South African", h1Size, Color.WHITE, 700, serif = true, lineMult = 1.1f))
    heroBody.put(text("National Gallery", h1Size, Pal.amber, 700, serif = true, italic = true, lineMult = 1.1f))
    heroBody.put(text("South Africa's premier public art museum. Over 10,000 artworks spanning five centuries, in the heart of Company's Garden, Cape Town.",
        18f, Color.argb(235, 255, 255, 255), lineMult = 1.45f).apply { maxWidth = dp(612) }, w = WRAP, t = 22, b = 30)
    val b1 = btn("${t("Explore Artworks")} ›") { go("artworks") }
    val b2 = btn(t("Plan Your Visit"), "ghost") { go("visit") }
    val btns = if (contentDp >= 380) hbox() else vbox()
    if (contentDp >= 380) { btns.put(b1, w = WRAP, r = 12); btns.put(b2, w = WRAP) }
    else { btns.put(b1, w = WRAP); btns.put(b2, w = WRAP, t = 12) }
    heroBody.put(btns, w = WRAP)
    hero.addView(heroBody, FrameLayout.LayoutParams(dp(minOf(widthDp, 1100)), MATCH, Gravity.CENTER_HORIZONTAL))
    page.put(hero, h = dp(heroH))

    // ----- quick links band -----
    val quick = vbox()
    quick.setBackgroundColor(Pal.rust)
    val cells = listOf("🖼️" to "artworks" to "Artworks", "🎨" to "artists" to "Artists", "🗓️" to "exhibitions" to "Exhibitions", "🏆" to "quiz" to "Quiz")
        .map { (a, label) ->
            val (emoji, route) = a
            val cell = hbox()
            cell.setPadding(dp(24), dp(20), dp(24), dp(20))
            cell.background = null
            cell.put(text(emoji, 16f, Color.WHITE), w = WRAP, r = 12)
            cell.put(text(t(label), 16f, Color.WHITE, 500), w = 0, weight = 1f)
            cell.put(text("›", 16f, Color.argb(153, 255, 255, 255)), w = WRAP)
            val framed = hbox()
            framed.put(View(ctx).apply { setBackgroundColor(Color.argb(46, 255, 255, 255)) }, w = 1, h = MATCH)
            framed.put(cell, w = 0, weight = 1f)
            tap(framed) { go(route) }
            framed as View
        }
    val qWrap = rows(cells, if (atLeast(700)) 4 else 2, 0)
    quick.put(qWrap, w = dp(minOf(widthDp, 1100)), gravity = Gravity.CENTER_HORIZONTAL)
    page.put(quick)

    // ----- history + opening hours + location -----
    page.put(section {
        val hist = vbox()
        hist.put(eyebrow("Our history"), b = 8)
        hist.put(h1("Over a century of South African art"), b = 10)
        listOf(
            "The origins of the gallery trace to the South African Fine Arts Association, founded in **1850**. The national collection began in **1872** through a donation from Thomas Butterworth Bayley.",
            "Construction of the current building began in **1914**. The gallery officially opened on **3 November 1930**.",
            "In **2001** it became part of Iziko Museums of South Africa. Today the collection spans paintings, sculpture, photography, beadwork, textiles and architecture from South African, African and international artists."
        ).forEach { hist.put(text(rich(it, Pal.ink), 16f, Pal.mute, lineMult = 1.5f), t = 16) }

        val side = vbox()
        val red = vbox()
        red.setBackgroundColor(Pal.rust)
        red.setPadding(dp(28), dp(28), dp(28), dp(28))
        red.put(text("🕘 Opening Hours", 19f, Color.WHITE, 700, serif = true), b = 16)
        red.put(hoursRow("Monday – Sunday", "09:00 – 17:00", onRed = true))
        red.put(text("Closed on Christmas Day and Workers Day (1 May).", 13f, Color.argb(217, 255, 255, 255)), t = 12)
        side.put(red)
        val loc = box()
        loc.put(text("📍 Location", 19f, Pal.ink, 700, serif = true), b = 16)
        loc.put(text("Government Avenue\nCompany's Garden\nCape Town, South Africa", 16f, Pal.mute, lineMult = 1.5f))
        loc.put(lk("Get directions ›") { openUrl(mapsDirections()) }, w = WRAP, t = 6)
        side.put(loc, t = 16)
        put(twoCols(hist, side, 860, 1.3f, 1f, 56))
    })

    // ----- featured -----
    val featured = MuseumRepository.allArtworks().filter { it.featured && it.image != null }.take(4)
    page.put(section(alt = true) {
        put(eyebrow("Featured"), b = 8)
        put(h1("Highlights from the collection"))
        put(autoGrid(featured.map { artCard(it) }, 250), t = 28)
    })

    // ----- exhibitions -----
    page.put(section {
        put(eyebrow("Now showing"), b = 8)
        put(h1("Exhibitions"))
        MuseumRepository.getExhibitions().take(2).forEach { put(exCard(it), t = 20) }
    })

    // ----- facts -----
    page.put(section(alt = true) {
        put(eyebrow("Did you know?"), b = 8)
        put(h1(t("Gallery Facts")))
        val cellsF = Content.FACTS.mapIndexed { i, f ->
            val c = hbox()
            c.gravity = Gravity.TOP
            c.setBackgroundColor(Pal.card)
            c.setPadding(dp(24), dp(26), dp(24), dp(26))
            c.put(text((i + 1).toString().padStart(2, '0'), 22f, Pal.amber, 700, serif = true), w = WRAP, r = 16)
            c.put(text(f, 15f, Pal.mute, lineMult = 1.4f), w = 0, weight = 1f)
            c as View
        }
        val frame = vbox()
        frame.setBackgroundColor(Pal.line)
        frame.setPadding(dp(1), dp(1), dp(1), dp(1))
        if (atLeast(860)) {
            frame.put(rows(cellsF.take(9), 3, 1))
            frame.put(cellsF.last(), t = 1)
        } else frame.put(rows(cellsF, 1, 1))
        put(frame, t = 28)
    })
    page
}

internal fun Pages.artworksPage(): View = with(ui) {
    var cat = ""
    val cats = listOf("") + MuseumRepository.categories()
    val list = vbox()
    val chips = FlowLayout(ctx, dp(8), dp(8))
    val input = EditText(ctx).apply {
        hint = "Search by title or artist"
        contentDescription = "Search artworks"
        setHintTextColor(Pal.mute)
        setTextColor(Pal.ink)
        setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
        typeface = Fonts.get(ctx, false, 400, false)
        isSingleLine = true
        imeOptions = EditorInfo.IME_ACTION_SEARCH
        setPadding(dp(14), dp(12), dp(14), dp(12))
        background = shape(Pal.card, Pal.line)
    }
    fun render() {
        val results = MuseumRepository.searchArtworks(input.text.toString(), cat)
        list.removeAllViews()
        if (results.isEmpty()) list.put(empty("No artworks match your search", "Try a different title, artist or category."))
        else list.put(autoGrid(results.map { artCard(it) }, 250))
        chips.removeAllViews()
        cats.forEach { c ->
            val on = c == cat
            chips.addView(text(if (c.isEmpty()) "All" else c, 14f, if (on) Color.WHITE else Pal.ink).apply {
                setPadding(dp(16), dp(8), dp(16), dp(8))
                background = shape(if (on) Pal.rust else Color.TRANSPARENT, if (on) Pal.rust else Pal.line)
                tap(this) { cat = c; render() }
            })
        }
    }
    input.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun afterTextChanged(s: Editable?) { render() }
    })
    render()
    page(section {
        head("Collection", t("Explore Artworks"), "Search by title or artist, then tap the heart to save a work to My Collection.")
        put(input, t = 24)
        put(chips, t = 12)
        put(list, t = 28)
    })
}

internal fun Pages.artworkPage(id: Int?): View = with(ui) {
    val w = id?.let { MuseumRepository.getArtwork(it) }
    if (w == null) return page(section { put(empty("Artwork not found", "", button = "Back to artworks" to "artworks")) })
    val left = FrameLayout(ctx)
    naturalImage(w.image)?.let { left.addView(it.apply { contentDescription = "${w.title} by ${w.artistName}" }, FrameLayout.LayoutParams(MATCH, WRAP)) }
    left.addView(heart(w.id), FrameLayout.LayoutParams(dp(38), dp(38), Gravity.TOP or Gravity.END).apply { setMargins(0, dp(10), dp(10), 0) })
    val right = vbox()
    right.put(eyebrow(w.category), b = 8)
    right.put(h1(w.title), b = 10)
    right.put(text(w.artistName, 16f, Pal.amber).apply { tap(this) { go("artist/${w.artistId}") } }, w = WRAP)
    val dl = vbox()
    listOf("Date" to w.date, "Medium" to w.medium, "Size" to w.dimensions, "Collection" to w.acquisition)
        .filter { !it.second.isNullOrEmpty() }.forEach { (k, v) ->
            val row = LinearLayout(ctx)
            row.put(text(k, 15f, Pal.mute), w = dp(110))
            row.put(text(v!!, 15f, Pal.ink), w = 0, weight = 1f)
            dl.put(row, b = 8)
        }
    right.put(dl, t = 20, b = 12)
    right.put(lead(w.description), w = WRAP)
    page(section {
        put(text("‹ Back", 16f, Pal.amber, 500).apply { tap(this) { host.back() } }, w = WRAP)
        put(twoCols(left, right, 800, gapDp = 48), t = 20)
    })
}

internal fun Pages.artistsPage(): View = with(ui) {
    val cards = MuseumRepository.getArtists().map { a ->
        val c = vbox()
        c.background = shape(Pal.card, Pal.line)
        val im = AspectFrame(ctx, 0.75f)
        im.setBackgroundColor(Pal.bg2)
        if (image(a.image) != null) im.addView(cover(a.image, 0).apply { contentDescription = a.name }, FrameLayout.LayoutParams(MATCH, MATCH))
        else im.addView(text(a.name, 15f, Pal.mute, serif = true, italic = true).apply { gravity = Gravity.CENTER }, FrameLayout.LayoutParams(MATCH, MATCH, Gravity.CENTER))
        c.put(im)
        val b = vbox()
        pad(b, 18, 16, 18, 20)
        b.put(text(listOf(a.lifespan, a.origin).filter { it.isNotEmpty() }.joinToString(" · "), 11f, Pal.amber, 700, caps = true, track = 0.12f))
        b.put(text(a.name, 19f, Pal.ink, 700, serif = true, lineMult = 1.2f), t = 4, b = 4)
        b.put(text("${MuseumRepository.artworksBy(a.id).size} artwork(s) in the collection", 14f, Pal.mute))
        c.put(b)
        tap(c) { go("artist/${a.id}") }
        c as View
    }
    page(section {
        head("People", "Artists", "The artists represented in the collection.")
        put(autoGrid(cards, 250), t = 28)
    })
}

internal fun Pages.artistPage(id: Int?): View = with(ui) {
    val a = id?.let { MuseumRepository.getArtist(it) }
    if (a == null) return page(section { put(empty("Artwork not found", "", button = "Back to artworks" to "artworks")) })
    val left = vbox()
    naturalImage(a.image)?.let { left.put(it.apply { contentDescription = a.name }) }
    val right = vbox()
    right.put(eyebrow(listOf(a.lifespan, a.origin).filter { it.isNotEmpty() }.joinToString(" · ")), b = 8)
    right.put(h1(a.name), b = 10)
    right.put(lead(a.biography), w = WRAP)
    page(section {
        put(text("‹ Back", 16f, Pal.amber, 500).apply { tap(this) { host.back() } }, w = WRAP)
        put(twoCols(left, right, 800, gapDp = 48), t = 20)
        put(autoGrid(MuseumRepository.artworksBy(a.id).map { artCard(it) }, 250), t = 28)
    })
}

internal fun Pages.exhibitionsPage(): View = with(ui) {
    page(section {
        head("What's on", "Exhibitions", "Current and recent exhibitions at the gallery.")
        MuseumRepository.getExhibitions().forEach { put(exCard(it), t = 20) }
    })
}
