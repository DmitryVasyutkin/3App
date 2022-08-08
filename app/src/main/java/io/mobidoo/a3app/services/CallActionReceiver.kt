package io.mobidoo.a3app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.TELECOM_SERVICE
import android.content.Intent
import android.telecom.TelecomManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import io.mobidoo.a3app.ui.CallActivity
import io.mobidoo.a3app.utils.*

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACCEPT_CALL -> {
                val tm = context.getSystemService(TELECOM_SERVICE) as TelecomManager
                tm.acceptRingingCall()
                tm.showInCallScreen(false)
                context.notificationManager.cancel(CALL_NOTIFICATION_ID)

            }
            DECLINE_CALL -> {
                val tm = context.getSystemService(TELECOM_SERVICE) as TelecomManager
                tm.endCall()
                context.notificationManager.cancel(CALL_NOTIFICATION_ID)
            }
        }
    }
}
