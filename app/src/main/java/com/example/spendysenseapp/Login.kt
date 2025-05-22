package com.example.spendysenseapp

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.RoomDB.UserDao
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.ActivityLoginBinding
import com.example.spendysenseapp.databinding.ActivityRegistrationBinding
import com.example.spendysenseapp.ui.home.HomeFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern


class Login : AppCompatActivity() {
//    private lateinit var db: SpendySenseDatabase
//    private lateinit var userDao: UserDao
//    private lateinit var sessionManager: SessionManager
        private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //sessionManager = SessionManager.getInstance(this)
        //enable view binding for interaction with UI components
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        //initalise db
//        db = SpendySenseDatabase.getDatabase(this)
//        userDao = db.userDao()

        //adapted from finki -
        //https://stackoverflow.com/questions/31718707/how-to-underline-text-of-button-in-android
        binding.forgotPasswordBtn.paintFlags = binding.forgotPasswordBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.launchRegisterActivityBtn.setOnClickListener{
            startActivity(Intent(this, Registration::class.java))
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(!isValidEmail(email)){
                Toast.makeText(this, "Email not in correct format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Firebase. n.d. Get started with firebase authentication on android. [Online]. Available at: https://firebase.google.com/docs/auth/android/start [Accessed 20 May 2025]
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task ->
                    if(task.isSuccessful){
                        //show the use of logging
                        Log.d("User logging in", "signInWithEmail:success")
                        Toast.makeText(this,"Welcome to spendy sense",Toast.LENGTH_SHORT,).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        Log.w("User logging in", "signInWithEmail:failure", task.exception)
                        Toast.makeText(this,"Authentication failed.",Toast.LENGTH_SHORT,).show()
                    }
                }
//            lifecycleScope.launch {
//                val user = userDao.findByEmail(username)
//
//                if (user != null && user.Password == password) {
//                    Toast.makeText(this@Login, "Login successful!", Toast.LENGTH_SHORT).show()
//                    if(sessionManager.getCurrentUserId() == null)
//                    {
//                        sessionManager.saveUserSession(user)
//                    }
//                    Toast.makeText(this@Login, user.id.toString(), Toast.LENGTH_SHORT).show()
//
//                    startActivity(Intent(this@Login, MainActivity::class.java))
//                    finish()
//                } else {
//                    Toast.makeText(this@Login, "Login failed. Check your email and password.", Toast.LENGTH_SHORT).show()
//                }
//
//                auth.signInWithEmailAndPassword(email, password)
//            }
        }
    }

    //utilized from Biraj Zalavadia. 2014. Regular expressions in android for password field, 22 April 2014. [Online]. Available at: https://stackoverflow.com/questions/23214434/regular-expression-in-android-for-password-field [Accessed 20 May 2025]
    private fun isValidEmail(email: String): Boolean{
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}