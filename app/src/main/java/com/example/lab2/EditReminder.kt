package com.example.lab2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.*
import androidx.room.Room
import com.example.lab2.db.PaymentInfo
import com.example.lab2.db.AppDatabase
import java.util.*


//https://developer.android.com/training/basics/firstapp/starting-activity
class EditReminder : AppCompatActivity() {
    var msgUid: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_reminder)
        val datePicker = findViewById<DatePicker>(R.id.editReminderDatePicker)

        msgUid = intent.getStringExtra("EXTRA_UID").toInt()
        val msgTitle = intent.getStringExtra("EXTRA_TITLE")
        val msgDate = intent.getStringExtra("EXTRA_DATE")
        val msgLocationX = intent.getStringExtra("EXTRA_LOCATION_X")
        val msgLocationY = intent.getStringExtra("EXTRA_LOCATION_Y")
        Log.d("Lab", " X: $msgLocationX, Y: $msgLocationY")
        val position = intent.getStringExtra("EXTRA_POSITION").toInt()
        val titleTextView = findViewById<TextView>(R.id.editReminderTitle)
        var dateTextView = findViewById<TextView>(R.id.editReminderDateDisplay)
        val locationXTextView = findViewById<TextView>(R.id.editReminderLocationX)
        val locationYTextView = findViewById<TextView>(R.id.editReminderLocationY)
        titleTextView.text = msgTitle.toString()
        val tempLocX = msgLocationX.toInt()

        locationXTextView.text = Integer.toString(tempLocX)
        locationYTextView.text = msgLocationY.toString()


        AsyncTask.execute {
            val db = Room
                    .databaseBuilder(
                            applicationContext,
                            AppDatabase::class.java,
                            getString(R.string.dbFileName)
                    )
                    .build()
            val paymentInfo = db.paymentDao().getPaymentInfo(msgUid)

            db.close()
            Log.d("Lab", "paymentInfo in onCreate: $paymentInfo")
        }



        Log.d("Lab", "Editing Uid: $msgUid, Title: $msgTitle, Date: $msgDate, X: $msgLocationX, Y: $msgLocationY")
        Log.d("Lab", "POS: $position")
        val dateparts = msgDate.split(".").toTypedArray()
        val origDay = dateparts[0].toInt()
        val origMonth = dateparts[1].toInt()
        val origYear = dateparts[2].toInt()
        Log.d("Lab", "Dateparts: $origYear, $origMonth, $origDay")
        datePicker.updateDate(origYear, origMonth - 1, origDay)
        dateTextView.text = "$origDay.$origMonth.$origYear"

        datePicker.init(
            origYear, origMonth - 1, origDay

        ) { view, year, monthOfYear, dayOfMonth ->
            val month = monthOfYear + 1
            val msg = "$dayOfMonth.$month.$year"

            Toast.makeText(this@EditReminder, msg, Toast.LENGTH_SHORT).show()
            dateTextView.text = msg

        }

        findViewById<Button>(R.id.btnEditReminderDelete).setOnClickListener {
            Log.d("Lab", "Delete Button Clicked")

            AsyncTask.execute {
                val db = Room
                        .databaseBuilder(
                                applicationContext,
                                AppDatabase::class.java,
                                getString(R.string.dbFileName)
                        )
                        .build()
                db.paymentDao().delete(msgUid!!)
                db.close()
            }





            var mainIntent = Intent(applicationContext, MenuActivity::class.java)
            startActivity(mainIntent)
        }
        findViewById<Button>(R.id.btnEditReminderBack).setOnClickListener {
            Log.d("Lab", "Menu Button Clicked")
            var mainIntent = Intent(applicationContext, MenuActivity::class.java)
            startActivity(mainIntent)
        }
        findViewById<Button>(R.id.btnEditReminderApply).setOnClickListener {
            Log.d("Lab", "Apply Button Clicked")
            var mainIntent = Intent(applicationContext, MenuActivity::class.java)
            var newTitleString = titleTextView.text.toString()
            val newDateString = dateTextView.text.toString()
            val newLocationXString = locationXTextView.text.toString()
            val newLocationYString = locationYTextView.text.toString()
            AsyncTask.execute {
                val db = Room
                        .databaseBuilder(
                                applicationContext,
                                AppDatabase::class.java,
                                getString(R.string.dbFileName)
                        )
                        .build()
                db.paymentDao().editTable(newTitleString, newDateString, newLocationXString, newLocationYString, msgUid)
                db.close()
            }
            startActivity(mainIntent)
        }

    }
    /*
    override fun onResume() {
        super.onResume()
        refreshListView()
    }
    private fun refreshListView() {
        var refreshTask = LoadReminderInfoEntry()
        Log.d("Lab", "Refreshing...")
        refreshTask.execute()
    }

    inner class LoadReminderInfoEntry : AsyncTask<String?, String?, List<PaymentInfo>>() {
        override fun doInBackground(vararg params: String?): List<PaymentInfo> {
            val db = Room
                    .databaseBuilder(
                            applicationContext,
                            AppDatabase::class.java,
                            getString(R.string.dbFileName)
                    )
                    .build()
            val paymentInfo = db.paymentDao().getPaymentInfo(msgUid)
            db.close()
            Log.d("Lab", "paymentInfo in LoadReminderInfoEntry: $paymentInfo")
            return paymentInfo
        }

    }

     */
}

