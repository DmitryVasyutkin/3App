package io.mobidoo.a3app.utils

import android.content.Context
import android.net.Uri
import android.os.Looper
import android.telecom.Call
import androidx.loader.content.CursorLoader
import io.mobidoo.a3app.R
data class CallContact(var name: String, var photoUri: String, var number: String, var numberLabel: String)
fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

