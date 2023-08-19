package com.example.learningsix

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.learningsix.databinding.ActivityImageViewBinding
import com.example.learningsix.model.Conversation
import com.example.learningsix.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ImageViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageViewBinding
    private lateinit var database: DatabaseReference
    private lateinit var mstorageref: StorageReference

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_view)
        database = FirebaseDatabase.getInstance().reference
        mstorageref = FirebaseStorage.getInstance().reference

        val imagePath = intent.getStringExtra("imagePath")
        val conversationId = intent.getStringExtra("ConversationId")
        val arrUser = intent.getStringArrayListExtra("UserArray")
        val senderUid = intent.getStringExtra("SenderUid")
        val senderName = intent.getStringExtra("SenderName")
        val receiverName = intent.getStringExtra("ReceiverName")
        val receiverUid = intent.getStringExtra("ReceiverUid")


        val fileUri: Uri = Uri.parse(imagePath)
        binding.image.setImageURI(fileUri)
        binding.sendingImage.setOnClickListener {
            val messageKey = database.child("Messages").child(conversationId!!).child("message").push().key.toString()
            val reference =  mstorageref.child("Images").child(senderUid!!).child(messageKey)
            reference.putFile(fileUri).addOnCompleteListener {
                if (it.isSuccessful) {
                    reference.downloadUrl.addOnSuccessListener {file->
                        database.child("Messages").child(conversationId).child("Users")
                            .setValue(arrUser)
                        val messageImage = HashMap<String, Any>()
                        messageImage["data"] = "$file"
                        messageImage["type"] = "0"
                        messageImage["senderId"] = senderUid
                        messageImage["timeStamp"] = ServerValue.TIMESTAMP
                        database.child("Messages").child(conversationId).child("message").child(messageKey).updateChildren(messageImage)

                        val conversationMessage = HashMap<String, Any>()
                        conversationMessage["lastMessage"] = "Attachment"
                        conversationMessage["contactName"] = receiverName!!
                        conversationMessage["contactId"] = receiverUid!!
                        conversationMessage["conversationId"] = conversationId
                        conversationMessage["lastSenderId"] = senderUid
                        conversationMessage["timeStamp"] = ServerValue.TIMESTAMP
                        database.child("Overview").child(senderUid).child(conversationId).updateChildren(conversationMessage)
                        database.child("Overview").child(receiverUid!!).child(conversationId).updateChildren(conversationMessage)

                        finish()
                    }
                }
            }
        }
        binding.close.setOnClickListener {
            onBackPressed()
        }
    }
}