package com.example.spendysenseapp.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.TransactionAdapter
import com.example.spendysenseapp.CreateCategoryActivity
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.TransactionDetailsActivity
import com.example.spendysenseapp.databinding.FragmentHomeBinding
import com.example.spendysenseapp.RoomDB.WashingSomethingActivity
import com.faltenreich.skeletonlayout.Skeleton
import com.google.firebase.auth.FirebaseUser
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
        transactionDetailsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data?.getBooleanExtra("TRANSACTION_DELETED", false) == true) {
                loadTransactionData()
            }
        }
        skeleton = binding.homeSkeleton
        skeleton.showSkeleton()

        lifecycleScope.launch {
            currentUser = sessionManager.getCurrentUser()!!
            if (currentUser == null) {
                Toast.makeText(requireContext(), "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
                return@launch
            }
            setupRecyclerView()
            setCurrentMonth()
            changeLinearLayout()
            fillValues()
            loadTransactionData()
            setMonthlyGoal()
            skeleton.showOriginal()
        }
        binding.SpendySenseLogoIv.setOnClickListener {
            startActivity(Intent(requireContext(), WashingSomethingActivity::class.java))
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

    private fun loadTransactionData(){
        lifecycleScope.launch {
            val firestoreService = FirestoreService("transactions", Transaction::class.java)
            val transactions = withContext(Dispatchers.IO) {
                val allTransactions = firestoreService.getMostRecent()
                currentUser?.let { user ->
                    allTransactions.filter { it.userID == user.uid }
                } ?: emptyList()
            }
            transactionAdapter.updateData(transactions)
        }
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

//    private suspend fun fillValues(){
//        val transactions = withContext(Dispatchers.IO) {
//            transactionsDao.getUserTransactionSortedByMonth(currentUser.uid, getCurrentYearMonth())
//        }
//
//        var totalIncome = 0.0
//        var totalExpense = 0.0
//
//        transactions.forEach { transaction ->
//            when(transaction.type) {
//                "income" -> totalIncome += transaction.amount
//                "expense" -> totalExpense += transaction.amount
//            }
//        }
//        val balance = totalIncome - totalExpense
//
//        withContext(Dispatchers.Main) {
//            binding.balanceValueTv.text = "%.2f".format(balance)
//            binding.incomeValueTv.text = "%.2f".format(totalIncome)
//            binding.expenseValueTv.text = "%.2f".format(totalExpense)
//        }
//    }
    private suspend fun fillValues() {
        val firestoreService = FirestoreService("transactions", Transaction::class.java)
        val transactions = withContext(Dispatchers.IO) {
            val allTransactions = firestoreService.getMostRecent()
            currentUser?.let { user ->
                allTransactions.filter {
                    it.userID == user.uid
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

        withContext(Dispatchers.Main) {
            binding.balanceValueTv.text = "%.2f".format(balance)
            binding.incomeValueTv.text = "%.2f".format(totalIncome)
            binding.expenseValueTv.text = "%.2f".format(totalExpense)
        }
    }
    private fun setMonthlyGoal() {
        val firestore = FirebaseFirestore.getInstance()
        val userId = currentUser.uid
        val budgetRef = firestore.collection("userMonthlyBudget").document(userId)

        // Load and display existing goals
        lifecycleScope.launch {
            val snapshot = withContext(Dispatchers.IO) { budgetRef.get().await() }
            if (snapshot.exists()) {
                val minGoal = snapshot.getDouble("minimumGoal") ?: 0.0
                val maxGoal = snapshot.getDouble("maximumGoal") ?: 0.0
                withContext(Dispatchers.Main) {
                    if (minGoal != 0.0) binding.minimumMonthlyGoalTv.text = "Min: R$minGoal"
                    if (maxGoal != 0.0) binding.maximumMonthlyGoalTv.text = "Max: R$maxGoal"
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
                val snapshot = withContext(Dispatchers.IO) { budgetRef.get().await() }
                val currentMax = snapshot.getDouble("maximumGoal") ?: 0.0
                if (currentMax != 0.0 && minGoal >= currentMax) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Minimum goal must be less than maximum goal", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val data = mapOf("minimumGoal" to minGoal)
                budgetRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
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
                budgetRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
                withContext(Dispatchers.Main) {
                    binding.maximumMonthlyGoalEt.text.clear()
                    binding.maximumMonthlyGoalTv.text = "Max: R$maxGoal"
                    Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show()
                }
            }
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