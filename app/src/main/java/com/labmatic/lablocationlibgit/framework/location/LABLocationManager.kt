package com.labmatic.lablocation.framework.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.GpsStatus
import android.location.Location
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.labmatic.lablocation.core.data.source.LocationManager
import com.labmatic.lablocation.core.data.source.LocationManagerCallbacks

class LABLocationManager: LocationManager {
    override var delegate: LocationManagerCallbacks? = null

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationManager: android.location.LocationManager

    private var usingNmea = false
    private var lastNmeaElevationTime: Long = 0
    private var lastElevationUpdateTime: Long = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            locationResult ?: return

            locationResult.locations.firstOrNull().let { location ->
                if(location!!.hasAccuracy() && location!!.accuracy <= 50) {
                    if(location.hasSpeed()) {
                        delegate?.speedUpdated(location.speed)
                    }

                    val currentTime = System.currentTimeMillis()

                    if(!usingNmea || currentTime - lastNmeaElevationTime >= 20 * 1000) {
                        if(location.hasAltitude()) {
                            lastElevationUpdateTime = currentTime

                            delegate?.gpsAltitudeUpdated(location.altitude.toFloat())
                        }
                    }
                    delegate?.locationUpdated(com.labmatic.lablocation.core.domain.Location(
                        location.latitude, location.longitude, location.accuracy
                    ))
                } else {

                }
            }
        }
    }

    override fun startLocationTracking(activity: Activity, permissionRequestCode: Int, interval: Long) {
        if(checkPermissions(activity)) {
            requestLocationUpdates(activity, interval)
        } else {
            requestLocationPermission(activity, permissionRequestCode)
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun stopLocationTracking() {
        try {
            locationClient.removeLocationUpdates(locationCallback)
            locationManager.removeUpdates(locationListener)
        } catch(exception: Exception) {}
    }

    override fun isGpsEnabled(context: Context) =
        (context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager).isProviderEnabled(
            android.location.LocationManager.GPS_PROVIDER)

    private fun requestLocationUpdates(activity: Activity, interval: Long) {
        locationClient = LocationServices.getFusedLocationProviderClient(activity)

        val locationRequest = LocationRequest()
        locationRequest.interval = interval
        locationRequest.fastestInterval = interval
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().
            addLocationRequest(locationRequest).
            setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
        result.addOnCompleteListener(activity) { task ->
            try {
                startLocationUpdates(activity, locationRequest)
            } catch(exception: ApiException) {
                when(exception.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> startLocationUpdates(activity, locationRequest)
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        if(isGpsEnabled(activity)) startLocationUpdates(activity, locationRequest)

                        try {
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(activity, 10007)
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun startLocationUpdates(activity: Activity, request: LocationRequest) {
        locationClient.requestLocationUpdates(request, locationCallback, null)

        if(android.os.Build.VERSION.SDK_INT >= 24) {
            lastNmeaElevationTime = System.currentTimeMillis()

            locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)

            usingNmea = try {
                locationManager.addNmeaListener { nmea: String?, timestamp: Long ->
                    try {
                        if(nmea != null) {
                            parseNmeaString(nmea)
                        }
                    } catch(exception: Exception) {}
                }
                true
            } catch(exception: Exception) {
                false
            }
        } else {
            lastNmeaElevationTime = System.currentTimeMillis()

            locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)

            try {
                locationManager.addNmeaListener(GpsStatus.NmeaListener { timestamp, nmea ->
                    try {
                        if(nmea != null) {
                            parseNmeaString(nmea)
                        }
                    } catch(exception: Exception) {}
                })
                usingNmea = true
            } catch(exception: Exception) {
                usingNmea = false
            }
        }
    }

    private fun checkPermissions(context: Context) =
        ActivityCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED

    private fun requestLocationPermission(activity: Activity, permissionRequestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            permissionRequestCode)
    }

    private val locationListener: android.location.LocationListener = object : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {}
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun parseNmeaString(nmea: String) {
        println("--- NMEA ${nmea}")

        if (nmea.startsWith("$")) {
            val tokens = nmea.split(",")

            if (tokens.isEmpty()) {
                return
            }

            val type = tokens[0]

            if (type.startsWith("\$GPGGA")) {
                if (tokens.size < 10) {
                    return
                }

                if (tokens[9].isNotEmpty()) {
                    try {
                        val elevation = tokens[9].toDouble().toFloat()

                        delegate?.gpsAltitudeUpdated(elevation)

                        lastNmeaElevationTime = System.currentTimeMillis()
                    } catch (exception: Exception) {

                    }
                }
            } else if (type.startsWith("\$GNGGA")) {
                if (tokens.size < 10) {
                    return
                }

                if (tokens[9].isNotEmpty()) {
                    try {
                        val elevation = tokens[9].toDouble().toFloat()

                        delegate?.gpsAltitudeUpdated(elevation)

                        lastNmeaElevationTime = System.currentTimeMillis()
                    } catch (exception: Exception) {

                    }
                }
            }
        }
    }
}