package com.example.spendysenseapp.Services

import com.example.spendysenseapp.RoomDB.Achievements
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

//this class is used to manage the various achievements checks, instead of doing it
//within each achievement block, the comments below explain the process or adding
//a new achievement based on the one in the firestore
//this object, that is gotten from the class it is called
//getting the userId, the name of the event from the activity
//and units with actions on unlock, already unlocked, and failure
object AchievementManager {
    fun checkAndUnlock(
        userId: String,
        event: String,
        onUnlocked: (Achievements) -> Unit = {},
        onAlreadyUnlocked: (Achievements) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ){
        when(event){
            // dear ayush
            //this is a singular achievement block, add the logic for the others
            //for one time achievements, use tryUnlockAchievement
            //for counter ones, use increment one
            //format of an achivement
            //once of achievement, like easter egg
            //use tryUnlockAchievement
            //if its couting progress, use increment progress one
            //below is example of single once off achievemen
            // this is the name of the event that you pass from the
            //method, as shown in HomeFragment.kt
            "washing_something" -> tryUnlockAchievement(
                //passes the user id from the method it originates from
                userId,
                //the id of the achievement, get this from firebase, or how
                //you named it
                "find_easter_egg",
                //these are the methods that send back a onSuccess or onFailure, depending
                onUnlocked,
                onAlreadyUnlocked,
                onFailure
            )
            //this one is an example of a counted variable, where you need multiple instances
            //to complete an achievement
            //use the incrementProgress method
            "make_3_categories" -> incrementProgress(
                //again the user id
                userId,
                //id of the achievement from firebase
                "make_3_categories",
                //target refers to how many actions need to be done to complete the achievement
                //its not an array, so you dont have to start counting from 0, it will be
                //from 1
                3 ,
                //this is again onSuccess or onFailure methods
                onUnlocked,
                onFailure = onFailure
            )
            //start from here  -->  love..Jerry ;)

            //provide user feedback
            "provide_user_feedback" -> tryUnlockAchievement(
                //passes the user id from the method it originates from
                userId,
                //the id of the achievement, get this from firebase, or how
                //you named it
                "provide_user_feedback",
                //these are the methods that send back a onSuccess or onFailure, depending
                onUnlocked,
                onAlreadyUnlocked,
                onFailure
            )
            //create 5 transactions of any type
            "create_5_transactions" -> incrementProgress(
                userId,
                "create_5_transactions",
                5 ,
                //this is again onSuccess or onFailure methods
                onUnlocked,
                onFailure = onFailure
            )

            //create 10 income transactions
            "make_10_income_transactions" -> incrementProgress(
                userId,
                "make_10_income_transactions",
                10 ,
                onUnlocked,
                onFailure = onFailure
            )

            //create R69 expense
            "create_expense_69" -> tryUnlockAchievement(
                userId,
                "create_expense_69",
                onUnlocked,
                onAlreadyUnlocked,
                onFailure
            )

            //delete 5 transactions
            "delete_5_transactions" -> incrementProgress(
                userId,
                "delete_5_transactions",
                5,
                onUnlocked,
                onFailure = onFailure
            )

            //spooky achievement
            "find_spooky_message" -> tryUnlockAchievement(
                userId,
                "find_spooky_message",
                onUnlocked,
                onAlreadyUnlocked,
                onFailure
            )

            //fuel expense achievement
            "create_fuel_1000__image_expense" -> tryUnlockAchievement(
                userId,
                "create_fuel_1000__image_expense",
                onUnlocked,
                onAlreadyUnlocked,
                onFailure
            )

            //all easy completed achievements
            "easy_completed" -> tryUnlockAchievement(
                userId,
                "easy_completed",
                onUnlocked,
                onAlreadyUnlocked,
                onFailure
            )

            //all medium completed achievements
            "medium_completed" -> tryUnlockAchievement(
                userId,
                "medium_completed",
                onUnlocked,
                onAlreadyUnlocked,
                onFailure
            )

            //all hard completed achievements
            "hard_completed" -> tryUnlockAchievement(
                userId,
                "hard_completed",
                onUnlocked,
                onAlreadyUnlocked,
                onFailure
            )
            }
        }
    }



