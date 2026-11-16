package com.example.izikomuseum.data.repository

import android.content.Context
import com.example.izikomuseum.data.model.Artist
import com.example.izikomuseum.data.model.Artwork
import com.example.izikomuseum.data.model.Exhibition
import org.json.JSONArray
import org.json.JSONObject

/**
 * Museum data. It is read from assets/museum_seed.json, the very same file the website uses as its
 * offline data, so the app and the website always show the same artworks, artists and exhibitions.
 * Swap [load] for API calls later.
 */
object MuseumRepository {

    private var artists: List<Artist> = emptyList()
    private var artworks: List<Artwork> = emptyList()
    private var exhibitions: List<Exhibition> = emptyList()

    fun load(context: Context) {
        if (artworks.isNotEmpty()) return
        val text = context.assets.open("museum_seed.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(text)
        artists = root.getJSONArray("artists").map { o ->
            Artist(
                o.getInt("id"), o.getString("name"), o.optStr("lifespan") ?: "", o.optStr("origin") ?: "",
                o.optStr("biography") ?: "", o.optStr("image")
            )
        }
        val byId = artists.associateBy { it.id }
        artworks = root.getJSONArray("artworks").map { o ->
            val artistId = o.getInt("artistId")
            Artwork(
                o.getInt("id"), o.getString("title"), artistId,
                o.optStr("artistName") ?: byId[artistId]?.name ?: "",
                o.optStr("category") ?: "", o.optStr("date") ?: "",
                o.optStr("medium"), o.optStr("dimensions"), o.optStr("acquisition"),
                o.optStr("description") ?: "", o.optStr("image"), o.optBoolean("featured", false)
            )
        }
        exhibitions = root.getJSONArray("exhibitions").map { o ->
            Exhibition(
                o.getInt("id"), o.getString("title"), o.optStr("dates") ?: "", o.optStr("status") ?: "",
                o.optStr("curator"), o.optStr("summary") ?: "", o.optStr("image")
            )
        }
    }

    private fun JSONObject.optStr(key: String): String? =
        if (isNull(key) || !has(key)) null else getString(key).takeIf { it.isNotBlank() }

    private fun <T> JSONArray.map(block: (JSONObject) -> T): List<T> =
        (0 until length()).map { block(getJSONObject(it)) }

    fun allArtworks(): List<Artwork> = artworks
    fun categories(): List<String> = artworks.map { it.category }.distinct()
    fun getArtwork(id: Int): Artwork? = artworks.firstOrNull { it.id == id }
    fun getArtists(): List<Artist> = artists
    fun getArtist(id: Int): Artist? = artists.firstOrNull { it.id == id }
    fun artworksBy(artistId: Int): List<Artwork> = artworks.filter { it.artistId == artistId }
    fun getExhibitions(): List<Exhibition> = exhibitions

    /** Same rule as the website: category filter plus a search over title and artist name. */
    fun searchArtworks(query: String, category: String?): List<Artwork> {
        val q = query.trim().lowercase()
        return artworks.filter { w ->
            (category.isNullOrEmpty() || w.category == category) &&
                (q.isEmpty() || (w.title + w.artistName).lowercase().contains(q))
        }
    }
}
