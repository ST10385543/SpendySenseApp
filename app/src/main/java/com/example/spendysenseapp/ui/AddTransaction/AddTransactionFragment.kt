package com.example.spendysenseapp.ui.AddTransaction

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.Deflater
import androidx.navigation.fragment.findNavController

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private var currentUser: FirebaseUser? = null
    private lateinit var db: SpendySenseDatabase
    private lateinit var categoryDao: CategoriesDao
    private lateinit var sessionManager: SessionManager

    private var selectedCategoryId: String? = null
    private var selectedImageBytes: ByteArray? = null
    private var transactionType: String? = null
    private var categoriesList: List<Categories> = emptyList()

    private val calculatorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val resultValue = result.data?.getStringExtra("calc_result")
            resultValue?.let {
                binding.edtAmount.setText(it)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { handleImageUri(it) }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            selectedImageBytes = outputStream.toByteArray()
            binding.imgPreview.setImageBitmap(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
            showImageSourceDialog()
        }

        binding.btnCreate.setOnClickListener {
            saveTransaction()
        }

        binding.btnIncome.setOnClickListener {
            selectIncomeType()
        }

        binding.btnExpense.setOnClickListener {
            selectExpenseType()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher.launch(null)
                    1 -> {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        intent.type = "image/*"
                        galleryLauncher.launch(intent)
                    }
                }
            }
            .show()
    }

    private fun loadCategoriesIntoSpinner() {
        // Reset icon and selection
        binding.imgIcon.setImageResource(R.drawable.ic_launcher_foreground)
        selectedCategoryId = null

        lifecycleScope.launch {
            val firestoreService = FirestoreService("categories", Categories::class.java)
            val categories = firestoreService.getAll().filter { it.userId == (currentUser?.uid ?: "") }

            // Save for use in selection
            categoriesList = categories

            withContext(Dispatchers.Main) {
                val categoryNames = mutableListOf("Select Category")
                categoryNames.addAll(categories.map { it.CategoryName })

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spnCategory.adapter = adapter
                binding.spnCategory.setSelection(0) // Set to "Select Category"

                binding.spnCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position == 0) {
                            // Default selection, clear icon
                            selectedCategoryId = null
                            binding.imgIcon.setImageResource(R.drawable.ic_launcher_foreground)
                            return
                        }

                        val selectedCategory = categoriesList[position - 1] // Adjust index
                        selectedCategoryId = selectedCategory.id

                        if (!selectedCategory.iconImgPath.isNullOrEmpty()) {
                            setCategoryIcon(selectedCategory.iconImgPath)
                        } else {
                            binding.imgIcon.setImageResource(R.drawable.ic_launcher_foreground)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedCategoryId = null
                        binding.imgIcon.setImageResource(R.drawable.ic_launcher_foreground)
                    }
                }
            }
        }
    }

    private fun setCategoryIcon(imgPath: String) {
        val resId = resources.getIdentifier(imgPath, "drawable", requireContext().packageName)
        binding.imgIcon.setImageResource(if (resId != 0) resId else R.drawable.ic_launcher_foreground)
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

                    findNavController().navigate(R.id.action_addTransactionFragment_to_homeFragment)
                }
            } catch (e: Exception) {
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

    private fun handleImageUri(uri: Uri) {
        val compressedBytes = uriToCompressedByteArray(uri, maxSizeKB = 300)
        selectedImageBytes = compressedBytes
        val bitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
        binding.imgPreview.setImageBitmap(bitmap)
    }

    private fun uriToCompressedByteArray(uri: Uri, maxSizeKB: Int = 5120): ByteArray {
        return requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            val originalBytes = inputStream.readBytes()
            if (originalBytes.size > maxSizeKB * 1024) {
                compressByteArray(originalBytes)
            } else {
                originalBytes
            }
        } ?: ByteArray(0)
    }

    private fun compressByteArray(data: ByteArray, quality: Int = 80): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val isLikelyJpegOrWebP = data.size > 5120 && data[0] == 0xFF.toByte()

        if (isLikelyJpegOrWebP) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            bitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
            bitmap.recycle()
        } else {
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
        binding.btnIncome.setBackgroundResource(R.drawable.button_type_select)
        binding.btnIncome.setElevation(10f)
        binding.btnExpense.setBackgroundResource(R.drawable.button_expense_background)
        binding.btnExpense.setElevation(0f)
        transactionType = "income"
    }

    private fun selectExpenseType() {
        binding.btnExpense.setBackgroundResource(R.drawable.button_type_select)
        binding.btnExpense.setElevation(10f)
        binding.btnIncome.setBackgroundResource(R.drawable.button_income_background)
        binding.btnIncome.setElevation(0f)
        transactionType = "expense"
    }

    private fun resetForm() {
        binding.edtTransactionName.setText("")
        binding.edtAmount.setText("")
        binding.spnCategory.setSelection(-1)  // Reset to default item
        selectedCategoryId = null
        selectedImageBytes = null
        transactionType = null
        binding.imgPreview.setImageResource(android.R.drawable.btn_star_big_off)
        binding.imgIcon.setImageResource(R.drawable.ic_launcher_foreground)
        binding.btnIncome.setBackgroundResource(R.drawable.button_income_background)
        binding.btnExpense.setBackgroundResource(R.drawable.button_expense_background)
        binding.btnIncome.setElevation(0f)
        binding.btnExpense.setElevation(0f)
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
