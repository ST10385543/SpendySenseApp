package com.example.spendysenseapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendysenseapp.FriendsActivity
import com.example.spendysenseapp.R

class FriendsListAdapter(
    private val friends: List<FriendsActivity.FriendInfo>,
    private val onViewAchievements: (String) -> Unit,
    private val onRemoveFriend: (String) -> Unit
) : RecyclerView.Adapter<FriendsListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendEmail: TextView = itemView.findViewById(R.id.friendEmail)
        val viewAchievementsBtn: Button = itemView.findViewById(R.id.viewAchievementsBtn)
        val removeFriendBtn: Button = itemView.findViewById(R.id.removeFriendBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_list_item, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.friendEmail.text = friend.email
        holder.viewAchievementsBtn.setOnClickListener { onViewAchievements(friend.uid) }
        holder.removeFriendBtn.setOnClickListener { onRemoveFriend(friend.uid) }
    }

    override fun getItemCount(): Int = friends.size
}