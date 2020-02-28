package com.labmatic.lablocation.framework

import java.util.concurrent.TimeUnit

data class LABLocationConfig(
    val gpsMeasurementInterval: Long,
    val apiElevationRequestIntervalInMillis: Long,
    val barometerElevationIntervalInMillis: Long,
    val apiElevationBaseUrl: String
) {
    companion object {
        fun default(apiElevationBaseUrl: String): LABLocationConfig =
            LABLocationConfig(
                0,
                TimeUnit.MINUTES.toMillis(1),
                TimeUnit.MINUTES.toMillis(1),
                apiElevationBaseUrl)
    }
}