package com.example.spendysenseapp.ui.AddTransaction

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.spendysenseapp.CalculatorActivity
import com.example.spendysenseapp.Login
import com.example.spendysenseapp.MainActivity
import com.example.spendysenseapp.databinding.FragmentAddTransactionBinding

class AddTransactionFragment : Fragment() {

    private val viewModel: AddTransactionViewModel by viewModels()
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set onClickListener for your ImageView
        binding.imgCalc.setOnClickListener {
            val intent = Intent(requireContext(), CalculatorActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
