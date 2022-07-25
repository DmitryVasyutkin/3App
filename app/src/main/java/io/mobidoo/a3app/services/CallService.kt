package io.mobidoo.a3app.services

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import io.mobidoo.a3app.ui.CallActivity
import io.mobidoo.a3app.utils.*

val Context.powerManager: PowerManager get() = getSystemService(Context.POWER_SERVICE) as PowerManager
class CallService : InCallService() {

    private val callNotificationManager by lazy { CallNotificationManager(this) }
    private val callListener = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            if (state != Call.STATE_DISCONNECTED) {

            }
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.onCallAdded(call)
        CallManager.inCallService = this

        call.registerCallback(callListener)
     //   RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, Uri.parse(""))
        val isScreenLocked = (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked
        Log.i("CallService", "call added $call")
        Log.i("CallService", "call cycles ${!powerManager.isInteractive || call.isOutgoing() || isScreenLocked}")
        if (!powerManager.isInteractive || call.isOutgoing() || isScreenLocked) {
            try {
                Log.i("CallService", "call state ${call.getStateCompat()}")
                startActivity(CallActivity.getStartIntent(this))
                if (call.getStateCompat() != Call.STATE_RINGING) {

                  //  callNotificationManager.setupNotification()
                    startActivity(CallActivity.getStartIntent(this))
                }
            } catch (e: ActivityNotFoundException) {
                Log.i("CallService", "error $e")
                // seems like startActivity can thrown AndroidRuntimeException and ActivityNotFoundException, not yet sure when and why, lets show a notification
                callNotificationManager.setupNotification()
              //  startActivity(CallActivity.getStartIntent(this))
            }
        } else {
           // callNotificationManager.setupNotification()
            startActivity(CallActivity.getStartIntent(this))
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callListener)
        val wasPrimaryCall = call == CallManager.getPrimaryCall()
        CallManager.onCallRemoved(call)
        if (CallManager.getPhoneState() == NoCall) {
            CallManager.inCallService = null
            callNotificationManager.cancelNotification()
        } else {
            callNotificationManager.setupNotification()
            if (wasPrimaryCall) {
                startActivity(CallActivity.getStartIntent(this))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callNotificationManager.cancelNotification()
    }
}





