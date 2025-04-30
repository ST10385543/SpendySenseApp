package com.example.spendysenseapp.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Users::class,
        Achievements::class,
        Categories::class,
        Transaction::class,
        Feedback::class,
        User_Achievements::class
    ],
    version = 1
)
abstract class SpendySenseDatabase : RoomDatabase(){

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: SpendySenseDatabase? = null
        fun getDatabase(context: Context): SpendySenseDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendySenseDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}