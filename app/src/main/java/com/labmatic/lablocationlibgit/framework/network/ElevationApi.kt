package com.labmatic.lablocation.framework.network

import com.labmatic.lablocation.framework.network.model.ElevationResponse
import com.labmatic.lablocationlibgit.BuildConfig
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ElevationApi {
    @GET("lookup")
    fun getElevation(@Query("locations") pointsText: String): Call<ElevationResponse>

    companion object Factory {
        fun create(baseUrl: String): ElevationApi {
            val retrofit = createApiClient(baseUrl, true, null, BuildConfig.DEBUG)

            return retrofit.create(ElevationApi::class.java)
        }
    }
}