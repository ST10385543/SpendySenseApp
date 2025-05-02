package com.example.spendysenseapp.ui.UserProfile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.DeleteAccount
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.Users
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.WelcomePage
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private var currentUser: Users? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager.getInstance(requireContext())

        lifecycleScope.launch {
            // Fetch user from session manager
            currentUser = sessionManager.getCurrentUser()

            if (currentUser == null) {
                // Handle the case where there's no user in the session
                Toast.makeText(requireContext(), "No user logged in. Redirecting to WelcomePage.", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireActivity(), WelcomePage::class.java)
                startActivity(intent)
                requireActivity().finish()
                return@launch
            }

            // Proceed with logout functionality only if user is available
            logout(view)
        }

        // Handle delete account button click
        val deleteAccountBtn = view.findViewById<Button>(R.id.deleteAccountBtn)
        deleteAccountBtn?.setOnClickListener {
            val intent = Intent(requireActivity(), DeleteAccount::class.java)
            startActivity(intent)
        }
    }

    private fun logout(view: View) {
        // Make sure this view is not null when trying to set the click listener
        val logoutBtn = view.findViewById<Button>(R.id.Logoutbtn)
        logoutBtn?.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(requireActivity(), WelcomePage::class.java)
            startActivity(intent)
            requireActivity().finish()
        } ?: run {
            // If logout button is not found, log a message
            Toast.makeText(requireContext(), "Logout button not found", Toast.LENGTH_SHORT).show()
        }
    }
}
