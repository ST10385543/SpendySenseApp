package com.example.spendysenseapp

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.spendysenseapp.RoomDB.Feedback
import com.example.spendysenseapp.Services.AchievementManager
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.SessionManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class UserFeedback : AppCompatActivity() {

    private val firestoreService = FirestoreService("feedback", Feedback::class.java)
    private lateinit var sessionManager: SessionManager
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_feedback)

        val enterText = findViewById<EditText>(R.id.EnterText)
        val submitButton = findViewById<Button>(R.id.Submitbtn)
        val backButton = findViewById<Button>(R.id.backbtn)
        sessionManager = SessionManager.getInstance(applicationContext)

        currentUser = sessionManager.getCurrentUser()

        backButton.setOnClickListener {
            finish()
        }

        submitButton.setOnClickListener {
            val feedbackText = enterText.text.toString()
            val feedbackId = "user_feedback_${UUID.randomUUID().toString().substring(0, 8)}_${
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    Date()
                )}"
            if (feedbackText.isNotEmpty()) {
                val feedback = currentUser?.let { it1 ->
                    Feedback(
                        id = feedbackId,
                        UserId = it1.uid,
                        Title = "User Feedback",
                        Description = feedbackText
                    )
                }

                val documentId = UUID.randomUUID().toString()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (feedback != null) {
                            firestoreService.add(documentId, feedback)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@UserFeedback, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                            enterText.text.clear()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@UserFeedback, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                        }
                    }
                    currentUser?.let { it1 ->
                        AchievementManager.checkAndUnlock(
                            //pass the current user id, as shown in the achievementManager class
                            it1.uid,
                            //the name of the event, not nessesarily the achievement_id, can use
                            //this to obcuscate the achievement name
                            "provide_user_feedback",
                            //make sure the onUnlock passes an achievement lambda to get the achievement name
                            //this is a successful achievement
                            //next, in the createCategoryActivity, you will see an example of a counter achievement
                            //passes to achievementManager
                            //line 98
                            onUnlocked = { achievement ->
                                Toast.makeText(applicationContext, "🎉 Achievement unlocked: ${achievement.achievementName} unlocked!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                Toast.makeText(applicationContext, "⚠️ Could not unlock achievement", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            } else {
                Toast.makeText(this, "Please enter some feedback", Toast.LENGTH_SHORT).show()
            }
        }
    }
}