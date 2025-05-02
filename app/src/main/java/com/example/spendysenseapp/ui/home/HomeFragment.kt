package com.example.spendysenseapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.TransactionAdapter
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.RoomDB.TransactionsDao
import com.example.spendysenseapp.RoomDB.Users
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var currentUser : Users
    private val transactionsDao: TransactionsDao by lazy {
        SpendySenseDatabase.getDatabase(requireContext()).transactionDao()
    }
    private lateinit var sessionManager : SessionManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //gets user information through the SessionManager class
        sessionManager = SessionManager.getInstance(requireContext())
        lifecycleScope.launch {
            currentUser = sessionManager.getCurrentUser()
            fillValues()
            loadTransactionData()
        }
        setCurrentMonth()
        setupRecyclerView()
        changeLinearLayout()
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
        transactionAdapter = TransactionAdapter(mutableListOf())
        binding.transactionRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private fun loadTransactionData(){
        lifecycleScope.launch {
            val transactions = withContext(Dispatchers.IO){
                transactionsDao.getFiveTransactions(currentUser.id).also {
                    Log.d("TRANSACTIONS", "Fetched: ${it.size} items")
                }
            }
            withContext(Dispatchers.Main) {
                transactionAdapter.updateData(transactions)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setCurrentMonth(){
        val calendar = Calendar.getInstance()
        val currentMonth = "%02d".format(
            calendar.get(Calendar.MONTH) + 1
        )
        val monthString = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
        binding.currentMonthTv.text = "${monthString}"
    }

    private fun getCurrentYearMonth(): String{
        val calendar = Calendar.getInstance()
        val currentYearMonth = "%04d-%02d".format(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1
        )
        return currentYearMonth
    }

    private suspend fun fillValues(){
        val transactions = withContext(Dispatchers.IO) {
            transactionsDao.getUserTransactionSortedByMonth(currentUser.id, getCurrentYearMonth())
        }

        var totalIncome = 0.0
        var totalExpense = 0.0

        transactions.forEach { transaction ->
            when(transaction.type) {
                "income" -> totalIncome += transaction.amount
                "expense" -> totalExpense += transaction.amount
            }
        }

        withContext(Dispatchers.Main) {
            binding.incomeValue.text = "%.2f".format(totalIncome)
            binding.expenseValue.text = "%.2f".format(totalExpense)
        }
    }
}