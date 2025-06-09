package com.example.spendysenseapp.ui.home

import android.R.attr.button
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.TransactionAdapter
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.RoomDB.WashingSomethingActivity
import com.example.spendysenseapp.Services.AchievementManager
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.TransactionDetailsActivity
import com.example.spendysenseapp.databinding.FragmentHomeBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.AggregateField.count
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    //private lateinit var currentUser : Users
//    private val transactionsDao: TransactionsDao by lazy {
//        SpendySenseDatabase.getDatabase(requireContext()).transactionDao()
//    }

//    private val usersDao: UserDao by lazy {
//        SpendySenseDatabase.getDatabase(requireContext()).userDao()
//    }
    private lateinit var sessionManager : SessionManager
    //make global current user item
    private lateinit var currentUser: FirebaseUser

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var transactionAdapter: TransactionAdapter

    private lateinit var transactionDetailsLauncher: ActivityResultLauncher<Intent>

    //skeleton while data loads
    //gotten from Fahlteich, P. 2025. SkeletonLayout: Skeleton view pattern for Android, Github. [Online].
    //Avaiable at: https://github.com/Faltenreich/SkeletonLayout [Accessed 29 May 2025]
    private lateinit var skeleton: Skeleton

    //Pie Chart inspired by CodingWithMitch (2016) Creating a Simple Pie Chart in Android Studio, YouTube video, [Online].
    //Available at: https://www.youtube.com/watch?v=8BcTXbwDGbg [Accessed: 29 May 2025].
    private lateinit var budgetPieChart: PieChart

    private var chartMaxGoal: Double = 0.0
    private var chartTotalExpense: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        //instantiate it and populate it
        sessionManager = SessionManager.getInstance(requireContext())
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        transactionDetailsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data?.getBooleanExtra("TRANSACTION_DELETED", false) == true) {
                lifecycleScope.launch {
                    loadTransactionData()
                }
            }
        }
        skeleton = binding.homeSkeleton
        skeleton.showSkeleton()
        budgetPieChart = binding.budgetPieChart

        lifecycleScope.launch {
            currentUser = sessionManager.getCurrentUser()!!
            setupRecyclerView()
            setCurrentMonth()
            changeLinearLayout()
            fillValues()
            loadTransactionData()
            setMonthlyGoal()
            skeleton.showOriginal()
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }

        var logoClickCount = 0
        var lastClickTime = 0L
        binding.SpendySenseLogoIv.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 1000) {
                logoClickCount = 0
            }
            //this is the launcher for washing something activity
            logoClickCount++
            lastClickTime = currentTime
            if (logoClickCount == 3) {
                logoClickCount = 0
                //so, to check for an achievement, call the class AchievementManager
                //then the checkAndUnlock with the following information:
                AchievementManager.checkAndUnlock(
                    //pass the current user id, as shown in the achievementManager class
                    currentUser.uid,
                    //the name of the event, not nessesarily the achievement_id, can use
                    //this to obcuscate the achievement name
                    "washing_something",
                    //make sure the onUnlock passes an achievement lambda to get the achievement name
                    //this is a successful achievement
                    //next, in the createCategoryActivity, you will see an example of a counter achievement
                    //passes to achievementManager
                    //line 98
                    onUnlocked = { achievement ->
                        Toast.makeText(requireContext(), "🎉 Achievement unlocked: ${achievement.achievementName} unlocked!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {
                        Toast.makeText(requireContext(), "⚠️ Could not unlock achievement", Toast.LENGTH_SHORT).show()
                    }
                    )
                startActivity(Intent(requireContext(), WashingSomethingActivity::class.java))
            }
        }
    }

    private fun changeLinearLayout(){
        binding.setMonthlyBudgetSw.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                binding.recentTransactionsLinLay.visibility = View.GONE
                binding.monthlyGoalLinLay.visibility = View.VISIBLE
            }
            else {
                binding.recentTransactionsLinLay.visibility = View.VISIBLE
                binding.monthlyGoalLinLay.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerView(){
        transactionAdapter = TransactionAdapter(mutableListOf()){ transactionId ->
            val intent = Intent(requireContext(), TransactionDetailsActivity::class.java)
            intent.putExtra("TRANSACTION_ID", transactionId)
            transactionDetailsLauncher.launch(intent)
        }
        binding.transactionRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private suspend fun loadTransactionData(){
            val firestoreService = FirestoreService("transactions", Transaction::class.java)
            val transactions = withContext(Dispatchers.IO) {
                val allTransactions = firestoreService.getMostRecent()
                currentUser?.let { user ->
                    allTransactions.filter { it.userID == user.uid }
                } ?: emptyList()
            }
            transactionAdapter.updateData(transactions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setCurrentMonth(){
        val calendar = Calendar.getInstance()
        val monthString = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
        binding.currentMonthTv.text = "${monthString}"
    }

    private fun getCurrentYearMonth(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
    }
    private fun getYearMonthFromMillis(millis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
    }

    private suspend fun fillValues() {
        val firestoreService = FirestoreService("transactions", Transaction::class.java)
        val yearMonth = getCurrentYearMonth()
        val transactions = withContext(Dispatchers.IO) {
            val allTransactions = firestoreService.getAll()
            currentUser?.let { user ->
                allTransactions.filter {
                    it.userID == user.uid && getYearMonthFromMillis(it.dateCreated) == yearMonth
                }
            } ?: emptyList()
        }
        var totalIncome = 0.0
        var totalExpense = 0.0

        transactions.forEach { transaction ->
            when (transaction.type) {
                "income" -> totalIncome += transaction.amount
                "expense" -> totalExpense += transaction.amount
            }
        }
        val balance = totalIncome - totalExpense
        chartTotalExpense = totalExpense

        withContext(Dispatchers.Main) {
            _binding?.let { binding ->
                binding.balanceValueTv.text = "%.2f".format(balance)
                binding.incomeValueTv.text = "%.2f".format(totalIncome)
                binding.expenseValueTv.text = "%.2f".format(totalExpense)
                updateBudgetPieChart(chartTotalExpense, chartMaxGoal)
            }
        }
    }
    private suspend fun setMonthlyGoal() {
        val firestore = FirebaseFirestore.getInstance()
        val userId = currentUser.uid
        val userRef = firestore.collection("user").document(userId)

        // Load and display existing goals
            val snapshot = withContext(Dispatchers.IO) { userRef.get().await() }
            if (snapshot.exists()) {
                val minGoal = snapshot.getDouble("minimumGoal") ?: 0.0
                val maxGoal = snapshot.getDouble("maximumGoal") ?: 0.0
                withContext(Dispatchers.Main) {
                    if (minGoal != 0.0) binding.minimumMonthlyGoalTv.text = "Min: R$minGoal"
                    if (maxGoal != 0.0) {
                        chartMaxGoal = maxGoal
                        binding.maximumMonthlyGoalTv.text = "Max: R$maxGoal"
                        updateBudgetPieChart(chartTotalExpense, chartMaxGoal)
                    }
                }
            }

        binding.setMinimumGoalBtn.setOnClickListener {
            val minGoalStr = binding.minimumMonthlyGoalEt.text.toString()
            if (minGoalStr.isEmpty()) {
                Toast.makeText(requireContext(), "No value entered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val minGoal = minGoalStr.toDouble()
            lifecycleScope.launch {
                val snapshot = withContext(Dispatchers.IO) { userRef.get().await() }
                val currentMax = snapshot.getDouble("maximumGoal") ?: 0.0
                if (currentMax != 0.0 && minGoal >= currentMax) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Minimum goal must be less than maximum goal", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val data = mapOf("minimumGoal" to minGoal)
                userRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
                withContext(Dispatchers.Main) {
                    binding.minimumMonthlyGoalEt.text.clear()
                    binding.minimumMonthlyGoalTv.text = "Min: R$minGoal"
                    Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.setMaximumGoalBtn.setOnClickListener {
            val maxGoalStr = binding.maximumMonthlyGoalEt.text.toString()
            if (maxGoalStr.isEmpty()) {
                Toast.makeText(requireContext(), "No value entered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val maxGoal = maxGoalStr.toDouble()
            lifecycleScope.launch {
                val data = mapOf("maximumGoal" to maxGoal)
                userRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
                withContext(Dispatchers.Main) {
                    binding.maximumMonthlyGoalEt.text.clear()
                    binding.maximumMonthlyGoalTv.text = "Max: R$maxGoal"
                    Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show()
                    chartMaxGoal = maxGoal
                    updateBudgetPieChart(chartTotalExpense, chartMaxGoal)
                }
            }
        }
    }
    private fun updateBudgetPieChart(totalExpense: Double, maxGoal: Double) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        // Expense section (red)
        if (totalExpense > 0) {
            entries.add(PieEntry(totalExpense.toFloat(), "Spent"))
            colors.add(android.graphics.Color.RED)
        }

        // Remaining budget (grey)
        val remaining = (maxGoal - totalExpense).coerceAtLeast(0.0)
        if (remaining > 0) {
            entries.add(PieEntry(remaining.toFloat(), "Remaining"))
            colors.add(android.graphics.Color.LTGRAY)
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 16f
            valueTextColor = android.graphics.Color.BLACK
        }

        budgetPieChart.apply {
            data = PieData(dataSet)
            budgetPieChart.setUsePercentValues(true)
            val data = PieData(dataSet)
            data.setValueFormatter(PercentFormatter(budgetPieChart))
            budgetPieChart.data = data
            description.isEnabled = false
            centerText = "Budget: R${"%.2f".format(maxGoal)}"
            setCenterTextSize(18f)
            setEntryLabelColor(android.graphics.Color.BLACK)
            setEntryLabelTextSize(14f)
            setDrawEntryLabels(false)
            isDrawHoleEnabled = true
            holeRadius = 50f
            transparentCircleRadius = 45f
            legend.isEnabled = true
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER  // Horizontal center
            legend.xEntrySpace = 10f  // Spacing between legend entries
            legend.yEntrySpace = 5f
            legend.textSize = 15f  // Adjust text size if needed
            animateY(1000)
            invalidate()
        }

        // Show overbudget warning if needed
        val warningTv = binding.overBudgetWarningTv
        if (totalExpense > maxGoal && maxGoal > 0.0) {
            val overAmount = totalExpense - maxGoal
            warningTv.text = "You are overbudget by R${"%.2f".format(overAmount)}"
            warningTv.visibility = View.VISIBLE
            //added style
            android.text.style.StyleSpan(android.graphics.Typeface.BOLD)
        } else {
            warningTv.visibility = View.GONE
        }
    }

//    private fun setMonthlyGoal(){
//        binding.setMinimumGoalBtn.setOnClickListener{
//            if(binding.minimumMonthlyGoalEt.text.toString().equals("")){
//                Toast.makeText(requireContext(), "No value entered", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//            val minGoal = binding.minimumMonthlyGoalEt.text.toString()
//            lifecycleScope.launch {
//                val user = usersDao.getUser(currentUser.id).apply{
//                    minimumGoal = minGoal.toDouble()
//                }
//                usersDao.updateUser(user)
//            }
//            binding.minimumMonthlyGoalEt.text.clear()
//            binding.minimumMonthlyGoalTv.text = "Min: R${minGoal}"
//            Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show()
//        }
//        binding.setMaximumGoalBtn.setOnClickListener{
//            if(binding.maximumMonthlyGoalEt.text.toString().equals("")){
//                Toast.makeText(requireContext(), "No value entered", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//            val maxGoal = binding.maximumMonthlyGoalEt.text.toString()
//            lifecycleScope.launch {
//                val user = usersDao.getUser(currentUser.id).apply{
//                    maximumGoal = maxGoal.toDouble()
//                }
//                usersDao.updateUser(user)
//            }
//            binding.maximumMonthlyGoalEt.text.clear()
//            binding.maximumMonthlyGoalTv.text = "Max: R${maxGoal}"
//            Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show()
//        }
//    }

//    private fun setTextViewForGoals(){
//        lifecycleScope.launch {
//            if(currentUser.minimumGoal != 0.0){
//                binding.minimumMonthlyGoalTv.text = "Min: R${currentUser.minimumGoal}"
//            }
//            if(currentUser.maximumGoal != 0.0){
//                binding.maximumMonthlyGoalTv.text = "Max: R${currentUser.maximumGoal}"
//            }
//        }
//    }
}