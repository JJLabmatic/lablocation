package com.labmatic.lablocation.framework.network.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ElevationResponse(@SerializedName("results")
                             var elevations: List<Elevation>? = null): Serializable

data class Elevation(@SerializedName("elevation")
                     var elevation: Float? = null): Serializable