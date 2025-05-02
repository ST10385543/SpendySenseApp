package com.example.spendysenseapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
import kotlin.math.min

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

            transaction?.let{ it ->
                binding.transactionNameTv.text = it.name
                binding.transactionCategoryIdTv.text = it.categoryId.toString()
                binding.transactionAmountTv.text = it.amount.toString()
                binding.transactionTypeTv.text = it.type
                binding.transactionDateTv.text = formatDate(it.DateCreated)
                // Handle the image ByteArray if it exists
                it.receiptImage?.let { byteArray ->
                    // Convert on background thread to avoid UI freeze
                    val bitmap = withContext(Dispatchers.Default) {
                        byteArray.toBitmap()
                    }
                    // Update UI on main thread
                    withContext(Dispatchers.Main) {
                        binding.transactionImageIv.setImageBitmap(bitmap)
                    }
                }?: run {
                    // Set default image if no image exists
                    binding.transactionImageIv.setImageResource(R.drawable.ic_launcher_foreground)
                }
            }
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
    }
    private fun formatDate(date: Date): String {
        return dateFormatter.format(date)
    }
    // Extension function for ByteArray to Bitmap conversion
    private fun ByteArray.toBitmap(): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(this, 0, this.size)?.let { originalBitmap ->
                val maxWidth = resources.displayMetrics.widthPixels
                val maxHeight = (maxWidth * 0.75).toInt()

                if (originalBitmap.width > maxWidth || originalBitmap.height > maxHeight) {
                    val scaleFactor = min(
                        maxWidth.toFloat() / originalBitmap.width,
                        maxHeight.toFloat() / originalBitmap.height
                    )
                    Bitmap.createScaledBitmap(
                        originalBitmap,
                        (originalBitmap.width * scaleFactor).toInt(),
                        (originalBitmap.height * scaleFactor).toInt(),
                        true
                    )
                } else {
                    originalBitmap
                }
            }
        } catch (e: Exception) {
            Log.e("ImageConversion", "Failed to convert byte array to bitmap", e)
            null
        }
    }
}