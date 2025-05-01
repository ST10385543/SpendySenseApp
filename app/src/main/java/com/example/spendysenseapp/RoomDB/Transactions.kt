package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "tblTransactions",
    foreignKeys = [ForeignKey(
        entity = Categories::class,
        parentColumns = ["id"],
        childColumns = ["id"],
        onDelete = ForeignKey.CASCADE // deletes transactions if category is deleted
    )]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val categoryId: Int,
    val amount: Double,
    val type: String // income / expense
)
