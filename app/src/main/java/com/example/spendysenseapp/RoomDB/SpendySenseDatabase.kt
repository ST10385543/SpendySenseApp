package com.example.spendysenseapp.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Users::class,
        //Achievements::class,
        Categories::class,
        //Transaction::class,
        Feedback::class,
        //User_Achievements::class
    ],
    version = 15
    //7 - added a migration due to adding min and max goals
    //8 - added a migration for changing user id in add transaction to string to accompany firebase uid
    //9 - added a migration to change transaction image to a byte array
    //10 - added a migration to change images to a List<string>
    //11 - reverted everything
    //12 - change reciept image to string
    //13 - changed transaction id to string
    //14 - changed dateCreated to long because realtime only supports this
    //15 - changes some things in transaction and category to make it compatable with firestore
)
@TypeConverters(Converters::class)
abstract class SpendySenseDatabase : RoomDatabase(){

    abstract fun userDao(): UserDao
    //abstract fun achievementDao(): AchievementsDao
    abstract fun categoryDao(): CategoriesDao
    //abstract fun transactionDao(): TransactionsDao
    abstract fun feedbackDao(): FeedbackDao
    //abstract fun user_achievementsDao(): User_AchievementsDao

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
                )
                    //change this if it breaks
                   // .createFromAsset("database/transactions.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}