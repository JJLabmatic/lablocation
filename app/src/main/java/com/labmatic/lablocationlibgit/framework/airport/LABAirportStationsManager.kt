package com.labmatic.lablocation.framework.airport

import com.labmatic.lablocation.core.data.source.AirportStationsManager
import com.labmatic.lablocation.core.data.source.AirportStationsManagerCallbacks
import com.labmatic.lablocation.framework.network.AirportStationsApi
import com.labmatic.lablocation.framework.network.model.toPressureInHpa
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class LABAirportStationsManager: AirportStationsManager {
    override var delegate: AirportStationsManagerCallbacks? = null

    private val job = SupervisorJob()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    private val radiusesToCheck = intArrayOf(10, 50, 100)
    private var radiusesCounter = 0
    private var processing = false
    private var lastRequestTime: Long = 0

    override fun requestPressureAtZeroLevel(latitude: Double, longitude: Double) {
        if(processing || System.currentTimeMillis() - lastRequestTime < TimeUnit.MINUTES.toMillis(10)) {
            return
        }
        lastRequestTime = System.currentTimeMillis()
        processing = true
        radiusesCounter = 0

        getStation(latitude, longitude)
    }

    private fun getStation(latitude: Double, longitude: Double) {
        ioScope.launch {
            val response = AirportStationsApi.create().getAirportStations(
                "${radiusesToCheck[radiusesCounter]};$longitude,$latitude").execute()
            when(response.isSuccessful) {
                true -> {
                    var gotStationId = false
                    response.body()?.data?.stations?.firstOrNull()?.stationId?.let {
                        gotStationId = true
                        getPressure(it)
                    }
                    if(!gotStationId) {
                        radiusesCounter++

                        if(radiusesCounter < 3) {
                            getStation(latitude, longitude)
                        } else {
                            processing = false
                        }
                    }
                }
                false -> {
                    radiusesCounter++

                    if(radiusesCounter < 3) {
                        getStation(latitude, longitude)
                    } else {
                        processing = false
                    }
                }
            }
        }
    }

    private fun getPressure(stationId: String) {
        ioScope.launch {
            val response = AirportStationsApi.create().getAirportStationPressure(
                "metars",
                "retrieve",
                "xml",
                stationId,
                3,
                "true"
            ).execute()
            when(response.isSuccessful) {
                true -> {
                    response?.body()?.data?.station?.toPressureInHpa()?.let {
                        delegate?.onPressureAtZeroLevelUpdated(it)
                    }
                    processing = false
                }
                false -> {
                    processing = false
                }
            }
        }
    }
}