package com.example.spendysenseapp.RoomDB

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date


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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val categoryId: Int,
    val amount: Double,
    val type: String,// income / expense

    //New entity
    val DateCreated: Date,

    //Newer Entity
    val UserID: String,

    //New Entity
    val receiptImage: ByteArray? = null // <- new field for image
)
