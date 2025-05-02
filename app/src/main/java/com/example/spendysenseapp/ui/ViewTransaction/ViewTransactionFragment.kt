package com.example.spendysenseapp.ui.ViewTransaction

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.TransactionAdapter
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.RoomDB.TransactionsDao
import com.example.spendysenseapp.RoomDB.Users
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.FragmentHomeBinding
import com.example.spendysenseapp.databinding.FragmentViewTransactionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewTransactionFragment : Fragment() {
    private var _binding: FragmentViewTransactionBinding? = null
    private lateinit var currentUser : Users
    private val transactionsDao: TransactionsDao by lazy {
        SpendySenseDatabase.getDatabase(requireContext()).transactionDao()
    }
    private lateinit var sessionManager : SessionManager
    private lateinit var transactionAdapter: TransactionAdapter

    private val binding get() = _binding!!
    companion object {
        fun newInstance() = ViewTransactionFragment()
    }

    private val viewModel: ViewTransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager.getInstance(requireContext())
        lifecycleScope.launch {
            currentUser = sessionManager.getCurrentUser()
            loadTransactionData()
        }
        setupRecyclerView()
    }
    private fun setupRecyclerView(){
        transactionAdapter = TransactionAdapter(mutableListOf())
        binding.transactionRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }
    private fun loadTransactionData(){
        lifecycleScope.launch {
            val transactions = withContext(Dispatchers.IO){
                transactionsDao.getUsersTransactions(currentUser.id)
            }
            transactionAdapter.updateData(transactions)
        }
    }
}