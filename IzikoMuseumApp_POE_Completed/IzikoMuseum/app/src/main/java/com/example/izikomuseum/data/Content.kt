package com.example.izikomuseum.data

import com.example.izikomuseum.data.repository.Store

/** All fixed text and game content,same as the website's site.js so both match. */
object Content {

    val NAV = listOf(
        "home" to "Home", "artworks" to "Artworks", "artists" to "Artists", "exhibitions" to "Exhibitions",
        "quiz" to "Quiz", "visit" to "Visit", "collection" to "My Collection", "tour" to "Virtual Tour",
        "challenge" to "Art Challenge"
    )

    val LANGS = linkedMapOf("en" to "English", "af" to "Afrikaans", "zu" to "isiZulu", "xh" to "isiXhosa")

    private val TR = mapOf(
        "af" to listOf("Tuis", "Kunswerke", "Kunstenaars", "Uitstallings", "Vasvra", "Besoek", "My Versameling", "Virtuele Toer", "Kunsuitdaging", "Verken Kunswerke", "Beplan Jou Besoek", "Galery-feite"),
        "zu" to listOf("Ikhaya", "Imidwebo", "Abadwebi", "Imibukiso", "Imibuzo", "Vakashela", "Iqoqo Lami", "Uhambo Lwangempela", "Inselelo Yobuciko", "Hlola Imidwebo", "Hlela Uhambo Lwakho", "Amaqiniso Egalari"),
        "xh" to listOf("Ikhaya", "Imisebenzi yobugcisa", "Amagcisa", "Imiboniso", "Imibuzo", "Utyelelo", "Ingqokelela Yam", "Uhambo lwe-Virtual", "Umceli-mngeni Wobugcisa", "Phonononga Ubugcisa", "Cwangcisa Utyelelo Lwakho", "Iinyani Zegalari")
    )
    private val KEYS = NAV.map { it.second } + listOf("Explore Artworks", "Plan Your Visit", "Gallery Facts")

    /** Translates a menu label or key heading into the chosen language (English if there is no translation). */
    fun t(key: String): String {
        val i = KEYS.indexOf(key)
        val list = TR[Store.lang]
        return if (i >= 0 && list != null) list[i] else key
    }

    val FACTS = listOf(
        "The gallery officially opened to the public on 3 November 1930.",
        "It is South Africa's national art gallery, part of Iziko Museums of South Africa.",
        "The collection includes over 10,000 artworks spanning five centuries.",
        "The gallery holds South African, African and international works side by side.",
        "It is in Company's Garden, Cape Town, one of South Africa's oldest gardens, established in 1652.",
        "Its origins trace to the South African Fine Arts Association, founded in 1850.",
        "The national collection began in 1872 with a donation from Thomas Butterworth Bayley.",
        "The Sir Abe Bailey Bequest (1940) is the largest in the gallery's history, with over 400 works.",
        "It holds one of the largest collections of British sporting art in any public museum.",
        "Educational programmes serve thousands of school learners across South Africa each year."
    )

    class Question(val q: String, val options: List<String>, val answer: Int)
    class Level(val name: String, val emoji: String, val desc: String, val questions: List<Question>)

