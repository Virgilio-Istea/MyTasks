package com.istea.mytasks.model

import java.util.*
import java.io.Serializable

data class Task(
        val userId: String,
        val title: String,
        val dateTask : Date,
        val descriptionTask : String,
        val dateReminder : Date,
        val activityGroup : String,
        val done : Boolean,
        val documentId : String
) : Serializable
