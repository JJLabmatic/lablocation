package com.labmatic.lablocation.framework.network

import com.labmatic.lablocation.core.domain.Config
import com.labmatic.lablocation.framework.network.model.AirportStationPressureResponse
import com.labmatic.lablocation.framework.network.model.AirportStationsResponse
import com.labmatic.lablocationlibgit.BuildConfig
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AirportStationsApi {
    @GET("adds/dataserver_current/httpparam?dataSource=stations&requestType=retrieve&format=xml")
    fun getAirportStations(@Query("radialDistance") radialDistanceLatAndLong: String): Call<AirportStationsResponse>

    @GET("adds/dataserver_current/httpparam")
    fun getAirportStationPressure(@Query("dataSource") dataSource: String,
                                  @Query("requestType") requestType: String,
                                  @Query("format") format: String,
                                  @Query("stationString") stationId: String,
                                  @Query("hoursBeforeNow") hoursBeforeNow: Int,
                                  @Query("MostRecent") mostRecent: String): Call<AirportStationPressureResponse>

    companion object Factory {
        fun create(): AirportStationsApi {
            val retrofit = createApiClient(Config.AIRPORT_STATIONS_API_URL, false, null, BuildConfig.DEBUG, xml = true)

            return retrofit.create(AirportStationsApi::class.java)
        }
    }
}