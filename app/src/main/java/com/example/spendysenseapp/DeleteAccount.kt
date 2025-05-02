package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.ActivityDeleteAccountBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeleteAccount : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager.getInstance(applicationContext)

        binding.deleteAccountBtn.setOnClickListener {
            val enteredPassword = binding.passwordEt.text.toString()

            lifecycleScope.launch {
                val userDao = SpendySenseDatabase.getDatabase(applicationContext).userDao()
                val transactionDao = SpendySenseDatabase.getDatabase(applicationContext).transactionDao()
                val currentUser = sessionManager.getCurrentUser()

                if (enteredPassword == currentUser.Password) {
                    withContext(Dispatchers.IO) {
                        transactionDao.deleteTransactionsByUserId(currentUser.id)
                        userDao.delete(currentUser)
                        sessionManager.clearSession()
                    }

                    Toast.makeText(this@DeleteAccount, "User deleted successfully", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@DeleteAccount, WelcomePage::class.java))
                    finish()
                } else {
                    Toast.makeText(this@DeleteAccount, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
