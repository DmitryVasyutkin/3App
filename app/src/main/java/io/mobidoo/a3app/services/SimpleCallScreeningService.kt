package io.mobidoo.a3app.services

import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import android.util.Log
import androidx.annotation.RequiresApi
import io.mobidoo.a3app.ui.CallActivity
import io.mobidoo.a3app.utils.CallNotificationManager
import io.mobidoo.a3app.utils.isQPlus
import io.mobidoo.a3app.utils.notificationManager

@RequiresApi(Build.VERSION_CODES.N)
class SimpleCallScreeningService : CallScreeningService() {

   // private val callNotificationManager by lazy { CallNotificationManager(this) }

    override fun onScreenCall(callDetails: Call.Details) {
        val callNotificationManager = CallNotificationManager(this, callDetails.handle.schemeSpecificPart)
        val tm = getSystemService(TELECOM_SERVICE) as TelecomManager

        if (isQPlus()){
            if(callDetails.callDirection == Call.Details.DIRECTION_INCOMING){
                respondToCall(callDetails, false)
                startActivity(CallActivity.getStartIntent(this, callDetails.handle.schemeSpecificPart))
            }
        }else{
            respondToCall(callDetails, false)
            startActivity(CallActivity.getStartIntent(this, callDetails.handle.schemeSpecificPart))
        }
    }

    private fun respondToCall(callDetails: Call.Details, isBlocked: Boolean) {
        val response = CallResponse.Builder()
            .setDisallowCall(isBlocked)
            .setRejectCall(isBlocked)
            .setSkipCallLog(isBlocked)
            .setSkipNotification(isBlocked)
            .build()

        respondToCall(callDetails, response)
    }
}
