package com.labmatic.lablocation.core.data.source

interface AirportStationsManager {
    var delegate: AirportStationsManagerCallbacks?

    fun requestPressureAtZeroLevel(latitude: Double, longitude: Double)
}