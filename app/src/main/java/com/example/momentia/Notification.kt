package com.example.momentia

data class Notification(
    val id:        String  = "",
    val fromName:  String  = "",
    val type:      String  = "",
    val postId:    String  = "",
    val postImage: String  = "",
    val message:   String  = "",
    val read:      Boolean = false,
    val createdAt: Long    = 0L
)