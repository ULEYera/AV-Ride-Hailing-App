package com.example.avaride_1.domain.model

data class User(
    val phoneNumber: String = "",
    val name: String = "",
    val homeLocation: String? = null,
    val workLocation: String? = null,
    val paymentMethod: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
