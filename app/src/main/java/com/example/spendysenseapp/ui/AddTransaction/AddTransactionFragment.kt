//package com.example.spendysenseapp.ui.AddTransaction
//
//import android.app.Activity
//import android.content.Intent
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import android.widget.Toast
//import android.widget.AdapterView
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import com.example.spendysenseapp.CalculatorActivity
//import com.example.spendysenseapp.CreateCategoryActivity
//import com.example.spendysenseapp.R
//import com.example.spendysenseapp.RoomDB.CategoriesDao
//import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
//import com.example.spendysenseapp.RoomDB.Transaction
//import com.example.spendysenseapp.RoomDB.Users
//import com.example.spendysenseapp.Services.SessionManager
//import com.example.spendysenseapp.databinding.FragmentAddTransactionBinding
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.util.*
//
//class AddTransactionFragment : Fragment() {
//
//    private var _binding: FragmentAddTransactionBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var currentUser : Users
//
//    private lateinit var db: SpendySenseDatabase
//    private lateinit var categoryDao: CategoriesDao
//    private lateinit var sessionManager : SessionManager
//
//    private var selectedCategoryId: Int? = null
//    private var selectedImageBytes: ByteArray? = null
//    private var transactionType: String? = null
//
//    private val calculatorLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == AppCompatActivity.RESULT_OK) {
//            val data = result.data
//            val resultValue = data?.getStringExtra("calc_result")
//            resultValue?.let {
//                binding.edtAmount.setText(it)
//            }
//        }
//    }
//
//    private val imagePickerLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val uri = result.data?.data
//            uri?.let {
//                val bytes = uriToByteArray(it)
//                selectedImageBytes = bytes
//                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//                binding.imgPreview.setImageBitmap(bitmap)
//            }
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        db = SpendySenseDatabase.getDatabase(requireContext())
//        categoryDao = db.categoryDao()
//
//        sessionManager = SessionManager.getInstance(requireContext())
//
//        lifecycleScope.launch {
//            currentUser = sessionManager.getCurrentUser()
//        }
//
//        loadCategoriesIntoSpinner()
//
//        binding.imgCalc.setOnClickListener {
//            val intent = Intent(requireContext(), CalculatorActivity::class.java)
//            calculatorLauncher.launch(intent)
//        }
//
//        binding.btnAddCategory.setOnClickListener {
//            val intent = Intent(requireContext(), CreateCategoryActivity::class.java)
//            startActivity(intent)
//        }
//
//        binding.btnAddImage.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            intent.type = "image/*"
//            imagePickerLauncher.launch(intent)
//        }
//
//        binding.btnCreate.setOnClickListener {
//            saveTransaction()
//        }
//
//        // Income Button Logic
//        binding.btnIncome.setOnClickListener {
//            selectIncomeType()
//        }
//
//        // Expense Button Logic
//        binding.btnExpense.setOnClickListener {
//            selectExpenseType()
//        }
//    }
//
//    private fun loadCategoriesIntoSpinner() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val categories = categoryDao.getAllCategories()
//            withContext(Dispatchers.Main) {
//                val categoryNames = categories.map { it.CategoryName }
//                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                binding.spnCategory.adapter = adapter
//
//                binding.spnCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                        selectedCategoryId = categories.getOrNull(position)?.id
//                    }
//
//                    override fun onNothingSelected(parent: AdapterView<*>?) {
//                        // Handle the case where no item is selected
//                        selectedCategoryId = null
//                    }
//                }
//            }
//        }
//    }
//
//    private fun saveTransaction() {
//        val name = binding.edtTransactionName.text.toString().trim()
//        val amountStr = binding.edtAmount.text.toString().trim()
//
//        if (name.isEmpty() || amountStr.isEmpty() || selectedCategoryId == null || transactionType.isNullOrEmpty()) {
//            Toast.makeText(requireContext(), "Please complete all fields", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val amount = amountStr.toDoubleOrNull()
//        if (amount == null) {
//            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        //check if category exist
//        lifecycleScope.launch {
//            val catexist = withContext(Dispatchers.IO) {
//                db.categoryDao().getCategoryById(selectedCategoryId!!) != null
//            }
//            if (!catexist) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Invalid category selected", Toast.LENGTH_SHORT).show()
//                }
//                return@launch
//            }
//        }
//
//
//        val transaction = Transaction(
//            name = name,
//            categoryId = selectedCategoryId!!,
//            amount = amount,
//            type = transactionType!!,
//            DateCreated = Date(),
//            UserID = currentUser.id,
//            receiptImage = selectedImageBytes
//        )
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            db.transactionDao().insertTransaction(transaction)
//            withContext(Dispatchers.Main) {
//                Toast.makeText(requireContext(), "Transaction saved", Toast.LENGTH_SHORT).show()
//                resetForm()
//            }
//        }
//    }
//
//    private fun resetForm() {
//        binding.edtTransactionName.setText("")
//        binding.edtAmount.setText("")
//        binding.spnCategory.setSelection(0)
//        selectedImageBytes = null
//        binding.imgPreview.setImageResource(android.R.drawable.btn_star_big_off)
//        binding.btnIncome.setBackgroundResource(R.drawable.button_income_background)
//        binding.btnExpense.setBackgroundResource(R.drawable.button_expense_background)
//        binding.btnIncome.setElevation(0f)
//        binding.btnExpense.setElevation(0f)
//    }
//
//    private fun uriToByteArray(uri: Uri): ByteArray {
//        return requireContext().contentResolver.openInputStream(uri)?.use {
//            it.readBytes()
//        } ?: ByteArray(0)
//    }
//
//    private fun selectIncomeType() {
//        // Highlight Income button, reset Expense button
//        binding.btnIncome.setBackgroundResource(R.drawable.button_type_select) // Apply blue background
//        binding.btnIncome.setElevation(10f)
//        binding.btnExpense.setBackgroundResource(R.drawable.button_expense_background)
//        binding.btnExpense.setElevation(0f)
//
//        // Set the transaction type to "income"
//        transactionType = "income"
//    }
//
//    private fun selectExpenseType() {
//        // color Expense button, reset Income button
//        binding.btnExpense.setBackgroundResource(R.drawable.button_type_select) // Apply blue background
//        binding.btnExpense.setElevation(10f)
//        binding.btnIncome.setBackgroundResource(R.drawable.button_income_background)
//        binding.btnIncome.setElevation(0f)
//
//        // Set the transaction type to "expense"
//        transactionType = "expense"
//    }
//
//    override fun onResume() {
//        super.onResume()
//        loadCategoriesIntoSpinner()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
