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
    @Query("SELECT * FROM tblTransactions")
    suspend fun getAllTransactions(): List<Transaction>

    //getting 5 transactions from latest first
    @Query("SELECT * FROM tblTransactions WHERE UserID =:userId ORDER BY DateCreated DESC LIMIT 5")
    suspend fun getFiveTransactions(userId: Int): List<Transaction>

    // selecting a specific transaction, by its id
    @Query("SELECT * FROM tblTransactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Int): Transaction?

//    // get all transactions by category
//    @Query("SELECT * FROM `Transaction` WHERE categoryId = :categoryId")
//    suspend fun getTransactionsByCategory(categoryId: Int): List<Transaction>
    @Query("SELECT * FROM tblTransactions WHERE UserID = :userId")
    suspend fun  getUsersTransactions(userId: Int): List<Transaction>

    //sort transactions for a specific user by month
    @Query("""
        SELECT * FROM tblTransactions 
        WHERE userId = :userId 
        AND strftime('%Y-%m', DateCreated/1000, 'unixepoch') = :yearMonth
        ORDER BY DateCreated DESC
    """)
    suspend fun getUserTransactionSortedByMonth(userId: Int, yearMonth: String): List<Transaction>

    @Query("DELETE FROM tblTransactions WHERE UserID = :userId")
    suspend fun deleteTransactionsByUserId(userId: Int)

    @Query("SELECT * FROM tblTransactions WHERE UserID = :userId AND categoryId = :categoryId")
    suspend fun getTransactionsByCategory(userId: Int, categoryId: Int): List<Transaction>

}