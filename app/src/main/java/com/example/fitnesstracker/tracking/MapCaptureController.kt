package com.example.fitnesstracker.tracking

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

internal class MapCaptureController {
    var request: (((Bitmap?) -> Unit) -> Unit)? = null
    suspend fun capture(): Bitmap? = withContext(Dispatchers.Main.immediate) {
        withTimeoutOrNull(8000) {
            suspendCancellableCoroutine<Bitmap?> { continuation ->
                val take = request
                if (take == null) continuation.resume(null)
                else take { bitmap -> if (continuation.isActive) continuation.resume(bitmap) }
            }
        }
    }
}
