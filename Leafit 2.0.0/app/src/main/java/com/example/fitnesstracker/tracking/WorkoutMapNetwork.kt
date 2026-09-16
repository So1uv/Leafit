package com.example.fitnesstracker.tracking

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.module.http.HttpRequestUtil
import java.io.File

internal object WorkoutMapNetwork {
    private var initialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        val app = context.applicationContext
        MapLibre.getInstance(app)
        val client = OkHttpClient.Builder()
            .cache(Cache(File(app.cacheDir, "workout_map_http"), 64L * 1024 * 1024))
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Leafit/2.0.0 (Android; ${app.packageName})")
                    .build()
                chain.proceed(request)
            }
            .build()
        // Native and HTTP caches retain server expiry/ETag semantics. No forced refresh or preloading.
        HttpRequestUtil.setOkHttpClient(client)
        HttpRequestUtil.setPrintRequestUrlOnFailure(false)
        initialized = true
    }
}
