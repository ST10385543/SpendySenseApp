package com.example.spendysenseapp

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.AchievementAdapter
import com.example.spendysenseapp.RoomDB.Achievements
import com.example.spendysenseapp.databinding.ActivityAchievementsBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AchievementsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAchievementsBinding
    private lateinit var skeleton: Skeleton

    private val unlockedList = mutableListOf<Achievements>()
    private val lockedList = mutableListOf<Achievements>()
    private lateinit var unlockedAdapter: AchievementAdapter
    private lateinit var lockedAdapter: AchievementAdapter

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        skeleton = binding.achievementSkeleton
        skeleton.showSkeleton()

        unlockedAdapter = AchievementAdapter(unlockedList, false)
        lockedAdapter = AchievementAdapter(lockedList, true)

        binding.unlockedRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.lockedRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.unlockedRecyclerView.adapter = unlockedAdapter
        binding.lockedRecyclerView.adapter = lockedAdapter

        val userUid = intent.getStringExtra("userUid")
        val userEmail = intent.getStringExtra("userEmail")

        binding.usernameAchievementTv.text = "${userEmail}'s achievements"

        if (userUid != null) {
            loadAchievements(userUid)
        }

        binding.backbtn.setOnClickListener {
            finish()
        }
    }

    private fun loadAchievements(userId: String) {
        val unlockedIds = mutableSetOf<String>()

        db.collection("user_achievement")
            .whereEqualTo("userId", userId)
            .whereEqualTo("completed", true)
            .addSnapshotListener { userAchievementDocs, error1 ->
                if (error1 != null) {
                    Log.e("AchievementsDebug", "Error listening to user_achievement: ${error1.message}")
                    return@addSnapshotListener
                }

                unlockedIds.clear()
                unlockedIds.addAll(userAchievementDocs?.documents?.mapNotNull {
                    it.getString("achievementId")?.trim()?.lowercase()
                } ?: emptyList())

                db.collection("achievements")
                    .addSnapshotListener { achievementDocs, error2 ->
                        if (error2 != null) {
                            Log.e("AchievementsDebug", "Error listening to achievements: ${error2.message}")
                            return@addSnapshotListener
                        }

                        unlockedList.clear()
                        lockedList.clear()

                        val easyList = mutableListOf<Achievements>()
                        val mediumList = mutableListOf<Achievements>()
                        val hardList = mutableListOf<Achievements>()
                        val platinumList = mutableListOf<Achievements>()
                        val unknownList = mutableListOf<Achievements>()

                        achievementDocs?.documents?.forEach { doc ->
                            try {
                                val achievement = doc.toObject(Achievements::class.java) ?: return@forEach
                                val id = achievement.achievementId?.trim()?.lowercase() ?: return@forEach
                                val difficulty = achievement.achievementDifficulty?.trim()?.lowercase() ?: ""

                                if (unlockedIds.contains(id)) {
                                    unlockedList.add(achievement)
                                } else {
                                    when (difficulty) {
                                        "easy" -> easyList.add(achievement)
                                        "medium" -> mediumList.add(achievement)
                                        "hard" -> hardList.add(achievement)
                                        "platinum" -> platinumList.add(achievement)
                                        else -> unknownList.add(achievement)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AchievementsDebug", "Parse error: ${e.message}")
                            }
                        }

                        lockedList.addAll(easyList + mediumList + hardList + platinumList + unknownList)
                        unlockedAdapter.notifyDataSetChanged()
                        lockedAdapter.notifyDataSetChanged()

                        binding.score.text = "${unlockedList.size}/${unlockedList.size + lockedList.size}"
                        skeleton.showOriginal()

                        val allEasyIds = (easyList + unlockedList).filter {
                            it.achievementDifficulty?.trim()?.lowercase() == "easy"
                        }.mapNotNull { it.achievementId?.trim()?.lowercase() }.toSet()

                        val allMediumIds = (mediumList + unlockedList).filter {
                            it.achievementDifficulty?.trim()?.lowercase() == "medium"
                        }.mapNotNull { it.achievementId?.trim()?.lowercase() }.toSet()

                        val allHardIds = (hardList + unlockedList).filter {
                            it.achievementDifficulty?.trim()?.lowercase() == "hard"
                        }.mapNotNull { it.achievementId?.trim()?.lowercase() }.toSet()

                        val allAchievementIds = (easyList + mediumList + hardList + platinumList + unknownList + unlockedList)
                            .mapNotNull { it.achievementId?.trim()?.lowercase() }.toSet()

                        if (allEasyIds.isNotEmpty() && unlockedIds.containsAll(allEasyIds) && !unlockedIds.contains("easy_completed")) {
                            checkAndUnlock(userId, "easy_completed")
                        }

                        if (allMediumIds.isNotEmpty() && unlockedIds.containsAll(allMediumIds) && !unlockedIds.contains("medium_completed")) {
                            checkAndUnlock(userId, "medium_completed")
                        }

                        if (allHardIds.isNotEmpty() && unlockedIds.containsAll(allHardIds) && !unlockedIds.contains("hard_completed")) {
                            checkAndUnlock(userId, "hard_completed")
                        }

                        if (allAchievementIds.isNotEmpty()  && !unlockedIds.contains("all_completed")) {
                            checkAndUnlock(userId, "all_completed")
                        }
                    }
            }
    }

    private fun checkAndUnlock(userId: String, achievementId: String) {
        val userAchievementRef = db.collection("user_achievement")

        userAchievementRef
            .whereEqualTo("userId", userId)
            .whereEqualTo("achievementId", achievementId)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    Log.d("AchievementUnlock", "$achievementId already unlocked.")
                    return@addOnSuccessListener
                }

                db.collection("achievements").document(achievementId).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val achievement = doc.toObject(Achievements::class.java)
                            if (achievement != null) {
                                val newDoc = hashMapOf(
                                    "userId" to userId,
                                    "achievementId" to achievementId,
                                    "completed" to true,
                                    "timestamp" to FieldValue.serverTimestamp()
                                )

                                userAchievementRef.add(newDoc)
                                    .addOnSuccessListener {
                                        playUnlockSound()
                                        Toast.makeText(applicationContext, "Achievement Unlocked: ${achievement.achievementName}", Toast.LENGTH_SHORT).show()
                                        Log.d("AchievementUnlock", "Unlocked: $achievementId")
                                    }
                                    .addOnFailureListener {
                                        Log.e("AchievementUnlock", "Failed to unlock $achievementId: ${it.message}")
                                    }
                            }
                        } else {
                            Log.e("AchievementUnlock", "Achievement $achievementId not found in DB.")
                        }
                    }
            }
    }

    private fun playUnlockSound() {
        val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.platinum_sound)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener { it.release() }
    }
}
