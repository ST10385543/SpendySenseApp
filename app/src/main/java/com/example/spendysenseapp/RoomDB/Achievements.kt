package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

class Achievements(
    val achievementId: String = "",
    var achievementName: String = "",
    var achievementDescription: String = "",
    var achievementIconPath: String = "",
    var achievementDifficulty: String = ""
)
