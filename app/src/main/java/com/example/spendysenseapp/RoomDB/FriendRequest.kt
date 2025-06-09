package com.example.spendysenseapp.RoomDB

//this data class is used to bind the incoming firebase data to a readable
//List of type friend request, to show the friend request in the recycler view
data class FriendRequest(
    val requestId: String = "",
    val fromUserId: String = "",
    val fromUsername: String = "",
    val toUserId: String = "",
    val status: String = ""
)