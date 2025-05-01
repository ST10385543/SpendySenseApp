package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spendysenseapp.databinding.ActivityCalculatorBinding

class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding
    private var expression = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            }
        }

        binding.btnAC.setOnClickListener {
            expression = ""
            binding.tvCalcResult.text = "0"
        }

        binding.btnDel.setOnClickListener {
            if (expression.isNotEmpty()) {
                expression = expression.dropLast(1)
                binding.tvCalcResult.text = if (expression.isEmpty()) "0" else expression
            }
        }

        binding.btnEquals.setOnClickListener {
            try {
                val result = evaluateExpression(expression)
                binding.tvCalcResult.text = result.toString()
                expression = result.toString()
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid expression", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCalculate.setOnClickListener {
            val result = binding.tvCalcResult.text.toString()
            val intent = Intent().apply {
                putExtra("calc_result", result)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
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
