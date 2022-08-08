package io.mobidoo.a3app.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.TELECOM_SERVICE
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.app.NotificationCompat
import io.mobidoo.a3app.R
import io.mobidoo.a3app.services.CallActionReceiver
import io.mobidoo.a3app.services.powerManager
import io.mobidoo.a3app.ui.CallActivity
const val CALL_NOTIFICATION_ID = 1223
val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
class CallNotificationManager(private val context: Context, private val phoneCaller: String = "") {

    private val ACCEPT_CALL_CODE = 0
    private val DECLINE_CALL_CODE = 1
    private val notificationManager = context.notificationManager


    @SuppressLint("NewApi")
    fun setupNotification() {

            val callState = Call.STATE_RINGING
            val isHighPriority = true
             //   context.powerManager.isInteractive && callState == Call.STATE_RINGING
            val channelId = if (isHighPriority) "simple_dialer_call_high_priority" else "simple_dialer_call"
//            if (isOreoPlus()) {
//                val importance = if (isHighPriority) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
                val name = if (isHighPriority) "call_notification_channel_high_priority" else "call_notification_channel"
//
//                NotificationChannel(channelId, name, importance).apply {
//                    setSound(null, null)
//                    notificationManager.createNotificationChannel(this)
//                }
//            }
            NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }

            val openAppIntent = CallActivity.getStartIntent(context, phoneCaller)
            val openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_CANCEL_CURRENT)

            val acceptCallIntent = Intent(context, CallActionReceiver::class.java)
            acceptCallIntent.action = ACCEPT_CALL
            val acceptPendingIntent =
                PendingIntent.getBroadcast(context, ACCEPT_CALL_CODE, acceptCallIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)

            val declineCallIntent = Intent(context, CallActionReceiver::class.java)
            declineCallIntent.action = DECLINE_CALL
            val declinePendingIntent =
                PendingIntent.getBroadcast(context, DECLINE_CALL_CODE, declineCallIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)

            var callerName = phoneCaller

            val contentTextId = when (callState) {
                Call.STATE_RINGING -> R.string.is_calling
                Call.STATE_DIALING -> R.string.dialing
                Call.STATE_DISCONNECTED -> R.string.call_ended
                Call.STATE_DISCONNECTING -> R.string.call_ending
                else -> R.string.ongoing_call
            }

            val collapsedView = RemoteViews(context.packageName, R.layout.call_notification).apply {
                setText(R.id.notification_caller_name, callerName)
                setText(R.id.notification_call_status, context.getString(contentTextId))
                setVisibleIf(R.id.notification_accept_call, callState == Call.STATE_RINGING)

                setOnClickPendingIntent(R.id.notification_decline_call, declinePendingIntent)
                setOnClickPendingIntent(R.id.notification_accept_call, acceptPendingIntent)

//                if (callContactAvatar != null) {
//                    setImageViewBitmap(R.id.notification_thumbnail, callContactAvatarHelper.getCircularBitmap(callContactAvatar))
//                }
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_phone_green_vector)
                .setContentIntent(openAppPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_CALL)
                .setCustomContentView(collapsedView)
                .setChannelId(channelId)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setFullScreenIntent(openAppPendingIntent, true)


            val notification = builder.build()
            notificationManager.notify(CALL_NOTIFICATION_ID, notification)
        }


    fun cancelNotification() {
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun RemoteViews.setText(id: Int, text: String) {
    setTextViewText(id, text)
}

fun RemoteViews.setVisibleIf(id: Int, beVisible: Boolean) {
    val visibility = if (beVisible) View.VISIBLE else View.GONE
    setViewVisibility(id, visibility)
}