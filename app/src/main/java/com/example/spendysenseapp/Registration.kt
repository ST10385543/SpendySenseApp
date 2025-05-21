package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spendysenseapp.databinding.ActivityRegistrationBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.regex.Matcher
import java.util.regex.Pattern


class Registration : AppCompatActivity() {

    //private lateinit var db: SpendySenseDatabase
    //private lateinit var userDao: UserDao
    private lateinit var auth: FirebaseAuth

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

        try {
            FirebaseApp.initializeApp(this)
            Log.d("Registration", "Firebase initialized")
        } catch (e: IllegalStateException) {
            Log.d("Registration", "Firebase already initialized")
        }

        auth = Firebase.auth

        binding.launchLoginActivityBtn.setOnClickListener{
            startActivity(Intent(this, Login::class.java))
        }


        //initalise db
//        db = SpendySenseDatabase.getDatabase(this)
//        userDao = db.userDao()

        binding.signUpbtn.setOnClickListener {
            val email = binding.edtEmail.text.toString()
//            Toast.makeText(this,
//            username,
//            Toast.LENGTH_SHORT)
//            .show()

            val password = binding.passwordEt.text.toString()
//            Toast.makeText(this,
//               userpassword,
//                Toast.LENGTH_SHORT)
//                .show()

            val confirmPassword = binding.confirmPasswordEt.text.toString()
//            Toast.makeText(this,
//                confirmPassword,
//                Toast.LENGTH_SHORT)
//                .show()

            if(email.isEmpty() || !isValidEmail(email)){
                Toast.makeText(this, "Please enter a valid email address",
                Toast.LENGTH_SHORT)
                .show()
                return@setOnClickListener
            }

            if(password.isEmpty() || !isValidPassword(password)){
                Toast.makeText(this, "Password must contain at least 4 characters, including a digit, a lowercase letter, an uppercase letter, and a special character",
                    Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this,
                    "Password and confirm password do not match",
                    Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            Log.d("Registration", "Attempting to create user with email: $email")
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        Log.d("Registration", "Task completed: ${task.isSuccessful}")
                        if (task.isSuccessful) {
                            Log.d("Registration", "User ID: ${task.result?.user?.uid}")
                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, WelcomePage::class.java))
                        } else {
                            Toast.makeText(this, "Account creation failed", Toast.LENGTH_SHORT).show()
                            Log.e("Registration", "Error: ${task.exception?.printStackTrace()}")
                        }
                    }
                    .addOnCanceledListener {
                        Log.e("Registration", "Operation canceled")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Registration", "Failure: ${e.stackTraceToString()}")
                    }
            } catch (e: Exception) {
                Log.e("Registration", "Caught exception: ${e.stackTraceToString()}")
            }
//                val newUser = Users(userEmail = username, Password = userpassword, minimumGoal = 0.0, maximumGoal = 0.0)
//
//                lifecycleScope.launch {
//                    userDao.insert(newUser)
//                    // Optionally navigate to next screen or show success message
//                    val intent = Intent(this@Registration, WelcomePage::class.java)
//                    startActivity(intent)
//                }
        }
    }

    //utilized from iversoncru. 2013. How to check valid email format entered in EditText?, 29 May 2013. [Online]. Available at: https://stackoverflow.com/questions/16812039/how-to-check-valid-email-format-entered-in-edittext [Accessed 20 May 2025]
    private fun isValidEmail(email: String): Boolean{
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    //utilized from Biraj Zalavadia. 2014. Regular expressions in android for password field, 22 April 2014. [Online]. Available at: https://stackoverflow.com/questions/23214434/regular-expression-in-android-for-password-field [Accessed 20 May 2025]
    fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern

        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{4,}$"

        pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher: Matcher = pattern.matcher(password)

        return matcher.matches()
    }
}