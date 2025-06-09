package com.example.spendysenseapp.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.Categories
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.FragmentAnalyticsBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.SkeletonConfig
import com.faltenreich.skeletonlayout.createSkeleton
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
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

    // Chart views
    private lateinit var incomePieChart: PieChart
    private lateinit var expensePieChart: PieChart
    private lateinit var barChart: BarChart

    // Session manager to get the logged-in user
    private lateinit var sessionManager: SessionManager

    //gotten from Fahlteich, P. 2025. SkeletonLayout: Skeleton view pattern for Android, Github. [Online].
    //Avaiable at: https://github.com/Faltenreich/SkeletonLayout [Accessed 29 May 2025]

    //Pie Chart inspired by CodingWithMitch (2016) Creating a Simple Pie Chart in Android Studio, YouTube video, [Online].
    //Available at: https://www.youtube.com/watch?v=8BcTXbwDGbg [Accessed: 29 May 2025].

    //Bar graph inspired by KGP Talkie (2017) MPAndroidChart Tutorial Better Than Android GraphView 5- Beautiful Multiple Bar Chart, YouTube video, [Online].
    //Available at: https://www.youtube.com/watch?v=_uQrJ0TkZlc [Accessed: 29 May 2025].

    //Induct Automation, 2020 .  Code review: Modifying RGB by Manipulating HSV colors. [Online].
    //Avaiable at: https://forum.inductiveautomation.com/t/code-review-modifying-rgb-by-manipulating-hsv-colors/58295 [Accessed 29 May 2025]



    private lateinit var skeleton: Skeleton
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
        barChart = binding.summaryBarChart

        skeleton = binding.analyticsSkeleton
        // Hide "No chart data available" for all charts
        incomePieChart.setNoDataText("")
        expensePieChart.setNoDataText("")
        barChart.setNoDataText("")

        skeleton.showSkeleton()
        fetchTransactionData()
    }


     // Fetch transactions for logged-in user,
     // filter it to current month and display charts.
    private fun fetchTransactionData() {
        lifecycleScope.launch {
            val currentUser = sessionManager.getCurrentUser()
            if (currentUser == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // initialize Firestore db
            val transactionService = FirestoreService("transactions", Transaction::class.java)
            val categoryService = FirestoreService("categories", Categories::class.java)

            // Getting all transactions for the current user
            val allTransactions = withContext(Dispatchers.IO) {
                transactionService.getAll().filter { it.userID == currentUser.uid }
            }

            // Filter transactions for the current month
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

            // Maps for pie chart data
            val incomeMap = mutableMapOf<String, Double>()
            val expenseMap = mutableMapOf<String, Double>()

            // Maps for weekly bar chart
            val incomeByWeek = mutableMapOf<Int, Double>()
            val expenseByWeek = mutableMapOf<Int, Double>()


            for (transaction in transactions) {
                val amount = transaction.amount ?: 0.0
                val type = transaction.type?.lowercase() ?: continue
                val categoryId = transaction.categoryId ?: continue

                // Fetch category name
                val category = withContext(Dispatchers.IO) {
                    categoryService.get(categoryId)
                }

                val categoryName = category?.CategoryName ?: "Unknown"

                // update pie chart data maps
                when (type) {
                    "income" -> incomeMap[categoryName] = incomeMap.getOrDefault(categoryName, 0.0) + amount
                    "expense" -> expenseMap[categoryName] = expenseMap.getOrDefault(categoryName, 0.0) + amount
                }

                // determine the week of month for bar chart
                val timestamp = transaction.dateCreated ?: continue
                val date = Date(timestamp)
                val cal = Calendar.getInstance()
                cal.time = date
                val weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH)

                // Update bar chart data maps
                when (type) {
                    "income" -> incomeByWeek[weekOfMonth] = incomeByWeek.getOrDefault(weekOfMonth, 0.0) + amount
                    "expense" -> expenseByWeek[weekOfMonth] = expenseByWeek.getOrDefault(weekOfMonth, 0.0) + amount
                }
            }

            // display data on charts
            displayIncomeChart(incomeMap)
            displayExpenseChart(expenseMap)
            displayBarChart(incomeByWeek, expenseByWeek)
            skeleton.showOriginal()
        }
    }


    // Displays income data in a pie chart
    private fun displayIncomeChart(incomeMap: Map<String, Double>) {
        val sortedIncome = incomeMap.entries.sortedByDescending { it.value }
        val entries = sortedIncome.map {
            PieEntry(it.value.toFloat(), "${it.key}")
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
            //legend stuff
            legend.xEntrySpace = 3f
            legend.textColor = Color.parseColor("#FFFFFF")
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            invalidate()
        }
    }


    //Displays expense data in a pie chart
    private fun displayExpenseChart(expenseMap: Map<String, Double>) {
        val sortedExpense = expenseMap.entries.sortedByDescending { it.value }
        val entries = sortedExpense.map {
            PieEntry(it.value.toFloat(), "${it.key}")
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
            //legend stuff
            legend.xEntrySpace = 3f
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.textColor = Color.parseColor("#FFFFFF")
            invalidate()
        }
    }


    //displays a grouped bar chart of weekly income and expenses
    private fun displayBarChart(incomeByWeek: Map<Int, Double>, expenseByWeek: Map<Int, Double>) {
        val maxWeek = maxOf(
            incomeByWeek.keys.maxOrNull() ?: 0,
            expenseByWeek.keys.maxOrNull() ?: 0,
            4 // show at least 4 weeks
        )

        val incomeEntries = mutableListOf<BarEntry>()
        val expenseEntries = mutableListOf<BarEntry>()

        //prepare data entries for each week
        for (week in 1..maxWeek) {
            incomeEntries.add(BarEntry(week.toFloat(), incomeByWeek.getOrDefault(week, 0.0).toFloat()))
            expenseEntries.add(BarEntry(week.toFloat(), expenseByWeek.getOrDefault(week, 0.0).toFloat()))
        }

        val incomeSet = BarDataSet(incomeEntries, "Income").apply {
            color = Color.BLUE
            valueTextColor = Color.WHITE
            valueTextSize = 10f
        }
        val expenseSet = BarDataSet(expenseEntries, "Expense").apply {
            color = Color.RED
            valueTextColor = Color.WHITE
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

            // X-Axis config for weeks
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setCenterAxisLabels(true)
                textColor = Color.WHITE
                axisMinimum = 0f
                axisMaximum = maxWeek + 1f
                valueFormatter = IndexAxisValueFormatter((1..maxWeek).map { "Week $it" })
                setDrawGridLines(false)
            }

            axisLeft.apply {
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                axisMinimum = 0f
                granularity = 1f
            }

            // Group bars for each week
            barData.groupBars(0f, 0.2f, 0.05f)

            legend.isEnabled = true
            //legend stuff
            legend.xEntrySpace = 5f
            legend.textColor = Color.parseColor("#FFFFFF")
            animateY(1000)
            invalidate()
        }
    }


    //Generates a list of color shades from a base color for chart segments
    //inspired by https://chatgpt.com/share/68420050-0770-800b-a938-164f64c5e017
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
}
