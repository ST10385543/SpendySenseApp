package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.RoomDB.Categories
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.RoomDB.Users
import com.example.spendysenseapp.databinding.ActivityDeleteAccountBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DeleteAccount : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding
    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.CanceldeleteBtn.setOnClickListener {
            finish()
        }

        binding.deleteAccountBtn.setOnClickListener {
            val enteredPassword = binding.passwordEt.text.toString()
            val currentUser = auth.currentUser

            if (currentUser == null) {
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading UI
            binding.progressBar.visibility = View.VISIBLE
            binding.deleteAccountBtn.isEnabled = false
            binding.deleteAccountBtn.text = "Deleting..."

            // Re-authenticate in non-suspending callback, then call suspend function in coroutine
            verifyPassword(enteredPassword) { isCorrect ->
                if (!isCorrect) {
                    binding.progressBar.visibility = View.GONE
                    binding.deleteAccountBtn.isEnabled = true
                    binding.deleteAccountBtn.text = "Delete"
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                    return@verifyPassword
                }

                lifecycleScope.launch {
                    try {
                        val userId = currentUser.uid
                        deleteUserData(userId)

                        // Delete Firebase Auth user
                        currentUser.delete().addOnCompleteListener { task ->
                            binding.progressBar.visibility = View.GONE
                            binding.deleteAccountBtn.isEnabled = true
                            binding.deleteAccountBtn.text = "Delete"

                            if (task.isSuccessful) {
                                Toast.makeText(this@DeleteAccount, "Account deleted", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@DeleteAccount, WelcomePage::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@DeleteAccount, "Failed to delete Firebase user", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        binding.progressBar.visibility = View.GONE
                        binding.deleteAccountBtn.isEnabled = true
                        binding.deleteAccountBtn.text = "Delete"
                        Toast.makeText(this@DeleteAccount, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    private fun verifyPassword(enteredPassword: String, result: (Boolean) -> Unit) {
        val currentUser = auth.currentUser

        if (currentUser != null && currentUser.email != null) {
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, enteredPassword)
            currentUser.reauthenticate(credential)
                .addOnCompleteListener { t ->
                    result(t.isSuccessful)
                }
        } else {
            result(false)
        }
    }

    // ✅ DELETES: Categories, Transactions, Users, User_Achievements, Friend Requests, Friends
    private suspend fun deleteUserData(userId: String) = withContext(Dispatchers.IO) {
        val categoryService = FirestoreService("categories", Categories::class.java)
        val transactionService = FirestoreService("transactions", Transaction::class.java)
        val userService = FirestoreService("users", Users::class.java)

        // Delete categories
        val categories = categoryService.getAll().filter { it.userId == userId }
        categories.forEach { it.id?.let { id -> categoryService.delete(id) } }

        // Delete transactions
        val transactions = transactionService.getAll().filter { it.userID == userId }
        transactions.forEach { it.id?.let { id -> transactionService.delete(id) } }

        // Delete user profile document
        userService.delete(userId)

        val db = firestore

        // Delete user_achievements
        val achievementsQuery = db.collection("user_achievement").whereEqualTo("userId", userId).get().await()
        for (doc in achievementsQuery.documents) {
            db.collection("user_achievement").document(doc.id).delete().await()
        }

        // Delete friend_requests where user is sender or receiver
        val requestsSent = db.collection("friend_requests").whereEqualTo("fromUserId", userId).get().await()
        val requestsReceived = db.collection("friend_requests").whereEqualTo("toUserId", userId).get().await()
        for (doc in (requestsSent.documents + requestsReceived.documents)) {
            db.collection("friend_requests").document(doc.id).delete().await()
        }

        // Remove user from other people's friends lists
        val userDoc = db.collection("user").document(userId).get().await()
        val friends = userDoc.get("friends") as? List<String> ?: emptyList()
        for (friendId in friends) {
            val friendRef = db.collection("user").document(friendId)
            friendRef.update("friends", FieldValue.arrayRemove(userId)).await()
        }

        // Finally, delete user's own document in "user" collection
        db.collection("user").document(userId).delete().await()
    }
}
