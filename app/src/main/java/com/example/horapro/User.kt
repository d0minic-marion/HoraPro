package com.example.horapro.model

data class User(
    val uid: String = "",
    val email: String = "",
    val termsAccepted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val emailVerified: Boolean = false,
)