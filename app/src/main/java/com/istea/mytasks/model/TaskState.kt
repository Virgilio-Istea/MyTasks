package com.istea.mytasks.model

data class TaskState(val titleTaskError: Int? = null,
                     val dateTaskError: Int? = null,
                     val hourTaskError: Int? = null,
                     val descriptionTaskError: Int? = null,
                     val dateReminderError: Int? = null,
                     val hourReminderError: Int? = null,
                     val isDataValid: Boolean = false
                     )