package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.RoomDB.UserDao
import com.example.spendysenseapp.RoomDB.Users
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.ActivityDeleteAccountBinding
import kotlinx.coroutines.launch

class DeleteAccount : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteAccountBinding
    private lateinit var currentUser : Users
    private val usersDao: UserDao by lazy {
        SpendySenseDatabase.getDatabase(applicationContext).userDao()
    }
    private lateinit var sessionManager : SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delete_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager.getInstance(applicationContext)
        lifecycleScope.launch {
            currentUser = sessionManager.getCurrentUser()
        }

        binding.deleteAccountBtn.setOnClickListener{
            if(binding.passwordEt.text.toString() == currentUser.Password){
                lifecycleScope.launch {
                    usersDao.delete(currentUser)
                }
                sessionManager.clearSession()
            }
            Toast.makeText(this, "User deleted successfully", Toast.LENGTH_LONG).show()
        }
        startActivity(Intent(this, WelcomePage::class.java))
        finish()

    }
}