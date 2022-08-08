package io.mobidoo.a3app.services

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityManagerCompat
import io.mobidoo.a3app.ui.CallActivity
import io.mobidoo.a3app.utils.CallManager
import io.mobidoo.a3app.utils.CallStateManager

class CallBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent?.extras
        val actions = intent?.action
        if (extras?.getString(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING){
            Log.i("CallBroadcastReceiver", "new incoming call")
            try {
                val number = extras?.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
                Log.i("CallBroadcastReceiver", "number $number")

//                context?.startActivity(CallActivity.getStartIntent(context, number!!))
            }catch (e: Exception){
                Log.i("CallBroadcastReceiver", "call activity start exc $e")
            }
        }

        val state = extras?.getString(TelephonyManager.EXTRA_STATE)
        if (state == TelephonyManager.EXTRA_STATE_IDLE || state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
            Log.i("CallBroadcastReceiver", "call skipped")
            if (state != null) {
                CallStateManager.changeState(state)
            }
        }
    }
}