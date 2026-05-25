package com.example.momentia

data class Post(
    val id: String = "",
    val uid: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val location: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val likes: List<String> = emptyList(),
    val createdAt: Long = 0L
)