//these 2 methods you shouldnt??? have to change, only having to make new achievement instances
//in the above class
//to see an example of an achivement, go to the homeFragment, look for the launch code for the
//washing something activity
//this checks for achievments with a singular activity, like a once off
    fun tryUnlockAchievement(
        userId: String,
        achievementId: String,
        onUnlocked: (Achievements) -> Unit = {},
        onAlreadyUnlocked: (Achievements) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        //prepares the document id for creation
        val db = Firebase.firestore
        val docId = "${userId}_$achievementId"

        //fetch achievement info first
        db.collection("achievements").document(achievementId)
            .get()
            .addOnSuccessListener { achievementDoc ->
                val achievement = achievementDoc.toObject(Achievements::class.java)
                if (achievement == null) {
                    onFailure(Exception("Achievement not found"))
                    return@addOnSuccessListener
                }

                val ref = db.collection("user_achievement").document(docId)

                //gets the user_achievement document
                ref.get()
                    .addOnSuccessListener { doc ->
                        //checks if the event is already unlocked
                        if (doc.exists() && doc.getBoolean("completed") == true) {
                            onAlreadyUnlocked(achievement)
                        } else {
                            //if not, create a new firebase document
                            val data = hashMapOf(
                                "id" to docId,
                                "userId" to userId,
                                "achievementId" to achievementId,
                                "completed" to true,
                                "dateTimeAchieved" to System.currentTimeMillis(),
                                "progress" to 100
                            )

                            //add the reference
                            ref.set(data)
                                .addOnSuccessListener { onUnlocked(achievement) }
                                .addOnFailureListener { onFailure(it) }
                        }
                    }
                    .addOnFailureListener { onFailure(it) }
            }
    }


    //this method is used for incrementing achievements
    //such as deleting a transaction
    fun incrementProgress(
        userId: String,
        achievementId: String,
        target: Int,
        onUnlocked: (Achievements) -> Unit = {},
        onProgress: (current: Int, target: Int) -> Unit = { _, _ -> },
        onFailure: (Exception) -> Unit = {}
    ) {
        //again prepares the document name
        val db = Firebase.firestore
        val docId = "${userId}_$achievementId"
        //fetch achievement info first
        db.collection("achievements").document(achievementId)
            .get()
            .addOnSuccessListener { achievementDoc ->
                val achievement = achievementDoc.toObject(Achievements::class.java)
                if (achievement == null) {
                    onFailure(Exception("Achievement not found"))
                    return@addOnSuccessListener
                }

                val ref = db.collection("user_achievement").document(docId)

                //runs a firestore transaction to get the values
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(ref)
                    val currentProgress = snapshot.getLong("progress")?.toInt() ?: 0
                    val alreadyCompleted = snapshot.getBoolean("completed") == true

                    if (alreadyCompleted) {
                        return@runTransaction
                    }

                    //increments the progress
                    val newProgress = currentProgress + 1

                    //prepares the data for a new user_achievement making
                    val data = hashMapOf(
                        "id" to docId,
                        "userId" to userId,
                        "achievementId" to achievementId,
                        "progress" to newProgress,
                        "dateTimeAchieved" to System.currentTimeMillis()
                    )

                    //checks the progress, if its above target, change field completed to true
                    if (newProgress >= target) {
                        data["completed"] = true
                        transaction.set(ref, data)
                    } else {
                        data["completed"] = false
                        transaction.set(ref, data)
                    }
                }.addOnSuccessListener {
                    ref.get().addOnSuccessListener { updatedDoc ->
                        val prog = updatedDoc.getLong("progress")?.toInt() ?: 0
                        val isCompleted = updatedDoc.getBoolean("completed") == true

                        if (isCompleted) onUnlocked(achievement) else onProgress(prog, target)
                    }
                }
                    .addOnFailureListener { onFailure(it) }
            }
    }