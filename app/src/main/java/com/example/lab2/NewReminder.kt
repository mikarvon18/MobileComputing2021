package com.example.lab2

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import java.util.Calendar
import java.util.*
import com.example.lab2.db.AppDatabase
import com.example.lab2.db.PaymentInfo


//https://tutorialwing.com/android-datepicker-using-kotlin-example/
class NewReminder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_reminder)
        // Initialize a new calendar instance
        //val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val textView = findViewById<TextView>(R.id.newReminderTextView)
        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val dateText = findViewById<TextView>(R.id.editTextReminderContent)
        val today = Calendar.getInstance()
        val locationX = findViewById<TextView>(R.id.locationX)
        val locationY = findViewById<TextView>(R.id.locationY)
        val creationTime = findViewById<TextView>(R.id.creationTime)
        val creatorId = findViewById<TextView>(R.id.creatorId)
        val reminderSeen = findViewById<TextView>(R.id.reminderSeen)
        Log.d("Lab", "datePicker: $datePicker")
        datePicker.init(
            today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)

        ) { view, year, monthOfYear, dayOfMonth ->
            val month = monthOfYear + 1
            val msg = "$dayOfMonth.$month.$year"

            Toast.makeText(this@NewReminder, msg, Toast.LENGTH_SHORT).show()

            if (textView != null) {
                textView.text = msg
            }
        }

        findViewById<Button>(R.id.btnCreate).setOnClickListener {
            Log.d("Lab", "Create Button Clicked")
            val dateString = textView.text.toString()
            val titleString = dateText.text.toString()
            Log.d("Lab", "Title: $titleString Date: $dateString")
            var newReminderIntent = Intent(applicationContext, ManageReminders::class.java)

            val paymentInfo = PaymentInfo(
                null,
                title = titleString,
                date = dateString,
                location_x = locationX.text.toString(),
                location_y = locationY.text.toString(),
                creation_time = creationTime.text.toString(),
                creator_id = creatorId.text.toString(),
                reminder_seen = reminderSeen.text.toString()
            )


            //convert date  string value to Date format using dd.mm.yyyy
            // here it is asummed that date is in dd.mm.yyyy

            val dateparts = paymentInfo.date.split(".").toTypedArray()
            val paymentCalender = GregorianCalendar(
                dateparts[2].toInt(),
                dateparts[1].toInt() - 1,
                dateparts[0].toInt()
            )



            AsyncTask.execute {
                //save payment to room datbase
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    getString(R.string.dbFileName)
                ).build()
                val uuid = db.paymentDao().insert(paymentInfo).toInt()
                db.close()

                // payment happens in the future set reminder
                if (paymentCalender.timeInMillis > Calendar.getInstance().timeInMillis) {
                    // payment happens in the future set reminder
                    val message =
                        "Reminder: ${paymentInfo.title}  Date: ${paymentInfo.title}"
                    /*
                    MenuActivity.setReminder(
                        applicationContext,
                        uuid,
                        paymentCalender.timeInMillis,
                        message
                    )*/
                }
            }

            if (paymentCalender.timeInMillis > Calendar.getInstance().timeInMillis) {
                Toast.makeText(
                    applicationContext,
                    "New reminder added!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            finish()
            startActivity(newReminderIntent)
        }

        findViewById<Button>(R.id.btnNewReminderBack).setOnClickListener {
            Log.d("Lab", "Reminder Back Button Clicked")
            var newReminderIntent = Intent(applicationContext, ManageReminders::class.java)
            startActivity(newReminderIntent)
        }
    }
}