package com.example.spendysenseapp

import com.example.spendysenseapp.Adapter.FriendsListAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendysenseapp.Adapter.FriendRequestAdapter
import com.example.spendysenseapp.RoomDB.FriendRequest
import com.example.spendysenseapp.Services.SessionManager
import com.example.spendysenseapp.databinding.ActivityFriendsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class FriendsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFriendsBinding
    private lateinit var sessionManager : SessionManager
    //make global current user item
    private lateinit var currentUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friends)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.friendlySkeleton.showSkeleton()

        binding.changeToFriendRequestSw.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.friendRequestsLayout.visibility = View.VISIBLE
                binding.friendsListLayout.visibility = View.GONE
            } else {
                binding.friendRequestsLayout.visibility = View.GONE
                binding.friendsListLayout.visibility = View.VISIBLE
            }

        }
        binding.friendRequestsRv.layoutManager = LinearLayoutManager(this)
        binding.friendsRv.layoutManager = LinearLayoutManager(this)

        sessionManager = SessionManager.getInstance(applicationContext)
        binding.friendsRv.layoutManager = LinearLayoutManager(this)
        lifecycleScope.launch {
            currentUser = sessionManager.getCurrentUser()!!
            getUsersFriendCode()
            setupFriendSearchListener()
            listenToFriendRequests()
            listenToFriendsList()
            binding.friendlySkeleton.showOriginal()
        }
    }
    private fun listenToFriendRequests() {
        val db = FirebaseFirestore.getInstance()
        db.collection("friend_requests")
            .whereEqualTo("toUserId", currentUser.uid)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    val friendRequests = snapshots.map { it.toObject(FriendRequest::class.java) }
                    binding.friendRequestsRv.adapter = FriendRequestAdapter(friendRequests,
                        { request -> acceptFriendRequest(request, currentUser.uid) },
                        { request -> rejectFriendRequest(request) }
                    )
                }
            }
    }
    //data class to get friend email for friends list
    data class FriendInfo(val uid: String, val email: String)

    private fun listenToFriendsList() {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("user").document(currentUser.uid)

        userRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val friends = snapshot.get("friends") as? List<String> ?: emptyList()
                if (friends.isEmpty()) {
                    binding.friendsRv.adapter = FriendsListAdapter(emptyList(), {}, {})
                    return@addSnapshotListener
                }
                db.collection("user")
                    .whereIn("userId", friends)
                    .get()
                    .addOnSuccessListener { docs ->
                        val friendInfos = docs.map {
                            FriendInfo(
                                uid = it.getString("userId") ?: "",
                                email = it.getString("userEmail") ?: ""
                            )
                        }
                        binding.friendsRv.adapter = FriendsListAdapter(
                            friendInfos,
                            onViewAchievements = { friendUid ->
                                val friend = friendInfos.find { it.uid == friendUid }
                                val intent = Intent(this, AchievementsActivity::class.java)
                                intent.putExtra("userUid", friendUid)
                                intent.putExtra("userEmail", friend?.email ?: "")
                                startActivity(intent)
                            },
                            onRemoveFriend = { friendUid ->
                                removeFriend(friendUid)
                            }
                        )
                    }
            }
        }
    }

    private fun removeFriend(friendUid: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("user").document(currentUser.uid)
        val friendRef = db.collection("user").document(friendUid)
        db.runBatch { batch ->
            batch.update(userRef, "friends", FieldValue.arrayRemove(friendUid))
            batch.update(friendRef, "friends", FieldValue.arrayRemove(currentUser.uid))
        }.addOnSuccessListener {
            Toast.makeText(this, "Friend removed", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                listenToFriendsList()
            }
        }
    }
    private suspend fun getUsersFriendCode() {
        val db = Firebase.firestore
        db.collection("user")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val friendCode = documents.documents[0].getString("friendCode")
                    binding.userFriendCodeTv.text = friendCode ?: "No friend code"
                } else {
                    binding.userFriendCodeTv.text = "No friend code"
                    Log.e("friendService", "No user document found for current user")
                }
            }
            .addOnFailureListener {
                binding.userFriendCodeTv.text = "Error"
                Log.e("friendService", "Failed to fetch user document")
            }
    }

    private suspend fun setupFriendSearchListener() {
        // Initially disable the button
        binding.sendFriendRequestBtn.isEnabled = false

        // Watch the EditText for changes
        binding.enterFriendCodeEt.addTextChangedListener {
            val input = it.toString()
            binding.sendFriendRequestBtn.isEnabled = input.isNotEmpty()
        }

        // Handle the button click when enabled
        binding.sendFriendRequestBtn.setOnClickListener {
            val inputCode = binding.enterFriendCodeEt.text.toString()
            if (inputCode.isEmpty()) return@setOnClickListener

            val db = Firebase.firestore
            db.collection("user")
                .whereEqualTo("friendCode", inputCode)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val toUserId = documents.documents[0].getString("userId") ?: return@addOnSuccessListener
                        // Check if already friends
                        db.collection("user").document(currentUser.uid).get()
                            .addOnSuccessListener { userDoc ->
                                val friends = userDoc.get("friends") as? List<String> ?: emptyList()
                                if (friends.contains(toUserId)) {
                                    Toast.makeText(this, "You are already friends!", Toast.LENGTH_SHORT).show()
                                    return@addOnSuccessListener
                                }
                                // Not friends, continue to send request
                                db.collection("user")
                                    .whereEqualTo("userId", currentUser.uid)
                                    .get()
                                    .addOnSuccessListener { userDocs ->
                                        val fromEmail = userDocs.documents[0].getString("userEmail") ?: "Unknown"
                                        val requestId = db.collection("friend_requests").document().id
                                        val request = hashMapOf(
                                            "requestId" to requestId,
                                            "fromUserId" to currentUser.uid,
                                            "fromUsername" to fromEmail,
                                            "toUserId" to toUserId,
                                            "timeSent" to System.currentTimeMillis(),
                                            "status" to "pending"
                                        )
                                        db.collection("friend_requests").document(requestId).set(request)
                                        Log.d("friendService", "Friend request sent")
                                        Toast.makeText(this, "Friend request sent successfully", Toast.LENGTH_SHORT).show()
                                    }
                            }
                    } else {
                        binding.enterFriendCodeEt.error = "No users found"
                        Log.d("friendService", "No users found")
                        Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Log.e("friendService", "Error searching for user", it)
                    Toast.makeText(this, "Error searching for friend", Toast.LENGTH_SHORT).show()
                }
            binding.enterFriendCodeEt.setText("")
        }
    }

    private fun acceptFriendRequest(request: FriendRequest, currentUserId: String) {
        val db = FirebaseFirestore.getInstance()
        val fromUserId = request.fromUserId

        // Add each user to the other's friends array
        val userRef = db.collection("user").document(currentUserId)
        val friendRef = db.collection("user").document(fromUserId)

        db.runBatch { batch ->
            batch.update(userRef, "friends", FieldValue.arrayUnion(fromUserId))
            batch.update(friendRef, "friends", FieldValue.arrayUnion(currentUserId))
            batch.delete(db.collection("friend_requests").document(request.requestId))
        }.addOnSuccessListener {
            Toast.makeText(applicationContext, "Friend added successfully!", Toast.LENGTH_SHORT).show()
            Log.d("friendService", "Friend added successfully")
        }.addOnFailureListener {
            Toast.makeText(applicationContext, "Error! Friend adding failed!", Toast.LENGTH_SHORT).show()
            Log.e("friendService", "Friend added failed")
        }
        lifecycleScope.launch {
            listenToFriendRequests()
            listenToFriendsList()
        }

    }

    private fun rejectFriendRequest(request: FriendRequest) {
        val db = FirebaseFirestore.getInstance()
        db.collection("friend_requests").document(request.requestId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Friend request denied!", Toast.LENGTH_SHORT).show()
                Log.e("friendService", "Friend request denied successfully ")

            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Error. Failed to deny friend request! Try again in a few minutes", Toast.LENGTH_SHORT).show()
                Log.e("friendService", "failed to deny friend request")

            }
        lifecycleScope.launch {
            listenToFriendRequests()
        }
    }
}