package com.istea.mytasks.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.istea.mytasks.R
import com.istea.mytasks.datevalidator.DateValidatorWithDateFormatter
import com.istea.mytasks.model.TaskState
import java.text.SimpleDateFormat
import java.util.*

class TaskViewModel : ViewModel() {

    private val _taskForm = MutableLiveData<TaskState>()
    val taskFormState: LiveData<TaskState> = _taskForm

    private val titleMaxChars = 20
    private val descMaxChars = 300

    fun taskDataChanged(title: String,
                        description: String,
                        dateTime: String,
                        hour: String,
                        dateTimeReminder: String,
                        hourReminder: String,
                        remember: Boolean
                        ) {

        var dataValid = true
        val oldActivityDate = true

        if (!isTitleTaskEmpty(title)) {
            _taskForm.value = TaskState(titleTaskError = R.string.nombre_task_invalido)
            dataValid = false
            }

        if (!isTitleTaskLongMax(title)) {
            _taskForm.value = TaskState(titleTaskError = R.string.largo_task_invalido)
            dataValid = false
        }

        if (!isDescriptionLongMax(description)) {
            _taskForm.value = TaskState(descriptionTaskError = R.string.largo_descripcion_task_invalido)
            dataValid = false
        }

        if(oldActivityDate) {
            if (!isActivityDateValid(dateTime)) {
                _taskForm.value = TaskState(dateTaskError = R.string.fecha_actividad_invalido)
                dataValid = false
            }
        }

        if (!isActivityHourValid(hour)) {
            _taskForm.value = TaskState(hourTaskError = R.string.hora_actividad_invalido)
            dataValid = false
        }

        if(remember) {
            if (!isActivityDateValid(dateTimeReminder)) {
                _taskForm.value = TaskState(dateReminderError = R.string.fecha_actividad_recordatorio_invalido)
                dataValid = false
            }

            if (!isActivityHourValid(hourReminder)) {
                _taskForm.value = TaskState(hourReminderError = R.string.hora_actividad_recordatorio_invalido)
                dataValid = false
            }
        }

        if (!isActivityRememberDateOlderThanActivityDate(dateTime, hour, dateTimeReminder, hourReminder)) {
            _taskForm.value = TaskState(dateReminderError = R.string.fecha_recordatorio_superior_fecha_actividad)
            dataValid = false
        }


        if(dataValid) {
            _taskForm.value = TaskState(isDataValid = true)
        }
        else{
            _taskForm.value = TaskState(isDataValid = false)
        }
    }

    private fun isTitleTaskEmpty(title: String): Boolean {
        if (title.count() > 0) {
            return true
        }

        return false
    }

    private fun isTitleTaskLongMax(title: String): Boolean {
        if (title.count() <= titleMaxChars) {
            return true
        }

        return false
    }

    private fun isDescriptionLongMax(description: String): Boolean {
        if (description.count() <= descMaxChars) {
            return true
        }

        return false
    }

    private fun isActivityDateValid(dateTime: String): Boolean {
        val formatter = SimpleDateFormat("dd/M/yyyy", Locale.ENGLISH)
        val dateValidator = DateValidatorWithDateFormatter(formatter)

        return dateValidator.isValid(dateTime)
    }


    private fun isActivityHourValid(hour: String): Boolean {
        val regex = Regex("([01]?[0-9]|2[0-3]):[0-5][0-9]")

        return regex.matches(hour)
    }

    private fun isActivityRememberDateOlderThanActivityDate(dateTime: String,
                                                            hour: String,
                                                            dateTimeReminder: String,
                                                            hourReminder: String

    ): Boolean {

        if(!isActivityDateValid(dateTime)) {
            return true
        }

        if(!isActivityDateValid(dateTimeReminder)) {
            return true
        }

        if(!isActivityHourValid(hour)) {
            return true
        }

        if(!isActivityHourValid(hourReminder)) {
            return true
        }

        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.ENGLISH)
        val dateTask: Date = formatter.parse("$dateTime $hour")!!
        val dateReminder: Date = formatter.parse("$dateTimeReminder $hourReminder")!!

        if(dateReminder.after(dateTask)) {
            return false
        }

        return true
    }
}