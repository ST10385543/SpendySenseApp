package com.example.spendysenseapp.ui.AddTransaction

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.CalculatorActivity
import com.example.spendysenseapp.CreateCategoryActivity
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.Categories
import com.example.spendysenseapp.RoomDB.CategoriesDao
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.Services.StorageService
import com.example.spendysenseapp.databinding.FragmentAddTransactionBinding
import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.Deflater

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private var currentUser: FirebaseUser? = null

    private lateinit var db: SpendySenseDatabase
    private lateinit var categoryDao: CategoriesDao
    private lateinit var sessionManager : SessionManager

    private var selectedCategoryId: String? = null
    private var selectedImageBytes: ByteArray? = null
    private var transactionType: String? = null

    //firestore db
//    private var firedb = Firebase.firestore

    private val calculatorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            val resultValue = data?.getStringExtra("calc_result")
            resultValue?.let {
                binding.edtAmount.setText(it)
            }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                // Get compressed bytes directly
                val compressedBytes = uriToCompressedByteArray(it, maxSizeKB = 300) // Target ~300KB
                selectedImageBytes = compressedBytes

                // Preview (optional: decode if needed)
                val bitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
                binding.imgPreview.setImageBitmap(bitmap)
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = SpendySenseDatabase.getDatabase(requireContext())
        categoryDao = db.categoryDao()

        sessionManager = SessionManager.getInstance(requireContext())

        lifecycleScope.launch {
            currentUser = sessionManager.getCurrentUser()
        }

        loadCategoriesIntoSpinner()

        binding.imgCalc.setOnClickListener {
            val intent = Intent(requireContext(), CalculatorActivity::class.java)
            calculatorLauncher.launch(intent)
        }

        binding.btnAddCategory.setOnClickListener {
            val intent = Intent(requireContext(), CreateCategoryActivity::class.java)
            startActivity(intent)
        }

        binding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        binding.btnCreate.setOnClickListener {
            saveTransaction()
        }

        // Income Button Logic
        binding.btnIncome.setOnClickListener {
            selectIncomeType()
        }

        // Expense Button Logic
        binding.btnExpense.setOnClickListener {
            selectExpenseType()
        }
    }

    private fun loadCategoriesIntoSpinner() {
        lifecycleScope.launch {
            val firestoreService = FirestoreService("categories", Categories::class.java)
            val categories = firestoreService.getAll().filter { it.userId == (currentUser?.uid ?: "") }
            withContext(Dispatchers.Main) {
                val categoryNames = categories.map { it.CategoryName }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spnCategory.adapter = adapter

                binding.spnCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedCategoryId = categories.getOrNull(position)?.id
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedCategoryId = null
                    }
                }
            }
        }
    }

    private fun saveTransaction() {
        val name = binding.edtTransactionName.text.toString().trim()
        val amountStr = binding.edtAmount.text.toString().trim()

        if (name.isEmpty() || amountStr.isEmpty() || selectedCategoryId == null || transactionType.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please complete all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress bar and disable button
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreate.isEnabled = false
        binding.btnCreate.text = "Uploading..."

        lifecycleScope.launch {
            val categoryService = FirestoreService("categories", Categories::class.java)
            val catexist = selectedCategoryId?.let { categoryService.exists(it) } ?: false
            if (!catexist) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Invalid category selected", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreate.isEnabled = true
                    binding.btnCreate.text = "Create"
                }
                return@launch
            }

            val transactionId = "Transaction_${UUID.randomUUID().toString().substring(0, 8)}_${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}"
            var imageUrl = ""

            selectedImageBytes?.let { imageBytes ->
                val storageService = StorageService()
                val imagePath = "transactions/$transactionId.jpg"
                imageUrl = storageService.uploadFile(imagePath, imageBytes)
            }

            val transaction = Transaction(
                id = transactionId,
                name = name,
                categoryId = selectedCategoryId!!,
                amount = amount,
                type = transactionType!!,
                dateCreated = System.currentTimeMillis(),
                userID = currentUser?.uid ?: "",
                receiptImage = imageUrl
            )

            val firestoreService = FirestoreService("transactions", Transaction::class.java)
            try {
                firestoreService.add(transactionId, transaction)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Transaction saved", Toast.LENGTH_SHORT).show()
                    resetForm()
                }
            } catch (e: Exception) {
                Log.w("TransactionAddingFailed", "Error writing document", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to save transaction", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreate.isEnabled = true
                    binding.btnCreate.text = "Create"
                }
            }
        }
    }

    private fun resetForm() {
        binding.edtTransactionName.setText("")
        binding.edtAmount.setText("")
        binding.spnCategory.setSelection(0)
        selectedImageBytes = null
        binding.imgPreview.setImageResource(android.R.drawable.btn_star_big_off)
        binding.btnIncome.setBackgroundResource(R.drawable.button_income_background)
        binding.btnExpense.setBackgroundResource(R.drawable.button_expense_background)
        binding.btnIncome.setElevation(0f)
        binding.btnExpense.setElevation(0f)
    }

    private fun uriToCompressedByteArray(uri: Uri, maxSizeKB: Int = 5120): ByteArray {
        return requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            // 1. Read original bytes
            val originalBytes = inputStream.readBytes()

            // 2. Compress if too large (e.g., >500KB)
            if (originalBytes.size > maxSizeKB * 5120) {
                compressByteArray(originalBytes) // Apply compression (see next step)
            } else {
                originalBytes // Already small enough
            }
        } ?: ByteArray(0)
    }

    // Helper: Compress ByteArray (lossy for JPEG/WEBP, lossless for PNG)
    private fun compressByteArray(data: ByteArray, quality: Int = 80): ByteArray {
        val outputStream = ByteArrayOutputStream()

        // Detect image type (simplified; assumes JPEG/WEBP/PNG)
        val isLikelyJpegOrWebP = data.size > 5120 && data[0] == 0xFF.toByte()

        if (isLikelyJpegOrWebP) {
            // Lossy recompression for JPEG/WEBP
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            bitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
            bitmap.recycle()
        } else {
            // Lossless for PNG (or unknown formats)
            val deflater = Deflater(Deflater.BEST_COMPRESSION)
            deflater.setInput(data)
            deflater.finish()
            val buffer = ByteArray(1024)
            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            deflater.end()
        }
        return outputStream.toByteArray()
    }

    private fun selectIncomeType() {
        // Highlight Income button, reset Expense button
        binding.btnIncome.setBackgroundResource(R.drawable.button_type_select) // Apply blue background
        binding.btnIncome.setElevation(10f)
        binding.btnExpense.setBackgroundResource(R.drawable.button_expense_background)
        binding.btnExpense.setElevation(0f)

        // Set the transaction type to "income"
        transactionType = "income"
    }

    private fun selectExpenseType() {
        // color Expense button, reset Income button
        binding.btnExpense.setBackgroundResource(R.drawable.button_type_select) // Apply blue background
        binding.btnExpense.setElevation(10f)
        binding.btnIncome.setBackgroundResource(R.drawable.button_income_background)
        binding.btnIncome.setElevation(0f)

        // Set the transaction type to "expense"
        transactionType = "expense"
    }

    override fun onResume() {
        super.onResume()
        loadCategoriesIntoSpinner()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
