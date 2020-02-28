package com.labmatic.lablocation.framework.network.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(strict = false, name="response")
data class AirportStationsResponse(
    @field:Element(name = "data")
    var data: AirportStationsData? = null
)

@Root(name = "data", strict = true)
data class AirportStationsData(
    @field:ElementList(name = "Station", inline = true, required = false)
    var stations: List<AirportStation>? = null
)

@Root(name = "Station", strict = true)
data class AirportStation(
    @field:Element(name = "station_id")
    var stationId: String = "",

    @field:Element(name = "latitude", required = false)
    var latitude: Double = -1.0,

    @field:Element(name = "longitude", required = false)
    var longitude: Double = -1.0,

    @field:Element(name = "elevation_m", required = false)
    var elevationInMeters: Float = -1.0f
)