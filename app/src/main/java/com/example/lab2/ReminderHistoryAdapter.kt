package com.example.lab2

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.RequiresApi
import com.example.lab2.db.PaymentInfo
import kotlinx.android.synthetic.main.reminder_item.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class ReminderHistoryAdaptor(context: Context, private  val list:List<PaymentInfo>): BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        val formatted = current.format(formatter)
        //Log.d("Lab", "Current Date is: $formatted")
        val row = inflater.inflate(R.layout.reminder_item, parent, false)

        //set payment info values to the list item
        row.reminderItemTitle.text=list[position].title
        row.reminderItemDate.text=list[position].date
        val tempDate = row.reminderItemDate.text
        val dateParts = tempDate.split(".").toTypedArray()
        //Log.d("Lab", "HistoryAdaptor dateParts: ${dateParts[0]}, ${dateParts[1]}, ${dateParts[2]}, kertoma: ")
        //Log.d("Lab", "HistoryAdaptor itemdate: ${row.reminderItemDate.text}")
        row.reminderItemLocationX.text=list[position].locationX
        row.reminderItemLocationY.text=list[position].locationY
        row.reminderItemCreationTime.text=list[position].creationTime
        row.reminderItemCreatorId.text=list[position].creatorId
        row.reminderItemReminderSeen.text=list[position].reminderSeen
        return row

    }
    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}
