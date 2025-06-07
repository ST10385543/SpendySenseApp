package com.example.spendysenseapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendysenseapp.Adapter.AchievementAdapter
import com.example.spendysenseapp.RoomDB.Achievements
import com.example.spendysenseapp.databinding.ActivityAchievementsBinding
import com.example.spendysenseapp.databinding.ActivityCalculatorBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore

class AchievementsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAchievementsBinding
    //gotten from Fahlteich, P. 2025. SkeletonLayout: Skeleton view pattern for Android, Github. [Online].
    //Avaiable at: https://github.com/Faltenreich/SkeletonLayout [Accessed 29 May 2025]
    private lateinit var skeleton: Skeleton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_achievements)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        skeleton = binding.achievementSkeleton

        skeleton.showSkeleton()

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            loadAchievements(currentUserId)
        }
    }
    private fun loadAchievements(userId: String) {
        val db = Firebase.firestore

        val unlocked = mutableListOf<Achievements>()
        val locked = mutableListOf<Achievements>()
        val unlockedIds = mutableSetOf<String>()

        val unlockedRecyclerView = findViewById<RecyclerView>(R.id.unlockedRecyclerView)
        val lockedRecyclerView = findViewById<RecyclerView>(R.id.lockedRecyclerView)

        unlockedRecyclerView.adapter = AchievementAdapter(unlocked, isLockedList = false)
        lockedRecyclerView.adapter = AchievementAdapter(locked, isLockedList = true)

        unlockedRecyclerView.layoutManager = LinearLayoutManager(this)
        lockedRecyclerView.layoutManager = LinearLayoutManager(this)

        // 1. Get unlocked achievement IDs for this user
        db.collection("user_achievements")
            .whereEqualTo("userId", userId)
            .whereEqualTo("completed", true)
            .get()
            .addOnSuccessListener { userAchievementDocs ->
                unlockedIds.addAll(userAchievementDocs.mapNotNull { it.getString("achievementId") })
                // 2. Get all achievements
                db.collection("achievements")
                    .get()
                    .addOnSuccessListener { achievementDocs ->
                        for (doc in achievementDocs) {
                            val achievement = doc.toObject(Achievements::class.java)
                            if (unlockedIds.contains(achievement.achievementId)) {
                                unlocked.add(achievement)
                            } else {
                                locked.add(achievement)
                            }
                        }
                        // 3. Update adapters
                        unlockedRecyclerView.adapter = AchievementAdapter(unlocked, isLockedList = false)
                        lockedRecyclerView.adapter = AchievementAdapter(locked, isLockedList = true)
                        skeleton.showOriginal()
                    }
            }
    }


}