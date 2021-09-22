package com.istea.mytasks.model
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.istea.mytasks.ui.viewmodel.TaskViewModel

class TaskViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
