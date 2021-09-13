package com.istea.mytasks.db

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
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

    private val _tasksResult = MutableLiveData<QuerySnapshot>()
    val tasksResult: LiveData<QuerySnapshot> = _tasksResult

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

    fun getTasksByUser(user: String, group: String){
        db = Firebase.firestore
        val tasksDB = db.collection("tasks")
                .whereEqualTo("userId",user)

        if (group != "Todos") {
            tasksDB.whereEqualTo("activityGroups", group)
        }

        // Create a query against the collection.
        tasksDB.get()
            .addOnSuccessListener { documents ->
                _tasksResult.value = documents
            }
            .addOnFailureListener { exception ->
                Log.w("", "Error getting documents: ", exception)
            }
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
        db.collection("groups").add(group)
            .addOnSuccessListener { document ->
                db.collection("groups").document(document.id).set(hashMapOf(
                        "documentId" to document.id), SetOptions.merge())
            }
            .addOnFailureListener { e ->
                Log.w("", "Error adding document", e)
            }
    }

    fun deleteGroup(group : Group){
        db = Firebase.firestore

        modifyTasksGroup(group)

        db.collection("groups").document(group.documentId).delete()

    }

    fun modifyTasksGroup(group : Group) {
        db = Firebase.firestore
        val tasksDB = db.collection("tasks")
                .whereEqualTo("userId",group.userId)
                .whereEqualTo("activityGroup", group.name)

        tasksDB.get()
                .addOnSuccessListener { documents ->
                    for (document in documents){
                        db.collection("tasks").document(document.id).set(hashMapOf(
                                "activityGroup" to ""), SetOptions.merge())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("", "Error getting documents: ", exception)
                }

    }

    fun modifyGroup(group : Group) {
        db = Firebase.firestore
        db.collection("groups").document(group.documentId).set(group)
    }

    fun createTask(task: Task){
        db = Firebase.firestore
        db.collection("tasks").add(task)
                .addOnSuccessListener { document ->
                    db.collection("tasks").document(document.id).set(hashMapOf(
                            "documentId" to document.id), SetOptions.merge())
                }
                .addOnFailureListener { e ->
                    Log.w("", "Error adding document", e)
                }
    }

    fun deleteTask(task : Task){
        db = Firebase.firestore
        db.collection("tasks").document(task.documentId).delete()
    }

    fun modifyTask(task : Task){
        db = Firebase.firestore
        db.collection("tasks").document(task.documentId).set(task)
    }

    fun toggleTaskCompletion(task : Task) {
        db = Firebase.firestore

        db.collection("tasks").document(task.documentId).set(hashMapOf(
                "done" to !task.done), SetOptions.merge())

    }


    fun logout(){
        Firebase.auth.signOut()
        _userResult.value = false
         Log.w("FirebaseAuth", "Logged Out")
    }
}