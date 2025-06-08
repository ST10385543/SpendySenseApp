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
            "washing_something" -> tryUnlockAchievement(
                userId,
                "find_easter_egg",
                onUnlocked,
                onAlreadyUnlocked,
                onFailure
            )
            "make_3_categories" -> incrementProgress(
                userId,
                "make_3_categories",
                3 ,
                onUnlocked,
                onFailure = onFailure
            )
            }
        }
    }
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