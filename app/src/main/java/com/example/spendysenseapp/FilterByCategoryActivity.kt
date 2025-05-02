package com.example.spendysenseapp

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
import com.example.spendysenseapp.RoomDB.CategoriesDao
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.RoomDB.TransactionsDao
import com.example.spendysenseapp.RoomDB.Users
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.ActivityFilterByCategoryBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilterByCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilterByCategoryBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactionDao: TransactionsDao
    private lateinit var categoryDao: CategoriesDao
    private lateinit var sessionManager: SessionManager
    private var currentUser: Users? = null

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

        // Initialize database and services
        transactionDao = SpendySenseDatabase.getDatabase(this).transactionDao()
        categoryDao = SpendySenseDatabase.getDatabase(this).categoryDao()
        sessionManager = SessionManager.getInstance(this)

        // Setup RecyclerView
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
            currentUser = sessionManager.getCurrentUser()
            currentUser?.let { user ->
                loadTransactionsAndCategories(user.id)
            }
        }
    }

    private suspend fun loadTransactionsAndCategories(userId: Int) {
        val transactions = withContext(Dispatchers.IO) {
            transactionDao.getUsersTransactions(userId)
        }

        val categoryIds = transactions
            .map { it.categoryId }
            .distinct()

        val categories = withContext(Dispatchers.IO) {
            categoryDao.getCategoriesByIds(categoryIds)
        }

        withContext(Dispatchers.Main) {
            setupCategoryFilter(categories)
            updateTransactionList(transactions)
            updateCategoryTotal(transactions)
        }
    }

    private fun setupCategoryFilter(categories: List<Categories>) {
        binding.categoryChipGroup.removeAllViews()

        // Add "All Categories" chip
        binding.categoryChipGroup.addView(createChip("All Categories", true))

        // Add category chips
        categories.forEach { category ->
            binding.categoryChipGroup.addView(createChip(category.CategoryName, false))
        }

        // Handle selection
        binding.categoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedCategory = when {
                checkedIds.isEmpty() -> null
                group.getChildAt(0).id == checkedIds.first() -> null // "All" selected
                else -> categories[group.indexOfChild(findViewById(checkedIds.first())) - 1]
            }
            filterTransactions(selectedCategory?.id)
        }
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

    private fun filterTransactions(categoryId: Int?) {
        lifecycleScope.launch {
            val transactions = withContext(Dispatchers.IO) {
                if (categoryId == null) {
                    transactionDao.getUsersTransactions(currentUser?.id ?: 0)
                } else {
                    transactionDao.getTransactionsByCategory(currentUser?.id ?: 0, categoryId)
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