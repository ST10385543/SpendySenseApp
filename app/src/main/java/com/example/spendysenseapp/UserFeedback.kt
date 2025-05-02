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

        //  references to the UI elements
        val enterText = findViewById<EditText>(R.id.EnterText)
        val submitButton = findViewById<Button>(R.id.Submitbtn)

        submitButton.setOnClickListener {
            val feedbackText = enterText.text.toString()

            // Validating that the feedback text is not empty
            if (feedbackText.isNotEmpty()) {
                // Create the Feedback object
                val feedback = Feedback(
                    id = 0,  // Room will auto-generate the ID
                    UserId = 1, // Set the UserId to the current user ID
                    Title = "User Feedback", // You can customize the title as needed
                    Description = feedbackText
                )

                // inserting the feedback into the database using a coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    // Get the DAO from the Room database
                    val feedbackDao = SpendySenseDatabase.getDatabase(application).feedbackDao()

                    feedbackDao.insertFeedback(feedback)


                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UserFeedback, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()

                        // clear EditText after submitting
                        enterText.text.clear()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter some feedback", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
