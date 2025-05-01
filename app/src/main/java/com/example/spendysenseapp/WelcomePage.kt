package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find buttons
        val loginButton = findViewById<Button>(R.id.Loginbtn)
        val registerButton = findViewById<Button>(R.id.Registerbtn)

        // Set click listeners
        loginButton.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, Registration::class.java))
        }
    }
}
