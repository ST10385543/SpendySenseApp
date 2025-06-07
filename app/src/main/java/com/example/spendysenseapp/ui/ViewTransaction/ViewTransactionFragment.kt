package com.example.spendysenseapp.ui.ViewTransaction

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.TransactionAdapter
import com.example.spendysenseapp.FilterByCategoryActivity
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.Transaction
import com.example.spendysenseapp.Services.FirestoreService
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.TransactionDetailsActivity
import com.example.spendysenseapp.databinding.FragmentViewTransactionBinding
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewTransactionFragment : Fragment() {
    private var _binding: FragmentViewTransactionBinding? = null
    private var currentUser: FirebaseUser? = null
    private lateinit var sessionManager : SessionManager
    private lateinit var transactionAdapter: TransactionAdapter

    //this is to update the recycler view when the transaction is deleted
    private lateinit var transactionDetailsLauncher: ActivityResultLauncher<Intent>

    //skeleton while data loads
    //gotten from Fahlteich, P. 2025. SkeletonLayout: Skeleton view pattern for Android, Github. [Online].
    //Avaiable at: https://github.com/Faltenreich/SkeletonLayout [Accessed 29 May 2025]
    private lateinit var skeleton: Skeleton

    private val binding get() = _binding!!
    companion object {
        fun newInstance() = ViewTransactionFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionDetailsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data?.getBooleanExtra("TRANSACTION_DELETED", false) == true) {
                loadTransactionData()
            }
        }
        sessionManager = SessionManager.getInstance(requireContext())
        lifecycleScope.launch {
            currentUser = sessionManager.getCurrentUser()
            loadTransactionData()
        }
        setupRecyclerView()

//        binding.editTransactionBtn.setOnClickListener{
//            Toast.makeText(requireContext(), "Not implemented yet!",Toast.LENGTH_LONG).show()
//        }
//        binding.deleteTransactionBtn.setOnClickListener{
//            Toast.makeText(requireContext(), "Not implemented yet!",Toast.LENGTH_LONG).show()
//        }

        skeleton = binding.transactionSkeletonLayout
        skeleton = binding.transactionRv.applySkeleton(R.layout.transaction_list_item)

        skeleton.showSkeleton()
        binding.filterByCategoryBtn.setOnClickListener {
            startActivity(Intent(requireContext(), FilterByCategoryActivity::class.java))
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
        }
    }
    private fun loadTransactionData(){
        lifecycleScope.launch {
            val firestoreService = FirestoreService("transactions", Transaction::class.java)
            val transactions = withContext(Dispatchers.IO) {
                val allTransactions = firestoreService.getAll()
                currentUser?.let { user ->
                    allTransactions.filter { it.userID == user.uid }
                } ?: emptyList()
            }
            transactionAdapter.updateData(transactions)
            //hides the skeleton when data is loaded
            skeleton.showOriginal()
        }
    }
}