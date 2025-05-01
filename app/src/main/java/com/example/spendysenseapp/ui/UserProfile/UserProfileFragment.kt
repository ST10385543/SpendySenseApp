package com.example.spendysenseapp.ui.UserProfile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.spendysenseapp.R
import com.example.spendysenseapp.WelcomePage

class UserProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        val logoutBtn = view.findViewById<Button>(R.id.Logoutbtn)
        logoutBtn.setOnClickListener {
            val intent = Intent(requireActivity(), WelcomePage::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }
}
