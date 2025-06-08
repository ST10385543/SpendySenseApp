package com.example.spendysenseapp.ui.UserProfile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.spendysenseapp.AchievementsActivity
import com.example.spendysenseapp.CategoryManagerFragment
import com.example.spendysenseapp.CreateCategoryActivity
import com.example.spendysenseapp.DeleteAccount
import com.example.spendysenseapp.FriendsActivity
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.Feedback
import com.example.spendysenseapp.RoomDB.Users
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.UserFeedback
import com.example.spendysenseapp.WelcomePage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        lifecycleScope.launch {
            currentUser = auth.currentUser
        }
        currentUser?.uid?.let { uid ->
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("user").document(uid).get()
                .addOnSuccessListener { document ->
                    val email = document.getString("userEmail")
                    view.findViewById<android.widget.TextView>(R.id.usernameTv).text = email ?: "Unknown"
                }
                .addOnFailureListener {
                    Log.e("UserProfile", "Failed to fetch user email", it)
                }
        }


        // Logout button
        val logoutBtn = view.findViewById<Button>(R.id.Logoutbtn)
        logoutBtn.setOnClickListener {
            Log.d("Account", "User ${currentUser?.email} submitted logout request")
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            Log.d("Account", "User logged out successfully")
            val intent = Intent(requireActivity(), WelcomePage::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // Delete Account button
        val deleteAccountBtn = view.findViewById<Button>(R.id.DeleteAccountbtn)
        deleteAccountBtn.setOnClickListener {
            val intent = Intent(requireActivity(), DeleteAccount::class.java)
            startActivity(intent)
        }

        // User Feedback button
        val feedbackBtn = view.findViewById<Button>(R.id.UserFeedbackbtn)
        feedbackBtn.setOnClickListener {
            val intent = Intent(requireActivity(), UserFeedback::class.java)
            startActivity(intent)
        }


        val categoryManagerBtn = view.findViewById<Button>(R.id.CategoryManagerBtn)
        categoryManagerBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_UserProfile_to_categoryManagerFragment)
        }

        val achievementsBtn = view.findViewById<Button>(R.id.Achievementsbtn)
        achievementsBtn.setOnClickListener {
            startActivity(Intent(requireContext(), AchievementsActivity::class.java))
        }

        val friendsBtn = view.findViewById<Button>(R.id.Friendsbtn)
        friendsBtn.setOnClickListener {
            startActivity(Intent(requireContext(), FriendsActivity::class.java))
        }

    }
}
