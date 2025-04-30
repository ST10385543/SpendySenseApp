package com.example.spendysenseapp.RoomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FeedbackDao {
    // user creating feedback
    @Insert
    suspend fun insertFeedback(feedback: Feedback)

    //for admin use
    @Delete
    suspend fun deleteFeedback(feedback: Feedback)

    //for admin use
    @Query("SELECT * FROM tblFeedback")
    suspend fun getAllFeedback(): List<Feedback>


}