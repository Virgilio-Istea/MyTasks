package com.istea.mytasks.db

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.istea.mytasks.model.Group
import com.istea.mytasks.model.Task

class FirebaseHelper {

    private var db : FirebaseFirestore
    private var settings : FirebaseFirestoreSettings

    private val _userResult = MutableLiveData<Boolean>()
    val userResult: LiveData<Boolean> = _userResult

    private val _groupsResult = MutableLiveData<QuerySnapshot>()
    val groupsResult: LiveData<QuerySnapshot> = _groupsResult

    init{
        db = Firebase.firestore

        settings = firestoreSettings {
            isPersistenceEnabled = true
            cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
        }
        db.firestoreSettings = settings
    }

    fun getUser() : String{
        return Firebase.auth.currentUser!!.uid
    }

    fun login(idToken: String){
        val tag = "Firebase Login"
        val auth = Firebase.auth
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(tag, "signInWithCredential:success")
                    _userResult.value = true
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(tag, "signInWithCredential:failure", task.exception)
                    _userResult.value = false
                }
            }
    }

    fun logout(){
        Firebase.auth.signOut()
        _userResult.value = false
        Log.w("FirebaseAuth", "Logged Out")
    }

    fun getGroupsByUser(user: String){
        db = Firebase.firestore
        val groupsDB = db.collection("groups")
                .whereEqualTo("userId",user)

        // Create a query against the collection.
        groupsDB.get()
                .addOnSuccessListener { documents ->
                    _groupsResult.value = documents
                }
                .addOnFailureListener { exception ->
                    Log.w("", "Error getting documents: ", exception)
                }
    }

    fun createGroup(group : Group){
        db = Firebase.firestore
        var newGroup = db.collection("groups").document()
        group.documentId = newGroup.id
        newGroup.set(group)
            .addOnSuccessListener { _ ->

            }
            .addOnFailureListener { e ->
                Log.w("", "Error adding document", e)
            }
    }

    fun createNoGroup(user : String){
        val group = Group(user,user,"Sin Grupo", arrayListOf())
        db = Firebase.firestore
        db.collection("groups").document(user).set(group)
    }

    fun deleteGroup(group : Group, deleteTasks : Boolean){
        db = Firebase.firestore

        if (!deleteTasks){
            modifyTasksGroup(group)
        }

        db.collection("groups").document(group.documentId).delete()

    }

    private fun modifyTasksGroup(group : Group) {
        db = Firebase.firestore
        val tasks = group.tasks
        val tasksDB = db.collection("groups").document(group.userId)

        for (task in tasks){
            tasksDB.update("tasks", FieldValue.arrayUnion(task))
        }
    }

    fun modifyGroup(group : Group) {
        db = Firebase.firestore
        db.collection("groups").document(group.documentId).set(group)
    }

    fun createTask(task: Task, group: String){
        db = Firebase.firestore
        val taskDB = db.collection("groups").document(group)

        taskDB.update("tasks",FieldValue.arrayUnion(task))
    }

    fun deleteTask(task : Task, group: String){
        db = Firebase.firestore
        val taskDB = db.collection("groups").document(group)

        taskDB.update("tasks",FieldValue.arrayRemove(task))
    }

    fun modifyTask(newTask : Task, oldTask : Task){
        deleteTask(oldTask, oldTask.groupId)
        createTask(newTask, newTask.groupId)
    }

    fun toggleTaskCompletion(task : Task) {
        val toggledTask = Task(task.userId,task.title,task.dateTask,task.descriptionTask,task.dateReminder,!task.done,task.groupId)

        modifyTask(toggledTask, task)
    }
}