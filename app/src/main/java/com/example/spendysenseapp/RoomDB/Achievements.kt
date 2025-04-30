package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblAchievements")
data class Achievements(
    @PrimaryKey(autoGenerate = true) val id: Int,
    var Description: String,
    var Level: String
)
