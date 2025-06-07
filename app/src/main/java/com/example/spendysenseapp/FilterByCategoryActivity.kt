package com.example.spendysenseapp

import android.R
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.TransactionAdapter
import com.example.spendysenseapp.RoomDB.Categories
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.ActivityFilterByCategoryBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

class FilterByCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilterByCategoryBinding
    private lateinit var transactionAdapter: TransactionAdapter
    //private lateinit var transactionDao: TransactionsDao
    //private lateinit var categoryDao: CategoriesDao
    private var currentUser: FirebaseUser? = null
    val firestoreTransactionService = FirestoreService("transactions", Transaction::class.java)
    val firestoreCategoryService = FirestoreService("categories", Categories::class.java)
    private lateinit var transactionDetailsLauncher: ActivityResultLauncher<Intent>
    private var selectedCategoryId: String? = null
    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFilterByCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        transactionDetailsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data?.getBooleanExtra("TRANSACTION_DELETED", false) == true) {
                setupRecyclerView()
            }
        }

        val sessionManager = SessionManager.getInstance(this)
        if (!sessionManager.isLoggedIn()) {
            finish()
            startActivity(Intent(this, Login::class.java))
            return
        }
        currentUser = sessionManager.getCurrentUser()

        setupCategorySpinner()
        setupDatePickers()
        setupRecyclerView()
        filterTransactions()
        binding.resetDateBtn.setOnClickListener {
            val today = System.currentTimeMillis()
            startDateMillis = today
            endDateMillis = today
            binding.startDateBtn.text = dateFormat.format(Date(today))
            binding.endDateBtn.text = dateFormat.format(Date(today))
            filterTransactions()
        }
    }
    private fun setupCategorySpinner() {
        lifecycleScope.launch {
            val categories = firestoreCategoryService.getAll().filter { it.userId == currentUser?.uid }
            val categoryNames = mutableListOf("All Categories")
            val categoryIds = mutableListOf<String?>(null)
            categories.forEach {
                categoryNames.add(it.CategoryName)
                categoryIds.add(it.id)
            }
            val adapter = ArrayAdapter(this@FilterByCategoryActivity, R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            binding.categorySpinner.adapter = adapter
            binding.categorySpinner.setSelection(0)
            binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedCategoryId = categoryIds[position]
                    filterTransactions()
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun setupDatePickers() {
        val today = System.currentTimeMillis()
        startDateMillis = today
        endDateMillis = today
        binding.startDateBtn.text = dateFormat.format(Date(today))
        binding.endDateBtn.text = dateFormat.format(Date(today))

        binding.startDateBtn.setOnClickListener {
            showDatePicker { millis ->
                startDateMillis = millis
                binding.startDateBtn.text = dateFormat.format(Date(millis))
                // If end date is before new start date, reset end date to start date
                if (endDateMillis != null && endDateMillis!! < millis) {
                    endDateMillis = millis
                    binding.endDateBtn.text = dateFormat.format(Date(millis))
                }
                filterTransactions()
            }
        }
        binding.endDateBtn.setOnClickListener {
            showDatePicker { millis ->
                if (startDateMillis != null && millis < startDateMillis!!) {
                    Toast.makeText(this, "End date can't be before start date", Toast.LENGTH_SHORT).show()
                    return@showDatePicker
                }
                endDateMillis = millis
                binding.endDateBtn.text = dateFormat.format(Date(millis))
                filterTransactions()
            }
        }
    }

    private fun showDatePicker(onDateSet: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            onDateSet(calendar.timeInMillis)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
    private fun filterTransactions() {
        lifecycleScope.launch {
            val allTransactions = firestoreTransactionService.getAll().filter { it.userID == currentUser?.uid }
            val filtered = allTransactions.filter { tx ->
                val matchesCategory = selectedCategoryId == null || tx.categoryId == selectedCategoryId
                val matchesStart = startDateMillis == null || tx.dateCreated >= startDateMillis!!
                val matchesEnd = endDateMillis == null || tx.dateCreated <= endDateMillis!!
                matchesCategory && matchesStart && matchesEnd
            }
            withContext(Dispatchers.Main) {
                updateTransactionList(filtered)
                updateCategoryTotal(filtered)
                binding.noDataTv.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateTransactionList(transactions: List<Transaction>) {
        transactionAdapter.updateData(transactions.sortedByDescending { it.amount }.toMutableList())
    }

    private fun updateCategoryTotal(transactions: List<Transaction>) {
        val totalExpenses = transactions
            //if we want to filter with expense or income only, will ask the boys
            //.filter { it.type.equals("expense", ignoreCase = true) }
            .sumOf { it.amount }
        binding.categoryTotalTv.text = "Total spent: R${"%.2f".format(totalExpenses)}"
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(mutableListOf()){ transactionId ->
            val intent = Intent(applicationContext, TransactionDetailsActivity::class.java)
            intent.putExtra("TRANSACTION_ID", transactionId)
            transactionDetailsLauncher.launch(intent)
        }
        binding.transactionRv.apply {
            layoutManager = LinearLayoutManager(this@FilterByCategoryActivity)
            adapter = transactionAdapter
        }
    }
}