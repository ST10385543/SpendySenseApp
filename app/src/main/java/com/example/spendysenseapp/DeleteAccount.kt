package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
//import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.ActivityDeleteAccountBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeleteAccount : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding
    //private lateinit var sessionManager: SessionManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //sessionManager = SessionManager.getInstance(applicationContext)


        binding.deleteAccountBtn.setOnClickListener {
            val enteredPassword = binding.passwordEt.text.toString()

            lifecycleScope.launch {
                val userDao = SpendySenseDatabase.getDatabase(applicationContext).userDao()
                val transactionDao = SpendySenseDatabase.getDatabase(applicationContext).transactionDao()

                verifyPassword(enteredPassword) {isCorrect ->

                }
            }
        }
    }

    private fun verifyPassword(enteredPassword: String, result: (Boolean) -> Unit){
        //getting
        auth = Firebase.auth
        val currentUser = auth.currentUser

        if(currentUser != null && currentUser.email != null){
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, enteredPassword)

            currentUser.reauthenticate(credential)
                .addOnCompleteListener{ t ->
                    if(t.isSuccessful){
                        result(true)
                    }
                    else {
                        result(false)
                    }
                }
        } else {
            result(false)
        }
    }
}
