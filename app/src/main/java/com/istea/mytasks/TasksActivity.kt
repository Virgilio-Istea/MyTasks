package com.istea.mytasks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class TasksActivity : AppCompatActivity() {

    private lateinit var createActivity: Button
    private lateinit var title: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        val grupo = intent.getStringExtra("group")

        createActivity = findViewById(R.id.create_activity)
        title = findViewById(R.id.group_title)

        title.text = grupo

        createActivity.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            intent.putExtra("group", grupo)
            startActivity(intent)
        }
    }
}