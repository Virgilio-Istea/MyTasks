package com.istea.mytasks.model

import java.util.*

data class Notification(
        val title: String,
        var dateTask : Date,
        var dateReminder : Date,
        var userId : String,
        var reminderId : String
)
