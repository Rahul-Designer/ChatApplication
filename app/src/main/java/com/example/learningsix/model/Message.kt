package com.example.learningsix.model

data class Message(
    val data : String? = null,
    val type : String? = null,
    val deleted : String? = null,
    val reply: String? = null,
    val replyKey: String? = null,
    val replyMessage: String? = null,
    val senderId : String? = null,
    val timeStamp : Long? = null
){
    var key : String? = null
}
