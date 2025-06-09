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

        val allAchievementsRef = db.collection("achievements")
        val userAchievementsRef = db.collection("user_achievement")
            .whereEqualTo("userId", userId)
            .whereEqualTo("completed", true)

        // Listen for real-time changes in user's completed achievements
        userAchievementsRef.addSnapshotListener { userSnapshot, userError ->
            if (userError != null) {
                Log.e("AchievementsDebug", "User achievement listener failed: ${userError.message}")
                return@addSnapshotListener
            }

            if (userSnapshot == null) return@addSnapshotListener

            unlockedIds.clear()
            unlockedIds.addAll(userSnapshot.documents.mapNotNull {
                it.getString("achievementId")?.trim()?.lowercase()
            })

            allAchievementsRef.get(Source.SERVER).addOnSuccessListener { achievementDocs ->
                unlockedList.clear()
                lockedList.clear()

                val easyList = mutableListOf<Achievements>()
                val mediumList = mutableListOf<Achievements>()
                val hardList = mutableListOf<Achievements>()
                val platinumList = mutableListOf<Achievements>()
                val unknownList = mutableListOf<Achievements>()

                for (doc in achievementDocs.documents) {
                    val achievement = doc.toObject(Achievements::class.java) ?: continue
                    val id = achievement.achievementId?.trim()?.lowercase() ?: continue
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
                }

                lockedList.addAll(easyList + mediumList + hardList + platinumList + unknownList)

                unlockedAdapter.notifyDataSetChanged()
                lockedAdapter.notifyDataSetChanged()

                binding.score.text = "${unlockedList.size}/${unlockedList.size + lockedList.size}"
                skeleton.showOriginal()

                // Prepare all IDs per difficulty
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

                val hasCompletedAllEasy = allEasyIds.isNotEmpty() && unlockedIds.containsAll(allEasyIds)
                val hasCompletedAllMedium = allMediumIds.isNotEmpty() && unlockedIds.containsAll(allMediumIds)
                val hasCompletedAllHard = allHardIds.isNotEmpty() && unlockedIds.containsAll(allHardIds)
                val hasCompletedAllAchievements = allAchievementIds.isNotEmpty() && unlockedIds.containsAll(allAchievementIds)

                unlockAchievementIfEligible(userId, hasCompletedAllEasy, "easy_completed", unlockedIds)
                unlockAchievementIfEligible(userId, hasCompletedAllMedium, "medium_completed", unlockedIds)
                unlockAchievementIfEligible(userId, hasCompletedAllHard, "hard_completed", unlockedIds)
                unlockAchievementIfEligible(userId, hasCompletedAllAchievements, "all_completed", unlockedIds)
            }
        }
    }

    private fun playUnlockSound() {
        val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.platinum_sound)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener { it.release() }
    }

    private fun unlockAchievementIfEligible(
        userId: String,
        conditionMet: Boolean,
        achievementId: String,
        unlockedIds: MutableSet<String>
    ) {
        if (conditionMet && !unlockedIds.contains(achievementId)) {
            AchievementManager.checkAndUnlock(
                userId,
                achievementId,
                onUnlocked = {
                    unlockedIds.add(achievementId) // Update local tracking
                    playUnlockSound()
                    Toast.makeText(applicationContext, "Achievement Unlocked: ${it.achievementName}!", Toast.LENGTH_SHORT).show()
                },
                onAlreadyUnlocked = {
                    unlockedIds.add(achievementId)
                }
            )
        }
    }

}
