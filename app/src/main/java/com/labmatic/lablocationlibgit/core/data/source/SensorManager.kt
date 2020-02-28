package com.labmatic.lablocation.core.data.source

import android.content.Context

interface SensorManager {
    var delegate: SensorManagerCallbacks?

    fun startPressureMonitoring(context: Context)
    fun stopPressureMonitoring()
    fun hasBarometer(): Boolean
}