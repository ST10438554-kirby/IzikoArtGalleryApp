package com.example.izikomuseum.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils

const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

/** The website's colour variables (style.css :root and :root[data-theme=dark]). */
object Pal {
    var dark = false
    private fun c(light: String, night: String) = Color.parseColor(if (dark) night else light)
    val bg get() = c("#F7F1E8", "#1B1714")
    val bg2 get() = c("#EBE5D9", "#241F1B")
    val card get() = c("#FFFCF7", "#2A2420")
    val ink get() = c("#1E1410", "#F2EDE4")
    val mute get() = c("#6B5F55", "#B9B1A6")
    val line get() = c("#DDD3C4", "#3F372F")
    val rust get() = c("#8E2A06", "#C2410C")
    val amber get() = c("#C8772E", "#E39A55")
    val ok: Int get() = Color.parseColor("#2E9E5B")
    val red: Int get() = Color.parseColor("#D52B4B")
    val white: Int get() = Color.WHITE
    fun mix(base: Int, over: Int, pct: Float) = ColorUtils.blendARGB(base, over, pct)
}

/**
 * Fonts
 */
object Fonts {
    private val cache = HashMap<String, Typeface>()

    fun get(ctx: Context, serif: Boolean, weight: Int, italic: Boolean): Typeface {
        val key = "$serif-$weight-$italic"
        return cache.getOrPut(key) {
            val name = if (serif) {
                when {
                    weight >= 600 && italic -> "playfair_display_bolditalic"
                    weight >= 600 -> "playfair_display_bold"
                    italic -> "playfair_display_italic"
                    else -> "playfair_display_regular"
                }
            } else {
                when { weight >= 700 -> "dm_sans_bold"; weight >= 500 -> "dm_sans_medium"; else -> "dm_sans_regular" }
            }
            val id = ctx.resources.getIdentifier(name, "font", ctx.packageName)
            val custom = if (id != 0) ResourcesCompat.getFont(ctx, id) else null
            custom ?: if (serif) {
                Typeface.create("serif", if (weight >= 600 && italic) Typeface.BOLD_ITALIC else if (weight >= 600) Typeface.BOLD else if (italic) Typeface.ITALIC else Typeface.NORMAL)
            } else {
                if (weight >= 700) Typeface.create("sans-serif", Typeface.BOLD)
                else if (weight >= 500) Typeface.create("sans-serif-medium", Typeface.NORMAL)
                else Typeface.create("sans-serif", Typeface.NORMAL)
            }
        }
    }
}

/** A frame whose height is always [ratio] x its width (the website's aspect-ratio: 4/3 image boxes). */
class AspectFrame(ctx: Context, private val ratio: Float) : FrameLayout(ctx) {
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val w = MeasureSpec.getSize(widthSpec)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec((w * ratio).toInt(), MeasureSpec.EXACTLY)
        )
    }
}

/** object-fit: cover. Its own size never depends on the picture, so it can fill a row of any height. */
class CoverImageView(ctx: Context, private val minHeightPx: Int) : ImageView(ctx) {
    init { scaleType = ScaleType.CENTER_CROP }
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val w = MeasureSpec.getSize(widthSpec)
        val h = if (MeasureSpec.getMode(heightSpec) == MeasureSpec.EXACTLY) MeasureSpec.getSize(heightSpec) else minHeightPx
        setMeasuredDimension(w, h)
    }
}

/** Small toolkit that builds views in the website's visual style. */
class Ui(val ctx: Context) {

    private val density = ctx.resources.displayMetrics.density
    fun dp(v: Number): Int = (v.toFloat() * density + 0.5f).toInt()
    val widthDp: Int get() = ctx.resources.configuration.screenWidthDp
    val heightDp: Int get() = ctx.resources.configuration.screenHeightDp

    /** Width of the centred content column: max 1100dp minus 24dp padding each side. */
    val contentDp: Int get() = minOf(widthDp, 1100) - 48

    /** Responsive helpers that follow the website's @media breakpoints. */
    fun atLeast(dpWidth: Int) = widthDp > dpWidth

    // ---------- shapes and text ----------

    fun shape(fill: Int, stroke: Int = 0, strokeDp: Float = 1f, oval: Boolean = false): GradientDrawable =
        GradientDrawable().apply {
            if (oval) this.shape = GradientDrawable.OVAL
            setColor(fill)
            if (stroke != 0) setStroke(maxOf(1, dp(strokeDp)), stroke)
        }

