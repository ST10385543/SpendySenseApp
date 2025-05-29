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
    val id: String = "",
    val name: String = "",
    val categoryId: String = "",
    val amount: Double = 0.0,
    val type: String = "",// income / expense

    //New entity
    val dateCreated: Long = 0L,

    //Newer Entity
    val userID: String = "",

    //New Entity
    val receiptImage: String = ""// <- new field for image
)
