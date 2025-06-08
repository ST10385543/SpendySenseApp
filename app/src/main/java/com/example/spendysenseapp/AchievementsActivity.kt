package com.example.spendysenseapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.AchievementAdapter
import com.example.spendysenseapp.RoomDB.Achievements
import com.example.spendysenseapp.databinding.ActivityAchievementsBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AchievementsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAchievementsBinding
    //gotten from Fahlteich, P. 2025. SkeletonLayout: Skeleton view pattern for Android, Github. [Online].
    //Avaiable at: https://github.com/Faltenreich/SkeletonLayout [Accessed 29 May 2025]
    private lateinit var skeleton: Skeleton

    // Hold references to adapters and lists for updating
    private val unlockedList = mutableListOf<Achievements>()
    private val lockedList = mutableListOf<Achievements>()
    private lateinit var unlockedAdapter: AchievementAdapter
    private lateinit var lockedAdapter: AchievementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Only one setContentView call with binding
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        skeleton = binding.achievementSkeleton
        skeleton.showSkeleton()

        // Initialize RecyclerViews and adapters once
        unlockedAdapter = AchievementAdapter(unlockedList, false)
        lockedAdapter = AchievementAdapter(lockedList, true)

        binding.unlockedRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.lockedRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.unlockedRecyclerView.adapter = unlockedAdapter
        binding.lockedRecyclerView.adapter = lockedAdapter

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            loadAchievements(currentUserId)
        }
    }

    private fun loadAchievements(userId: String) {
        val db = Firebase.firestore
        val unlockedIds = mutableSetOf<String>()

        db.collection("user_achievement")
            .whereEqualTo("userId", userId)
            .whereEqualTo("completed", true)
            .get()
            .addOnSuccessListener { userAchievementDocs ->
                // Normalize IDs: trim and lowercase for consistency
                unlockedIds.addAll(
                    userAchievementDocs.mapNotNull {
                        it.getString("achievementId")?.trim()?.lowercase()
                    }
                )

                db.collection("achievements")
                    .get()
                    .addOnSuccessListener { achievementDocs ->
                        unlockedList.clear()
                        lockedList.clear()

                        val easyList = mutableListOf<Achievements>()
                        val mediumList = mutableListOf<Achievements>()
                        val hardList = mutableListOf<Achievements>()
                        val unknownList = mutableListOf<Achievements>() // fallback bucket

                        for (doc in achievementDocs) {
                            try {
                                val achievement = doc.toObject(Achievements::class.java)

                                val normalizedId = achievement.achievementId?.trim()?.lowercase()
                                if (normalizedId.isNullOrBlank()) {
                                    Log.w("AchievementsDebug", "Skipped doc with null/blank ID: ${doc.id}")
                                    continue
                                }

                                val difficulty = achievement.achievementDifficulty?.trim()?.lowercase() ?: ""

                                if (unlockedIds.contains(normalizedId)) {
                                    unlockedList.add(achievement)
                                } else {
                                    when (difficulty) {
                                        "easy" -> easyList.add(achievement)
                                        "medium" -> mediumList.add(achievement)
                                        "hard" -> hardList.add(achievement)
                                        else -> {
                                            Log.w("AchievementsDebug", "Unknown difficulty for ID: $normalizedId, using fallback")
                                            unknownList.add(achievement)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AchievementsDebug", "Error parsing doc ${doc.id}: ${e.message}")
                            }
                        }

                        // Sort lockedList with easy → medium → hard → unknown
                        lockedList.addAll(easyList + mediumList + hardList + unknownList)

                        // Update UI
                        unlockedAdapter.notifyDataSetChanged()
                        lockedAdapter.notifyDataSetChanged()

                        binding.score.text = "${unlockedList.size}/${achievementDocs.size()}"
                        skeleton.showOriginal()
                    }
                    .addOnFailureListener {
                        Log.e("AchievementsDebug", "Error fetching achievements: ${it.message}")
                    }
            }
            .addOnFailureListener {
                Log.e("AchievementsDebug", "Error fetching user achievements: ${it.message}")
            }
    }
}
