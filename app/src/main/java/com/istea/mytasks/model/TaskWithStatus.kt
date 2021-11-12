package com.istea.mytasks.model

import java.util.*
import java.io.Serializable

data class TaskWithStatus(
        val title: String,
        var dateTask : Date,
        val descriptionTask : String,
        var dateReminder : Date?,
        var reminderId : String?,
        var groupId : String,
        var status : String
) : Serializable
