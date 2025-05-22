package com.example.spendysenseapp.ui.ViewTransaction

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.TransactionAdapter
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.RoomDB.TransactionsDao
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.FragmentViewTransactionBinding
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewTransactionFragment : Fragment() {
    private var _binding: FragmentViewTransactionBinding? = null
    private var currentUser: FirebaseUser? = null
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

        binding.editTransactionBtn.setOnClickListener{
            Toast.makeText(requireContext(), "Not implemented yet!",Toast.LENGTH_LONG).show()
        }
        binding.deleteTransactionBtn.setOnClickListener{
            Toast.makeText(requireContext(), "Not implemented yet!",Toast.LENGTH_LONG).show()
        }
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
                currentUser?.let { transactionsDao.getUsersTransactions(it.uid) }
            }
            if (transactions != null) {
                transactionAdapter.updateData(transactions)
            }
        }
    }
}