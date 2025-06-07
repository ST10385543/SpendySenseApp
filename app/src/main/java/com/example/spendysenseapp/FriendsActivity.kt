package com.example.spendysenseapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.Adapter.AchievementAdapter
import com.example.spendysenseapp.RoomDB.Achievements
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.ActivityAchievementsBinding
import com.example.spendysenseapp.databinding.ActivityFriendsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class FriendsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFriendsBinding
    private lateinit var sessionManager : SessionManager
    //make global current user item
    private lateinit var currentUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friends)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager.getInstance(applicationContext)
        lifecycleScope.launch {
            currentUser = sessionManager.getCurrentUser()!!
            getUsersFriendCode()
        }
    }
    private fun getUsersFriendCode() {
        val db = Firebase.firestore
        db.collection("user")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                val friendCode = documents.documents[0].getString("friendCode")
                binding.userFriendCodeTv.text = friendCode ?: "No friend code"
            }
    }
}