package com.example.spendysenseapp

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.spendysenseapp.RoomDB.Feedback
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserFeedback : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_feedback)

        // Get references to the UI elements
        val enterText = findViewById<EditText>(R.id.EnterText)
        val submitButton = findViewById<Button>(R.id.Submitbtn)

        submitButton.setOnClickListener {
            val feedbackText = enterText.text.toString()

            // Validate that the feedback text is not empty
            if (feedbackText.isNotEmpty()) {
                // Create the Feedback object
                val feedback = Feedback(
                    id = 0,  // Room will auto-generate the ID
                    UserId = 1, // Set the UserId to the current user ID
                    Title = "User Feedback", // You can customize the title as needed
                    Description = feedbackText
                )

                // Insert the feedback into the database using a coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    // Get the DAO from the Room database
                    val feedbackDao = SpendySenseDatabase.getDatabase(application).feedbackDao()

                    // Insert the feedback into the database
                    feedbackDao.insertFeedback(feedback)

                    // Switch back to the main thread to show a Toast
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UserFeedback, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()

                        // Optionally, clear the EditText after submitting
                        enterText.text.clear()
                    }
                }
            } else {
                // Handle the case where the feedback is empty
                Toast.makeText(this, "Please enter some feedback", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
