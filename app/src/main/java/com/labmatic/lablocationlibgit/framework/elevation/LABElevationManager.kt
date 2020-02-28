package com.labmatic.lablocation.framework.elevation

import android.hardware.SensorManager
import com.labmatic.lablocation.core.data.source.ElevationManager
import com.labmatic.lablocation.core.data.source.ElevationManagerCallbacks
import com.labmatic.lablocation.framework.network.ElevationApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LABElevationManager: ElevationManager {
    override var delegate: ElevationManagerCallbacks? = null

    private var nearestStationZeroLevelPressure: Float? = null
    private val job = SupervisorJob()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    private var barometerPressure: Float? = null

    override fun requestApiElevation(
        latitude: Double,
        longitude: Double,
        baseUrl: String) {
        ioScope.launch {
            val response = ElevationApi.create(baseUrl).getElevation("$latitude,$longitude").execute()
            when(response.isSuccessful) {
                true -> {
                    response.body()?.elevations?.firstOrNull()?.elevation?.let {
                        delegate?.apiAltitudeUpdated(it)
                    }
                }
            }
        }
    }

    override fun updateBarometerPressure(pressure: Float) {
        barometerPressure = pressure

        println("Pressure: $pressure")

        nearestStationZeroLevelPressure?.let { basePressure ->
            delegate?.barometerAltitudeUpdated(SensorManager.getAltitude(basePressure, pressure))
        }
    }

    override fun updateZeroLevelPressure(pressure: Float) {
        nearestStationZeroLevelPressure = pressure

        barometerPressure?.let {
            delegate?.barometerAltitudeUpdated(SensorManager.getAltitude(pressure, it))
        }
    }
    override fun hasZeroLevelPressure() = nearestStationZeroLevelPressure != null
}