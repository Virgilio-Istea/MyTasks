package com.istea.mytasks.ui.create

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.MotionEvent
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.istea.mytasks.R
import com.istea.mytasks.db.FirebaseHelper
import com.istea.mytasks.model.Group
import com.istea.mytasks.model.Task
import com.istea.mytasks.model.TaskViewModelFactory
import com.istea.mytasks.ui.view.MainActivity
import com.istea.mytasks.ui.viewmodel.TaskViewModel
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CreateTaskActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

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
    private lateinit var guardarVoz : Button

    private lateinit var firebase : FirebaseHelper

    private lateinit var grupoId : String
    private lateinit var status : String
    private lateinit var task : Task

    private var mRecorder = MediaRecorder()

    @SuppressLint("ClickableViewAccessibility")
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

        grupoId = ""
        task = Task("", Calendar.getInstance().time, "", Calendar.getInstance().time, "","")

        initializeFields()
        giveMicStoragePermissions()

        if (create){
            grupoId = (intent.getSerializableExtra("group") as Group).documentId
        }
        if (!create){
            task = intent.getSerializableExtra("task") as Task
            grupoId = intent.getStringExtra("group").toString()
            status = intent.getStringExtra("status").toString()

            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val hourFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

            if (task.title.isNotEmpty()){
                titleTask.setText(task.title)
                dateTask.setText(dateFormatter.format(task.dateTask))
                hourTask.setText(hourFormatter.format(task.dateTask))
                descriptionTask.setText(task.descriptionTask)
                if (task.dateReminder != null){
                    dateReminderTask.setText(dateFormatter.format(task.dateReminder!!))
                    hourReminderTask.setText(hourFormatter.format(task.dateReminder!!))
                    recordar.isChecked = true
                }

                createActivity.text = getString(R.string.modificar_actividad)
            }
        }

        fillDropdownListGroups(grupoId)

        createActivity.setOnClickListener{
            if (create){
                firebase.createTask(
                        createTaskObject(),
                        (activityGroups.selectedItem as Group).documentId, Group.TODO
                )
            }
            else {
                firebase.modifyTask(createTaskObject(),
                        (activityGroups.selectedItem as Group).documentId,
                        status, task, task.groupId, status)
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
            )}

        descriptionTask.doAfterTextChanged {
            taskviewmodel.taskDataChanged(
                    titleTask.text.toString(),
                    descriptionTask.text.toString(),
                    dateTask.text.toString(),
                    hourTask.text.toString(),
                    dateReminderTask.text.toString(),
                    hourReminderTask.text.toString(),
                    recordar.isChecked
            )}

        dateTask.doAfterTextChanged {

            taskviewmodel.taskDataChanged(
                    titleTask.text.toString(),
                    descriptionTask.text.toString(),
                    dateTask.text.toString(),
                    hourTask.text.toString(),
                    dateReminderTask.text.toString(),
                    hourReminderTask.text.toString(),
                    recordar.isChecked
            )}

        hourTask.doAfterTextChanged {

            taskviewmodel.taskDataChanged(
                    titleTask.text.toString(),
                    descriptionTask.text.toString(),
                    dateTask.text.toString(),
                    hourTask.text.toString(),
                    dateReminderTask.text.toString(),
                    hourReminderTask.text.toString(),
                    recordar.isChecked
            )}

        dateReminderTask.doAfterTextChanged {

            taskviewmodel.taskDataChanged(
                    titleTask.text.toString(),
                    descriptionTask.text.toString(),
                    dateTask.text.toString(),
                    hourTask.text.toString(),
                    dateReminderTask.text.toString(),
                    hourReminderTask.text.toString(),
                    recordar.isChecked
            )}

        hourReminderTask.doAfterTextChanged {

                taskviewmodel.taskDataChanged(
                        titleTask.text.toString(),
                        descriptionTask.text.toString(),
                        dateTask.text.toString(),
                        hourTask.text.toString(),
                        dateReminderTask.text.toString(),
                        hourReminderTask.text.toString(),
                        recordar.isChecked
                )}

        recordar.setOnCheckedChangeListener { _, _ ->

            taskviewmodel.taskDataChanged(
                    titleTask.text.toString(),
                    descriptionTask.text.toString(),
                    dateTask.text.toString(),
                    hourTask.text.toString(),
                    dateReminderTask.text.toString(),
                    hourReminderTask.text.toString(),
                    recordar.isChecked
            )

            if (recordar.isChecked) {
                dateReminderTask.isEnabled = true
                hourReminderTask.isEnabled = true
                dateReminderTask.setText(setTodayDate())
            } else {
                dateReminderTask.isEnabled = false
                dateReminderTask.setText("")

                hourReminderTask.isEnabled = false
                hourReminderTask.setText("")
            }
        }

        dateTask.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if(hasFocus) {
                showDatePickerDialog("dateTask")
            }
        }

        dateTask.setOnClickListener {
            showDatePickerDialog("dateTask")
        }

        dateReminderTask.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if(hasFocus) {
                showDatePickerDialog("dateReminderTask")
            }
        }

        dateReminderTask.setOnClickListener {
            showDatePickerDialog("dateReminderTask")
        }

        guardarVoz.setOnTouchListener(OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopRecording()
                }
            }
            false
        })


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            guardarVoz.isEnabled = true
    }

    private fun giveMicStoragePermissions() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 111)
        } else {
            guardarVoz.isEnabled = true
        }

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),112)
        }

    }

    private fun startRecording() {
        // Byte array for audio record
        val byteArrayOutputStream = ByteArrayOutputStream()

        val descriptors = ParcelFileDescriptor.createPipe()
        val parcelRead = ParcelFileDescriptor(descriptors[0])
        val parcelWrite = ParcelFileDescriptor(descriptors[1])

        val inputStream: InputStream = ParcelFileDescriptor.AutoCloseInputStream(parcelRead)

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mRecorder.setOutputFile(parcelWrite.fileDescriptor)
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mRecorder.prepare()

        mRecorder.start()

        var read: Int
        val data = ByteArray(16384)

        while (inputStream.read(data, 0, data.size).also { read = it } != -1) {
            byteArrayOutputStream.write(data, 0, read)
        }

        byteArrayOutputStream.flush()

        byteArrayOutputStream.toByteArray()
    }

    private fun stopRecording() {
        mRecorder.stop()
        mRecorder.reset()
        mRecorder.release()

    }


    private fun showDatePickerDialog(dateEditText: String) {

        val datePickerDialog = DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.tag = dateEditText

        datePickerDialog.show()
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
        createActivity.isEnabled = false
        dateTask.keyListener = null
        dateReminderTask.keyListener = null
        dateTask.setText(setTodayDate())

        guardarVoz = findViewById(R.id.ta_btn_task_descripcion_audio)
        guardarVoz.isEnabled = false;
    }

    private fun setTodayDate(): String {
        val calendar = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        return dateFormat.format(calendar.time)
    }


    @Suppress("UNCHECKED_CAST")
    private fun fillDropdownListGroups(grupo: String){

        firebase.getGroupsByUser()

        val items = ArrayList<Group>()

        firebase.groupsResult.observe(this, {

            var selectedGroup = Group("", "")
            for (group in it.data?.get("groups") as ArrayList<HashMap<String, String>>) {
                for (value in group) {
                    items.add(
                            Group(
                                    value.value,
                                    value.key
                            )
                    )
                    if (grupo == value.value) {
                        selectedGroup = Group(
                                value.value,
                                value.key
                        )
                    }
                }
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

            activityGroups.adapter = adapter
            if (selectedGroup.name.isNotEmpty()) {
                activityGroups.setSelection(items.indexOf(selectedGroup))
            }
        })
    }

    private fun createTaskObject() : Task{
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val dateTask: Date = formatter.parse("${dateTask.text} ${hourTask.text}")!!
        val dateReminder : Date? = if (recordar.isChecked){
            formatter.parse("${dateReminderTask.text} ${hourReminderTask.text}")!!
        }
        else{
            null
        }

        return Task(
                titleTask.text.toString(),
                dateTask,
                descriptionTask.text.toString(),
                dateReminder,
               "",
                (activityGroups.selectedItem as Group).toId()
        )
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val correctMonth = month + 1
        val date = "$dayOfMonth/$correctMonth/$year"

        if(view?.tag == "dateTask") {
            dateTask.setText(date)

        } else if(view?.tag == "dateReminderTask") {
            dateReminderTask.setText(date)
        }
    }

}
