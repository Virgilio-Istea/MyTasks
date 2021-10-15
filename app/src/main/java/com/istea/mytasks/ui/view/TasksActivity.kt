package com.istea.mytasks.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
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
    private lateinit var calendarAcitivtyButton: ImageView

    private lateinit var tasksList : ArrayList<TaskList>

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
        calendarAcitivtyButton = findViewById(R.id.calendar_button)

        recycleViewTasks.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,false)

        title.text = group.name

        if (group.name == "Todos"){
            firebase.getTasks()
        }
        else{
            firebase.getTasksByGroup(group)
        }

        tasksList = arrayListOf()

        firebase.tasksResult.observe(this, {

            for(document in it.documents){
                val taskListAux = arrayListOf<Task>()
                for (task in document.data?.get("tasks") as ArrayList<HashMap<*,*>>){
                    val taskAux = Task(task["title"].toString(),
                            (task["dateTask"] as Timestamp).toDate() ,
                            if (task["description"] != null ){
                                task["description"].toString()}
                            else {""},
                            if (task["dateReminder"] != null){
                                    (task["dateReminder"] as Timestamp).toDate()
                            } else {null},
                            task["groupId"].toString()
                            )
                    taskListAux.add(taskAux)
                }
                tasksList.add(TaskList(document.data?.get("status").toString(),
                        taskListAux))
            }

            val taskExpandableList = toExpandableList(tasksList)

            recycleViewTasks.adapter = TaskAdapter(this, taskExpandableList){selectedItem ->
                when(selectedItem){
                    is TaskAdapter.ListenerType.SelectTaskListener -> {
                        val intent = Intent(this@TasksActivity, CreateTaskActivity::class.java)
                        intent.putExtra("task", selectedItem.task)
                        intent.putExtra("status", selectedItem.status)
                        intent.putExtra("group", selectedItem.task.groupId)
                        intent.putExtra("create", false)
                        startActivity(intent)
                    }
                    is TaskAdapter.ListenerType.PopupMenuListener -> {
                        showPopup(selectedItem.view, selectedItem.task, group, groups, selectedItem.status)
                    }
                    is TaskAdapter.ListenerType.ToggleTaskCompletionListener -> {
                        var status = Group.TODO
                        if (selectedItem.status == Group.TODO){status = Group.DONE}
                        firebase.toggleTaskCompletion(selectedItem.task, status,selectedItem.status)

                        refreshActivity(group,groups)
                    }
                }
            }
        })

        createActivity.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            intent.putExtra("group", group)
            startActivity(intent)
        }

        calendarAcitivtyButton.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)

            intent.putExtra("taskList", tasksList)

            startActivity(intent)
        }
    }

    private fun toExpandableList(taskList : ArrayList<TaskList>) : ArrayList<ExpandableTasks>{
        val expandableModel = arrayListOf<ExpandableTasks>()
        val taskListUndone = arrayListOf<Task>()
        val taskListDone = arrayListOf<Task>()

        for (tasks in taskList){
            when (tasks.status){
                Group.TODO -> taskListUndone.addAll(tasks.tasks)
                Group.DONE -> taskListDone.addAll(tasks.tasks)
            }
        }

        expandableModel.add(
                ExpandableTasks(ExpandableTasks.PARENT, TaskList(Group.TODO,taskListUndone))
        )
        expandableModel.add(
                ExpandableTasks(ExpandableTasks.PARENT, TaskList(Group.DONE,taskListDone))
        )

        return expandableModel
    }

    private fun showPopup(v: View, task: Task, group : Group, groups : HashMap<String, Group>, status : String) {
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
                        firebase.deleteTask(task, group.documentId, status)

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