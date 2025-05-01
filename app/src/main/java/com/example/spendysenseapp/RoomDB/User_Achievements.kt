package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "User_Achievement",
    foreignKeys = [
        ForeignKey(
            entity = Users::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Achievements::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class User_Achievements(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val achievementId: Int,
    val dateTimeAchieved: String,
    val totalAchieved: Int
)
