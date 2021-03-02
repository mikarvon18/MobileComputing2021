package com.example.lab2

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(appContext:Context, workerParameters: WorkerParameters) :
        Worker(appContext,workerParameters) {

    override fun doWork(): Result {
        val text = inputData.getString("message") // this comes from the reminder parameters
        Log.d("Lab", "text $text")
        MenuActivity.showNofitication(applicationContext,text!!)
        Log.d("Lab", "ReminderWorker:Notify!")
        return   Result.success()
    }
}