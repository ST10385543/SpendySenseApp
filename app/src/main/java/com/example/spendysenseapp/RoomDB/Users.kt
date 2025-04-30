package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblUsers")
data class Users(
    @PrimaryKey(autoGenerate = true) val id: Int,
    var userEmail: String,
    var Password: String
)
