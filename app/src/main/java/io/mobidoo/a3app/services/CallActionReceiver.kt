package io.mobidoo.a3app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.mobidoo.a3app.ui.CallActivity
import io.mobidoo.a3app.utils.ACCEPT_CALL
import io.mobidoo.a3app.utils.CallManager
import io.mobidoo.a3app.utils.DECLINE_CALL

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACCEPT_CALL -> {
                context.startActivity(CallActivity.getStartIntent(context))
                CallManager.accept()
            }
            DECLINE_CALL -> CallManager.reject()
        }
    }
}
