package com.example.tactonprueba.utils

import android.annotation.SuppressLint
import android.content.Context
import com.mapbox.common.MapboxOptions

object MapInit {
    @SuppressLint("DiscouragedApi")
    fun provideAccessToken(context: Context) {
        val token = context.getString(
            context.resources.getIdentifier(
                "mapbox_access_token", "string", context.packageName
            )
        )
        MapboxOptions.accessToken = token
    }

    const val NORMAL = "mapbox://styles/devilwolverine/cmdcvi8dx00a701r108ticovj"
    const val SATELLITE = "mapbox://styles/mapbox/satellite-streets-v12"
    const val LIGHT = "mapbox://styles/mapbox/light-v11"
    const val DARK = "mapbox://styles/mapbox/dark-v11"
    const val NAV_DAY = "mapbox://styles/mapbox/navigation-guidance-day-v4"
    const val NAV_NIGHT = "mapbox://styles/mapbox/navigation-guidance-night-v4"


}