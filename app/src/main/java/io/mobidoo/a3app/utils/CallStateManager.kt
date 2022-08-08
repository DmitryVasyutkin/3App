package io.mobidoo.a3app.utils

import android.telephony.TelephonyManager

object CallStateManager {
    private var state = TelephonyManager.EXTRA_STATE_RINGING
    private var listener: SimpleCallStateListener? = null
    fun setListener(newListener: SimpleCallStateListener){
        listener = newListener
    }
    fun changeState(newState: String){
        state = newState
        listener?.onStateChanged(newState)
    }
}

interface SimpleCallStateListener {
    fun onStateChanged(state: String)
}