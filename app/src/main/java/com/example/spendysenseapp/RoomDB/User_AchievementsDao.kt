package com.example.spendysenseapp.RoomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface User_AchievementsDao {
    @Insert
    suspend fun insertUserAchievement(userAchievement: User_Achievements)

    @Update
    suspend fun updateUserAchievement(userAchievement: User_Achievements)

    @Delete
    suspend fun deleteUserAchievement(userAchievement: User_Achievements)

    @Query("SELECT * FROM User_Achievement")
    suspend fun getAllUserAchievements(): List<User_Achievements>

    @Query("SELECT * FROM User_Achievement WHERE userId = :userId")
    suspend fun getAchievementsByUser(userId: Int): List<User_Achievements>
}