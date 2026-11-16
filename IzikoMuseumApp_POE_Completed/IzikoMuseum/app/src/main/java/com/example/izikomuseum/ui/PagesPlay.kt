package com.example.izikomuseum.ui

import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.izikomuseum.data.Content
import com.example.izikomuseum.data.Content.t
import com.example.izikomuseum.data.repository.MuseumRepository
import com.example.izikomuseum.data.repository.Store
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

/** One answer button (.opt): letter box + answer text. */
private fun Ui.option(letter: String, label: String): LinearLayout {
    val o = hbox()
    o.setPadding(dp(16), dp(14), dp(16), dp(14))
    o.background = shape(Pal.card, Pal.line)
    o.put(text(letter, 13f, Pal.ink, 700).apply { gravity = Gravity.CENTER; background = shape(Color.TRANSPARENT, Pal.line) }, w = dp(28), h = dp(28), r = 14)
    o.put(text(label, 16f, Pal.ink), w = 0, weight = 1f)
    return o
}

/** Colours an answered option: green for the right answer, red for a wrong pick. */
private fun Ui.mark(o: View, good: Boolean) {
    val c = if (good) Pal.ok else Pal.red
    o.background = shape(Pal.mix(Pal.card, c, if (good) 0.14f else 0.12f), c)
}

internal fun Pages.quizPage(): View = with(ui) {
    val qz = vbox()

    fun showLevels() {
        qz.removeAllViews()
        qz.head("Test your knowledge", "Art Quiz", "Choose a difficulty level and earn your badge. Share your score with friends!")
        val cards = Content.QUIZ.map { lv ->
            val c = vbox()
            c.background = shape(Pal.card, Pal.line)
            c.setPadding(dp(24), dp(28), dp(24), dp(28))
            c.put(text(lv.emoji, 30f, Pal.ink))
            c.put(text(lv.name, 24f, Pal.ink, 700, serif = true), t = 24, b = 6)
            c.put(text(lv.desc, 16f, Pal.mute))
            Store.quiz[lv.name]?.let { c.put(text("Best: $it%", 14f, Pal.ok, 700), t = 10) }
            tap(c) { runQuiz(qz, lv, ::showLevels) }
            c as View
        }
        qz.put(rows(cards, if (atLeast(760)) 3 else 1, 20), t = 32)
    }
    showLevels()
    page(section { put(qz) })
}

