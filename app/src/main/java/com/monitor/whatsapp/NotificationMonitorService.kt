package com.monitor.whatsapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

class NotificationMonitorService : NotificationListenerService() {

    companion object {
        private val WHATSAPP_PACKAGES = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b"
        )
        private const val CHANNEL_ID = "keyword_alerts"
        private const val CHANNEL_NAME = "تنبيهات الكلمات"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in WHATSAPP_PACKAGES) return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

        val fullText = "$title $text $bigText"

        val keywords = getKeywords()
        val matchedKeyword = keywords.firstOrNull { keyword ->
            fullText.contains(keyword, ignoreCase = true)
        } ?: return

        sendAlert(title, text, matchedKeyword)
    }

    private fun getKeywords(): List<String> {
        val prefs = getSharedPreferences("keywords_prefs", Context.MODE_PRIVATE)
        val keywordsSet = prefs.getStringSet("keywords", emptySet()) ?: emptySet()
        return keywordsSet.toList()
    }

    private fun sendAlert(channelName: String, message: String, keyword: String) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("تنبيه: $keyword")
            .setContentText("$channelName: $message")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("القناة: $channelName\n\nالرسالة: $message\n\nالكلمة المطابقة: $keyword")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
