package com.example.lab2

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lab2.db.AppDatabase
import com.example.lab2.db.PaymentInfo
import kotlinx.android.synthetic.main.reminder_item.view.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.reflect.typeOf



class MenuActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    var viewAll = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        listView = findViewById<ListView>(R.id.listViewReminder)

        refreshListView()
        /*
        val names = arrayOf("Reminder1", "Reminder2", "Reminder3", "Reminder4")

        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                this, android.R.layout.simple_list_item_1, names
        )

        listView.adapter=arrayAdapter
        listView.setOnItemClickListener { adapterView, view, i, l ->
            Toast.makeText(this, "Item Selected " + names[i],Toast.LENGTH_LONG)
                    .show()
        }
*/
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            Log.d("Lab", "Menu Button Clicked")
            var mainIntent = Intent(applicationContext, MainActivity::class.java)
            startActivity(mainIntent)
        }
        findViewById<Button>(R.id.btnUserSettings).setOnClickListener {
            Log.d("Lab", "Menu Button Clicked")
            var userSettingsIntent = Intent(applicationContext, user_settings::class.java)
            startActivity(userSettingsIntent)
        }
        findViewById<Button>(R.id.btnNewReminder).setOnClickListener {
            Log.d("Lab", "NewReminder Clicked")
            var newReminderIntent = Intent(this, NewReminder::class.java)
            startActivity(newReminderIntent)
        }
        findViewById<Button>(R.id.btnViewAll).setOnClickListener {

            if (viewAll == 0) {
                viewAll = 1
            } else {
                viewAll = 0
            }
            Log.d("Lab", "ViewAll Button Clicked, value: $viewAll")
            refreshListView()
        }
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, id ->
            //retrieve selected Item
            val EXTRA_TITLE = "This is extra title"

            val selectedReminder = listView.adapter.getItem(position) as PaymentInfo
            val msgUid = selectedReminder.uid.toString()
            val msgTitle = selectedReminder.title
            val msgDate = selectedReminder.date
            val msgLocationX = selectedReminder.locationX
            val msgLocationY = selectedReminder.locationY
            val pos = position.toString()

            Log.d("Lab", "Selected: $selectedReminder title: $msgTitle, pos: $pos")
            val editReminderIntent = Intent(this, EditReminder::class.java).apply{
                putExtra("EXTRA_UID", msgUid);
                putExtra("EXTRA_TITLE", msgTitle);
                putExtra("EXTRA_DATE", msgDate);
                putExtra("EXTRA_LOCATION_X", msgLocationX);
                putExtra("EXTRA_LOCATION_Y", msgLocationY);
                putExtra("EXTRA_POSITION", pos);
            }
            //editReminderIntent.putStringArrayListExtra("EXTRA_POSITION", selectedReminder)
            startActivity(editReminderIntent)
        }
    }
    override fun onResume() {
        super.onResume()
        refreshListView()
    }
    private fun refreshListView() {
        var refreshTask = LoadReminderInfoEntries()
        refreshTask.execute()
    }
    inner class LoadReminderInfoEntries : AsyncTask<String?, String?, List<PaymentInfo>>(){
        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String?): List <PaymentInfo> {
            val db = Room
                .databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    getString(R.string.dbFileName)
                )
                .build()

            db.close()
            var current = LocalDateTime.now()
            val currentFormatted = current.format(DateTimeFormatter.ISO_DATE)
            //Log.d("Lab", "current: $current, formatted: $currentFormatted")
            val dateparts = currentFormatted.split("-").toTypedArray()
            val thisYear = dateparts[0].toInt()
            val thisMonth = dateparts[1].toInt()
            val thisDay = dateparts[2].toInt() + 1
            val newDate = SimpleDateFormat("dd-MM-yyyy").parse("$thisDay-$thisMonth-$thisYear")
            //Log.d("Lab", "day: $thisDay, month: $thisMonth, year: $thisYear, date: ${newDate.time}")
            val paymentInfos = db.paymentDao().getPaymentInfos()
            //Log.d("Lab", "paymentInfos in LoadReminderInfoEntries: $paymentInfos")
            //todo: return only infos which have date in the past or the current date
            var newPaymentInfos = listOf<PaymentInfo>()
            //val current = LocalDateTime.now()
            for (item in paymentInfos){
                val itemDateparts = item.date.split(".").toTypedArray()
                val itemDay = itemDateparts[0].toInt()
                val itemMonth = itemDateparts[1].toInt()
                val itemYear = itemDateparts[2].toInt()
                val itemDate = SimpleDateFormat("dd-MM-yyyy").parse("$itemDay-$itemMonth-$itemYear")
                //Log.d("Lab", "newDate: ${newDate.time},  itemDate: ${itemDate.time}, $newDate $itemDate")
                if (itemDate.time < newDate.time) {
                    newPaymentInfos += item
                }
            }
            //Log.d("Lab", "newPaymentInfos:  $newPaymentInfos")
            if (viewAll == 0) {
                return newPaymentInfos
            } else {
                return paymentInfos
            }
            return newPaymentInfos
        }
        override fun onPostExecute(paymentInfos: List<PaymentInfo>?){
            super.onPostExecute(paymentInfos)

            if (paymentInfos != null){
                if (paymentInfos.isNotEmpty()){
                    val adaptor = ReminderHistoryAdaptor(applicationContext, paymentInfos)
                    listView.adapter = adaptor
                } else {
                    listView.adapter = null
                    Toast.makeText(applicationContext, "No items now", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        //val paymenthistoryList = mutableListOf<PaymentInfo>()

        fun showNofitication(context: Context, message: String) {

            val CHANNEL_ID = "BANKING_APP_NOTIFICATION_CHANNEL"
            var notificationId = Random.nextInt(10, 1000) + 5
            // notificationId += Random(notificationId).nextInt(1, 500)

            var notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_money_24)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(CHANNEL_ID)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Notification chancel needed since Android 8
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

            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d("Lab", "Notify!")


        }

        fun setReminderWithWorkManager(
            context: Context,
            uid: Int,
            timeInMillis: Long,
            message: String
        ) {
            Log.d("Lab", "Time in millis: $timeInMillis")
            val reminderParameters = Data.Builder()
                .putString("message", message)
                .putInt("uid", uid)
                .build()

            // get minutes from now until reminder
            var minutesFromNow = 0L
            if (timeInMillis > System.currentTimeMillis())
                Log.d("Lab", "if (timeInMillis > System.currentTimeMillis())")
                minutesFromNow = timeInMillis - System.currentTimeMillis()

            val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(reminderParameters)
                    .setInitialDelay(timeInMillis, TimeUnit.MILLISECONDS)//.setInitialDelay(minutesFromNow, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueue(reminderRequest)
        }

        fun setReminder(context: Context, uid: Int, timeInMillis: Long, message: String) {
            val intent = Intent(context, ReminderReceiver::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("message", message)

            // create a pending intent to a  future action with a uniquie request code i.e uid
            val pendingIntent =
                PendingIntent.getBroadcast(context, uid, intent, PendingIntent.FLAG_ONE_SHOT)

            //create a service to moniter and execute the fure action.
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC, timeInMillis, pendingIntent)
        }

        fun cancelReminder(context: Context, pendingIntentId: Int) {

            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    pendingIntentId,
                    intent,
                    PendingIntent.FLAG_ONE_SHOT
                )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }


}