private fun Pages.runQuiz(qz: LinearLayout, level: Content.Level, back: () -> Unit): Unit = with(ui) {
    var i = 0
    var right = 0
    val qs = level.questions

    fun end() {
        val pct = (right * 100f / qs.size).roundToInt()
        val had = Content.BADGES.map { it.earned() }
        Store.quiz[level.name] = maxOf(Store.quiz[level.name] ?: 0, pct)
        Store.addPts(right * 10)
        val gained = Content.BADGES.filterIndexed { k, b -> b.earned() && !had[k] }.map { it.name }
        qz.removeAllViews()
        val s = vbox()
        s.gravity = Gravity.CENTER_HORIZONTAL
        s.put(eyebrow("${level.name} quiz complete").apply { gravity = Gravity.CENTER })
        s.put(text("$pct%", 72f, Pal.amber, 700, serif = true).apply { gravity = Gravity.CENTER }, w = WRAP)
        s.put(text("$right of ${qs.size} correct · +${right * 10} points", 24f, Pal.ink, 700, serif = true).apply { gravity = Gravity.CENTER })
        val msg = (if (pct >= 70) "Great work!" else "Keep exploring and try again.") + if (gained.isNotEmpty()) " New badge: ${gained.joinToString(", ")}." else ""
        s.put(lead(msg).apply { gravity = Gravity.CENTER }, w = WRAP, t = 12, b = 12)
        val share = btn("Share score") { host.share("I scored $pct% on the ${level.name} quiz at the Iziko SA National Gallery!") }
        val again = btn("Play again", "line") { runQuiz(qz, level, back) }
        val view = btn("View badges", "line") { go("challenge") }
        val row = if (contentDp >= 480) hbox().apply { gravity = Gravity.CENTER } else vbox().apply { gravity = Gravity.CENTER_HORIZONTAL }
        if (contentDp >= 480) { row.put(share, w = WRAP, r = 10); row.put(again, w = WRAP, r = 10); row.put(view, w = WRAP) }
        else { row.put(share, w = WRAP); row.put(again, w = WRAP, t = 10); row.put(view, w = WRAP, t = 10) }
        s.put(row, w = WRAP)
        qz.put(s, w = minOf(contentDp, 560).let { dp(it) }, gravity = Gravity.CENTER_HORIZONTAL, t = 50)
    }

    fun show() {
        val q = qs[i]
        qz.removeAllViews()
        val box = vbox()
        box.put(eyebrow("${level.name} · Question ${i + 1} of ${qs.size}"))
        val prog = LinearLayout(ctx)
        prog.setBackgroundColor(Pal.line)
        prog.put(View(ctx).apply { setBackgroundColor(Pal.amber) }, w = 0, h = MATCH, weight = i.toFloat())
        prog.put(View(ctx), w = 0, h = MATCH, weight = (qs.size - i).toFloat())
        box.put(prog, h = dp(4), t = 16, b = 28)
        box.put(text(q.q, 26f, Pal.ink, 700, serif = true, lineMult = 1.2f), b = 20)
        val opts = ArrayList<LinearLayout>()
        val fb = hbox()
        q.options.forEachIndexed { k, label ->
            val o = option("ABCD"[k].toString(), label)
            opts.add(o)
            tap(o) {
                opts.forEach { it.isClickable = false; it.foreground = null }
                mark(opts[q.answer], true)
                if (k != q.answer) mark(o, false)
                if (k == q.answer) right++
                fb.removeAllViews()
                fb.put(text(if (k == q.answer) "Correct!" else "Not quite.", 16f, Pal.mute), w = WRAP, r = 8)
                fb.put(btn(if (i < qs.size - 1) "Next ›" else "See score") { i++; if (i < qs.size) show() else end() }, w = WRAP)
            }
            box.put(o, t = 10)
        }
        box.put(fb, t = 16)
        qz.put(box, w = dp(minOf(contentDp, 720)), gravity = Gravity.CENTER_HORIZONTAL, t = 32)
    }
    show()
}

