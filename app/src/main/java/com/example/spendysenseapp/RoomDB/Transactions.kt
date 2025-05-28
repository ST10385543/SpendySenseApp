package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.ForeignKey


@Entity(
    tableName = "tblTransactions",
    foreignKeys = [ForeignKey(
        entity = Categories::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE // deletes transactions if category is deleted
    )]
)
data class Transaction(
    val id: String,
    val name: String,
    val categoryId: String,
    val amount: Double,
    val type: String,// income / expense

    //New entity
    val DateCreated: Long,

    //Newer Entity
    val UserID: String,

    //New Entity
    val receiptImage: String// <- new field for image
)