    fun text(
        s: CharSequence, size: Float = 16f, color: Int = Pal.ink, weight: Int = 400, serif: Boolean = false,
        italic: Boolean = false, caps: Boolean = false, track: Float = 0f, lineMult: Float = 1.35f
    ): TextView = TextView(ctx).apply {
        text = if (caps) s.toString().uppercase() else s
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        setTextColor(color)
        typeface = Fonts.get(ctx, serif, weight, italic)
        if (track != 0f) letterSpacing = track
        setLineSpacing(0f, lineMult)
    }

    fun vbox(): LinearLayout = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
    fun hbox(): LinearLayout = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }

    /** Adds [v] to this box with optional margins (in dp). */
    fun LinearLayout.put(
        v: View, w: Int = MATCH, h: Int = WRAP, weight: Float = 0f,
        l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0, gravity: Int = -1
    ): View {
        val p = LinearLayout.LayoutParams(w, h, weight)
        p.setMargins(dp(l), dp(t), dp(r), dp(b))
        if (gravity != -1) p.gravity = gravity
        addView(v, p)
        return v
    }

    fun pad(v: View, l: Int, t: Int, r: Int, b: Int) = v.setPadding(dp(l), dp(t), dp(r), dp(b))

    /** Press feedback (ripple) and a click handler. */
    fun tap(v: View, onClick: () -> Unit): View {
        v.isClickable = true
        v.setOnClickListener { onClick() }
        val a = ctx.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        v.foreground = a.getDrawable(0)
        a.recycle()
        return v
    }

    // ---------- common pieces ----------

    fun eyebrow(s: String, color: Int = Pal.amber): TextView =
        text(s, 12f, color, 700, caps = true, track = 0.14f)

    fun h1(s: String, color: Int = Pal.ink): TextView =
        text(s, (widthDp * 0.04f).coerceIn(30f, 44f), color, 700, serif = true, lineMult = 1.1f)

    fun h2(s: String, size: Float = 28f, color: Int = Pal.ink): TextView =
        text(s, size, color, 700, serif = true, lineMult = 1.1f)

    fun lead(s: String, color: Int = Pal.mute): TextView =
        text(s, 16f, color, lineMult = 1.4f).apply { maxWidth = dp(520) }

    /** eyebrow + big title + optional lead paragraph, like the website's head(). */
    fun LinearLayout.head(kicker: String, title: String, intro: String? = null) {
        put(eyebrow(kicker), b = 8)
        put(h1(title), b = 10)
        if (intro != null) put(lead(intro), w = WRAP)
    }

    fun btn(label: CharSequence, kind: String = "primary", onClick: () -> Unit): TextView {
        val fill = if (kind == "primary") Pal.amber else 0
        val border = when (kind) { "primary" -> Pal.amber; "ghost" -> Color.WHITE; else -> Pal.line }
        val ink = if (kind == "line") Pal.ink else Color.WHITE
        return text(label, 15f, ink, 500).apply {
            gravity = Gravity.CENTER
            setPadding(dp(22), dp(12), dp(22), dp(12))
            background = shape(if (fill == 0) Color.TRANSPARENT else fill, border)
            tap(this, onClick)
        }
    }

    fun image(name: String?): Drawable? {
        if (name.isNullOrEmpty()) return null
        val id = ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
        return if (id == 0) null else ContextCompat.getDrawable(ctx, id)
    }

    /** A picture at its natural proportions, as wide as its parent (img { max-width:100% }). */
    fun naturalImage(name: String?): ImageView? {
        val d = image(name) ?: return null
        return ImageView(ctx).apply { setImageDrawable(d); adjustViewBounds = true; scaleType = ImageView.ScaleType.FIT_CENTER }
    }

    fun cover(name: String?, minHeightDp: Int): CoverImageView =
        CoverImageView(ctx, dp(minHeightDp)).apply { setImageDrawable(image(name)) }

    fun spacer(hDp: Int): View = View(ctx).apply { layoutParams = LinearLayout.LayoutParams(1, dp(hDp)) }

    /** A card box: card colour, 1dp line border, 28dp padding (.box). */
    fun box(padDp: Int = 28): LinearLayout = vbox().apply {
        background = shape(Pal.card, Pal.line)
        setPadding(dp(padDp), dp(padDp), dp(padDp), dp(padDp))
    }

    /** A "label ........ value" row with a bottom line (.hr). */
    fun hoursRow(left: String, right: String, rightColor: Int = Pal.ink, onRed: Boolean = false, boldRight: Boolean = true): View {
        val row = hbox()
        row.setPadding(0, dp(10), 0, dp(10))
        val col = if (onRed) Color.WHITE else Pal.ink
        row.put(text(left, 15f, col), w = 0, weight = 1f)
        row.put(text(right, 15f, if (onRed) Color.WHITE else rightColor, if (boldRight) 700 else 400), w = WRAP)
        val wrapper = vbox()
        wrapper.put(row)
        wrapper.put(View(ctx).apply { setBackgroundColor(if (onRed) Color.argb(64, 255, 255, 255) else Pal.line) }, h = 1)
        return wrapper
    }

    // ---------- layout: sections, grids, columns ----------

    /** A full-width band (.sec) holding a centred content column. */
    fun section(alt: Boolean = false, topDp: Int = 56, bottomDp: Int = 56, build: LinearLayout.() -> Unit): LinearLayout {
        val s = vbox()
        s.setBackgroundColor(if (alt) Pal.bg2 else Pal.bg)
        s.setPadding(0, dp(topDp), 0, dp(bottomDp))
        val w = vbox()
        w.setPadding(dp(24), 0, dp(24), 0)
        w.build()
        s.put(w, w = dp(minOf(widthDp, 1100)), gravity = Gravity.CENTER_HORIZONTAL)
        return s
    }

    /** Lays items out in [cols] equal columns with [gapDp] gaps (CSS grid with 1fr columns). */
    fun rows(items: List<View>, cols: Int, gapDp: Int = 20): LinearLayout {
        val column = vbox()
        val n = maxOf(1, cols)
        var i = 0
        var r = 0
        while (i < items.size) {
            val row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
            for (k in 0 until n) {
                val p = LinearLayout.LayoutParams(0, MATCH, 1f)
                if (k < n - 1) p.marginEnd = dp(gapDp)
                row.addView(items.getOrNull(i + k) ?: View(ctx), p)
            }
            column.put(row, t = if (r > 0) gapDp else 0)
            i += n
            r++
        }
        return column
    }

    /** repeat(auto-fill, minmax(minDp, 1fr)) */
    fun autoGrid(items: List<View>, minDp: Int, gapDp: Int = 20): LinearLayout {
        val cols = maxOf(1, (contentDp + gapDp) / (minDp + gapDp))
        return rows(items, cols, gapDp)
    }

    /** Two columns side by side when wide enough, stacked otherwise (e.g. .two, .detail, .tour). */
    fun twoCols(left: View, right: View, stackBelowDp: Int, leftWeight: Float = 1f, rightWeight: Float = 1f, gapDp: Int = 32): LinearLayout {
        val c = LinearLayout(ctx)
        if (atLeast(stackBelowDp)) {
            c.orientation = LinearLayout.HORIZONTAL
            c.put(left, w = 0, weight = leftWeight, r = gapDp / 2)
            c.put(right, w = 0, weight = rightWeight, l = gapDp / 2)
        } else {
            c.orientation = LinearLayout.VERTICAL
            c.put(left)
            c.put(right, t = gapDp)
        }
        return c
    }
}

