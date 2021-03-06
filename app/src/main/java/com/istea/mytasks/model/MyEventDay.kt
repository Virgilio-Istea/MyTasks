package com.istea.mytasks.model

import com.applandeo.materialcalendarview.EventDay
import java.util.*

class MyEventDay(day: Calendar, imageResource:Int, title: String, description: String, status: String) : EventDay(day, imageResource) {
    val eventTitle = title
    val eventDescription = description
    val status = status
}