    val QUIZ = listOf(
        Level("Beginner", "🎨", "Gallery basics and opening facts", listOf(
            Question("When did the gallery officially open?", listOf("1872", "1930", "1950", "2001"), 1),
            Question("Where is the gallery?", listOf("Company's Garden, Cape Town", "Sandton, Johannesburg", "Durban beachfront", "Stellenbosch"), 0),
            Question("Which group is the gallery part of?", listOf("Iziko Museums of South Africa", "Robben Island Museum", "Apartheid Museum", "Zeitz MOCAA"), 0),
            Question("The collection spans how many centuries?", listOf("Five", "One", "Two", "Ten"), 0),
            Question("Who made The Butcher Boys?", listOf("Jane Alexander", "Irma Stern", "Gerard Sekoto", "Pieter Wenning"), 0)
        )),
        Level("Explorer", "🌟", "Artists, artworks and key dates", listOf(
            Question("Who made the linocut The Revelation of St John?", listOf("Azaria Mbatha", "William Kentridge", "Thomas Baines", "Anton van Wouw"), 0),
            Question("Who made African Head, bought in 1926?", listOf("Anton van Wouw", "Neville Lewis", "Irma Stern", "Alexis Preller"), 0),
            Question("What medium are Kentridge's Soho Eckstein works?", listOf("Animated film", "Bronze", "Tapestry", "Photography"), 0),
            Question("In which year did Irma Stern first travel to Zanzibar?", listOf("1939", "1925", "1951", "1966"), 0),
            Question("Who gave the donation that began the national collection in 1872?", listOf("Thomas Butterworth Bayley", "Abe Bailey", "Cecil Rhodes", "Thomas Baines"), 0)
        )),
        Level("Curator", "🏆", "Deep history and provenance", listOf(
            Question("When was the South African Fine Arts Association founded?", listOf("1850", "1872", "1914", "1930"), 0),
            Question("In which year was the Sir Abe Bailey Bequest made?", listOf("1940", "1926", "1972", "2001"), 0),
            Question("How many items did the Sekoto Study Collection add in 2000?", listOf("More than 2 500", "About 40", "About 300", "About 800"), 0),
            Question("In which year did the gallery become part of Iziko?", listOf("2001", "1930", "1950", "1990"), 0),
            Question("Construction of the current building began in?", listOf("1914", "1872", "1926", "1930"), 0)
        ))
    )

    class Daily(val artworkId: Int, val q: String, val options: List<String>, val answer: Int)
    val DAILY = listOf(
        Daily(1, "The Butcher Boys represents which of the following themes?", listOf("Beauty and nature", "Dehumanisation and apartheid", "Celebration of sport", "Colonial exploration"), 1),
        Daily(2, "The Revelation of St John was made using which technique?", listOf("Linocut", "Oil on canvas", "Bronze casting", "Watercolour"), 0),
        Daily(4, "Who made African Head?", listOf("Anton van Wouw", "Gerard Sekoto", "Thomas Baines", "Neville Lewis"), 0),
        Daily(11, "How many Soho Eckstein films does the gallery hold?", listOf("Two", "Five", "Nine", "Twelve"), 1),
        Daily(6, "Who painted Two Arabs, Dakar?", listOf("Irma Stern", "Pieter Wenning", "Alexis Preller", "Neville Lewis"), 0)
    )

    class ExplorerLevel(val name: String, val from: Int, val desc: String)
    val LEVELS = listOf(
        ExplorerLevel("Curious Visitor", 0, "You've started exploring the gallery."),
        ExplorerLevel("Art Enthusiast", 100, "You know the key artworks and artists."),
        ExplorerLevel("Art Explorer", 300, "You're diving into history and context."),
        ExplorerLevel("Junior Curator", 600, "You understand provenance and technique."),
        ExplorerLevel("Gallery Master", 1000, "The collection holds no secrets from you.")
    )

    /** The highest explorer level whose point threshold has been reached. */
    fun levelIndex(): Int = LEVELS.indices.lastOrNull { Store.pts >= LEVELS[it].from } ?: 0

    class Badge(val emoji: String, val name: String, val how: String, val earned: () -> Boolean)
    val BADGES = listOf(
        Badge("🎨", "Gallery Visitor", "Complete your first quiz") { Store.quiz.isNotEmpty() },
        Badge("🌟", "Art Explorer", "Score 70%+ on any quiz") { Store.quiz.values.any { it >= 70 } },
        Badge("🏆", "Junior Curator", "Complete all three difficulty levels") { QUIZ.all { Store.quiz.containsKey(it.name) } },
        Badge("💎", "Art Master", "Score 100% on the hard quiz") { Store.quiz["Curator"] == 100 },
        Badge("⚡", "Daily Challenger", "Complete 5 daily challenges") { Store.dailyCount >= 5 },
        Badge("❤️", "Collector", "Save 5 artworks to My Collection") { Store.saved.size >= 5 }
    )

