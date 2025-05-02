package com.example.spendysenseapp.Services

import android.content.Context
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.RoomDB.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionManager(context: Context) {
    private val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val userDao = SpendySenseDatabase.getDatabase(context).userDao()

    companion object {
        @Volatile private var instance:  SessionManager? =null

        fun getInstance(context: Context): SessionManager{
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it}
            }
        }
    }

    //save user session
    suspend fun saveUserSession(user: Users){
        withContext(Dispatchers.IO){
            sharedPref.edit().putInt("user_id", user.id).apply()
            val existingUser = userDao.getUser(user.id)
            if (existingUser == null) {
                userDao.insert(user)
            }
        }
    }

    // Get current user
    suspend fun getCurrentUser(): Users {
        return withContext(Dispatchers.IO) {
            val userId = sharedPref.getInt("user_id", 0)
            userId.let { userDao.getUser(it) }
        }
    }
    fun getCurrentUserId(): Int? {
        return sharedPref.getInt("user_id", -1).takeIf { it != -1 }
    }

    // Clear session on logout
    fun clearSession() {
        sharedPref.edit().clear().apply()
    }
}