package com.istea.mytasks.adapter

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.istea.mytasks.R
import com.istea.mytasks.model.Group
import com.istea.mytasks.model.Task
import com.istea.mytasks.model.TaskList
import com.istea.mytasks.model.TaskWithStatus
import java.text.SimpleDateFormat
import java.util.*

class CalendarTaskAdapter(private val dataset: ArrayList<TaskList>) : RecyclerView.Adapter<CalendarTaskAdapter.ViewHolder>(){
    var tasks = ArrayList<TaskWithStatus>()
    class ViewHolder(view: View):RecyclerView.ViewHolder(view){

        val title: TextView
        var dateTask : TextView
        val descriptionTask : TextView
        var dateReminder : TextView
        var status : TextView
        var soundDescription : Button

        init {
            title = view.findViewById(R.id.ta_i_title)
            dateTask = view.findViewById(R.id.ta_i_hora)
            descriptionTask = view.findViewById(R.id.ta_i_description)
            dateReminder = view.findViewById(R.id.ta_i_recordatorio)
            status = view.findViewById(R.id.ta_i_status)
            soundDescription = view.findViewById(R.id.ta_i_playDescription)
        }
    }

    override fun getItemCount(): Int {
        var count = 0
        for (tasklist in dataset){
            count += tasklist.tasks.count()
        }
        return count
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)

        for (tasklist in dataset){
            for (task in tasklist.tasks){
                tasks.add( TaskWithStatus(task.title,
                    task.dateTask,
                    task.descriptionTask,
                    task.dateReminder,
                    task.reminderId,
                    task.groupId,
                    tasklist.status,
                    task.voicePathFile))
            }
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeStampFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val GetDate = tasks[position].dateTask
        val dateTaskStr: String = timeStampFormat.format(GetDate)
        var dateTaskReminderStr = ""

        if(tasks[position].dateReminder != null) {
            val GetDateReminder = tasks[position].dateReminder
            dateTaskReminderStr = timeStampFormat.format(GetDateReminder)
        }


        holder.soundDescription.isVisible = false

        if(tasks[position].soundFile != ""){
            holder.soundDescription.isVisible = true

            holder.soundDescription.setOnClickListener(){
                var mp = MediaPlayer()
                mp.setDataSource(tasks[position].soundFile)
                mp.prepare()
                mp.start()
            }
        }

        holder.title.text = tasks[position].title
        holder.dateTask.text = dateTaskStr
        holder.descriptionTask.text = tasks[position].descriptionTask
        holder.dateReminder.text = dateTaskReminderStr
        when (tasks[position].status){
            Group.DONE -> holder.status.text = "Listo"
            Group.TODO -> holder.status.text = "Para Hacer"
        }

    }


}