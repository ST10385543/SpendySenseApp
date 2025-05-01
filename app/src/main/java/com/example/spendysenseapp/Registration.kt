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
import com.example.spendysenseapp.databinding.ActivityRegistrationBinding
import kotlinx.coroutines.launch

class Registration : AppCompatActivity() {

    private lateinit var db: SpendySenseDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //enable view binding for interaction with UI components
        val binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //initalise db
        db = SpendySenseDatabase.getDatabase(this)
        userDao = db.userDao()

        binding.signUpbtn.setOnClickListener {
            var username = binding.edtUsername.text.toString()
            Toast.makeText(this,
            username,
            Toast.LENGTH_SHORT)
            .show()

            var userpassword = binding.passwordEt.text.toString()
            Toast.makeText(this,
               userpassword,
                Toast.LENGTH_SHORT)
                .show()
            var confirmPassword = binding.confirmPasswordEt.text.toString()
            Toast.makeText(this,
                confirmPassword,
                Toast.LENGTH_SHORT)
                .show()


            if (username.isNotEmpty() && userpassword == confirmPassword) {
                val newUser = Users(userEmail = username, Password = userpassword)

                lifecycleScope.launch {
                    userDao.insert(newUser)
                    // Optionally navigate to next screen or show success message
                    val intent = Intent(this@Registration, WelcomePage::class.java)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this,
                    "Sign-up failed. Please check your input.",
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}