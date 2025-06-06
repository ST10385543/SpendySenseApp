package com.example.spendysenseapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.spendysenseapp.RoomDB.Categories
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.StorageService
import com.example.spendysenseapp.databinding.ActivityTransactionDetailsBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min

class TransactionDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionDetailsBinding
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    //gotten from Fahlteich, P. 2025. SkeletonLayout: Skeleton view pattern for Android, Github. [Online].
    //Avaiable at: https://github.com/Faltenreich/SkeletonLayout [Accessed 29 May 2025]
    private lateinit var skeleton: Skeleton

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
        skeleton = binding.transactionDetailsSkeleteon

        skeleton.showSkeleton()

        val transactionId = intent.getStringExtra("TRANSACTION_ID") ?: ""

        lifecycleScope.launch {
            val firestoreService = FirestoreService("transactions", Transaction::class.java)
            val transaction = withContext(Dispatchers.IO) {
                firestoreService.get(transactionId)
            }

            transaction?.let { it ->
                binding.transactionNameTv.text = it.name
                binding.transactionAmountTv.text = String.format(Locale.getDefault(), "%.2f", it.amount)
                binding.transactionTypeTv.text = it.type
                binding.transactionDateTv.text = formatDate(it.dateCreated)

                val categoryService = FirestoreService("categories", Categories::class.java)
                val category = withContext(Dispatchers.IO) {
                    categoryService.get(transaction.categoryId)
                }
                withContext(Dispatchers.Main) {
                    binding.transactionCategoryNameTv.text = category?.CategoryName ?: "Unknown Category"
                }

                if (it.receiptImage.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        Glide.with(this@TransactionDetailsActivity)
                            .load(it.receiptImage)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(binding.transactionImageIv)
                    }
                    binding.noImageUploadedTv.visibility = View.GONE
                } else {
                    binding.transactionImageIv.visibility = View.GONE
                    binding.noImageUploadedTv.visibility = View.VISIBLE
                }
            }
            skeleton.showOriginal()
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.deleteTransactionBtn.setOnClickListener{
            deleteTransaction()
        }
    }

    private fun deleteTransaction(){
        val transactionId = intent.getStringExtra("TRANSACTION_ID") ?: return

        lifecycleScope.launch {
            val firestoreService = FirestoreService("transactions", Transaction::class.java)
            val storageService = StorageService()

            // Fetch transaction to get image URL
            val transaction = withContext(Dispatchers.IO) {
                firestoreService.get(transactionId)
            }

            try {
                // Delete image from storage if exists
                if (transaction != null && transaction.receiptImage.isNotEmpty()) {
                    // Extract the storage path from the URL
                    val imageUrl = transaction.receiptImage
                    val storagePath = imageUrl.substringAfter("/o/").substringBefore("?").replace("%2F", "/")
                    withContext(Dispatchers.IO) {
                        storageService.deleteFile(storagePath)
                    }
                }
                // Delete transaction from Firestore
                withContext(Dispatchers.IO) {
                    firestoreService.delete(transactionId)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TransactionDetailsActivity, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent()
                    resultIntent.putExtra("TRANSACTION_DELETED", true)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TransactionDetailsActivity, "Failed to delete transaction", Toast.LENGTH_SHORT).show()
                }
            }
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