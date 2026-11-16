package com.example.izikomuseum.data.model

/** Same fields as the website's API / museum_seed.json, so both clients show identical content. */
data class Artist(
    val id: Int,
    val name: String,
    val lifespan: String,
    val origin: String,
    val biography: String,
    val image: String?
)

data class Artwork(
    val id: Int,
    val title: String,
    val artistId: Int,
    val artistName: String,
    val category: String,
    val date: String,
    val medium: String?,
    val dimensions: String?,
    val acquisition: String?,
    val description: String,
    val image: String?,
    val featured: Boolean
)

data class Exhibition(
    val id: Int,
    val title: String,
    val dates: String,
    val status: String,
    val curator: String?,
    val summary: String,
    val image: String?
)
