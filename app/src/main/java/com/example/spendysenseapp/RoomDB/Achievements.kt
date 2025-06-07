package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

class Achievements(
    val achievementId: String = "",
    val achievementName: String = "",
    val achievementDescription: String = "",
    val achievementDifficulty: String = "",
    val achievementIconPath: String = ""
)
