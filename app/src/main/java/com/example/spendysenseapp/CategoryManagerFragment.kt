package com.example.spendysenseapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.CategoryAdapter
import com.example.spendysenseapp.Adapter.FriendRequestAdapter
import com.example.spendysenseapp.RoomDB.Categories
import com.example.spendysenseapp.RoomDB.FriendRequest
import com.example.spendysenseapp.databinding.FragmentCategoryManagerBinding
import com.example.spendysenseapp.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class CategoryManagerFragment : Fragment() {
    private var _binding: FragmentCategoryManagerBinding? = null
    private val categoryList = mutableListOf<Categories>()
    private lateinit var adapter: CategoryAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryManagerBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.categorySkeleton.showSkeleton()
        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(requireContext(), CreateCategoryActivity::class.java))
        }
        adapter = CategoryAdapter(categoryList) { category ->
            deleteCategory(category)
        }
        binding.categoryRv.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryRv.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            loadCategories(userId)
        }
    }
    private fun loadCategories(userId: String) {
        Firebase.firestore.collection("categories")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    for (snapshot in snapshots) {
                        try {
                            val category = snapshot.toObject(Categories::class.java)
                            categoryList.add(category)
                        } catch (e: Exception) {
                            Log.e("CategoryManager", "Error parsing category: ${e.message}")
                        }
                    }
                    adapter.notifyDataSetChanged()
                    binding.categorySkeleton.showOriginal()
                }
            }
    }

    private fun deleteCategory(category: Categories) {
        Firebase.firestore.collection("transactions")
            .whereEqualTo("categoryId", category.id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    Toast.makeText(requireContext(), "Transactions exist with this category", Toast.LENGTH_SHORT).show()
                } else {
                    Firebase.firestore.collection("categories")
                        .document(category.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Category deleted", Toast.LENGTH_SHORT).show()
                            categoryList.remove(category)
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to delete category", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to check transactions", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}