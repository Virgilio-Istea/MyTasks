package com.istea.mytasks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.istea.mytasks.R
import com.istea.mytasks.model.Task
import java.text.SimpleDateFormat
import java.util.*

class CalendarTaskAdapter(private val dataset: ArrayList<Task>) : RecyclerView.Adapter<CalendarTaskAdapter.ViewHolder>(){

    class ViewHolder(view: View):RecyclerView.ViewHolder(view){

        val title: TextView
        var dateTask : TextView
        val descriptionTask : TextView
        var dateReminder : TextView

        init {
            title = view.findViewById(R.id.ta_i_title)
            dateTask = view.findViewById(R.id.ta_i_hora)
            descriptionTask = view.findViewById(R.id.ta_i_description)
            dateReminder = view.findViewById(R.id.ta_i_recordatorio)
        }
    }

    override fun getItemCount(): Int {
        return dataset.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeStampFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val GetDate = dataset[position].dateTask
        val dateTaskStr: String = timeStampFormat.format(GetDate)
        var dateTaskReminderStr = ""

        if(dataset[position].dateReminder != null) {
            val GetDateReminder = dataset[position].dateReminder
            dateTaskReminderStr = timeStampFormat.format(GetDateReminder)
        }

        holder.title.text = dataset[position].title
        holder.dateTask.text = dateTaskStr
        holder.descriptionTask.text = dataset[position].descriptionTask
        holder.dateReminder.text = dateTaskReminderStr
    }


}