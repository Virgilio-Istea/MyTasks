package com.istea.mytasks.ui.create

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.istea.mytasks.R
import com.istea.mytasks.db.FirebaseHelper
import com.istea.mytasks.model.Group
import com.istea.mytasks.model.Task
import com.istea.mytasks.model.TaskViewModelFactory
import com.istea.mytasks.ui.view.MainActivity
import com.istea.mytasks.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CreateTaskActivity : AppCompatActivity() {

    private lateinit var taskviewmodel: TaskViewModel

    private lateinit var titleTask : EditText
    private lateinit var dateTask : EditText
    private lateinit var hourTask : EditText
    private lateinit var descriptionTask : EditText
    private lateinit var dateReminder : EditText
    private lateinit var hourReminder : EditText
    private lateinit var activityGroups : Spinner
    private lateinit var createActivity : Button

    private lateinit var firebase : FirebaseHelper

    private lateinit var grupo : Group
    private lateinit var task : Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        firebase = FirebaseHelper()


        taskviewmodel = ViewModelProvider(this, TaskViewModelFactory())
                .get(TaskViewModel::class.java)

        taskviewmodel.taskFormState.observe(this@CreateTaskActivity, androidx.lifecycle.Observer {
            val taskState = it ?: return@Observer

            createActivity.isEnabled = taskState.isDataValid

            if (taskState.titleTaskError != null) {
                titleTask.error = getString(taskState.titleTaskError)
            }
        })

        val create = intent.getBooleanExtra("create", true)

        grupo = Group("","","")
        task = Task("","",Calendar.getInstance().time,"",Calendar.getInstance().time,"",false,"")

        initializeFields()

        if (create){
            grupo = intent.getSerializableExtra("group") as Group
        }
        if (!create){
            task = intent.getSerializableExtra("task") as Task

            val dateFormatter = SimpleDateFormat("dd/M/yyyy", Locale.ENGLISH)
            val hourFormatter = SimpleDateFormat("hh:mm", Locale.ENGLISH)

            if (task.title.isNotEmpty()){
                titleTask.setText(task.title)
                dateTask.setText(dateFormatter.format(task.dateTask))
                hourTask.setText(hourFormatter.format(task.dateTask))
                descriptionTask.setText(task.descriptionTask)
                dateReminder.setText(dateFormatter.format(task.dateReminder))
                hourReminder.setText(hourFormatter.format(task.dateReminder))

                grupo = Group(task.activityGroup,"","")
                createActivity.text = getString(R.string.modificar_actividad)
            }
        }

        fillDropdownListGroups(grupo)

        createActivity.setOnClickListener{
            if (create){
                firebase.createTask(createTaskObject(false,""))
            }
            else {
                firebase.modifyTask(createTaskObject(task.done, task.documentId))
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initializeFields(){
        titleTask = findViewById(R.id.ta_et_task_titulo)
        dateTask = findViewById(R.id.ta_et_task_fecha)
        hourTask = findViewById(R.id.ta_et_task_hora)
        descriptionTask = findViewById(R.id.ta_et_task_descripcion)
        dateReminder = findViewById(R.id.ta_et_task_fecha_recordatorio)
        hourReminder = findViewById(R.id.ta_et_task_hora_recordatorio)
        activityGroups = findViewById(R.id.ta_sp_groups)
        createActivity = findViewById(R.id.ta_bt_createTask)
    }

    private fun fillDropdownListGroups(grupo: Group){

        firebase.getGroupsByUser(firebase.getUser())

        val items = ArrayList<Group>()

        firebase.groupsResult.observe(this, {
            items.add(Group("","","Sin Grupo"))

            var selectedGroup = grupo
            for (group in it) {
                items.add(Group(
                        group.data["documentId"].toString(),
                        group.data["userId"].toString(),
                        group.data["name"].toString()))
                if (grupo.documentId == group.data["documentId"].toString()){
                    selectedGroup = Group(
                            group.data["documentId"].toString(),
                            group.data["userId"].toString(),
                            group.data["name"].toString())
                }
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

            activityGroups.adapter = adapter
            if (selectedGroup.name.isNotEmpty()) {
                activityGroups.setSelection(items.indexOf(selectedGroup))
            }
        })
    }

    private fun createTaskObject(done : Boolean,documentId : String) : Task{
        val formatter = SimpleDateFormat("dd/M/yyyy hh:mm", Locale.ENGLISH)

        val dateTask: Date = formatter.parse("${dateTask.text} ${hourTask.text}")!!
        val dateReminder: Date = formatter.parse("${dateReminder.text} ${hourReminder.text}")!!

        return Task(
                Firebase.auth.currentUser!!.uid,
                titleTask.text.toString(),
                dateTask,
                descriptionTask.text.toString(),
                dateReminder,
                (activityGroups.selectedItem as Group).documentId,
                done,
                documentId)
    }
}
