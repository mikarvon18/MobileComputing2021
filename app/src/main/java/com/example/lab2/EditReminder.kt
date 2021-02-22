package com.example.lab2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
//import android.content.Intent.EXTRA_TITLE
import android.util.Log
import android.widget.TextView


//https://developer.android.com/training/basics/firstapp/starting-activity
class EditReminder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_reminder)
        val msgUid = intent.getStringExtra("EXTRA_UID")
        val msgTitle = intent.getStringExtra("EXTRA_TITLE")
        val msgDate = intent.getStringExtra("EXTRA_DATE")
        val msgLocationX = intent.getStringExtra("EXTRA_LOCATION_X")
        val msgLocationY = intent.getStringExtra("EXTRA_LOCATION_Y")


        Log.d("Lab", "Editing Uid: $msgUid, Title: $msgTitle, Date: $msgDate, X: $msgLocationX, Y: $msgLocationY")

        val textView = findViewById<TextView>(R.id.editReminderTitle).apply{
            text = msgTitle
        }
    }
}

