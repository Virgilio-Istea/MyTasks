package com.istea.mytasks.ui.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.applandeo.materialcalendarview.EventDay
import com.istea.mytasks.R
import com.istea.mytasks.model.Group
import com.istea.mytasks.model.MyEventDay
import com.istea.mytasks.model.Task
import com.istea.mytasks.model.TaskList
import java.util.*
import kotlin.collections.ArrayList


class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView : com.applandeo.materialcalendarview.CalendarView
    private lateinit var arrayTasksList: ArrayList<TaskList>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView = findViewById(R.id.ca_calendar)
        setTodayDateToCalendar()

        val intent = intent
        var taskList = intent.getSerializableExtra("taskList") as ArrayList<TaskList>
        arrayTasksList = taskList

        var events = convertToListOfEvents(arrayTasksList)

        calendarView.setEvents(events as List<EventDay>?)

        calendarView.setOnDayClickListener { eventDay ->
            val clickedDayCalendar = eventDay.calendar
            val day = clickedDayCalendar.get(Calendar.DAY_OF_MONTH)
            val month = clickedDayCalendar.get(Calendar.MONTH)
            val year = clickedDayCalendar.get(Calendar.YEAR)

            val initDay = Date(year - 1900, month, day, 0,0,0)
            val lastDay = Date(year - 1900, month, day, 23,59,59)

            var eventsInDay = returnTasksBetweenDates(initDay, lastDay)

            if(eventsInDay.count() == 0) {
                Toast.makeText(this,"No hay eventos en este dia", Toast.LENGTH_SHORT).show()
            }

            if(eventsInDay.count() > 0) {
                //TODO: Abrir vista de actividades de ese dia (Proximo sprint)
            }

        }


    }

    private fun returnTasksBetweenDates(initDay: Date, lastDay: Date): ArrayList<Task>{

        var listOfTask = ArrayList<Task>()

        for (taskList in arrayTasksList) {
                for (task in taskList.tasks){
                    if(task.dateTask in initDay..lastDay) {
                        listOfTask.add(task)
                    }
                }
            }

        return  listOfTask
    }

    private fun setTodayDateToCalendar() {
        val calendar = Calendar.getInstance()
        calendarView.setDate(calendar)
    }

    private fun createEvent(task: Task): MyEventDay {

        val calendar = Calendar.getInstance()
        calendar.time = task.dateTask

        return MyEventDay(calendar, R.drawable.ic_arrow_down, task.title, task.descriptionTask)
    }

    private fun convertToListOfEvents(tasksLists: ArrayList<TaskList>): ArrayList<MyEventDay>{

        var listOfEventDays = ArrayList<MyEventDay>()
        for (taskList in tasksLists) {
            for (task in taskList.tasks)
            listOfEventDays.add(createEvent(task))
        }

        return listOfEventDays
    }
}