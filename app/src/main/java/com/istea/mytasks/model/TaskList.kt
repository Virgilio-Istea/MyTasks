package com.istea.mytasks.model

data class TaskList (
        val allTasks: List<Completed>
) {
    data class Completed(
            val done: Boolean,
            val tasks: ArrayList<Task>
    )
}
