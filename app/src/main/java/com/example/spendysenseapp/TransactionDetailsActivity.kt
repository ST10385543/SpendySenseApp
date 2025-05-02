package com.example.spendysenseapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.databinding.ActivityLoginBinding
import com.example.spendysenseapp.databinding.ActivityTransactionDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionDetailsBinding
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val binding = ActivityTransactionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val transactionId = intent.getIntExtra("TRANSACTION_ID", 0)

        lifecycleScope.launch {
            val transaction = withContext(Dispatchers.IO){
                SpendySenseDatabase.getDatabase(this@TransactionDetailsActivity)
                    .transactionDao()
                    .getTransactionById(transactionId)
            }

            transaction?.let{
                binding.transactionNameTv.text = it.name
                binding.transactionCategoryIdTv.text = it.categoryId.toString()
                binding.transactionAmountTv.text = it.amount.toString()
                binding.transactionTypeTv.text = it.type
                binding.transactionDateTv.text = formatDate(it.DateCreated)
                //binding.transactionImageIv =
            }
        }
    }
    private fun formatDate(date: Date): String {
        return dateFormatter.format(date)
    }
}