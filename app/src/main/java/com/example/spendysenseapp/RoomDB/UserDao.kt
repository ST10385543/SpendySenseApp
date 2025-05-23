package com.example.spendysenseapp.RoomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    //Get all users
    @Query("SELECT *  FROM tblUsers")
    fun getAllUsers() : List<Users>

    // Account creation
    @Insert
    suspend fun insert(user: Users)

    // Account Deletion
    @Delete
    suspend fun delete(user: Users)

    // find user by email (login)
    @Query("SELECT * FROM tblUsers WHERE userEmail = :email LIMIT 1")
    suspend fun findByEmail(email: String): Users?

    @Query("SELECT * FROM tblUsers WHERE id = :userId")
    suspend fun getUser(userId: Int): Users

    @Update
    suspend fun updateUser(user: Users)
}