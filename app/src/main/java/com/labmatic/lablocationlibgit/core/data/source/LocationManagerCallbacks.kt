package com.labmatic.lablocation.core.data.source

import com.labmatic.lablocation.core.domain.Location

interface LocationManagerCallbacks {
    fun locationUpdated(location: Location)
    fun speedUpdated(speed: Float)
    fun gpsAltitudeUpdated(altitude: Float)
}