package com.example.spendysenseapp.ui.analytics

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import com.example.spendysenseapp.FilterByCategoryActivity
import com.example.spendysenseapp.R

class AnalyticsFragment : Fragment() {

    companion object {
        fun newInstance() = AnalyticsFragment()
    }

    private val viewModel: AnalyticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //temp thing to fill analytics fragment
        startFilterByCategoryActivities()
    }

    private fun startFilterByCategoryActivities(){
        //val intent = Intent(requireActivity(), FilterByCategoryActivity::class.java)
        //startActivity(intent)
    }
}