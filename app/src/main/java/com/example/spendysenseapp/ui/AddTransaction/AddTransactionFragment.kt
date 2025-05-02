package com.example.spendysenseapp.ui.AddTransaction

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.spendysenseapp.CalculatorActivity
import com.example.spendysenseapp.CreateCategoryActivity
import com.example.spendysenseapp.databinding.FragmentAddTransactionBinding

class AddTransactionFragment : Fragment() {

    private val viewModel: AddTransactionViewModel by viewModels()
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val calculatorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            val resultValue = data?.getStringExtra("calc_result")
            resultValue?.let {
                binding.edtAmount.setText(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgCalc.setOnClickListener {
            val intent = Intent(requireContext(), CalculatorActivity::class.java)
            calculatorLauncher.launch(intent)
        }

        binding.btnAddCategory.setOnClickListener {
            val intent = Intent(requireContext(), CreateCategoryActivity::class.java)
            calculatorLauncher.launch(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
