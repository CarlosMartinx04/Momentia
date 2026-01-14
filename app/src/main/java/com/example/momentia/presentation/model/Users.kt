package com.example.momentia.presentation.model

import android.R

data class Users (
    //Deberian ser datos privados??
    val username: String,
    val biography:String,
    val profilePicture:String,
    val followers:Int,
    val following: Int,
    val years: Int,
    val posts:List<Post>
)