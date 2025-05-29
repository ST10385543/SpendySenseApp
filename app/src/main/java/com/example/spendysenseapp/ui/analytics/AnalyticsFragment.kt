package com.example.spendysenseapp.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.RoomDB.Categories
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var incomePieChart: PieChart
    private lateinit var expensePieChart: PieChart
    private lateinit var barChart: BarChart

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager.getInstance(requireContext())

        incomePieChart = binding.incomePieChart
        expensePieChart = binding.expensePieChart
        barChart = binding.summaryBarChart  // Ensure your layout has this BarChart with this ID

        fetchTransactionData()
    }

    private fun fetchTransactionData() {
        lifecycleScope.launch {
            val currentUser = sessionManager.getCurrentUser()
            if (currentUser == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val transactionService = FirestoreService("transactions", Transaction::class.java)
            val categoryService = FirestoreService("categories", Categories::class.java)

            val allTransactions = withContext(Dispatchers.IO) {
                transactionService.getAll().filter { it.userID == currentUser.uid }
            }

            // Filter transactions by current month and year
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)

            val transactions = allTransactions.filter {
                val timestamp = it.dateCreated ?: return@filter false
                val date = Date(timestamp)
                val cal = Calendar.getInstance()
                cal.time = date
                cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth
            }

            val incomeMap = mutableMapOf<String, Double>()
            val expenseMap = mutableMapOf<String, Double>()

            // For weekly aggregation bar chart
            val incomeByWeek = mutableMapOf<Int, Double>()
            val expenseByWeek = mutableMapOf<Int, Double>()

            for (transaction in transactions) {
                val amount = transaction.amount ?: 0.0
                val type = transaction.type?.lowercase() ?: continue
                val categoryId = transaction.categoryId ?: continue

                val category = withContext(Dispatchers.IO) {
                    categoryService.get(categoryId)
                }

                val categoryName = category?.CategoryName ?: "Unknown"

                when (type) {
                    "income" -> incomeMap[categoryName] = incomeMap.getOrDefault(categoryName, 0.0) + amount
                    "expense" -> expenseMap[categoryName] = expenseMap.getOrDefault(categoryName, 0.0) + amount
                }

                // Weekly grouping for bar chart
                val timestamp = transaction.dateCreated ?: continue
                val date = Date(timestamp)
                val cal = Calendar.getInstance()
                cal.time = date
                val weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH)

                when (type) {
                    "income" -> incomeByWeek[weekOfMonth] = incomeByWeek.getOrDefault(weekOfMonth, 0.0) + amount
                    "expense" -> expenseByWeek[weekOfMonth] = expenseByWeek.getOrDefault(weekOfMonth, 0.0) + amount
                }
            }

            displayIncomeChart(incomeMap)
            displayExpenseChart(expenseMap)
            displayBarChart(incomeByWeek, expenseByWeek)
        }
    }

    private fun displayIncomeChart(incomeMap: Map<String, Double>) {
        val sortedIncome = incomeMap.entries.sortedByDescending { it.value }
        val entries = sortedIncome.map {
            PieEntry(it.value.toFloat(), "${it.key} (R${"%.2f".format(it.value)})")
        }

        val baseColor = Color.parseColor("#2196F3") // Blue base
        val colors = generateShades(baseColor, entries.size)

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextColor = Color.BLACK
            valueTextSize = 14f
        }

        incomePieChart.apply {
            data = PieData(dataSet)
            setUsePercentValues(false)
            description.isEnabled = false
            centerText = "Income"
            setCenterTextSize(20f)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            isDrawHoleEnabled = true
            holeRadius = 50f
            transparentCircleRadius = 45f
            legend.isEnabled = true
            animateY(1000)
            invalidate()
        }
    }

    private fun displayExpenseChart(expenseMap: Map<String, Double>) {
        val sortedExpense = expenseMap.entries.sortedByDescending { it.value }
        val entries = sortedExpense.map {
            PieEntry(it.value.toFloat(), "${it.key} (R${"%.2f".format(it.value)})")
        }

        val baseColor = Color.parseColor("#F44336") // Red base
        val colors = generateShades(baseColor, entries.size)

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextColor = Color.BLACK
            valueTextSize = 14f
        }

        expensePieChart.apply {
            data = PieData(dataSet)
            setUsePercentValues(false)
            description.isEnabled = false
            centerText = "Expense"
            setCenterTextSize(20f)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            isDrawHoleEnabled = true
            holeRadius = 50f
            transparentCircleRadius = 45f
            legend.isEnabled = true
            animateY(1000)
            invalidate()
        }
    }

    private fun displayBarChart(incomeByWeek: Map<Int, Double>, expenseByWeek: Map<Int, Double>) {
        val maxWeek = maxOf(
            incomeByWeek.keys.maxOrNull() ?: 0,
            expenseByWeek.keys.maxOrNull() ?: 0,
            4 // show at least 4 weeks
        )

        val incomeEntries = mutableListOf<BarEntry>()
        val expenseEntries = mutableListOf<BarEntry>()

        for (week in 1..maxWeek) {
            incomeEntries.add(BarEntry(week.toFloat(), incomeByWeek.getOrDefault(week, 0.0).toFloat()))
            expenseEntries.add(BarEntry(week.toFloat(), expenseByWeek.getOrDefault(week, 0.0).toFloat()))
        }

        val incomeSet = BarDataSet(incomeEntries, "Income").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }
        val expenseSet = BarDataSet(expenseEntries, "Expense").apply {
            color = Color.RED
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }

        val barData = BarData(incomeSet, expenseSet).apply {
            barWidth = 0.4f
        }

        barChart.apply {
            data = barData
            description.isEnabled = false
            setDrawGridBackground(false)
            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setCenterAxisLabels(true)
                axisMinimum = 0f
                axisMaximum = maxWeek + 1f
                valueFormatter = IndexAxisValueFormatter((1..maxWeek).map { "Week $it" })
                setDrawGridLines(false)
            }

            axisLeft.apply {
                axisMinimum = 0f
                granularity = 1f
            }

            barData.groupBars(0f, 0.2f, 0.05f)

            legend.isEnabled = true
            animateY(1000)
            invalidate()
        }
    }

    private fun generateShades(baseColor: Int, count: Int): List<Int> {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)

        val shades = mutableListOf<Int>()
        val brightnessStep = 0.5f / count.coerceAtLeast(1)

        for (i in 0 until count) {
            val newValue = (hsv[2] - (i * brightnessStep)).coerceAtLeast(0.2f)
            hsv[2] = newValue
            shades.add(Color.HSVToColor(hsv.clone()))
        }

        return shades
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
