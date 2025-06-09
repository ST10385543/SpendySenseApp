package com.example.spendysenseapp

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import com.example.spendysenseapp.Services.AchievementManager
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.ActivityCalculatorBinding
import com.google.firebase.auth.FirebaseUser
import java.util.logging.Handler
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class CalculatorActivity : AppCompatActivity() {

    // Inspired by:
    // Foxandroid. 2025. How to Make Calculator App in Android Studio || Calculator App Tutorial || 2022,
    // Youtube [Online]
    // Avaiable at: https://www.youtube.com/watch?v=-VsatCUSxek [Accessed 30 April 2025]

    private lateinit var binding: ActivityCalculatorBinding
    private var expression = ""
    private var currentUser: FirebaseUser? = null //for achievements


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var sessionManager = SessionManager.getInstance(applicationContext)
        currentUser = sessionManager.getCurrentUser()

        val buttonValues = mapOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2", binding.btn3 to "3",
            binding.btn4 to "4", binding.btn5 to "5", binding.btn6 to "6", binding.btn7 to "7",
            binding.btn8 to "8", binding.btn9 to "9",
            binding.btnAddition to "+", binding.btnSubtract to "-",
            binding.btnMultiply to "*", binding.btnDivide to "/",
            binding.btnComma to "."
        )

        buttonValues.forEach { (button, value) ->
            button.setOnClickListener {
                expression += value
                binding.tvCalcResult.text = expression
                updateCalculateButtonState()
            }
        }

        binding.btnAC.setOnClickListener {
            expression = ""
            binding.tvCalcResult.text = "0"
            updateCalculateButtonState()
        }


        binding.btnDel.setOnClickListener {
            if (expression.isNotEmpty()) {
                expression = expression.dropLast(1)
                binding.tvCalcResult.text = if (expression.isEmpty()) "0" else expression
                updateCalculateButtonState()
            }
        }

        binding.btnEquals.setOnClickListener {
            try {
                val result = evaluateExpression(expression)
                binding.tvCalcResult.text = result.toString()
                expression = result.toString()
                updateCalculateButtonState()
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid expression", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCalculate.setOnClickListener {
            if (isCompleteNumber(expression)) {
                val result = binding.tvCalcResult.text.toString()

                // Achievement check
                if (result == "800") {
                    val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.spooktune)
                    mediaPlayer?.start()

                    // Stop playback after 9 seconds (7000 milliseconds)
                    mediaPlayer?.let { player ->
                        val handler = android.os.Handler()
                        handler.postDelayed({
                            if (player.isPlaying) {
                                player.stop()
                            }
                            player.release()
                        }, 9000)
                    }

                    //the ghost ASCII art
                    binding.tvCalcResult.apply {
                        gravity = Gravity.CENTER
                        text = """
     .-"      "-.
    /            \
   |              |
   |,  .-.  .-.  ,|
   | )(_o/  \o_)( |
   |/     /\     \|
   (_     ^^     _)
    \__|IIIIII|__/
     | \IIIIII/ |
     \          /
      `--------`
            """.trimIndent()
                    }

                    Toast.makeText(applicationContext, "BOO !!! 👻", Toast.LENGTH_SHORT).show()

                    //achievement for the spooky message
                    AchievementManager.checkAndUnlock(
                        currentUser?.uid ?: "",
                        "find_spooky_message",
                        onUnlocked = { achievement ->
                            Toast.makeText(applicationContext, "🎉 Achievement unlocked: ${achievement.achievementName} unlocked!", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Toast.makeText(applicationContext, "⚠️ Could not unlock achievement", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Coroutine delay for 3 seconds
                lifecycleScope.launch {
                    delay(7000) // 7 seconds
                    val intent = Intent().apply {
                        putExtra("calc_result", result)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }

            } else {
                Toast.makeText(this, "Please complete your calculation first", Toast.LENGTH_SHORT).show()
                updateCalculateButtonState()
            }
        }


        // Handle back navigation to AddTransactionFragment
        // Kumar, M. 2025. Goodbye to onBackPressed(): A Guide to Modern Back press Handling in Android [Online]
        // Avaiable at: https://www.youtube.com/watch?v=dxqD8FqMPRs [Accessed 30 May 2025]
        binding.btnReturnToAddTrans.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize button state at startup
        updateCalculateButtonState()
    }

    private fun updateCalculateButtonState() {
        val drawable = ContextCompat.getDrawable(this, R.drawable.btn_calc_rounded)?.mutate()
        if (isCompleteNumber(expression)) {
            binding.btnCalculate.isEnabled = true
            binding.btnCalculate.text = "Confirm   Amount?"
            drawable?.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark), PorterDuff.Mode.SRC_IN)
        } else {
            binding.btnCalculate.isEnabled = false
            binding.btnCalculate.text = "Complete Calculation"
            drawable?.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark), PorterDuff.Mode.SRC_IN)
        }
        drawable?.let {
            binding.btnCalculate.background = it
        }
    }

    private fun isCompleteNumber(expr: String): Boolean {
        val number = expr.toDoubleOrNull()
        return number != null && number >= 0 && !expr.contains(Regex("[+\\-*/]"))
    }

    private fun evaluateExpression(expression: String): Double {
        return object {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: ${expression[pos]}")
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    x = when {
                        eat('+'.code) -> x + parseTerm()
                        eat('-'.code) -> x - parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    x = when {
                        eat('*'.code) -> x * parseFactor()
                        eat('/'.code) -> x / parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                    while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: ${ch.toChar()}")
                }

                return x
            }
        }.parse()
    }
}
