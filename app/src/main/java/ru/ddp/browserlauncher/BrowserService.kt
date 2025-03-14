package ru.ddp.browserlauncher

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Calendar

class BrowserService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification("Сервис запущен"))
    }

    @SuppressLint("DefaultLocale")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("URL")
        val hour = intent?.getIntExtra("HOUR", 0) ?: 0
        val minute = intent?.getIntExtra("MINUTE", 0) ?: 0
        val actionAfterOpen = intent?.getStringExtra("ACTION_AFTER_OPEN") ?: "Остановить сервис"

        if (url.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d("BrowserService", "Запланировано открытие браузера")
        startForeground(1, createNotification(String.format("Запланировано на %02d:%02d", hour, minute)))
        scheduleBrowserLaunch(url, hour, minute, actionAfterOpen)
        return START_STICKY
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleBrowserLaunch(url: String, hour: Int, minute: Int, actionAfterOpen: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BrowserReceiver::class.java).apply {
            putExtra("URL", url)
            putExtra("ACTION_AFTER_OPEN", actionAfterOpen)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1) // Перенос на следующий день, если время уже прошло
        }

        Log.d("BrowserService", "Запуск браузера запланирован на: ${calendar.time}")

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "browser_service_channel",
                "Browser Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        val stopIntent = Intent(this, StopServiceReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val settingsIntent = Intent(this, MainActivity::class.java)
        val settingsPendingIntent = PendingIntent.getActivity(this, 0, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "browser_service_channel")
            .setContentTitle("Запуск браузера")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Остановить", stopPendingIntent)
            .addAction(android.R.drawable.ic_menu_preferences, "Настройки", settingsPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}