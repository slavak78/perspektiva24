package ru.crmkurgan.main.items

data class Notification(
    val id: Long,
    val imageUrl: String,
    val notification: String,
    val date: String,
    val name: String
)