package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblUsers")
data class Users(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var userEmail: String,
    var Password: String,

    //column for minimum and maximum goal
    var minimumGoal: Double,
    var maximumGoal: Double
)