/** Lays children out left to right and wraps to a new line when full (CSS flex-wrap). */
class FlowLayout(ctx: Context, private val hGapPx: Int, private val vGapPx: Int) : ViewGroup(ctx) {
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val maxW = MeasureSpec.getSize(widthSpec) - paddingLeft - paddingRight
        var x = 0; var y = 0; var rowH = 0
        for (i in 0 until childCount) {
            val c = getChildAt(i)
            if (c.visibility == View.GONE) continue
            c.measure(MeasureSpec.makeMeasureSpec(maxW, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            if (x > 0 && x + c.measuredWidth > maxW) { x = 0; y += rowH + vGapPx; rowH = 0 }
            x += c.measuredWidth + hGapPx
            rowH = maxOf(rowH, c.measuredHeight)
        }
        setMeasuredDimension(MeasureSpec.getSize(widthSpec), y + rowH + paddingTop + paddingBottom)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val maxW = r - l - paddingLeft - paddingRight
        var x = 0; var y = 0; var rowH = 0
        for (i in 0 until childCount) {
            val c = getChildAt(i)
            if (c.visibility == View.GONE) continue
            if (x > 0 && x + c.measuredWidth > maxW) { x = 0; y += rowH + vGapPx; rowH = 0 }
            c.layout(paddingLeft + x, paddingTop + y, paddingLeft + x + c.measuredWidth, paddingTop + y + c.measuredHeight)
            x += c.measuredWidth + hGapPx
            rowH = maxOf(rowH, c.measuredHeight)
        }
    }
}
