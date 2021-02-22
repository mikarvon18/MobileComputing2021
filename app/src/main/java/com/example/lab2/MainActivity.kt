package com.example.lab2

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private val name = "Mikael"
    lateinit var editTextName: EditText
    lateinit var editTextPw: EditText
    lateinit var stringName: String
    lateinit var stringPw: String
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        val sharedPref: SharedPreferences = getSharedPreferences(name, 0)
        setContentView(R.layout.activity_main)
        editTextName = findViewById(R.id.editUsername)
        editTextPw = findViewById(R.id.editPassword)
        //var editor = sharedPref.edit()
        //editor.putString("Name", "Mikael")
        //editor.putString("Password", "12345")
        //editor.commit()

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            //Log.d("Lab", "Menu Button Clicked")
            var menuIntent = Intent(applicationContext, MenuActivity::class.java)
            stringName = editTextName.text.toString()
            stringPw = editTextPw.text.toString()

            val corrName: String = sharedPref.getString("Name", "a") ?: "Not Set"
            val corrPw: String = sharedPref.getString("Password", "a") ?: "Not Set"

            Log.d("Lab", "corrName: $corrName")
            //Log.d("Lab", "Username: $stringName")
            //Log.d("Lab", "Pw: $stringPw")
            if (corrName == stringName && (corrPw == stringPw)){
                startActivity(menuIntent)
            }else{
                Toast.makeText(this, "Username or password is incorrect, please try again!",Toast.LENGTH_LONG).show()
                //Log.d("Lab", "corrName: $corrName")
            }

        }
    }
}