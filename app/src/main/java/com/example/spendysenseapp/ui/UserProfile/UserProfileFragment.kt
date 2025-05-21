//package com.example.spendysenseapp.ui.UserProfile
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import com.example.spendysenseapp.DeleteAccount
//import com.example.spendysenseapp.R
//import com.example.spendysenseapp.RoomDB.Feedback
//import com.example.spendysenseapp.RoomDB.Users
//import com.example.spendysenseapp.Services.SessionManager
//import com.example.spendysenseapp.UserFeedback
//import com.example.spendysenseapp.WelcomePage
//import kotlinx.coroutines.launch
//
//class UserProfileFragment : Fragment() {
//
//    private lateinit var sessionManager: SessionManager
//    private var currentUser: Users? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_user_profile, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        sessionManager = SessionManager.getInstance(requireContext())
//
//        lifecycleScope.launch {
//            currentUser = sessionManager.getCurrentUser()
//
//            if (currentUser == null) {
//                Toast.makeText(requireContext(), "No user logged in. Redirecting to WelcomePage.", Toast.LENGTH_SHORT).show()
//                val intent = Intent(requireActivity(), WelcomePage::class.java)
//                startActivity(intent)
//                requireActivity().finish()
//                return@launch
//            }
//        }
//
//        // Logout button
//        val logoutBtn = view.findViewById<Button>(R.id.Logoutbtn)
//        logoutBtn.setOnClickListener {
//            sessionManager.clearSession()
//            val intent = Intent(requireActivity(), WelcomePage::class.java)
//            startActivity(intent)
//            requireActivity().finish()
//        }
//
//        // Delete Account button
//        val deleteAccountBtn = view.findViewById<Button>(R.id.DeleteAccountbtn)
//        deleteAccountBtn.setOnClickListener {
//            val intent = Intent(requireActivity(), DeleteAccount::class.java)
//            startActivity(intent)
//        }
//
//        // User Feedback button
//        val feedbackBtn = view.findViewById<Button>(R.id.UserFeedbackbtn)
//        feedbackBtn.setOnClickListener {
//            val intent = Intent(requireActivity(), UserFeedback::class.java) // ✅ CORRECT
//            startActivity(intent)
//        }
//
//
//        val createCategoryBtn = view.findViewById<Button>(R.id.CreateCategorybtn)
//        createCategoryBtn.setOnClickListener {
//            Toast.makeText(requireContext(), "Create Category clicked", Toast.LENGTH_SHORT).show()
//
//        }
//
//        val achievementsBtn = view.findViewById<Button>(R.id.Achievementsbtn)
//        achievementsBtn.setOnClickListener {
//            Toast.makeText(requireContext(), "Achievements clicked", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
