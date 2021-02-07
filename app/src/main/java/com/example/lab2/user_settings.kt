package com.example.lab2

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText

class user_settings : AppCompatActivity() {
    lateinit var editTextName: EditText
    //testest
    lateinit var editTextPw: EditText
    lateinit var stringName: String
    lateinit var stringPw: String
    private val name = "Mikael"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref: SharedPreferences = getSharedPreferences(name, 0)
        setContentView(R.layout.activity_user_settings)
        //heihehi
        editTextName = findViewById(R.id.editTextUsername)
        editTextPw = findViewById(R.id.editTextTextPassword)
        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            Log.d("Lab", "Menu Button Clicked")
            var mainIntent = Intent(applicationContext, MenuActivity::class.java)
            startActivity(mainIntent)
        }

        findViewById<Button>(R.id.buttonConfirm).setOnClickListener {
            Log.d("Lab", "Menu Button Clicked")
            var mainIntent = Intent(applicationContext, MenuActivity::class.java)
            stringName = editTextName.text.toString()
            stringPw = editTextPw.text.toString()

            Log.d("Lab", "Username: $stringName")
            Log.d("Lab", "Pw: $stringPw")
            var editor = sharedPref.edit()
            editor.putString("Name", stringName)
            editor.putString("Password", stringPw)
            editor.commit()


            startActivity(mainIntent)
        }
    }
}