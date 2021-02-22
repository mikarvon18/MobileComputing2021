package com.example.lab2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.lab2.db.PaymentInfo
import kotlinx.android.synthetic.main.reminder_item.view.*

class ReminderHistoryAdaptor(context: Context, private  val list:List<PaymentInfo>): BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = inflater.inflate(R.layout.reminder_item, parent, false)

        //set payment info values to the list item
        row.reminderItemTitle.text=list[position].title
        row.reminderItemDate.text=list[position].date
        return  row
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
