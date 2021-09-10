package com.istea.mytasks.db

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.istea.mytasks.model.LoggedInUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings

class FirebaseHelper {

    private var db : FirebaseFirestore
    private var settings : FirebaseFirestoreSettings

    private val _loginResult = MutableLiveData<LoggedInUser>()
    val loginResult: LiveData<LoggedInUser> = _loginResult

    private val _userResult = MutableLiveData<Boolean>()
    val userResult: LiveData<Boolean> = _userResult

    private val _maxIdResult = MutableLiveData<Int>()
    val maxIdResult: LiveData<Int> = _maxIdResult

    private val _reportResult = MutableLiveData<QuerySnapshot>()
    val reportResult: LiveData<QuerySnapshot> = _reportResult

    init{
        db = Firebase.firestore

        settings = firestoreSettings {
            isPersistenceEnabled = true
            cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
        }
        db.firestoreSettings = settings
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

    fun getTasksByUser(user: Int){
        db = Firebase.firestore
        val userDB = db.collection("tasks").whereEqualTo("userId",user)

        // Create a query against the collection.
        userDB.get()
            .addOnSuccessListener { documents ->
                _reportResult.value = documents
            }
            .addOnFailureListener { exception ->
                Log.w("", "Error getting documents: ", exception)
            }
    }

//    fun createGroup(task : Task){
//        db = Firebase.firestore
//        db.collection("meals").add(meal)
//    }

    fun logout(){
        Firebase.auth.signOut()
        _userResult.value = false
         Log.w("FirebaseAuth", "Logged Out")
    }
}