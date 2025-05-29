package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblCategories")
data class Categories(
    @PrimaryKey val id: String = "",
    var CategoryName: String = "",
    var iconImgPath: String = "",
    var userId: String = ""
)
