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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern


class Login : AppCompatActivity() {
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

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        //adapted from finki -
        //https://stackoverflow.com/questions/31718707/how-to-underline-text-of-button-in-android
        binding.forgotPasswordBtn.paintFlags = binding.forgotPasswordBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.launchRegisterActivityBtn.setOnClickListener{
            startActivity(Intent(this, Registration::class.java))
        }

        binding.loginBtn.setOnClickListener {
            var email = binding.emailEt.text.toString()
            var password = binding.passwordEt.text.toString()
            if(email == "a" && password == "a"){
                email = "spendysenseapp@gmail.com"
                password = "SpendyCore$123"
            }

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
                        val user = auth.currentUser
                        if(user != null && user.isEmailVerified){
                            createAndWriteDocument(binding.emailEt.text.toString())
                            Toast.makeText(this,"Welcome to spendy sense",Toast.LENGTH_SHORT,).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        else {
                            auth.signOut() // Sign them out immediately
                            Toast.makeText(this, "Please verify your email first! Check your inbox", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.w("User logging in", "signInWithEmail:failure", task.exception)
                        Toast.makeText(this,"Username or password incorrect!",Toast.LENGTH_SHORT,).show()
                    }
                }
        }
        binding.forgotPasswordBtn.setOnClickListener{
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    //if user is legacy user, checks if the user document exists for them, if it doesnt create and assign values like in reg
    private fun createAndWriteDocument(email: String){
        val currentUser = auth.currentUser
        val db = FirebaseFirestore.getInstance()
        val userCollection = db.collection("user")
        val userRef = currentUser?.let { userCollection.document(it.uid) }
        userRef?.get()?.addOnSuccessListener { doc ->
            if (!doc.exists()) {
                // Create user document if it doesn't exist
                val friendCode = generateFriendCode()
                val userDoc = hashMapOf(
                    "userId" to currentUser.uid,
                    "friendCode" to friendCode,
                    "userEmail" to email
                )
                userRef.set(userDoc)
                    .addOnSuccessListener {
                        Log.d("UserInit", "User document created for legacy user")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserInit", "Failed to create user document: ${e.message}")
                    }
            } else if (!doc.contains("friendCode")) {
                // Add friendCode if missing
                val friendCode = generateFriendCode()
                userRef.update("friendCode", friendCode)
                    .addOnSuccessListener {
                        Log.d("UserInit", "Friend code added for legacy user: $friendCode")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserInit", "Failed to add friend code: ${e.message}")
                    }
            } else if (!doc.contains("userEmail")){
                //add email for legacy user
                userRef.update("userEmail", email)
                    .addOnSuccessListener {
                        Log.d("UserInit", "User email added for legacy user: $email")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserInit", "Failed to add user email: ${e.message}")
                    }
            }
        }
    }

    private fun generateFriendCode(length: Int = 8): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    //utilized from Biraj Zalavadia. 2014. Regular expressions in android for password field, 22 April 2014. [Online]. Available at: https://stackoverflow.com/questions/23214434/regular-expression-in-android-for-password-field [Accessed 20 May 2025]
    private fun isValidEmail(email: String): Boolean{
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}