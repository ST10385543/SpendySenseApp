package com.example.spendysenseapp.RoomDB

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spendysenseapp.R

//the activity is here to try and somewhat obfuscate the contained action
class WashingSomethingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_washing_something)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val rootLayout = findViewById<RelativeLayout>(R.id.main)
        val bubbleGameView = WashingSomething(
            this,
            //this timer to check for 15 seconds
            onTimerFinish = { score ->
                runOnUiThread {
                    //from this you can probably tell its purpose
                    showSuggestionDialog("Your bubble popping score: $score")
                    Handler(Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 2000)
                }
            },
            onBackPressed = {
                finish()
            }
        )
        rootLayout.addView(bubbleGameView)
    }

    //a toast suggestion dialog to show the score
    private fun showSuggestionDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Bubble Game Result")
            .setMessage(message)
            .setPositiveButton("Continue") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}