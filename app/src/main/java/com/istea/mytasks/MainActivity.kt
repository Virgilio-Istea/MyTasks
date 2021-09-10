package com.istea.mytasks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TableRow
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.istea.mytasks.db.FirebaseHelper

class MainActivity : AppCompatActivity() {

    private lateinit var allTasks: TableRow
    private lateinit var noGroup: TableRow
    private lateinit var createActivity: Button
    private lateinit var createGroup: Button
    private lateinit var logout : Button
    private lateinit var firebase : FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        allTasks = findViewById(R.id.group_all)
        noGroup = findViewById(R.id.group_none)
        createActivity = findViewById(R.id.create_activity)
        createGroup = findViewById(R.id.create_group)
        logout = findViewById(R.id.logout)

        firebase = FirebaseHelper()

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
            Toast.makeText(applicationContext, "Logged Out.", Toast.LENGTH_LONG).show()
            firebase.logout()
            finish()
        }

    }
}