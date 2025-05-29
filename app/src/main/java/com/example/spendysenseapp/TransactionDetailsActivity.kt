package com.example.spendysenseapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.StorageService
import com.example.spendysenseapp.databinding.ActivityTransactionDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min

class TransactionDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionDetailsBinding
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val transactionId = intent.getStringExtra("TRANSACTION_ID") ?: ""

        lifecycleScope.launch {
            val firestoreService = FirestoreService("transactions", Transaction::class.java)
            val transaction = withContext(Dispatchers.IO) {
                firestoreService.get(transactionId)
            }

            transaction?.let { it ->
                binding.transactionNameTv.text = it.name
                binding.transactionCategoryIdTv.text = it.categoryId
                binding.transactionAmountTv.text = String.format(Locale.getDefault(), "%.2f", it.amount)
                binding.transactionTypeTv.text = it.type
                binding.transactionDateTv.text = formatDate(it.dateCreated)

                if (it.receiptImage.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        Glide.with(this@TransactionDetailsActivity)
                            .load(it.receiptImage)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(binding.transactionImageIv)
                    }
                } else {
                    binding.transactionImageIv.visibility = View.GONE
                    binding.noImageUploadedTv.visibility = View.VISIBLE
                }
            }
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun formatDate(date: Long): String {
        return dateFormatter.format(date)
    }

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