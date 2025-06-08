// app/src/main/java/com/example/spendysenseapp/Adapter/FriendAdapter.kt
package com.example.spendysenseapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.recyclerview.widget.RecyclerView
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.FriendRequest

class FriendAdapter(
    private val friendRequests: List<FriendRequest>,
    private val onAccept: (FriendRequest) -> Unit,
    private val onReject: (FriendRequest) -> Unit
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendUsername: TextView = itemView.findViewById(R.id.friendEmail)
        val acceptButton: MaterialButton = itemView.findViewById(R.id.acceptButton)
        val rejectButton: MaterialButton = itemView.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_list_item, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val request = friendRequests[position]
        holder.friendUsername.text = request.fromUsername
        holder.acceptButton.setOnClickListener { onAccept(request) }
        holder.rejectButton.setOnClickListener { onReject(request) }
    }

    override fun getItemCount(): Int = friendRequests.size
}