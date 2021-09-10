package com.istea.mytasks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TableRow
import com.istea.mytasks.data.LoginDataSource
import com.istea.mytasks.data.LoginRepository

class MainActivity : AppCompatActivity() {

    private lateinit var allTasks: TableRow
    private lateinit var noGroup: TableRow
    private lateinit var createActivity: Button
    private lateinit var createGroup: Button
    private lateinit var logout : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        allTasks = findViewById(R.id.group_all)
        noGroup = findViewById(R.id.group_none)
        createActivity = findViewById(R.id.create_activity)
        createGroup = findViewById(R.id.create_group)
        logout = findViewById(R.id.logout)

        allTasks.setOnClickListener {
            val intent = Intent(this, TasksActivity::class.java)
            intent.putExtra("group", "Todos")
            startActivity(intent)
        }

        noGroup.setOnClickListener {
            val intent = Intent(this, TasksActivity::class.java)
            intent.putExtra("group", "Sin Grupo")
            startActivity(intent)
        }

        createActivity.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            startActivity(intent)
        }

        createGroup.setOnClickListener {
            val intent = Intent(this, CreateGroupActivity::class.java)
            startActivity(intent)
        }

        logout.setOnClickListener {
            var loginRepository = LoginRepository(LoginDataSource())
            loginRepository.logout()
            finish()
        }

    }
}