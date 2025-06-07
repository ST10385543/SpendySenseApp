package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

class User_Achievements(
    val id: String = "",
    val userId: String = "",
    val achievementId: String = "",
    val completed: Boolean = false,
    val dateTimeAchieved: Long = 0L,
    val progress: Int = 0
)
