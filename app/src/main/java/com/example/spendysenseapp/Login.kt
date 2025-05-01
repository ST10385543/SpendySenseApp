package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.RoomDB.UserDao
import com.example.spendysenseapp.databinding.ActivityLoginBinding
import com.example.spendysenseapp.databinding.ActivityRegistrationBinding
import com.example.spendysenseapp.ui.home.HomeFragment
import kotlinx.coroutines.launch


class Login : AppCompatActivity() {
    private lateinit var db: SpendySenseDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //enable view binding for interaction with UI components
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initalise db
        db = SpendySenseDatabase.getDatabase(this)
        userDao = db.userDao()

        binding.loginBtn.setOnClickListener {
            var username = binding.usernameEt.text.toString()
            var password = binding.passwordEt.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = userDao.findByEmail(username)

                if (user != null && user.Password == password) {
                    Toast.makeText(this@Login, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@Login, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@Login, "Login failed. Check your email and password.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}