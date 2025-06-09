package com.example.spendysenseapp

import android.R
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
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
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

class FilterByCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilterByCategoryBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private var currentUser: FirebaseUser? = null
    val firestoreTransactionService = FirestoreService("transactions", Transaction::class.java)
    val firestoreCategoryService = FirestoreService("categories", Categories::class.java)
    private lateinit var transactionDetailsLauncher: ActivityResultLauncher<Intent>
    private var selectedCategoryId: String? = null
    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var categoriesList: List<Categories> = emptyList()
    //for displaying in the pie chart
    private var minGoal: Double? = null
    private var maxGoal: Double? = null
    private lateinit var categoryPieChart: PieChart


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
        categoryPieChart = binding.categoryPieChart

        binding.backBtn.setOnClickListener {
            finish()
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

        fetchUserGoals()
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
            categoriesList = categories
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
                    if (position == 0) {
                        // Default selection, clear icon
                        selectedCategoryId = null
                        binding.imgIcon.setImageResource(com.example.spendysenseapp.R.drawable.ic_launcher_foreground)
                        return
                    }

                    val selectedCategory = categoriesList[position - 1] // Adjust index
                    selectedCategoryId = selectedCategory.id

                    if (!selectedCategory.iconImgPath.isNullOrEmpty()) {
                        setCategoryIcon(selectedCategory.iconImgPath)
                    } else {
                        binding.imgIcon.setImageResource(com.example.spendysenseapp.R.drawable.ic_launcher_foreground)
                    }
                    filterTransactions()
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedCategoryId = null
                    binding.imgIcon.setImageResource(com.example.spendysenseapp.R.drawable.ic_launcher_foreground)
                }
            }
        }
    }
    private fun setCategoryIcon(imgPath: String) {
        val resId = resources.getIdentifier(imgPath, "drawable", applicationContext.packageName)
        binding.imgIcon.setImageResource(if (resId != 0) resId else com.example.spendysenseapp.R.drawable.ic_launcher_foreground)
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
                updateCategoryPieChart(filtered)
                updateTransactionList(filtered)
                updateCategoryTotal(filtered)
                if(filtered.isEmpty()){
                    binding.noDataTv.visibility = View.VISIBLE
                    binding.pieChartLinLay.visibility = View.GONE
                }
                else {
                    binding.noDataTv.visibility = View.GONE
                    binding.pieChartLinLay.visibility = View.VISIBLE
                }
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
    private fun updateCategoryPieChart(transactions: List<Transaction>) {
        val categorySums = transactions.groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val entries = mutableListOf<PieEntry>()
        for ((catId, sum) in categorySums) {
            val category = categoriesList.find { it.id == catId }
            val name = category?.CategoryName ?: "Unknown"
            entries.add(PieEntry(sum.toFloat(), name))
        }

        val dataSet = PieDataSet(entries, "Category Split").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            valueTextColor = android.graphics.Color.BLACK
        }

        // Format center text with min and max goals
        val minGoalStr = minGoal?.let { "Min: R${"%.2f".format(it)}" } ?: "Min: No goal set"
        val maxGoalStr = maxGoal?.let { "Max: R${"%.2f".format(it)}" } ?: "Max: No goal set"
        val centerText = "$minGoalStr\n$maxGoalStr"

        categoryPieChart.apply {
            data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            this.centerText = centerText
            setCenterTextSize(16f)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            isDrawHoleEnabled = true
            holeRadius = 50f
            transparentCircleRadius = 45f
            legend.isEnabled = true
            legend.textColor = Color.WHITE
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            animateY(1000)
            invalidate()
        }
    }
    private fun fetchUserGoals() {
        val userId = currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance()
            .collection("user").document(userId)
        lifecycleScope.launch {
            val snapshot = withContext(Dispatchers.IO) { userRef.get().await() }
            if (snapshot.exists()) {
                minGoal = snapshot.getDouble("minimumGoal")
                maxGoal = snapshot.getDouble("maximumGoal")
                //updates chart when data is loaded
                filterTransactions()
            }
        }
    }
}