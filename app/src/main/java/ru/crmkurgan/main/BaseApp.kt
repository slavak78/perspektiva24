package ru.crmkurgan.main

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.places.PlacesFactory

class BaseApp : Application() {
    private val mapYandexKey = "eb00b752-492b-432f-ac10-867c91f87820"

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(mapYandexKey)
        MapKitFactory.initialize(this)
        PlacesFactory.initialize(this)
    }
}