internal fun Pages.challengePage(): View = with(ui) {
    val L = Content.levelIndex()
    val d = Content.DAILY[(Store.dayNumber() % Content.DAILY.size).toInt()]
    val w = MuseumRepository.getArtwork(d.artworkId)
    val done = Store.dailyLast == Store.today()
    val nxt = Content.LEVELS.getOrNull(L + 1)
    val date = SimpleDateFormat("d MMM yyyy", Locale.UK).format(java.util.Date()).uppercase()

    page(section {
        head("Gamification", "Art Challenge", "Daily challenges, badges and Art Explorer levels. Come back every day to grow your collection.")

        // ----- stats -----
        fun stat(emoji: String, bg: Int, label: String, value: String): View {
            val c = hbox()
            c.background = shape(Pal.card, Pal.line)
            c.setPadding(dp(22), dp(22), dp(22), dp(22))
            c.put(text(emoji, 22f, Color.WHITE).apply { gravity = Gravity.CENTER; setBackgroundColor(bg) }, w = dp(48), h = dp(48), r = 16)
            val col = vbox()
            col.put(text(label, 12f, Pal.mute, caps = true, track = 0.08f))
            col.put(text(value, 24f, Pal.ink, 700))
            c.put(col, w = 0, weight = 1f)
            return c
        }
        put(rows(listOf(
            stat("⚡", Pal.amber, "Day streak", "${Store.dailyStreak} days"),
            stat("🏆", Pal.rust, "Total points", "${Store.pts}"),
            stat("★", Color.parseColor("#1E1410"), "Explorer level", "Level ${L + 1}")
        ), if (atLeast(700)) 3 else 1, 20), t = 28)
        if (nxt != null) put(lead("${nxt.from - Store.pts} points to reach ${nxt.name}."), w = WRAP, t = 12)

        // ----- daily challenge -----
        val dailyHead = hbox()
        dailyHead.put(h2("Daily Challenge"), w = 0, weight = 1f)
        dailyHead.put(text("TODAY · $date", 11f, Color.WHITE, 700, track = 0.1f).apply { setPadding(dp(10), dp(5), dp(10), dp(5)); background = shape(Pal.amber) }, w = WRAP, l = 8)
        put(dailyHead, t = 40)

        val daily = LinearLayout(ctx)
        daily.background = shape(Pal.card, Pal.line)
        val body = vbox()
        pad(body, 28, 28, 28, 28)
        body.put(eyebrow("Artwork: ${w?.title ?: ""} · ${w?.artistName ?: ""}"), b = 0)
        body.put(text(d.q, 24f, Pal.ink, 700, serif = true, lineMult = 1.2f), t = 8, b = 12)
        val fb = text(if (done) "You've completed today's challenge. Come back tomorrow!" else "Answer correctly to earn 20 points and extend your streak.", 16f, Pal.mute)
        val opts = ArrayList<LinearLayout>()
        d.options.forEachIndexed { k, label ->
            val o = option("ABCD"[k].toString(), label)
            opts.add(o)
            body.put(o, t = 10)
            if (done) { o.isClickable = false } else tap(o) {
                opts.forEach { it.isClickable = false; it.foreground = null }
                mark(opts[d.answer], true)
                if (k != d.answer) mark(o, false)
                if (k == d.answer) {
                    Store.dailyStreak = if (Store.dailyLast == Store.today(-1)) Store.dailyStreak + 1 else 1
                    Store.dailyLast = Store.today()
                    Store.dailyCount++
                    Store.addPts(20)
                    fb.text = "Correct! +20 points."
                    fb.postDelayed({ host.refreshPage() }, 1400)
                } else fb.text = "Not quite. The right answer is highlighted. Try again tomorrow."
            }
        }
        body.put(fb, t = 16)
        if (atLeast(760)) {
            daily.orientation = LinearLayout.HORIZONTAL
            daily.put(w?.image?.let { cover(it, 180) } ?: View(ctx), w = dp(320), h = MATCH)
            daily.put(body, w = 0, weight = 1f)
        } else {
            daily.orientation = LinearLayout.VERTICAL
            w?.image?.let { naturalImage(it)?.let { im -> daily.put(im) } }
            daily.put(body)
        }
        put(daily, t = 20)

        // ----- levels -----
        put(h2("Art Explorer Levels"), t = 56)
        put(lead("Earn points through quizzes (10 per correct answer), daily challenges (20) and saving artworks (5)."), w = WRAP, t = 8)
        Content.LEVELS.forEachIndexed { i, l ->
            val row = hbox()
            val isDone = i < L
            val here = i == L
            row.setPadding(dp(20), dp(16), dp(20), dp(16))
            row.background = shape(Pal.card, if (here) Pal.amber else Pal.line)
            if (!isDone && !here) row.alpha = 0.55f
            val numBg = if (isDone) Pal.rust else if (here) Pal.amber else Pal.bg2
            val numFg = if (isDone || here) Color.WHITE else Pal.mute
            row.put(text("${i + 1}", 16f, numFg).apply { gravity = Gravity.CENTER; setBackgroundColor(numBg) }, w = dp(40), h = dp(40), r = 16)
            val col = vbox()
            val title = hbox()
            title.put(text(l.name, 16f, Pal.ink, 700), w = WRAP)
            if (here) title.put(text("← You are here", 13f, Pal.amber), w = WRAP, l = 6)
            col.put(title)
            val range = if (i < Content.LEVELS.size - 1) "${l.from}–${Content.LEVELS[i + 1].from}" else "${l.from}+"
            col.put(text("$range pts · ${l.desc}", 14f, Pal.mute))
            row.put(col, w = 0, weight = 1f)
            if (i <= L) row.put(text("✓", 16f, Pal.ok), w = WRAP, l = 8)
            put(row, t = 10)
        }

        // ----- badges -----
        put(h2("Badge Collection"), t = 56)
        put(lead("Complete challenges and activities to earn all 6 badges."), w = WRAP, t = 8)
        val badges = Content.BADGES.map { b ->
            val got = b.earned()
            val c = vbox()
            c.gravity = Gravity.CENTER_HORIZONTAL
            c.setPadding(dp(16), dp(26), dp(16), dp(26))
            c.background = shape(Pal.card, if (got) Pal.amber else Pal.line)
            c.put(text(b.emoji, 34f, Pal.ink).apply { gravity = Gravity.CENTER }, w = WRAP)
            c.put(text(b.name, 16f, Pal.ink, 700).apply { gravity = Gravity.CENTER }, t = 10, b = 4)
            c.put(text(b.how, 13f, Pal.mute).apply { gravity = Gravity.CENTER })
            if (got) c.put(text("EARNED", 10f, Color.WHITE, 700, track = 0.12f).apply {
                setPadding(dp(10), dp(4), dp(10), dp(4)); background = shape(Pal.amber)
            }, w = WRAP, t = 12)
            if (!got) {
                c.alpha = 0.5f
                val p = Paint(); p.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
                c.setLayerType(View.LAYER_TYPE_HARDWARE, p)
            }
            c as View
        }
        put(rows(badges, if (atLeast(700)) 3 else 2, 16), t = 20)
    })
}

