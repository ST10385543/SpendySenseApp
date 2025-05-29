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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeleteAccount : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.deleteAccountBtn.setOnClickListener {
            val enteredPassword = binding.passwordEt.text.toString()
            auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser == null) {
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Show progress and disable button
            binding.progressBar.visibility = View.VISIBLE
            binding.deleteAccountBtn.isEnabled = false
            binding.deleteAccountBtn.text = "Deleting..."

            lifecycleScope.launch {
                verifyPassword(enteredPassword) { isCorrect ->
                    if (isCorrect) {
                        lifecycleScope.launch {
                            val userId = currentUser.uid
                            val categoryService = FirestoreService("categories", Categories::class.java)
                            val transactionService = FirestoreService("transactions", Transaction::class.java)
                            val userService = FirestoreService("users", Users::class.java)
                            try {
                                withContext(Dispatchers.IO) {
                                    val categories = categoryService.getAll().filter { it.userId == userId }
                                    categories.forEach { category ->
                                        category.id?.let { categoryService.delete(it) }
                                    }
                                    val transactions = transactionService.getAll().filter { it.userID == userId }
                                    transactions.forEach { transaction ->
                                        transaction.id?.let { transactionService.delete(it) }
                                    }
                                    userService.delete(userId)
                                }
                                withContext(Dispatchers.Main) {
                                    currentUser.delete().addOnCompleteListener { task ->
                                        // Hide progress and enable button
                                        binding.progressBar.visibility = View.GONE
                                        binding.deleteAccountBtn.isEnabled = true
                                        binding.deleteAccountBtn.text = "Delete"
                                        if (task.isSuccessful) {
                                            Toast.makeText(this@DeleteAccount, "Account deleted", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this@DeleteAccount, WelcomePage::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(this@DeleteAccount, "Failed to delete account", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    binding.progressBar.visibility = View.GONE
                                    binding.deleteAccountBtn.isEnabled = true
                                    binding.deleteAccountBtn.text = "Delete"
                                    Toast.makeText(this@DeleteAccount, "Error deleting user data", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            binding.progressBar.visibility = View.GONE
                            binding.deleteAccountBtn.isEnabled = true
                            binding.deleteAccountBtn.text = "Delete"
                            Toast.makeText(this@DeleteAccount, "Incorrect password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun verifyPassword(enteredPassword: String, result: (Boolean) -> Unit) {
        auth = FirebaseAuth.getInstance()
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
}