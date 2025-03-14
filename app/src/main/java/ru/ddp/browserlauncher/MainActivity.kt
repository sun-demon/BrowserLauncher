package ru.ddp.browserlauncher

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var urlEditText: EditText
    private lateinit var timePickerButton: Button
    private lateinit var startServiceButton: Button
    private lateinit var stopServiceButton: Button
    private lateinit var spinnerOptions: Spinner
    private var selectedHour = 0
    private var selectedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlEditText = findViewById(R.id.urlEditText)
        timePickerButton = findViewById(R.id.timePickerButton)
        startServiceButton = findViewById(R.id.startServiceButton)
        stopServiceButton = findViewById(R.id.stopServiceButton)

        val options = arrayOf("Остановить сервис", "Продолжить работу", "Повторить через 5 мин")
        spinnerOptions = findViewById(R.id.spinnerOptions)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOptions.adapter = adapter

        timePickerButton.setOnClickListener { showTimePicker() }
        startServiceButton.setOnClickListener { startBrowserService() }
        stopServiceButton.setOnClickListener { stopBrowserService() }
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(this, { _, hour, minute ->
            selectedHour = hour
            selectedMinute = minute
            timePickerButton.text = String.format("%02d:%02d", hour, minute)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePickerDialog.show()
    }

    private fun startBrowserService() {
        val url = urlEditText.text.toString()
        if (url.isBlank()) {
            Toast.makeText(this, "Введите URL", Toast.LENGTH_SHORT).show()
            return
        }

        val serviceIntent = Intent(this, BrowserService::class.java).apply {
            putExtra("URL", url)
            putExtra("HOUR", selectedHour)
            putExtra("MINUTE", selectedMinute)
            putExtra("ACTION_AFTER_OPEN", spinnerOptions.selectedItem.toString())
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        Toast.makeText(this, "Сервис запущен", Toast.LENGTH_SHORT).show()
    }

    private fun stopBrowserService() {
        val serviceIntent = Intent(this, BrowserService::class.java)
        stopService(serviceIntent)
        Toast.makeText(this, "Сервис остановлен", Toast.LENGTH_SHORT).show()
    }
}