package com.istea.mytasks.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.istea.mytasks.R
import com.istea.mytasks.adapter.TaskAdapter
import com.istea.mytasks.db.FirebaseHelper
import com.istea.mytasks.model.ExpandableTasks
import com.istea.mytasks.model.Group
import com.istea.mytasks.model.Task
import com.istea.mytasks.model.TaskList
import com.istea.mytasks.ui.create.CreateTaskActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TasksActivity : AppCompatActivity() {

    private lateinit var createActivity: Button
    private lateinit var title: TextView
    private lateinit var firebase : FirebaseHelper
    private lateinit var recycleViewTasks: RecyclerView

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        firebase = FirebaseHelper()

        val group = intent.getSerializableExtra("group") as Group
        val groups = intent.getSerializableExtra("groups") as HashMap<String, Group>

        createActivity = findViewById(R.id.create_activity)
        title = findViewById(R.id.group_title)
        recycleViewTasks = findViewById(R.id.recyclerViewTasks)

        recycleViewTasks.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,false)

        title.text = group.name

        val tasks: ArrayList<Task> = group.tasks

        if (group.name == "Todos"){
            group.tasks = arrayListOf()
            for (groupAux in groups){
                for (task in groupAux.value.tasks){
                    tasks.add(task)
                }
            }
        }

        val taskList = toExpandableList(tasks)

        recycleViewTasks.adapter = TaskAdapter(this, taskList){selectedItem ->
            when(selectedItem){
                is TaskAdapter.ListenerType.SelectTaskListener -> {
                    val intent = Intent(this@TasksActivity, CreateTaskActivity::class.java)
                    intent.putExtra("task", selectedItem.task)
                    intent.putExtra("create", false)
                    startActivity(intent)
                }
                is TaskAdapter.ListenerType.PopupMenuListener -> {
                    showPopup(selectedItem.view, selectedItem.task, group,groups)
                }
                is TaskAdapter.ListenerType.ToggleTaskCompletionListener -> {
                    firebase.toggleTaskCompletion(selectedItem.task)
                    selectedItem.task.status = !selectedItem.task.status
                    refreshActivity(group,groups)
                }
            }
        }

        createActivity.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            intent.putExtra("group", group)
            startActivity(intent)
        }
    }

    private fun toExpandableList(tasks : ArrayList<Task>) : ArrayList<ExpandableTasks>{
        val expandableModel = arrayListOf<ExpandableTasks>()
        val taskListUndone = arrayListOf<Task>()
        val taskListDone = arrayListOf<Task>()

        for (task in tasks){
            when (task.status){
                false -> taskListUndone.add(task)
                true -> taskListDone.add(task)
            }
        }

        expandableModel.add(
                ExpandableTasks(ExpandableTasks.PARENT, TaskList.Completed(false,taskListUndone))
        )
        expandableModel.add(
                ExpandableTasks(ExpandableTasks.PARENT, TaskList.Completed(true,taskListDone))
        )

        return expandableModel
    }

    private fun showPopup(v: View, task: Task, group : Group, groups : HashMap<String, Group>) {
        PopupMenu(this, v).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_modify -> {
                        val intent = Intent(this@TasksActivity, CreateTaskActivity::class.java)
                        intent.putExtra("task", task)
                        intent.putExtra("create", false)
                        startActivity(intent)
                        true
                    }
                    R.id.action_delete -> {
                        firebase.deleteTask(task, task.groupId)
                        group.tasks.remove(task)
                        groups[group.documentId] = group
                        refreshActivity(group,groups)
                        true
                    }
                    else -> false
                }
            }
            inflate(R.menu.item_menu)
            show()
        }
    }

    private fun refreshActivity(group : Group, groups : HashMap<String, Group>){
        finish()
        //TODO: keep opened lists open
        overridePendingTransition(0, 0)
        val intent = Intent(this, TasksActivity::class.java)
        intent.putExtra("groups", groups)
        intent.putExtra("group", group)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}