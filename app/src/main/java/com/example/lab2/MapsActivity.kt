package com.example.lab2

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.util.*
import kotlin.random.Random


const val GEOFENCE_RADIUS = 200
const val GEOFENCE_ID = "REMINDER_GEOFENCE_ID"
const val GEOFENCE_EXPIRATION = 10 * 24 * 60 * 60 * 1000 // 10 days
const val GEOFENCE_DWELL_DELAY =  10 * 1000 // 10 secs // 2 minutes
const val GEOFENCE_LOCATION_REQUEST_CODE = 12345
const val LOCATION_REQUEST_CODE = 123
const val CAMERA_ZOOM_LEVEL = 13f
private val TAG: String = MapsActivity::class.java.simpleName
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private  var newLocation = LatLng(65.08238, 25.44262)



    override fun onCreate(savedInstanceState: Bundle?) {
        //FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        findViewById<Button>(R.id.btnMapConfirm).setOnClickListener {
            Log.d("Lab", "Map Confirm button clicked!, location to set: $newLocation")
            //var newReminderIntent = Intent(applicationContext, NewReminder::class.java)
            //startActivity(newReminderIntent)
            //finish()
            //https://stackoverflow.com/questions/2091465/how-do-i-pass-data-between-activities-in-android-application
            //createGeoFence(newLocation, "testi", geofencingClient)
            //scheduleJob()
            val database = Firebase.database
            val reference = database.getReference("reminders")
            val key = reference.push().key
            if (key != null) {
                val reminder = Reminder(key, newLocation.latitude, newLocation.longitude)
                reference.child(key).setValue(reminder)
            }
            //createGeoFence(newLocation, key!!, geofencingClient)
            Log.d("Lab", "newLocation in MapsActivity: $newLocation")

            val intent = Intent()
            val newLocationString = "${newLocation.latitude}, ${newLocation.longitude}"
            intent.putExtra(Intent.EXTRA_TEXT, newLocationString)
            setResult(Activity.RESULT_OK, intent)
            finish()
            //var tempIntent = Intent(applicationContext, NewReminder::class.java)
            //startActivity(tempIntent)

        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        if (!isLocationPermissionGranted()) {
            val permissions = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            ActivityCompat.requestPermissions(
                    this,
                    permissions.toTypedArray(),
                    LOCATION_REQUEST_CODE
            )
        } else {

            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
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
            this.mMap.isMyLocationEnabled = true

            // Zoom to last known location
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    with(mMap) {
                        val latLng = LatLng(it.latitude, it.longitude)
                        moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_LEVEL))
                    }
                } else {
                    with(mMap) {
                        moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                        LatLng(65.01355297927051, 25.464019811372978),
                                        CAMERA_ZOOM_LEVEL
                                )
                        )
                    }
                }
            }
        }
        // Add a marker in Oulu and move the camera
        //oulu = LatLng(65.08238, 25.44262)
        //mMap.addMarker(MarkerOptions().position(newLocation).title("Marker in Oulu"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15f))
        setMapLongClick(mMap)
        setPoiClick(mMap)
        enableMyLocation()
    }
    private fun isLocationPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latlng ->

            map.addMarker(
                    MarkerOptions().position(latlng)
                            .title("Current location")
            ).showInfoWindow()
            map.addCircle(
                    CircleOptions()
                            .center(latlng)
                            .strokeColor(Color.argb(50, 70, 70, 70))
                            .fillColor(Color.argb(70, 150, 150, 150))
                            .radius(GEOFENCE_RADIUS.toDouble())
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, CAMERA_ZOOM_LEVEL))
            newLocation = latlng

            val database = Firebase.database
            val reference = database.getReference("reminders")
            val key = reference.push().key
            if (key != null) {
                val reminder = Reminder(key, latlng.latitude, latlng.longitude)
                reference.child(key).setValue(reminder)
            }
            val context = applicationContext
            val thisContext = this
            //MapsActivity.createGeoFence()
            //createGeoFence(latlng, key!!, geofencingClient, context, thisContext)


        }
    }
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    //@SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
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
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }
    private fun setPoiClick(map: GoogleMap) {
        Log.d("Lab", "In setpoiclick..!")
        map.setOnPoiClickListener { poi ->
            map.addMarker(
                    MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name)
            ).showInfoWindow()

            scheduleJob()
        }
    }
    private fun scheduleJob() {
        Log.d("Lab", "scheduleJob called...")
        val componentName = ComponentName(this, ReminderJobService::class.java)
        val info = JobInfo.Builder(321, componentName)
                .setRequiresCharging(false)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build()

        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled")
        } else {
            Log.d(TAG, "Job scheduling failed")
            scheduleJob()
        }
    }
    private fun cancelJob() {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(321)
        Log.d(TAG, "Job cancelled")
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == GEOFENCE_LOCATION_REQUEST_CODE) {
            if (permissions.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                        this,
                        "This application needs background location to work on Android 10 and higher",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (
                    grantResults.isNotEmpty() && (
                            grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                                    grantResults[1] == PackageManager.PERMISSION_GRANTED)
            ) {
                if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                mMap.isMyLocationEnabled = true
                onMapReady(mMap)
            } else {
                Toast.makeText(
                        this,
                        "The app needs location permission to function",
                        Toast.LENGTH_LONG
                ).show()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (grantResults.isNotEmpty() && grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                            this,
                            "This application needs background location to work on Android 10 and higher",
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    companion object {
        fun createGeoFence(locationString: String, key: String, geofencingClient: GeofencingClient, context: Context, thisContext: Context) {
            Log.d("Lab", "CreateGeoFence called!")
            val dateparts = locationString.split(",").toTypedArray()
            val locLat = dateparts.get(0).toDouble()
            val locLong = dateparts.get(1).toDouble()
            val geofence = Geofence.Builder()
                    .setRequestId(GEOFENCE_ID)
                    //.setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS.toFloat())
                    .setCircularRegion(locLat, locLong, GEOFENCE_RADIUS.toFloat())
                    .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
                    .build()

            val geofenceRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

            val intent = Intent(thisContext, GeofenceReceiver::class.java)
                    .putExtra("key", key)
                    .putExtra("message", "Geofence alert - ${locLat}, ${locLong}")

            val pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            if (ActivityCompat.checkSelfPermission(thisContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
            Log.d("Lab", "addgeofences called")
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MapsActivity,
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

             */
            if (ActivityCompat.checkSelfPermission(thisContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
        }
        fun removeGeofences(context: Context, triggeringGeofenceList: MutableList<Geofence>) {
            val geofenceIdList = mutableListOf<String>()
            for (entry in triggeringGeofenceList) {
                geofenceIdList.add(entry.requestId)
            }
            LocationServices.getGeofencingClient(context).removeGeofences(geofenceIdList)
        }

        fun showNotification(context: Context?, message: String) {
            Log.d("Lab", "In mapsactivity.shownotification")
            val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
            var notificationId = 1589
            notificationId += Random(notificationId).nextInt(1, 30)

            val notificationBuilder = NotificationCompat.Builder(context!!.applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_alarm)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(message)
                    .setStyle(
                            NotificationCompat.BigTextStyle()
                                    .bigText(message)
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.app_name)
                }
                notificationManager.createNotificationChannel(channel)
            }
            Log.d("Lab", "MapsActivity Notification!")
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }

}