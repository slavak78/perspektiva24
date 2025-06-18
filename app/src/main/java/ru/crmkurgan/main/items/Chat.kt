package ru.crmkurgan.main.items

data class Chat(
    val type: Int,
    val message: String,
    val date: String,
    val time: String,
    val readied: Int,
    val avatar: String,
    val name: String,
    val id: String,
    val user_id:String,
    val last: Int
)