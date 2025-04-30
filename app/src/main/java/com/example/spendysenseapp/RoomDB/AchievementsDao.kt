package com.example.spendysenseapp.RoomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AchievementsDao {
    //creating an
    @Insert
    suspend fun insertAchievement(achievement: Achievements)

    //updating an Achievement
    @Update
    suspend fun updateAchievement(achievement: Achievements)

    //deleting an Achievement
    @Delete
    suspend fun deleteAchievement(achievement: Achievements)

    // getting all Achievements
    @Query("SELECT * FROM tblAchievements")
    suspend fun getAllAchievements(): List<Achievements>
}