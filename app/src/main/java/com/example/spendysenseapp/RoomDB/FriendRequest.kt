package com.example.spendysenseapp.RoomDB

data class FriendRequest(
    val requestId: String = "",
    val fromUserId: String = "",
    val fromUsername: String = "",
    val toUserId: String = "",
    val status: String = ""
)