    class TourStop(val room: String, val artworkId: Int, val text: String)
    val TOUR = listOf(
        TourStop("Entrance Hall", 1, "Begin your visit with sculpture that confronts the viewer from the first step."),
        TourStop("Sculpture Court", 4, "Bronze and plaster figures, including early works bought for the national collection in 1926."),
        TourStop("South African Art", 3, "Painters who shaped modern South African art, including Gerard Sekoto."),
        TourStop("Contemporary", 11, "Animated film and drawing from William Kentridge's Soho Eckstein series."),
        TourStop("Prints Room", 2, "Linocuts from the Rorke's Drift Art Centre tradition."),
        TourStop("Sir Abe Bailey", 8, "Landscapes and expeditions, part of the gallery's largest bequest era.")
    )

    val TICKETS = listOf("Adults" to 60, "Children (5–17)" to 40, "Students" to 40, "Pensioners" to 40)
    val FLOORS = listOf(
        "GROUND FLOOR" to listOf("Entrance Hall", "Sir Abe Bailey", "Sculpture Court", "Shop"),
        "FIRST FLOOR" to listOf("South African Art", "Contemporary", "Photography", "Café"),
        "SECOND FLOOR" to listOf("Temp. Exhibitions", "Education Suite", "Storage / Study")
    )
    val NEARBY = listOf(
        "Company's Garden" to "Park / Garden · Adjacent",
        "Iziko South African Museum" to "Natural History · 100m",
        "South African Jewish Museum" to "Museum · 200m",
        "District Six Museum" to "Heritage Museum · 1.2km",
        "Zeitz MOCAA" to "Contemporary Art · 2.4km"
    )

    const val BOOK_URL = "https://www.webtickets.co.za/v2/Client.aspx?clientcode=izikomuseum"
    const val MAP_QUERY = "Iziko South African National Gallery, Government Avenue, Cape Town"

    val INFO = mapOf(
        "about" to ("About the gallery" to "The Iziko South African National Gallery is South Africa's national art gallery in Company's Garden, Cape Town."),
        "faq" to ("FAQ" to "Opening hours: daily 09:00–17:00, closed on Christmas Day and 1 May. Tickets: adults R60, children, students and pensioners R40. Free entry for SA pensioners and students on Fridays with valid ID. Progress, points and saved artworks are stored only on your device."),
        "accessibility" to ("Accessibility" to "This app supports a dark mode, large text and four languages for menus. Tell us about any barrier: isang@iziko.org.za.")
    )

    /** Chat assistant: the first pattern found in the question decides the answer. */
    val FAQ = listOf(
        Regex("hour|open|close|time") to "We're open Monday to Sunday, 09:00–17:00. Closed on Christmas Day and Workers Day (1 May).",
        Regex("price|ticket|cost|fee|book") to "Adults R60; children (5–17), students and pensioners R40. SA students and pensioners enter free on Fridays with valid ID. Book at webtickets.co.za.",
        Regex("where|locat|address|direction|park") to "We're on Government Avenue in Company's Garden, Cape Town. See the Visit page for a map and directions.",
        Regex("quiz|badge|point|level|streak") to "Take a quiz on the Quiz page or answer the Daily Challenge to earn points and badges. See Art Challenge for your progress.",
        Regex("artist|sekoto|stern|kentridge") to "Our Artists page lists everyone in the collection, with biographies and their works.",
        Regex("butcher|alexander") to "The Butcher Boys is a sculpture by Jane Alexander. Open it from the Artworks page.",
        Regex("hello|hi|hey") to "Hello! Ask me about opening hours, tickets, location, quizzes or artworks."
    )
    const val FAQ_FALLBACK = "I'm not sure about that. Try the Visit or FAQ pages, or email isang@iziko.org.za."
}
