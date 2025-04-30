package com.example.spendysenseapp.RoomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface TransactionsDao {
    //creating a transaction
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    //editing a transaction
    @Update
    suspend fun updateTransaction(transaction: Transaction)

    // deleting a transaction
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // getting all transactions
    @Query("SELECT * FROM `Transaction`")
    suspend fun getAllTransactions(): List<Transaction>

    // selecting a specific transaction, by its id
    @Query("SELECT * FROM `Transaction` WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Int): Transaction?

//    // Optional: get all transactions by category
//    @Query("SELECT * FROM `Transaction` WHERE categoryId = :categoryId")
//    suspend fun getTransactionsByCategory(categoryId: Int): List<Transaction>
}