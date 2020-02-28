package com.labmatic.lablocation.framework.network.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(strict = false, name="response")
data class AirportStationPressureResponse(
    @field:Element(name = "data")
    var data: AirportStationPressureData? = null
)

@Root(name = "data", strict = true)
data class AirportStationPressureData(
    @field:Element(name = "METAR")
    var station: AirportStationPressure? = null
)

@Root(name = "METAR", strict = true)
data class AirportStationPressure(
    @field:Element(name = "altim_in_hg")
    var pressure: Float? = null
)

fun AirportStationPressure.toPressureInHpa(): Float? = pressure?.let { it * 33.8638f }