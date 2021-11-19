package com.istea.mytasks.db

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import com.istea.mytasks.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FirebaseHelper {

    private var db : FirebaseFirestore
    private var settings : FirebaseFirestoreSettings

    private val _userResult = MutableLiveData<Boolean>()
    val userResult: LiveData<Boolean> = _userResult

    private val _groupsResult = MutableLiveData<DocumentSnapshot>()
    val groupsResult: LiveData<DocumentSnapshot> = _groupsResult

    private val _tasksResult = MutableLiveData<QuerySnapshot>()
    val tasksResult: LiveData<QuerySnapshot> = _tasksResult

    private lateinit var user : User

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
                        user = User(getUser(),
                                Firebase.auth.currentUser!!.displayName!!,
                                document.data?.get("notificationId") as String,
                                arrayListOf())
                    }
                    else {
                        user = User(getUser(),Firebase.auth.currentUser!!.displayName!!,
                                "", arrayListOf())
                        groupsDB.set(user)
                        createNoGroup()
                    }
                    getNotificationToken()
                }
                .addOnFailureListener { exception ->
                    Log.w("", "Error getting documents: ", exception)
                }
    }

    fun getNotificationToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            if (user.notificationId != token) {
                val userDB = db.collection("users").document(getUser())
                userDB.update("notificationId", token)
                user.notificationId = token
            }
        })
    }

    fun createGroup(group : Group){
        db = Firebase.firestore
        val newGroup = db.collection("users").document(getUser())
                .collection("groups").document()
        val userDB = db.collection("users").document(getUser())
        group.documentId = newGroup.id
        userDB.update("groups", FieldValue.arrayUnion(mapOf(group.name to newGroup.id)))
                .addOnSuccessListener {
                    getGroupsByUser()
                }
        newGroup.set(group)
            .addOnSuccessListener {
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

        //TODO DELETE GROUP COLLECTION

    }

    private fun modifyTasksGroup(group : Group, newGroup : Group) {
        db = Firebase.firestore

        val groupDB = db.collection("users")
            .document(getUser())
            .collection("groups")
            .document(group.documentId)


        val newGroupDB = db.collection("users")
            .document(getUser())
            .collection("groups")
            .document(newGroup.documentId)


        for (status in listOf(Group.TODO, Group.DONE)) {
            groupDB.collection(status).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val tasks = document.data["tasks"] as ArrayList<*>
                        for (task in tasks) {
                            newGroupDB.collection(status).document(document.id).update(
                                "tasks",
                                FieldValue.arrayUnion(task)
                            )
                        }
                        groupDB.collection(status).document(document.id).delete()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("", "Error getting documents: ", exception)
                }
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

    @Suppress("UNCHECKED_CAST")
    fun getTasks(){
        db = Firebase.firestore
        val groupsDB = db.collection("users").document(getUser())

        // Create a query against the collection.
        groupsDB.get()
                .addOnSuccessListener { document ->
                    val groupsDoc = document.data?.get("groups") as ArrayList<HashMap<String, String>>

                    for (groups in groupsDoc){
                        for (group in groups){
                            val groupAux = Group(group.value, group.key)
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

    fun createTask(task: Task, status : String){
        db = Firebase.firestore
        val taskDB = db.collection("users").document(getUser())
                .collection("groups").document(task.groupId)
                .collection(status).document("0")

        if (task.dateReminder != null) {
            val task = createNotification(task)
        }

        //TODO check document size and create a new one in case it is full

        taskDB.update("tasks",FieldValue.arrayUnion(task))
    }

    fun deleteTask(task : Task, status : String){
        db = Firebase.firestore

        val taskDB = db.collection("users").document(getUser())
                .collection("groups").document(task.groupId)
                .collection(status).document("0")

        if (task.dateReminder != null) {
            deleteNotification(task)
        }

        taskDB.update("tasks",FieldValue.arrayRemove(task))
    }

    fun modifyTask(newTask : Task, newStatus : String, oldTask : Task, oldStatus : String){
        deleteTask(oldTask, oldStatus)
        createTask(newTask, newStatus)
    }

    fun toggleTaskCompletion(task : Task, newStatus: String, OldStatus : String) {

        modifyTask(task, newStatus, task, OldStatus)
    }

    private fun createNotification(task : Task):Task{
        db = Firebase.firestore
        val notificationsDB = db.collection("notifications").document(getUser())

        //TODO check document size and create a new one in case it is full

        val notificationId = UUID.randomUUID().toString()
            .replace("-", "").toUpperCase(Locale.ROOT).hashCode()

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy - HH:mm - ", Locale.getDefault())

        val message = mapOf(
            "data" to { mapOf(
                "title" to "Recordatório",
                "message" to dateFormatter.format(task.dateTask) + task.title,
                "isScheduled" to "true",
                "scheduledTime" to task.dateReminder)
        },
            "topic" to getUser()
        )

        notificationsDB.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    notificationsDB.update(
                        "notifications",
                        FieldValue.arrayUnion(
                            Notification(
                                task.title,
                                task.dateTask,
                                task.dateReminder!!,
                                getUser(),
                                notificationId.toString()
                            )
                        )
                    )
                }
                else{
                    val notifications = arrayListOf<Notification>()
                    notifications.add(Notification(
                        task.title,
                        task.dateTask,
                        task.dateReminder!!,
                        getUser(),
                        notificationId.toString()
                    ))
                    notificationsDB.set(NotificationList(notifications))
                }
            }
            .addOnFailureListener { exception ->
                Log.w("", "Error getting documents: ", exception)
            }

        task.reminderId = notificationId.toString()

        return task
    }

    @Suppress("UNCHECKED_CAST")
    private fun deleteNotification(task : Task){
        db = Firebase.firestore
        val notificationsDB = db.collection("notifications").document(getUser())

        notificationsDB.get()
            .addOnSuccessListener { document ->

                val notificationsDoc = document.data?.get("notifications") as ArrayList<HashMap<*,*>>

                for (doc in notificationsDoc){
                    if (doc["reminderId"].toString() == task.reminderId){
                        val notification = Notification(doc["title"].toString(),
                            (doc["dateTask"] as Timestamp).toDate(),
                            (doc["dateReminder"] as Timestamp).toDate(),
                            doc["userId"].toString(),
                            doc["reminderId"].toString())
                        notificationsDB.update(
                                "notifications",
                            FieldValue.arrayRemove( notification ))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("", "Error getting documents: ", exception)
            }

    }

    @Suppress("UNCHECKED_CAST")
    fun deleteNotification(id : String){
        db = Firebase.firestore
        val notificationsDB = db.collection("notifications").document(getUser())

        notificationsDB.get()
            .addOnSuccessListener { document ->

                val notificationsDoc = document.data?.get("notifications") as ArrayList<HashMap<*,*>>

                for (doc in notificationsDoc){
                    if (doc["reminderId"].toString() == id){
                        val notification = Notification(doc["title"].toString(),
                            (doc["dateTask"] as Timestamp).toDate(),
                            (doc["dateReminder"] as Timestamp).toDate(),
                            doc["userId"].toString(),
                            doc["reminderId"].toString())
                        notificationsDB.update(
                            "notifications",
                            FieldValue.arrayRemove( notification ))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("", "Error getting documents: ", exception)
            }

    }
}