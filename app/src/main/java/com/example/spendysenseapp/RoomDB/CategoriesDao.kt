package com.example.spendysenseapp.RoomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CategoriesDao {
    @Insert
    suspend fun insertCategory(category: Categories)

    @Update
    suspend fun updateCategory(category: Categories)

    @Delete
    suspend fun deleteCategory(category: Categories)

    @Query("SELECT * FROM tblCategories")
    suspend fun getAllCategories(): List<Categories>

    @Query("SELECT * FROM tblCategories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): Categories?

    @Query("SELECT * FROM tblCategories WHERE id IN (:categoryIds)")
    suspend fun getCategoriesByIds(categoryIds: List<Int>): List<Categories>
}