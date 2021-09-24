package com.istea.mytasks.model

import java.util.*
import java.io.Serializable

data class Task(
        val userId: String,
        val title: String,
        var dateTask : Date,
        val descriptionTask : String,
        var dateReminder : Date?,
        var status : Boolean,
        val groupId : String
) : Serializable
