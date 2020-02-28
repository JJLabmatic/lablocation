package com.labmatic.lablocation.core.data.source

interface ElevationManagerCallbacks {
    fun apiAltitudeUpdated(altitude: Float)
    fun barometerAltitudeUpdated(altitude: Float)
}