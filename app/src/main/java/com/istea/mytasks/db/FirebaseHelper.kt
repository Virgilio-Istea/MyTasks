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
import com.istea.mytasks.model.TaskList
import com.istea.mytasks.model.User

class FirebaseHelper {

    private var db : FirebaseFirestore
    private var settings : FirebaseFirestoreSettings

    private val _userResult = MutableLiveData<Boolean>()
    val userResult: LiveData<Boolean> = _userResult

    private val _groupsResult = MutableLiveData<DocumentSnapshot>()
    val groupsResult: LiveData<DocumentSnapshot> = _groupsResult

    private val _tasksResult = MutableLiveData<QuerySnapshot>()
    val tasksResult: LiveData<QuerySnapshot> = _tasksResult

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

    fun getGroupsByUser(){
        db = Firebase.firestore
        val groupsDB = db.collection("users").document(getUser())

        // Create a query against the collection.
        groupsDB.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        _groupsResult.value = document
                    }
                    else {
                        groupsDB.set(User(getUser(),Firebase.auth.currentUser!!.displayName!!,
                                arrayListOf()))
                        createNoGroup()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("", "Error getting documents: ", exception)
                }
    }

    fun createGroup(group : Group){
        db = Firebase.firestore
        val newGroup = db.collection("users").document(getUser())
                .collection("groups").document()
        val userDB = db.collection("users").document(getUser())
        group.documentId = newGroup.id
        newGroup.set(group)
            .addOnSuccessListener {
                userDB.update("groups", FieldValue.arrayUnion(mapOf(group.name to newGroup.id)))
                        .addOnSuccessListener {
                            getGroupsByUser()
                        }
                newGroup.collection(Group.TODO).document("0")
                        .set(TaskList(Group.TODO, arrayListOf()))
                newGroup.collection(Group.DONE).document("0")
                        .set(TaskList(Group.DONE, arrayListOf()))
            }
            .addOnFailureListener { e ->
                Log.w("", "Error adding document", e)
            }
    }

    fun createNoGroup(){
        val group = Group("","Sin Grupo")
        createGroup(group)
    }

    fun deleteGroup(group : Group, deleteTasks : Boolean, newGroup : Group){
        db = Firebase.firestore

        if (!deleteTasks){
            modifyTasksGroup(group, newGroup)
        }

        db.collection("users").document(getUser())
                .update("groups", FieldValue.arrayRemove(mapOf(group.name to group.documentId)))

    }

    private fun modifyTasksGroup(group : Group, newGroup : Group) {
        db = Firebase.firestore

        val groupDB = db.collection("users")
                .document(getUser())
                .collection("groups")
                .document(group.documentId)
                .collection("tasks")

        val newGroupDB = db.collection("users")
                .document(getUser())
                .collection("groups")
                .document(newGroup.documentId)
                .collection("tasks")

        groupDB.get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    newGroupDB.document(document.id).update("tasks",
                            FieldValue.arrayUnion(document.data["tasks"]))
                    groupDB.document(document.id).delete()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("", "Error getting documents: ", exception)
            }
    }

    fun modifyGroup(group : Group, oldGroup : Group) {
        db = Firebase.firestore
        db.collection("users").document(getUser())
                .collection("groups")
                .document(group.documentId)
                .set(group)

        db.collection("users").document(getUser())
                .update("groups", FieldValue.arrayRemove(mapOf(oldGroup.name to oldGroup.documentId)))

        db.collection("users").document(getUser())
                .update("groups", FieldValue.arrayUnion(mapOf(group.name to group.documentId)))



    }

    fun getTasks(){
        db = Firebase.firestore
        val groupsDB = db.collection("users").document(getUser())

        // Create a query against the collection.
        groupsDB.get()
                .addOnSuccessListener { document ->
                    var groupsDoc = document.data?.get("groups") as ArrayList<HashMap<String, String>>

                    for (groups in groupsDoc){
                        for (group in groups){
                            var groupAux = Group(group.value, group.key)
                            getTasksByGroup(groupAux)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("", "Error getting documents: ", exception)
                }
    }

    fun getTasksByGroup(group : Group){
        db = Firebase.firestore
        val groupsDB = db.collection("users").document(getUser())
                .collection("groups").document(group.documentId)

        // Create a query against the collection.
        groupsDB.collection(Group.DONE).get()
                .addOnSuccessListener { document ->
                   _tasksResult.value = document
                }
                .addOnFailureListener { exception ->
                    Log.w("", "Error getting documents: ", exception)
                }

        groupsDB.collection(Group.TODO).get()
                .addOnSuccessListener { document ->
                    _tasksResult.value = document
                }
                .addOnFailureListener { exception ->
                    Log.w("", "Error getting documents: ", exception)
                }
    }

    fun createTask(task: Task, group: String, status : String){
        db = Firebase.firestore
        val taskDB = db.collection("users").document(getUser())
                .collection("groups").document(group)
                .collection(status).document("0")

        taskDB.update("tasks",FieldValue.arrayUnion(task))
    }

    fun deleteTask(task : Task, group: String, status : String){
        db = Firebase.firestore

        val taskDB = db.collection("users").document(getUser())
                .collection("groups").document(group)
                .collection(status).document("0")

        taskDB.update("tasks",FieldValue.arrayRemove(task))
    }

    fun modifyTask(newTask : Task, newGroup : String, newStatus : String, oldTask : Task, oldGroup : String, oldStatus : String){
        deleteTask(oldTask, oldGroup, oldStatus)
        createTask(newTask, newGroup, newStatus)
    }

    fun toggleTaskCompletion(task : Task, newStatus: String, OldStatus : String) {
        val toggledTask = Task(task.title,task.dateTask,task.descriptionTask,task.dateReminder, task.groupId)

        modifyTask(toggledTask, task.groupId, newStatus, task, task.groupId, OldStatus)
    }
}