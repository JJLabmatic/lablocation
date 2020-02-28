package com.labmatic.lablocation.core.data.source

interface ElevationManager {
    var delegate: ElevationManagerCallbacks?

    fun requestApiElevation(latitude: Double, longitude: Double, baseUrl: String)
    fun updateBarometerPressure(pressure: Float)
    fun updateZeroLevelPressure(pressure: Float)
    fun hasZeroLevelPressure(): Boolean
}