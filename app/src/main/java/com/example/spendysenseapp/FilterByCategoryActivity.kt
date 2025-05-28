package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

class FilterByCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilterByCategoryBinding
    private lateinit var transactionAdapter: TransactionAdapter
    //private lateinit var transactionDao: TransactionsDao
    //private lateinit var categoryDao: CategoriesDao
    private var currentUser: FirebaseUser? = null
    val firestoreTransactionService = FirestoreService("transactions", Transaction::class.java)
    val firestoreCategoryService = FirestoreService("categories", Categories::class.java)


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

        val sessionManager = SessionManager.getInstance(this)
        if (!sessionManager.isLoggedIn()) {
            finish()
            startActivity(Intent(this, Login::class.java))
            return
        }
        currentUser = sessionManager.getCurrentUser()

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(mutableListOf())
        binding.transactionRv.apply {
            layoutManager = LinearLayoutManager(this@FilterByCategoryActivity)
            adapter = transactionAdapter
        }
        loadUserData()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            currentUser?.let { loadTransactionsAndCategories(it.uid) }
        }
    }

    private suspend fun loadTransactionsAndCategories(userId: String) {
        // Fetch transactions from your FirestoreService if you have one, else keep using Room for now
        // For categories, use FirestoreService
        val categories = firestoreCategoryService.getAll()
        val transactions = firestoreTransactionService.getAll().filter { it.UserID == userId }

        withContext(Dispatchers.Main) {
            setupCategoryFilter(categories)
            updateTransactionList(transactions)
            updateCategoryTotal(transactions)
        }
    }

    private fun setupCategoryFilter(categories: List<Categories>) {
        binding.categoryChipGroup.removeAllViews()

        val allChip = createChip("All Categories", true).apply {
            setOnClickListener {
                filterTransactions(null)
            }
        }
        binding.categoryChipGroup.addView(allChip)

        categories.forEach { category ->
            val chip = createChip(category.CategoryName, false).apply {
                setOnClickListener {
                    filterTransactions(category.id)
                }
            }
            binding.categoryChipGroup.addView(chip)
        }

        binding.categoryChipGroup.setOnCheckedStateChangeListener(null)
    }

    private fun createChip(text: String, checked: Boolean): Chip {
        return Chip(this).apply {
            id = View.generateViewId()
            setText(text)
            isCheckable = true
            isChecked = checked
            setEnsureMinTouchTargetSize(false)
            setChipBackgroundColorResource(R.color.chip_background_color)
            setTextColor(ContextCompat.getColor(this@FilterByCategoryActivity, R.color.white))
            setTextAppearance(R.style.ChipTextAppearance)
        }
    }

    private fun filterTransactions(categoryId: String?) {
        lifecycleScope.launch {
            val transactions = withContext(Dispatchers.IO) {
                val allTransactions = firestoreTransactionService.getAll()
                allTransactions.filter {
                    it.UserID == (currentUser?.uid ?: "") &&
                            (categoryId == null || it.categoryId == categoryId)
                }
            }
            withContext(Dispatchers.Main) {
                updateTransactionList(transactions)
                updateCategoryTotal(transactions)
            }
        }
    }

    private fun updateTransactionList(transactions: List<Transaction>) {
        val sortedTransactions = transactions.sortedByDescending { it.amount }
        transactionAdapter.updateData(sortedTransactions.toMutableList())
    }

    private fun updateCategoryTotal(transactions: List<Transaction>) {
        val totalExpenses = transactions
            .filter { it.type.equals("expense", ignoreCase = true) }
            .sumOf { it.amount }

        binding.categoryTotalTv.text = "Total spent: R${"%.2f".format(totalExpenses)}"
    }
}