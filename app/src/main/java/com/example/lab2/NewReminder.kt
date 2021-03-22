package com.example.lab2

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.lab2.db.AppDatabase
import com.example.lab2.db.PaymentInfo
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_new_reminder.*
import java.util.*
import kotlin.reflect.typeOf


//https://tutorialwing.com/android-datepicker-using-kotlin-example/
class NewReminder : AppCompatActivity() {
    private val SECOND_ACTIVITY_REQUEST_CODE = 0
    private var returnString = ""
    private lateinit var geofencingClient: GeofencingClient
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // get String data from Intent
                if (data != null) {
                    returnString = data.getStringExtra(Intent.EXTRA_TEXT)
                }
                //val newLocation = data.get

                val dateparts = returnString?.split(",")?.toTypedArray()
                val locLat = dateparts?.get(0)?.toFloat()
                val locLong = dateparts?.get(1)?.toFloat()
                val locLatView = findViewById<TextView>(R.id.editTextReminderLocationX)
                locLatView.text = locLat.toString()
                val locLngView = findViewById<TextView>(R.id.editTextReminderLocationY)
                locLngView.text = locLong.toString()

                Log.d("Lab", "Location in NewReminder onActivityResult: $returnString, $locLat, $locLong")


                //Log.d("Lab", "Location in NewReminder onActivityResult: $returnString")
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        geofencingClient = LocationServices.getGeofencingClient(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_reminder)
        // Initialize a new calendar instance
        //val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val textView = findViewById<TextView>(R.id.newReminderTextView)
        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val dateText = findViewById<TextView>(R.id.editTextReminderContent)
        val today = Calendar.getInstance()
        val locationXText = findViewById<TextView>(R.id.editTextReminderLocationX)
        val locationY = findViewById<TextView>(R.id.editTextReminderLocationY)


        //val creatorId = findViewById<TextView>(R.id.creatorId)
        //val reminderSeen = findViewById<TextView>(R.id.reminderSeen)
        //Log.d("Lab", "datePicker: $datePicker")
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
            val locationXString = locationXText.text.toString()
            val curDate = java.util.Calendar.getInstance()
            val curYear = curDate.get(Calendar.YEAR)
            val curMonth = curDate.get(Calendar.MONTH) + 1
            val curDay = curDate.get(Calendar.DAY_OF_MONTH)
            val curHour = (curDate.get(Calendar.HOUR_OF_DAY)) + 2
            val curMinute = curDate.get(Calendar.MINUTE)
            val curSec = curDate.get(Calendar.SECOND)
            //Log.d("Lab", "Aika: $curMonth.$curDay.$curYear")
            //Log.d("Lab", "Title: $titleString Date: $dateString")
            val creationTime = "$curMonth.$curDay.$curYear-$curHour:$curMinute:$curSec"
            val creatorId = "PlaceholderID"
            val reminderSeen = "PlaceHolderReminderSeen"

            var newReminderIntent = Intent(applicationContext, MenuActivity::class.java)

            val paymentInfo = PaymentInfo(
                null,
                title = titleString,
                date = dateString,
                locationX = locationXString,
                locationY = locationY.text.toString(),
                creationTime = creationTime.toString(),
                creatorId = creatorId.toString(),
                reminderSeen = reminderSeen.toString()
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

                //notification will appear at the day of the reminder


                // payment happens in the future set reminder
                if (paymentCalender.timeInMillis > Calendar.getInstance().timeInMillis) {
                    var timeLeft: Long = (paymentCalender.timeInMillis - today.timeInMillis) - 7200000
                    //to make the notification appear at 10am instead of midnight
                    timeLeft += 36000000
                    timeLeft = 10000
                    Log.d("Lab", "Time left: $timeLeft")
                    // payment happens in the future set reminder
                    val message =
                        "Reminder: ${paymentInfo.title}  Date: $dateString"
                    //Log.d("Lab", "Menuactivity.setreminder $message, PaymentCalendar time in millis: ${paymentCalender.timeInMillis}")
                    //paymentCalender.timeInMillis = 100000000
                    //Log.d("Lab", "Menuactivity.setreminder $message, PaymentCalendar time in millis: ${paymentCalender.timeInMillis}")
                    MenuActivity.setReminderWithWorkManager(

                        applicationContext,
                        uuid,
                            timeLeft, //timeLeft,paymentCalender.timeInMillis,
                        message
                    )
                }
            }

            if (paymentCalender.timeInMillis > Calendar.getInstance().timeInMillis) {
                Toast.makeText(
                    applicationContext,
                    "New reminder added!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            val database = Firebase.database
            val reference = database.getReference("reminders")
            val key = reference.push().key
            //createGeoFence(returnString, key!!, geofencingClient)
            //createNewGeoFence(returnString)
            val context = applicationContext
            val thisContext = this
            //MapsActivity.createGeoFence(returnString, key!!, geofencingClient, context, thisContext)
            finish()
            startActivity(newReminderIntent)
        }

        findViewById<Button>(R.id.btnNewReminderBack).setOnClickListener {
            Log.d("Lab", "Reminder Back Button Clicked")
            var newReminderIntent = Intent(applicationContext, MenuActivity::class.java)
            startActivity(newReminderIntent)
        }
        findViewById<Button>(R.id.btnSetLocation).setOnClickListener {
            Log.d("Lab", "Reminder Back Button Clicked")
            var newSetLocationIntent = Intent(applicationContext, MapsActivity::class.java)
            startActivityForResult(newSetLocationIntent, SECOND_ACTIVITY_REQUEST_CODE)
            //startActivity(newSetLocationIntent)
        }
    }

    private fun createNewGeoFence(location: String){
        val dateparts = location.split(",").toTypedArray()
        val locLat = dateparts.get(0).toDouble()
        val locLong = dateparts.get(1).toDouble()
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(locLat, locLong, GEOFENCE_RADIUS.toFloat())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong()) //todo add duration of the notification
            .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()
        val geofenceRequest = GeofencingRequest.Builder().apply{
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent)
        geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d("Lab", "geofencingclient addonssucceslistener....")
                // Geofences added
                // ...
            }
            addOnFailureListener {
                // Failed to add geofences
                // ...
            }
        }
    }


    private fun createGeoFence(location: String, key: String, geofencingClient: GeofencingClient) {
        Log.d("Lab", "CreateGeoFence called!")
        val dateparts = location.split(",").toTypedArray()
        val locLat = dateparts.get(0).toDouble()
        val locLong = dateparts.get(1).toDouble()
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(locLat, locLong, GEOFENCE_RADIUS.toFloat())
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceReceiver::class.java)
            .putExtra("key", key)
            .putExtra("message", "Geofence alert - ${locLat}, ${locLong}")

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    GEOFENCE_LOCATION_REQUEST_CODE
                )
            } else {
                geofencingClient.addGeofences(geofenceRequest, pendingIntent)
                Log.d("Lab", "addgeofences called")
            }
        } else {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
            Log.d("Lab", "addgeofences called")
        }
    }

}