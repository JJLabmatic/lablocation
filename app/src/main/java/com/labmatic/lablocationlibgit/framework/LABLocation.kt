package com.labmatic.lablocation.framework

import android.app.Activity
import android.content.Context
import com.labmatic.lablocation.core.data.source.*
import com.labmatic.lablocation.core.domain.Location
import com.labmatic.lablocation.framework.airport.LABAirportStationsManager
import com.labmatic.lablocation.framework.elevation.LABElevationManager
import com.labmatic.lablocation.framework.location.LABLocationManager
import com.labmatic.lablocation.framework.sensor.LABSensorManager

interface ILocation {
    fun startLocationTracking(activity: Activity, permissionRequestCode: Int)
    fun stopLocationTracking()
    fun startElevationTracking(context: Context, doUseOwnGpsProvider: Boolean = false)
    fun stopElevationTracking()
    fun isGpsEnabled(context: Context): Boolean
    fun updateLocation(location: Location)
}

interface LABLocationCallbacks {
    fun onLocation(location: Location) {}
    fun onSpeed(speed: Float) {}
    fun onAltitudeGps(altitude: Float) {}
    fun onAltitudeApi(altitude: Float) {}
    fun onAltitudeBarometer(altitude: Float) {}
}

class LABLocation(
    private val config: LABLocationConfig,
    private var delegate: LABLocationCallbacks?
): ILocation, LocationManagerCallbacks, ElevationManagerCallbacks, SensorManagerCallbacks, AirportStationsManagerCallbacks {
    private val locationManager: LocationManager = LABLocationManager()
    private val elevationManager: ElevationManager = LABElevationManager()
    private val sensorManager: SensorManager = LABSensorManager()
    private val airportStationManager: AirportStationsManager = LABAirportStationsManager()

    private var lastApiElevationReadingTimestamp: Long? = null
    private var lastBarometerElevationReadingTimestamp: Long? = null
    private var altitudeTracking = false
    private var usingOwnGpsProvider = false

    init {
        locationManager.delegate = this
    }

    override fun startLocationTracking(activity: Activity, permissionRequestCode: Int) =
        locationManager.startLocationTracking(activity, permissionRequestCode, config.gpsMeasurementInterval)
    override fun stopLocationTracking() = locationManager.stopLocationTracking()
    override fun isGpsEnabled(context: Context) = locationManager.isGpsEnabled(context)
    override fun startElevationTracking(context: Context, doUseOwnGpsProvider: Boolean) {
        this.usingOwnGpsProvider = doUseOwnGpsProvider
        sensorManager.delegate = this
        sensorManager.startPressureMonitoring(context)
        airportStationManager.delegate = this
        elevationManager.delegate = this
        altitudeTracking = true
    }

    override fun stopElevationTracking() {
        sensorManager.stopPressureMonitoring()
        altitudeTracking = false
    }

    override fun updateLocation(location: Location) {
        locationUpdated(location)
    }

    /**
     * LocationManager callbacks
     */
    override fun locationUpdated(location: Location) {
        delegate?.onLocation(location)

        if(!altitudeTracking) return

        if(!elevationManager.hasZeroLevelPressure()) {
            airportStationManager.requestPressureAtZeroLevel(location.latitude, location.longitude)
        }

        val time = System.currentTimeMillis()
        if(time - (lastApiElevationReadingTimestamp ?: 0) >= config.apiElevationRequestIntervalInMillis) {
            lastApiElevationReadingTimestamp = time

            elevationManager.requestApiElevation(location.latitude, location.longitude, config.apiElevationBaseUrl)
        }
    }
    override fun gpsAltitudeUpdated(altitude: Float) { if(altitudeTracking) delegate?.onAltitudeGps(altitude) }
    override fun speedUpdated(speed: Float) { delegate?.onSpeed(speed) }

    /**
     * SensorManager callbacks
     */
    override fun onPressureUpdated(pressure: Float) {
        val time = System.currentTimeMillis()
        if(time - (lastBarometerElevationReadingTimestamp ?: 0) >= config.barometerElevationIntervalInMillis) {
            lastBarometerElevationReadingTimestamp = time

            elevationManager.updateBarometerPressure(pressure)
        }
    }

    /**
     * ElevationManager callbacks
     */
    override fun apiAltitudeUpdated(altitude: Float) { delegate?.onAltitudeApi(altitude) }
    override fun barometerAltitudeUpdated(altitude: Float) { delegate?.onAltitudeBarometer(altitude) }

    /**
     * AirportStationsManager callbacks
     */
    override fun onPressureAtZeroLevelUpdated(pressure: Float) { elevationManager.updateZeroLevelPressure(pressure) }
}