package io.mobidoo.a3app.ui

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.view.*
import android.widget.EditText
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivityCallBinding
import io.mobidoo.a3app.utils.*
import io.mobidoo.a3app.utils.Constants.APP_PREFS
import io.mobidoo.a3app.utils.Constants.SP_FLASH_CAL_URI
import kotlinx.coroutines.*
import java.util.*

class CallActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val ANIMATION_DURATION = 250L
        fun getStartIntent(context: Context): Intent {
            val openAppIntent = Intent(context, CallActivity::class.java)
            openAppIntent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            return openAppIntent
        }
    }
    private lateinit var binding: ActivityCallBinding
    private lateinit var exoPlayer: ExoPlayer

    private var callDurationJob: Job? = null
    private var isCallEnded = false
    private var isSpeakerOn = false
    private var isMicrophoneOn = true
    private var proximityWakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setInset(binding.root)
        val uriString = getSharedPreferences(APP_PREFS, MODE_PRIVATE).getString(SP_FLASH_CAL_URI, "")!!
        initializePlayer(uriString)
        if (CallManager.getPhoneState() == NoCall) {
            finish()
            return
        }
        addLockScreenFlags()
        binding.ibAcceptCall.setOnClickListener(this)
        binding.ibDeclineCall.setOnClickListener(this)
        binding.ibEndCall.setOnClickListener(this)
        binding.ibIncCallHold.setOnClickListener(this)
        binding.ibIncCallKeypad.setOnClickListener(this)
        binding.ibIncCallMute.setOnClickListener(this)
        binding.ibIncCallSpeaker.setOnClickListener(this)
        binding.dialpadClose.setOnClickListener(this)

        setDialpadListeners()

        CallManager.addListener(callCallback)
        updateCallState(CallManager.getPrimaryCall()!!)
    }

    private fun setDialpadListeners() {
        binding.dialpad1Holder.setOnClickListener{
            dialpadPressed('1')
        }
        binding.dialpad2Holder.setOnClickListener{
            dialpadPressed('2')
        }
        binding.dialpad3Holder.setOnClickListener{
            dialpadPressed('3')
        }
        binding.dialpad4Holder.setOnClickListener{
            dialpadPressed('4')
        }
        binding.dialpad5Holder.setOnClickListener{
            dialpadPressed('5')
        }
        binding.dialpad6Holder.setOnClickListener{
            dialpadPressed('6')
        }
        binding.dialpad7Holder.setOnClickListener{
            dialpadPressed('7')
        }
        binding.dialpad8Holder.setOnClickListener{
            dialpadPressed('8')
        }
        binding.dialpad9Holder.setOnClickListener{
            dialpadPressed('9')
        }
        binding.dialpad0Holder.setOnClickListener{
            dialpadPressed('0')
        }
        binding.dialpad0Holder.setOnLongClickListener{
            dialpadPressed('+')
            true
        }
        binding.dialpadAsteriskHolder.setOnClickListener{
            dialpadPressed('*')
        }
        binding.dialpadHashtagHolder.setOnClickListener{
            dialpadPressed('#')
        }



    }

    private fun setInset(view: View) {
        view.setOnApplyWindowInsetsListener { view, windowInsets ->
            val bottomInsets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).bottom
            } else {
                windowInsets.stableInsetBottom
            }
            val topInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).top
            } else {
                windowInsets.stableInsetBottom
            }

            val params2 = binding.rlIncomingCallHolder.layoutParams as ViewGroup.MarginLayoutParams
            params2.setMargins(0, topInset, 0, bottomInsets)
            binding.rlIncomingCallHolder.layoutParams = params2

            return@setOnApplyWindowInsetsListener windowInsets
        }
    }

    private fun updateCallState(call: Call) {
        val state = call.getStateCompat()
        when (state) {
            Call.STATE_RINGING -> callRinging()
            Call.STATE_ACTIVE -> callStarted()
            Call.STATE_DISCONNECTED -> endCall()
            Call.STATE_CONNECTING, Call.STATE_DIALING -> initOutgoingCallUI()
            Call.STATE_SELECT_PHONE_ACCOUNT -> showPhoneAccountPicker()
        }
    }

    private fun showPhoneAccountPicker() {
        TODO("Not yet implemented")
    }

    private fun initOutgoingCallUI() {
        TODO("Not yet implemented")
    }

    private val callCallback = object : CallManagerListener {
        override fun onStateChanged() {

        }

        override fun onPrimaryCallChanged(call: Call) {
            updateCallState(call)
        }
    }

    private fun dialpadPressed(char: Char) {
        CallManager.keypad(char)
        binding.etDialpadInput.addCharacter(char)
    }
    private fun callStarted() {
        enableProximitySensor()
        exoPlayer.stop()
        exoPlayer.release()
        binding.rlIncomingCallRinging.visibility = View.GONE
        binding.rlIncomingCallHolder.visibility = View.VISIBLE
        startShowDuration()
    }

    private fun startShowDuration() {
        callDurationJob = CoroutineScope(Dispatchers.IO).launch {
            while (!isCallEnded) {
                val duration = CallManager.getPrimaryCall().getCallDuration()
                withContext(Dispatchers.Main) {
                    binding.tvIncomingCallDuration.text = duration.getFormattedDuration(false)
                }
                delay(1000)
            }
        }
    }

    private fun callRinging() {
        binding.rlIncomingCallRinging.visibility = View.VISIBLE
        binding.rlIncomingCallHolder.visibility = View.GONE
        val callNumber = CallManager.getPrimaryCall()?.details?.handle?.schemeSpecificPart
        val callerName = ContactsHelper.getContactNameByPhoneNumber(callNumber, this)
        if (callerName.isEmpty()){
            binding.tvIncomingCallIdentefier.text = callNumber
            binding.tvIncomingCallName.text = callNumber
        }else{
            binding.tvIncomingCallIdentefier.text = callerName
            binding.tvIncomingCallName.text = callerName
        }
        val photoUri = ContactsHelper.getContactPhotoByPhoneNumber(callNumber, this)
        if (photoUri != null && photoUri.isNotEmpty())
            binding.ivIncomingCallAvatar.load(CallContactAvatarHelper(this).getCallContactAvatar(photoUri))
    }

    private fun initializePlayer(uriString: String) {
        exoPlayer = ExoPlayer.Builder(this)
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            .build()
        binding.incomingCallPlayer.apply {
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            controllerAutoShow = false
            useController = false
            player = exoPlayer
            hideController()
        }

        exoPlayer.setMediaItem(MediaItem.fromUri(uriString))
        exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    private fun endCall() {
        disableProximitySensor()
        CallManager.reject()
        isCallEnded = true
        finishAffinity()
    }

    private fun acceptCall(){
        CallManager.accept()
        callStarted()
    }
    @SuppressLint("NewApi")
    private fun addLockScreenFlags() {
        if (isOreoMr1Plus()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        if (isOreoPlus()) {
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    private fun adjustAudio(setMute: Boolean) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val adJustMute: Int
        adJustMute = if (setMute) {
            AudioManager.ADJUST_MUTE
        } else {
            AudioManager.ADJUST_UNMUTE
        }
        audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, adJustMute, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, adJustMute, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, adJustMute, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, adJustMute, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, adJustMute, 0)
    }

    override fun onClick(p0: View?) {
        when(p0){
            binding.ibAcceptCall -> {
                acceptCall()
            }
            binding.ibDeclineCall -> {
                endCall()
            }
            binding.ibEndCall -> {
                endCall()
            }
            binding.ibIncCallHold -> {
                toggleHold()
            }
            binding.ibIncCallKeypad -> {
                toggleDialpadVisibility()
            }
            binding.ibIncCallMute -> {
                toggleMicrophone()
            }
            binding.ibIncCallSpeaker -> {
                toggleSpeaker()
            }
            binding.dialpadClose -> {
                toggleDialpadVisibility()
            }
        }
    }

    private fun toggleMicrophone() {
        isMicrophoneOn = !isMicrophoneOn
        val drawable = if (isMicrophoneOn) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24
        binding.ibIncCallMute.setImageDrawable(getDrawable(drawable))
        audioManager.isMicrophoneMute = !isMicrophoneOn
        CallManager.inCallService?.setMuted(!isMicrophoneOn)
        binding.tvIncCallMute.text = getString(if (isMicrophoneOn) R.string.mute else R.string.unmute)
    }

    private fun toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn
        val drawable = if (isSpeakerOn) R.drawable.ic_speaker_on_vector else R.drawable.ic_speaker_off_vector
        binding.ibIncCallSpeaker.setImageDrawable(getDrawable(drawable))
        audioManager.isSpeakerphoneOn = isSpeakerOn

        val newRoute = if (isSpeakerOn) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
        CallManager.inCallService?.setAudioRoute(newRoute)
       // binding.tvIncCallSpeaker.text = getString(if (isSpeakerOn) R.string.speaker else R.string.turn_speaker_on)

        if (isSpeakerOn) {
            disableProximitySensor()
        } else {
            enableProximitySensor()
        }
    }

    private fun toggleHold() {
        val isOnHold = CallManager.toggleHold()
        val drawable = if (isOnHold) R.drawable.ic_pause_crossed else R.drawable.ic_baseline_pause_24
        if (isOnHold){
            callDurationJob?.cancel()
        }else{
            startShowDuration()
        }

        binding.ibIncCallHold.setImageDrawable(getDrawable(drawable))
        binding.tvIncCallHold.text = getString(if (isOnHold) R.string.resume_call else R.string.hold_call)
    }

    private fun toggleDialpadVisibility() {
        if (binding.rlDialpadHolder.visibility == View.VISIBLE) hideDialpad() else showDialpad()
    }

    private fun showDialpad() {
        binding.rlDialpadHolder.animate().withStartAction { binding.rlDialpadHolder.beVisible() }.alpha(1f)
        binding.rlMainCallActions.beGone()
    }

    private fun hideDialpad() {
        binding.rlDialpadHolder.animate().alpha(0f).withEndAction { binding.rlDialpadHolder.beGone() }
        binding.rlMainCallActions.beVisible()
    }

    private fun enableProximitySensor() {
        if ((proximityWakeLock == null || proximityWakeLock?.isHeld == false)) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            proximityWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "com.simplemobiletools.dialer.pro:wake_lock")
            proximityWakeLock!!.acquire(60 * 60 * 1000L)
        }
    }

    private fun disableProximitySensor() {
        if (proximityWakeLock?.isHeld == true) {
            proximityWakeLock!!.release()
        }
    }

    override fun onBackPressed() {

    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        callDurationJob?.cancel()
    }
}

fun Int.getFormattedDuration(forceShowHours: Boolean = false): String {
    val sb = StringBuilder(8)
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60

    if (this >= 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    } else if (forceShowHours) {
        sb.append("0:")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O_MR1)
fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

val Context.audioManager: AudioManager get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}
fun EditText.addCharacter(char: Char) {
    dispatchKeyEvent(getKeyEvent(getCharKeyCode(char)))
}

fun EditText.getKeyEvent(keyCode: Int) = KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, 0)

private fun getCharKeyCode(char: Char) = when (char) {
    '0' -> KeyEvent.KEYCODE_0
    '1' -> KeyEvent.KEYCODE_1
    '2' -> KeyEvent.KEYCODE_2
    '3' -> KeyEvent.KEYCODE_3
    '4' -> KeyEvent.KEYCODE_4
    '5' -> KeyEvent.KEYCODE_5
    '6' -> KeyEvent.KEYCODE_6
    '7' -> KeyEvent.KEYCODE_7
    '8' -> KeyEvent.KEYCODE_8
    '9' -> KeyEvent.KEYCODE_9
    '*' -> KeyEvent.KEYCODE_STAR
    '+' -> KeyEvent.KEYCODE_PLUS
    else -> KeyEvent.KEYCODE_POUND
}