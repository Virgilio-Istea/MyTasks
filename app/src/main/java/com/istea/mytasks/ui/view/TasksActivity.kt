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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.istea.mytasks.R
import com.istea.mytasks.adapter.TaskAdapter
import com.istea.mytasks.db.FirebaseHelper
import com.istea.mytasks.model.ExpandableTasks
import com.istea.mytasks.model.Group
import com.istea.mytasks.model.Task
import com.istea.mytasks.model.TaskList
import com.istea.mytasks.ui.create.CreateGroupActivity
import com.istea.mytasks.ui.create.CreateTaskActivity
import java.util.*
import kotlin.collections.ArrayList

class TasksActivity : AppCompatActivity() {

    private lateinit var createActivity: Button
    private lateinit var title: TextView
    private lateinit var firebase : FirebaseHelper
    private lateinit var recycleViewTasks: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        firebase = FirebaseHelper()

        val grupo = intent.getSerializableExtra("group") as Group

        createActivity = findViewById(R.id.create_activity)
        title = findViewById(R.id.group_title)
        recycleViewTasks = findViewById(R.id.recyclerViewTasks)

        recycleViewTasks.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,false)

        title.text = grupo.name

        firebase.getTasksByUser(firebase.getUser(), grupo.documentId)

        val tasks = arrayListOf<Task>()

        firebase.tasksResult.observe(this, {
            for(task in it){
                if (grupo.name == "Todos" || grupo.documentId == task.data["activityGroup"].toString()){
                    tasks.add(queryToTask(task))
                }
            }

            val taskList = toExpandableList(tasks)

            recycleViewTasks.adapter = TaskAdapter(taskList){selectedItem ->
                when(selectedItem){
                    is TaskAdapter.ListenerType.SelectTaskListener -> {
                        val intent = Intent(this@TasksActivity, CreateTaskActivity::class.java)
                        intent.putExtra("task", selectedItem.task)
                        intent.putExtra("create", false)
                        startActivity(intent)
                    }
                    is TaskAdapter.ListenerType.PopupMenuListener -> {
                        showPopup(selectedItem.view, selectedItem.task)
                    }
                    is TaskAdapter.ListenerType.ToggleTaskCompletionListener -> {
                        firebase.toggleTaskCompletion(selectedItem.task)
                        finish();
                        startActivity(intent);
                    }
                }
            }
        })

        createActivity.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            intent.putExtra("group", grupo)
            startActivity(intent)
        }
    }

    private fun queryToTask(query: QueryDocumentSnapshot) : Task{
        return Task(firebase.getUser(),
                    query.data["title"].toString(),
                    (query.data["dateTask"] as Timestamp).toDate(),
                    query.data["descriptionTask"].toString(),
                    (query.data["dateReminder"] as Timestamp).toDate(),
                    query.data["activityGroup"].toString(),
                    query.data["done"].toString().toBoolean(),
                    query.id)
    }

    private fun toExpandableList(tasks : ArrayList<Task>) : ArrayList<ExpandableTasks>{
        val expandableModel = arrayListOf<ExpandableTasks>()
        val taskListUndone = arrayListOf<Task>()
        val taskListDone = arrayListOf<Task>()

        for (task in tasks){
            when (task.done){
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

    private fun showPopup(v: View, task: Task) {
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
                        firebase.deleteTask(task)
                        finish();
                        startActivity(intent);
                        true
                    }
                    else -> false
                }
            }
            inflate(R.menu.item_menu)
            show()
        }
    }
}