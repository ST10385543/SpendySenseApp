package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "tblFeedback",
    foreignKeys = [ForeignKey(
        entity = Users::class,
        parentColumns = ["id"],
        childColumns = ["id"],
        onDelete = ForeignKey.CASCADE // deletes transactions if category is deleted
    )])
data class Feedback(
    @PrimaryKey(autoGenerate = true) val id: Int,
    var UserId: Int,
    var Title: String,
    var Description: String
)
