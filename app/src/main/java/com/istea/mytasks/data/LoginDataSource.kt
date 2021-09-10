package com.istea.mytasks.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.istea.mytasks.data.model.LoggedInUser
import com.istea.mytasks.db.FirebaseHelper
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private lateinit var firebase : FirebaseHelper

    fun login(token : String): Result<LoggedInUser> {

        firebase = FirebaseHelper()

        try {
            firebase.login(token)

            val user = Firebase.auth.currentUser?.let { it.displayName?.let { it1 -> LoggedInUser(it.uid, it1) } }
            return Result.Success(user!!)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        firebase = FirebaseHelper()
        firebase.logout()
    }

}