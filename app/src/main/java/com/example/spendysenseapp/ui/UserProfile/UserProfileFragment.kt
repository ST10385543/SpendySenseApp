package com.example.spendysenseapp.ui.UserProfile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.spendysenseapp.DeleteAccount
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.SpendySenseDatabase
import com.example.spendysenseapp.RoomDB.UserDao
import com.example.spendysenseapp.RoomDB.Users
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.WelcomePage
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment() {
    private lateinit var currentUser : Users
    private lateinit var sessionManager : SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionManager = SessionManager.getInstance(requireContext())
        lifecycleScope.launch {
            currentUser = sessionManager.getCurrentUser()
            logout()
        }
        super.onViewCreated(view, savedInstanceState)

        val deleteAccountBtn = view.findViewById<Button>(R.id.deleteAccountBtn)
        deleteAccountBtn.setOnClickListener {
            val intent = Intent(requireActivity(), DeleteAccount::class.java)
            startActivity(intent)
        }
    }
    private fun logout(){
        val logoutBtn = view?.findViewById<Button>(R.id.Logoutbtn)
        logoutBtn?.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(requireActivity(), WelcomePage::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
