package com.example.lab2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val listView = findViewById<ListView>(R.id.listView)
        val names = arrayOf("Reminder1", "Reminder2", "Reminder3", "Reminder4")

        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                this, android.R.layout.simple_list_item_1, names
        )

        listView.adapter=arrayAdapter
        listView.setOnItemClickListener { adapterView, view, i, l ->
            Toast.makeText(this, "Item Selected " + names[i],Toast.LENGTH_LONG)
                    .show()
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            Log.d("Lab", "Menu Button Clicked")
            var mainIntent = Intent(applicationContext, MainActivity::class.java)
            startActivity(mainIntent)
        }
        findViewById<Button>(R.id.btnUserSettings).setOnClickListener {
            Log.d("Lab", "Menu Button Clicked")
            var mainIntent = Intent(applicationContext, user_settings::class.java)
            startActivity(mainIntent)
        }

    }
}