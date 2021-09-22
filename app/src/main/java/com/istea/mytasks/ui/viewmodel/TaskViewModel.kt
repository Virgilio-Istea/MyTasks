package com.istea.mytasks.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.istea.mytasks.R
import com.istea.mytasks.model.TaskState

class TaskViewModel(): ViewModel() {

    private val _taskForm = MutableLiveData<TaskState>()
    val taskFormState: LiveData<TaskState> = _taskForm


    fun taskDataChanged(username: String, password: String) {
        if (!isTitleTaskValid(username)) {
            _taskForm.value = TaskState(titleTaskError = R.string.nombre_task_invalido)
            }
        else{
            _taskForm.value = TaskState(isDataValid = true)
        }
    }

    private fun isTitleTaskValid(title: String): Boolean {
        return (title == "" || title == null)
    }
}