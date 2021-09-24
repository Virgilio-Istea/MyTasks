package com.istea.mytasks.ui.create

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.istea.mytasks.R
import com.istea.mytasks.db.FirebaseHelper
import com.istea.mytasks.model.Group
import com.istea.mytasks.ui.view.MainActivity

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var titleGroup : EditText
    private lateinit var createGroup : Button
    private lateinit var back : Button

    private lateinit var firebase : FirebaseHelper

    private lateinit var grupo : Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        initializeFields()

        val create = intent.getBooleanExtra("create", true)

        grupo = Group("","","", arrayListOf())

        if (!create){
            grupo = intent.getSerializableExtra("group") as Group

            if (grupo.name.isNotEmpty()){
                titleGroup.setText(grupo.name)
                createGroup.text = getString(R.string.modificar_grupo)
            }
        }

        createGroup.setOnClickListener {
            if (create){
            val group = Group("",
                    Firebase.auth.currentUser!!.uid,
                    titleGroup.text.toString(),
                    arrayListOf())
            firebase.createGroup(group)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            }
            else{
                firebase.modifyGroup(
                        Group(grupo.documentId,
                              grupo.userId,
                              titleGroup.text.toString(),
                              grupo.tasks))
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        back.setOnClickListener {
            finish()
        }

    }

    private fun initializeFields(){
        titleGroup = findViewById(R.id.ta_et_group_titulo)
        createGroup = findViewById(R.id.ta_bt_createGroup)
        back = findViewById(R.id.ta_bt_back)

        firebase = FirebaseHelper()
    }

}