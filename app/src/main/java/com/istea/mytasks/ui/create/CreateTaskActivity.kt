package com.istea.mytasks.ui.create

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
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
    private lateinit var dateReminderTask : EditText
    private lateinit var hourReminderTask : EditText
    private lateinit var activityGroups : Spinner

    private lateinit var recordar : Switch
    private lateinit var createActivity : Button

    private lateinit var firebase : FirebaseHelper

    private lateinit var grupo : Group
    private lateinit var task : Task

    @RequiresApi(Build.VERSION_CODES.O)
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

            if (taskState.descriptionTaskError != null) {
                descriptionTask.error = getString(taskState.descriptionTaskError)
            }

            if (taskState.dateTaskError != null) {
                dateTask.error = getString(taskState.dateTaskError)
            }

            if (taskState.hourTaskError != null) {
                hourTask.error = getString(taskState.hourTaskError)
            }

            if (taskState.dateReminderError != null) {
                dateReminderTask.error = getString(taskState.dateReminderError)
            }

            if (taskState.hourReminderError != null) {
                hourReminderTask.error = getString(taskState.hourReminderError)
            }

        })

        val create = intent.getBooleanExtra("create", true)

        grupo = Group("", "", "", arrayListOf())
        task = Task("", "", Calendar.getInstance().time, "", Calendar.getInstance().time, false, "")

        initializeFields()

        if (create){
            grupo = intent.getSerializableExtra("group") as Group
        }
        if (!create){
            task = intent.getSerializableExtra("task") as Task

            val dateFormatter = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
            val hourFormatter = SimpleDateFormat("hh:mm", Locale.getDefault())

            if (task.title.isNotEmpty()){
                titleTask.setText(task.title)
                dateTask.setText(dateFormatter.format(task.dateTask))
                hourTask.setText(hourFormatter.format(task.dateTask))
                descriptionTask.setText(task.descriptionTask)
                dateReminderTask.setText(dateFormatter.format(task.dateReminder))
                hourReminderTask.setText(hourFormatter.format(task.dateReminder))

                grupo = Group(task.groupId, "", "", arrayListOf())
                createActivity.text = getString(R.string.modificar_actividad)
            }
        }

        fillDropdownListGroups(grupo)

        createActivity.setOnClickListener{
            if (create){
                firebase.createTask(createTaskObject(false),
                        (activityGroups.selectedItem as Group).documentId)
            }
            else {
                firebase.modifyTask(createTaskObject(task.done), task)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }



        titleTask.doAfterTextChanged {
            taskviewmodel.taskDataChanged(
                    titleTask.text.toString(),
                    descriptionTask.text.toString(),
                    dateTask.text.toString(),
                    hourTask.text.toString(),
                    dateReminderTask.text.toString(),
                    hourReminderTask.text.toString(),
                    recordar.isChecked
            )
        }

        descriptionTask.doAfterTextChanged {
            taskviewmodel.taskDataChanged(
                    titleTask.text.toString(),
                    descriptionTask.text.toString(),
                    dateTask.text.toString(),
                    hourTask.text.toString(),
                    dateReminderTask.text.toString(),
                    hourReminderTask.text.toString(),
                    recordar.isChecked
            )
        }

        dateTask.doAfterTextChanged {
            taskviewmodel.taskDataChanged(
                    titleTask.text.toString(),
                    descriptionTask.text.toString(),
                    dateTask.text.toString(),
                    hourTask.text.toString(),
                    dateReminderTask.text.toString(),
                    hourReminderTask.text.toString(),
                    recordar.isChecked
            )
        }

        hourTask.doAfterTextChanged {
            taskviewmodel.taskDataChanged(
                    titleTask.text.toString(),
                    descriptionTask.text.toString(),
                    dateTask.text.toString(),
                    hourTask.text.toString(),
                    dateReminderTask.text.toString(),
                    hourReminderTask.text.toString(),
                    recordar.isChecked
            )
        }

        dateReminderTask.doAfterTextChanged {

            if(dateReminderTask.text.toString() != ""){
                taskviewmodel.taskDataChanged(
                        titleTask.text.toString(),
                        descriptionTask.text.toString(),
                        dateTask.text.toString(),
                        hourTask.text.toString(),
                        dateReminderTask.text.toString(),
                        hourReminderTask.text.toString(),
                        recordar.isChecked
                )
            } else {
                dateReminderTask.error = null
            }



        }

        hourReminderTask.doAfterTextChanged {

            if(hourReminderTask.text.toString() != ""){
                taskviewmodel.taskDataChanged(
                        titleTask.text.toString(),
                        descriptionTask.text.toString(),
                        dateTask.text.toString(),
                        hourTask.text.toString(),
                        dateReminderTask.text.toString(),
                        hourReminderTask.text.toString(),
                        recordar.isChecked
                )
            } else {
                hourReminderTask.error = null
            }



        }

        recordar.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            taskviewmodel.taskDataChanged(
                    titleTask.text.toString(),
                    descriptionTask.text.toString(),
                    dateTask.text.toString(),
                    hourTask.text.toString(),
                    dateReminderTask.text.toString(),
                    hourReminderTask.text.toString(),
                    recordar.isChecked
            )

            if(recordar.isChecked) {
                dateReminderTask.isEnabled = true
                hourReminderTask.isEnabled = true
            } else {
                dateReminderTask.isEnabled = false
                dateReminderTask.setText("")
                dateReminderTask.error = null

                hourReminderTask.isEnabled = false
                hourReminderTask.setText("")
                hourReminderTask.error = null
            }
        })

    }



    private fun initializeFields(){
        titleTask = findViewById(R.id.ta_et_task_titulo)
        dateTask = findViewById(R.id.ta_et_task_fecha)
        hourTask = findViewById(R.id.ta_et_task_hora)
        descriptionTask = findViewById(R.id.ta_et_task_descripcion)
        dateReminderTask = findViewById(R.id.ta_et_task_fecha_recordatorio)
        hourReminderTask = findViewById(R.id.ta_et_task_hora_recordatorio)
        activityGroups = findViewById(R.id.ta_sp_groups)

        recordar = findViewById(R.id.ta_et_task_recordar)
        createActivity = findViewById(R.id.ta_bt_createTask)
    }

    private fun fillDropdownListGroups(grupo: Group){

        firebase.getGroupsByUser(firebase.getUser())

        val items = ArrayList<Group>()

        firebase.groupsResult.observe(this, {

            var selectedGroup = grupo
            for (group in it) {
                items.add(Group(
                        group.data["documentId"].toString(),
                        group.data["userId"].toString(),
                        group.data["name"].toString(),
                        group.data["tasks"] as ArrayList<Task>))
                if (grupo.documentId == group.data["documentId"].toString()) {
                    selectedGroup = Group(
                            group.data["documentId"].toString(),
                            group.data["userId"].toString(),
                            group.data["name"].toString(),
                            group.data["tasks"] as ArrayList<Task>)
                }
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

            activityGroups.adapter = adapter
            if (selectedGroup.name.isNotEmpty()) {
                activityGroups.setSelection(items.indexOf(selectedGroup))
            }
        })
    }

    private fun createTaskObject(done: Boolean) : Task{
        val formatter = SimpleDateFormat("dd/M/yyyy hh:mm", Locale.ENGLISH)

        val dateTask: Date = formatter.parse("${dateTask.text} ${hourTask.text}")!!
        val dateReminder = formatter.parse("${dateReminderTask.text} ${hourReminderTask.text}")!!


        return Task(
                Firebase.auth.currentUser!!.uid,
                titleTask.text.toString(),
                dateTask,
                descriptionTask.text.toString(),
                dateReminder,
                done,
                (activityGroups.selectedItem as Group).documentId)
    }
}
