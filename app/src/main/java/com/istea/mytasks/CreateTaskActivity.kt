package com.istea.mytasks

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity


class CreateTaskActivity : AppCompatActivity() {

    lateinit var titleTask : EditText
    lateinit var dateTask : EditText
    lateinit var hourTask : EditText
    lateinit var descriptionTask : EditText
    lateinit var dateReminderTask : EditText
    lateinit var hourReminderTask : EditText
    lateinit var activityGroups : Spinner
    lateinit var createActivity : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        initializeFields()
        fillDropdownListGroups()

        createActivity.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initializeFields(){
        titleTask = findViewById(R.id.ta_et_task_titulo)
        dateTask = findViewById(R.id.ta_et_task_fecha)
        hourTask = findViewById(R.id.ta_et_task_hora)
        descriptionTask = findViewById(R.id.ta_et_task_descripcion)
        dateReminderTask = findViewById(R.id.ta_et_task_fecha_recordatorio)
        hourReminderTask = findViewById(R.id.ta_et_task_hora_recordatorio)
        activityGroups = findViewById(R.id.ta_sp_groups)
        createActivity = findViewById(R.id.ta_bt_createTask)
    }

    private fun fillDropdownListGroups(){

        val items = arrayOf("My Task Group", "Study Tasks", "Java Class Group")

        val adapter: ArrayAdapter<Any?> = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_dropdown_item, items)

        activityGroups.setAdapter(adapter)
    }


}