package com.example.spendysenseapp

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.AchievementAdapter
import com.example.spendysenseapp.RoomDB.Achievements
import com.example.spendysenseapp.Services.AchievementManager
import com.example.spendysenseapp.databinding.ActivityAchievementsBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source

class AchievementsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAchievementsBinding

    private lateinit var skeleton: Skeleton

    private val unlockedList = mutableListOf<Achievements>()
    private val lockedList = mutableListOf<Achievements>()
    private lateinit var unlockedAdapter: AchievementAdapter
    private lateinit var lockedAdapter: AchievementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
        val db = Firebase.firestore
        val unlockedIds = mutableSetOf<String>()

        val userAchievementsTask = db.collection("user_achievement")
            .whereEqualTo("userId", userId)
            .whereEqualTo("completed", true)
            .get(Source.SERVER)

        val achievementsTask = db.collection("achievements")
            .get(Source.SERVER)

        Tasks.whenAllSuccess<QuerySnapshot>(userAchievementsTask, achievementsTask)
            .addOnSuccessListener { results ->
                val userAchievementDocs = results[0]
                val achievementDocs = results[1]

                unlockedIds.addAll(
                    (userAchievementDocs as QuerySnapshot).documents.mapNotNull {
                        it.getString("achievementId")?.trim()?.lowercase()
                    }
                )

                unlockedList.clear()
                lockedList.clear()

                val easyList = mutableListOf<Achievements>()
                val mediumList = mutableListOf<Achievements>()
                val hardList = mutableListOf<Achievements>()
                val platinumList = mutableListOf<Achievements>()
                val unknownList = mutableListOf<Achievements>()

                for (doc in (achievementDocs as QuerySnapshot).documents) {
                    try {
                        val achievement = doc.toObject(Achievements::class.java) ?: continue
                        val normalizedId =
                            achievement.achievementId?.trim()?.lowercase() ?: continue
                        val difficulty =
                            achievement.achievementDifficulty?.trim()?.lowercase() ?: ""

                        if (unlockedIds.contains(normalizedId)) {
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
                        Log.e("AchievementsDebug", "Error parsing doc ${doc.id}: ${e.message}")
                    }
                }

                lockedList.addAll(easyList + mediumList + hardList + platinumList + unknownList)

                unlockedAdapter.notifyDataSetChanged()
                lockedAdapter.notifyDataSetChanged()

                binding.score.text = "${unlockedList.size}/${unlockedList.size + lockedList.size}"
                skeleton.showOriginal()

                // Prepare sets of all achievements IDs per difficulty (locked + unlocked)
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

                // Check if user completed all achievements in each difficulty category
                val hasCompletedAllEasy = allEasyIds.isNotEmpty() && unlockedIds.containsAll(allEasyIds)
                val hasCompletedAllMedium = allMediumIds.isNotEmpty() && unlockedIds.containsAll(allMediumIds)
                val hasCompletedAllHard = allHardIds.isNotEmpty() && unlockedIds.containsAll(allHardIds)
                val hasCompletedAllAchievements = allAchievementIds.isNotEmpty() && unlockedIds.containsAll(allAchievementIds)

                if (hasCompletedAllEasy) {
                    AchievementManager.checkAndUnlock(
                        userId,
                        "easy_completed",
                        onUnlocked = { achievement ->
                            val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.platinum_sound)
                            mediaPlayer?.start()
                            mediaPlayer?.setOnCompletionListener { it.release() }

                            Toast.makeText(applicationContext, "Achievement Unlocked: ${achievement.achievementName}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                if (hasCompletedAllMedium) {
                    AchievementManager.checkAndUnlock(
                        userId,
                        "medium_completed",
                        onUnlocked = { achievement ->
                            val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.platinum_sound)
                            mediaPlayer?.start()
                            mediaPlayer?.setOnCompletionListener { it.release() }

                            Toast.makeText(applicationContext, "Achievement Unlocked: ${achievement.achievementName}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                if (hasCompletedAllHard) {
                    AchievementManager.checkAndUnlock(
                        userId,
                        "hard_completed",
                        onUnlocked = { achievement ->
                            val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.platinum_sound)
                            mediaPlayer?.start()
                            mediaPlayer?.setOnCompletionListener { it.release() }

                            Toast.makeText(applicationContext, "Achievement Unlocked: ${achievement.achievementName}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                if (hasCompletedAllAchievements) {
                    AchievementManager.checkAndUnlock(
                        userId,
                        "all_completed",
                        onUnlocked = { achievement ->
                            val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.platinum_sound)
                            mediaPlayer?.start()
                            mediaPlayer?.setOnCompletionListener { it.release() }

                            Toast.makeText(applicationContext, "Achievement Unlocked: ${achievement.achievementName}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            .addOnFailureListener {
                Log.e("AchievementsDebug", "Error fetching data: ${it.message}")
            }
    }

}
