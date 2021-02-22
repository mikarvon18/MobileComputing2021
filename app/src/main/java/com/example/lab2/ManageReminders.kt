package com.example.lab2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class ManageReminders : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_reminders)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            Log.d("Lab", "Back button Clicked")
            var newReminderIntent = Intent(applicationContext, MenuActivity::class.java)
            startActivity(newReminderIntent)
        }
        findViewById<Button>(R.id.btnNew).setOnClickListener {
            Log.d("Lab", "Back button Clicked")
            var newReminderIntent = Intent(applicationContext, NewReminder::class.java)
            startActivity(newReminderIntent)
        }
    }
}