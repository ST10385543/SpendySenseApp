package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblCategories")
data class Categories(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var CategoryName: String,
    var iconImgPath: String
)
