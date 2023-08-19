package com.example.learningsix.model

data  class Conversation(
    val lastMessage: String?=null,
    val contactName: String?=null,
    val contactId : String?=null,
    val conversationId: String?=null,
    val lastSenderId:String?=null,
    val timeStamp: Long?=null
)