package com.istea.mytasks.model

import java.io.Serializable

data class TaskList(
        val status: String,
        val tasks: ArrayList<Task>
    ) : Serializable
