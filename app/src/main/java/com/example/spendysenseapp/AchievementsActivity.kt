package com.example.spendysenseapp

import android.content.Intent
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
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source

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

        val userUid = intent.getStringExtra("userUid")
        val userEmail = intent.getStringExtra("userEmail")

        binding.usernameAchievementTv.text = "${userEmail}'s achievements"

        if (userUid != null) {
            loadAchievements(userUid)
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
                val unknownList = mutableListOf<Achievements>()

                for (doc in (achievementDocs as QuerySnapshot).documents) {
                    try {
                        val achievement = doc.toObject(Achievements::class.java) ?: continue
                        val normalizedId = achievement.achievementId?.trim()?.lowercase() ?: continue
                        val difficulty = achievement.achievementDifficulty?.trim()?.lowercase() ?: ""

                        if (unlockedIds.contains(normalizedId)) {
                            unlockedList.add(achievement)
                        } else {
                            when (difficulty) {
                                "easy" -> easyList.add(achievement)
                                "medium" -> mediumList.add(achievement)
                                "hard" -> hardList.add(achievement)
                                else -> unknownList.add(achievement)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AchievementsDebug", "Error parsing doc ${doc.id}: ${e.message}")
                    }
                }

                lockedList.addAll(easyList + mediumList + hardList + unknownList)

                unlockedAdapter.notifyDataSetChanged()
                lockedAdapter.notifyDataSetChanged()

                binding.score.text = "${unlockedList.size}/${unlockedList.size + lockedList.size}"
                skeleton.showOriginal()
            }
            .addOnFailureListener {
                Log.e("AchievementsDebug", "Error fetching data: ${it.message}")
            }
    }
}
