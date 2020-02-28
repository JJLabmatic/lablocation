package com.labmatic.lablocation.core.data.source

import android.app.Activity
import android.content.Context

interface LocationManager {
    var delegate: LocationManagerCallbacks?

    fun startLocationTracking(activity: Activity, permissionRequestCode: Int, interval: Long)
    fun stopLocationTracking()
    fun isGpsEnabled(context: Context): Boolean
}