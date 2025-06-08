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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

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

        // 1. Get unlocked achievement IDs for this user
        db.collection("user_achievement")
            .whereEqualTo("userId", userId)
            .whereEqualTo("completed", true)
            .get()
            .addOnSuccessListener { userAchievementDocs ->
                unlockedIds.addAll(
                    userAchievementDocs.mapNotNull { it.getString("achievementId") }
                        .map { it.trim().lowercase() }
                )
                Log.d("AchievementsDebug", "Unlocked IDs: $unlockedIds")

                // 2. Get all achievements
                db.collection("achievements")
                    .get()
                    .addOnSuccessListener { achievementDocs ->
                        Log.d("AchievementsDebug", "Total achievements fetched: ${achievementDocs.size()}")

                        // Clear lists before adding
                        unlockedList.clear()
                        lockedList.clear()

                        for (doc in achievementDocs) {
                            Log.d("AchievementsDebug", "Raw data: ${doc.data}")

                            try {
                                val achievement = doc.toObject(Achievements::class.java)
                                val normalizedId = achievement.achievementId.trim().lowercase()

                                Log.d(
                                    "AchievementsDebug",
                                    "Parsed achievement: ID=${achievement.achievementId} Name=${achievement.achievementName}"
                                )

                                if (normalizedId.isNotBlank()) {
                                    if (unlockedIds.contains(normalizedId)) {
                                        unlockedList.add(achievement)
                                    } else {
                                        lockedList.add(achievement)
                                    }
                                } else {
                                    Log.w("AchievementsDebug", "Skipped doc with blank achievementId: ${doc.id}")
                                }
                            } catch (e: Exception) {
                                Log.e("AchievementsDebug", "Error parsing doc ${doc.id}: ${e.message}")
                            }
                        }

                        Log.d("AchievementsDebug", "Unlocked count: ${unlockedList.size}, Locked count: ${lockedList.size}")
                        binding.score.text = "${userAchievementDocs.count()}/${achievementDocs.count()}"

                        // 3. Notify adapters after data is updated
                        unlockedAdapter.notifyDataSetChanged()
                        lockedAdapter.notifyDataSetChanged()

                        skeleton.showOriginal()
                    }
            }
    }
}
