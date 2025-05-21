//// File: app/src/main/java/com/example/spendysenseapp/Services/SessionManager.kt
//package com.example.spendysenseapp.Services
//
//import android.content.Context
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//
//class SessionManager private constructor(context: Context) {
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
//
//    companion object {
//        @Volatile private var instance: SessionManager? = null
//
//        fun getInstance(context: Context): SessionManager {
//            return instance ?: synchronized(this) {
//                instance ?: SessionManager(context.applicationContext).also { instance = it }
//            }
//        }
//    }
//
//    fun getCurrentUser(): FirebaseUser? {
//        return auth.currentUser
//    }
//
//    fun isLoggedIn(): Boolean {
//        return auth.currentUser != null
//    }
//}