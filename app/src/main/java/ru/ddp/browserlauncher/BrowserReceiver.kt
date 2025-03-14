package ru.ddp.browserlauncher

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class BrowserReceiver : BroadcastReceiver() {
    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val url = intent.getStringExtra("URL") ?: return
        val actionAfterOpen = intent.getStringExtra("ACTION_AFTER_OPEN") ?: "Остановить сервис"

        Log.d("BrowserReceiver", "Открываем браузер по адресу: $url")

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)

        when (actionAfterOpen) {
            "Остановить сервис" -> {
                Log.d("BrowserReceiver", "Останавливаем сервис")
                context.stopService(Intent(context, BrowserService::class.java))
            }
            "Повторить через 5 мин" -> {
                Log.d("BrowserReceiver", "Повторный запуск через 5 минут")
                val newIntent = Intent(context, BrowserReceiver::class.java).apply {
                    putExtra("URL", url)
                    putExtra("ACTION_AFTER_OPEN", "Остановить сервис")
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context, 1, newIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5 * 60 * 1000, // Через 5 минут
                    pendingIntent
                )
            }
        }
    }
}