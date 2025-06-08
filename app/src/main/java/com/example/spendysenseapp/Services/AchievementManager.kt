package com.example.spendysenseapp.Services

import com.example.spendysenseapp.RoomDB.Achievements
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

object AchievementManager {
    fun checkAndUnlock(
        userId: String,
        event: String,
        onUnlocked: (Achievements) -> Unit = {},
        onAlreadyUnlocked: (Achievements) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ){
        when(event){
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
            }
        }
    }
        //love..Jerry ;)


//these 2 methods you shouldnt??? have to change, only having to make new achievement instances
//in the above class
//to see an example of an achivement, go to the homeFragment, look for the launch code for the
//washing something activity
    fun tryUnlockAchievement(
        userId: String,
        achievementId: String,
        onUnlocked: (Achievements) -> Unit = {},
        onAlreadyUnlocked: (Achievements) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
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

                ref.get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists() && doc.getBoolean("completed") == true) {
                            onAlreadyUnlocked(achievement)
                        } else {
                            val data = hashMapOf(
                                "id" to docId,
                                "userId" to userId,
                                "achievementId" to achievementId,
                                "completed" to true,
                                "dateTimeAchieved" to System.currentTimeMillis(),
                                "progress" to 100
                            )

                            ref.set(data)
                                .addOnSuccessListener { onUnlocked(achievement) }
                                .addOnFailureListener { onFailure(it) }
                        }
                    }
                    .addOnFailureListener { onFailure(it) }
            }
    }

        fun incrementProgress(
            userId: String,
            achievementId: String,
            target: Int,
            onUnlocked: (Achievements) -> Unit = {},
            onProgress: (current: Int, target: Int) -> Unit = { _, _ -> },
            onFailure: (Exception) -> Unit = {}
        ) {
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

                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(ref)
                        val currentProgress = snapshot.getLong("progress")?.toInt() ?: 0
                        val alreadyCompleted = snapshot.getBoolean("completed") == true

                        if (alreadyCompleted) {
                            return@runTransaction
                        }

                        val newProgress = currentProgress + 1

                        val data = hashMapOf(
                            "id" to docId,
                            "userId" to userId,
                            "achievementId" to achievementId,
                            "progress" to newProgress,
                            "dateTimeAchieved" to System.currentTimeMillis()
                        )

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