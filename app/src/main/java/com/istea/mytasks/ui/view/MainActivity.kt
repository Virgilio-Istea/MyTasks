package com.istea.mytasks.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.istea.mytasks.R
import com.istea.mytasks.adapter.GroupAdapter
import com.istea.mytasks.db.FirebaseHelper
import com.istea.mytasks.model.Group
import com.istea.mytasks.ui.create.CreateGroupActivity
import com.istea.mytasks.ui.create.CreateTaskActivity

class MainActivity : AppCompatActivity() {

    private lateinit var createActivity: Button
    private lateinit var createGroup: Button
    private lateinit var logout : Button
    private lateinit var firebase : FirebaseHelper
    private lateinit var recycleViewReport: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createActivity = findViewById(R.id.create_activity)
        createGroup = findViewById(R.id.create_group)
        logout = findViewById(R.id.logout)

        firebase = FirebaseHelper()

        recycleViewReport = findViewById(R.id.recyclerViewGroups)

        recycleViewReport.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,false)

        firebase.getGroupsByUser(firebase.getUser())

        val items = ArrayList<Group>()

        firebase.groupsResult.observe(this, {
            items.add(Group("",firebase.getUser(),"Todos"))
            items.add(Group("",firebase.getUser(),"Sin Grupo"))
            for(group in it){
                items.add(Group(group.id,firebase.getUser(),group.data["name"].toString()))
            }

            recycleViewReport.adapter = GroupAdapter(items) { selectedButton ->
                when(selectedButton) {
                    is GroupAdapter.ListenerType.SelectGroupListener -> {
                        val intent = Intent(this, TasksActivity::class.java)
                        intent.putExtra("group", selectedButton.group)
                        startActivity(intent)
                        }
                    is GroupAdapter.ListenerType.PopupMenuListener -> {
                        showPopup(selectedButton.view, selectedButton.group)
                    }
                }

                }
        })

        createActivity.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            intent.putExtra("group", Group("","",""))
            startActivity(intent)
        }

        createGroup.setOnClickListener {
            val intent = Intent(this, CreateGroupActivity::class.java)
            startActivity(intent)
        }

        logout.setOnClickListener {
            Toast.makeText(applicationContext, "Logged Out.", Toast.LENGTH_LONG).show()
            firebase.logout()
            finish()
        }
    }

    private fun showPopup(v: View, group: Group) {
        PopupMenu(this, v).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_modify -> {
                        val intent = Intent(this@MainActivity, CreateGroupActivity::class.java)
                        intent.putExtra("group", group)
                        intent.putExtra("create", false)
                        startActivity(intent)
                        true
                    }
                    R.id.action_delete -> {
                        firebase.deleteGroup(group)
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