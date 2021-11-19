package com.istea.mytasks.ui.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.istea.mytasks.R
import com.istea.mytasks.adapter.CalendarTaskAdapter
import com.istea.mytasks.model.TaskList

class CalendarTasksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_tasks)

        val tasks = intent.getSerializableExtra("tasks") as ArrayList<TaskList>
        setearRecycleViewHistorico(tasks)

    }

    @SuppressLint("WrongConstant")
    private fun setearRecycleViewHistorico(tasks:ArrayList<TaskList>) {

        val recycleViewHistorico: RecyclerView = findViewById(R.id.ct_recyclerView)
        recycleViewHistorico.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL,false)

        val adapterHistorico = CalendarTaskAdapter(tasks)

        recycleViewHistorico.adapter=adapterHistorico
    }
}