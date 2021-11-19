package com.istea.mytasks.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.istea.mytasks.db.FirebaseHelper
import com.istea.mytasks.util.ScheduledWorker.Companion.NOTIFICATION_MESSAGE
import com.istea.mytasks.util.ScheduledWorker.Companion.NOTIFICATION_TITLE
import com.istea.mytasks.util.NotificationUtil
import com.istea.mytasks.util.isTimeAutomatic
import java.text.SimpleDateFormat
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // Get Message details
            val title = remoteMessage.data["title"]
            val taskTime = remoteMessage.data["taskTime"]
            val message = remoteMessage.data["message"]

            // Check that 'Automatic Date and Time' settings are turned ON.
            // If it's not turned on, Return
            if (!isTimeAutomatic(applicationContext)) {
                Log.d(TAG, "`Automatic Date and Time` is not enabled")
                return
            }

            // Check whether notification is scheduled or not
            val isScheduled = remoteMessage.data["isScheduled"]?.toBoolean()

            if (isScheduled == true) {
                // This is Scheduled Notification, Schedule it
                val scheduledTime = remoteMessage.data["scheduledTime"]
                scheduleAlarm(scheduledTime, title, message)
            } else {
                // This is not scheduled notification, show it now
                showNotification(title!!, message!!)
            }
        }
    }

    private fun scheduleAlarm(
        scheduledTimeString: String?,
        title: String?,
        message: String?
    ) {
        val alarmMgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent =
            Intent(applicationContext, NotificationBroadcastReceiver::class.java).let { intent ->
                intent.putExtra(NOTIFICATION_TITLE, title)
                intent.putExtra(NOTIFICATION_MESSAGE, message)
                PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
            }

        // Parse Schedule time
        val scheduledTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        scheduledTime.timeZone = TimeZone.getTimeZone("GMT")

        val scheduledTimeGMT = scheduledTime.parse(scheduledTimeString!!)

        scheduledTimeGMT?.let {
            if (it.after(Date())) {
                // With set(), it'll set non repeating one time alarm.
                alarmMgr.set(
                    AlarmManager.RTC_WAKEUP,
                    it.time,
                    alarmIntent
                )
            }
        }

    }

    private fun showNotification(title: String, message: String) {
        NotificationUtil(applicationContext).showNotification(title, message)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        FirebaseHelper().getNotificationToken()
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}