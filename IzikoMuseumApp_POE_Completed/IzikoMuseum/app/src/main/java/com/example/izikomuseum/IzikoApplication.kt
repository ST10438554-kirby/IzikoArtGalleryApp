package com.example.izikomuseum

import android.app.Application
import com.example.izikomuseum.data.repository.AuthRepository
import com.example.izikomuseum.data.repository.MuseumRepository
import com.example.izikomuseum.data.repository.Store

/** Runs once when the app starts: loads the saved progress and the museum data. */
class IzikoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Store.init(this)
        AuthRepository.init(this)
        MuseumRepository.load(this)
    }
}