internal fun Pages.collectionPage(): View = with(ui) {
    val list = Store.saved.mapNotNull { MuseumRepository.getArtwork(it) }
    page(section {
        head("Personal", "My Collection", "${list.size} saved artworks")
        if (list.isNotEmpty()) put(autoGrid(list.map { artCard(it) }, 250), t = 28)
        else put(empty("No saved artworks yet", "Tap the heart icon on any artwork to save it here.", "♡", "Browse artworks" to "artworks"))
    })
}

internal fun Pages.tourPage(): View = with(ui) {
    val holder = vbox()
    var i = 0
    val stops = Content.TOUR
    fun draw() {
        holder.removeAllViews()
        val s = stops[i]
        val w = MuseumRepository.getArtwork(s.artworkId) ?: MuseumRepository.allArtworks().first()

        val pic = FrameLayout(ctx)
        pic.setBackgroundColor(Color.BLACK)
        if (image(w.image) != null) {
            val iv = ImageView(ctx).apply {
                setImageDrawable(image(w.image)); adjustViewBounds = true; maxHeight = dp(480)
                scaleType = ImageView.ScaleType.FIT_CENTER; contentDescription = w.title
            }
            pic.addView(iv, FrameLayout.LayoutParams(MATCH, WRAP, Gravity.CENTER))
        } else {
            pic.addView(text(w.title, 22f, Color.WHITE, serif = true, italic = true).apply { gravity = Gravity.CENTER; setPadding(dp(40), dp(40), dp(40), dp(40)) },
                FrameLayout.LayoutParams(MATCH, WRAP, Gravity.CENTER))
        }
        pic.minimumHeight = dp(420)

        val txt = vbox()
        pad(txt, 36, 36, 36, 36)
        val dots = LinearLayout(ctx)
        stops.indices.forEach { k ->
            dots.put(View(ctx).apply { setBackgroundColor(if (k <= i) Pal.amber else Color.argb(51, 255, 255, 255)) }, w = 0, h = dp(4), weight = 1f, r = if (k < stops.size - 1) 6 else 0)
        }
        txt.put(dots, b = 24)
        txt.put(eyebrow("Room ${i + 1} of ${stops.size}"), b = 8)
        txt.put(text(s.room, 30f, Color.WHITE, 700, serif = true, lineMult = 1.15f))
        txt.put(text(s.text, 16f, Color.argb(217, 255, 255, 255), lineMult = 1.5f), t = 14, b = 24)
        val cap = vbox()
        cap.put(text(w.title, 16f, Color.argb(217, 255, 255, 255), 700))
        cap.put(text(w.artistName, 16f, Color.argb(217, 255, 255, 255)))
        txt.put(cap, b = 24)
        val prev = btn("‹ Previous", "ghost") { if (i > 0) { i--; draw() } }
        if (i == 0) { prev.alpha = 0.5f; prev.isClickable = false; prev.foreground = null }
        val next = btn(if (i < stops.size - 1) "Next room ›" else "Start again") { i = (i + 1) % stops.size; draw() }
        val row = hbox()
        row.put(prev, w = WRAP, r = 10)
        row.put(next, w = WRAP)
        txt.put(row)

        val panel = LinearLayout(ctx)
        panel.setBackgroundColor(Color.parseColor("#140A06"))
        if (atLeast(800)) {
            panel.orientation = LinearLayout.HORIZONTAL
            panel.put(pic, w = 0, h = MATCH, weight = 1.4f)
            panel.put(txt, w = 0, weight = 1f)
        } else {
            panel.orientation = LinearLayout.VERTICAL
            panel.put(pic); panel.put(txt)
        }
        holder.put(panel)
    }
    draw()
    page(section {
        head("Explore from anywhere", "Virtual Tour", "Walk through the gallery room by room.")
        put(holder, t = 28)
    })
}
