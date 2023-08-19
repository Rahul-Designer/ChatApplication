package com.example.learningsix

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learningsix.adapter.MessageAdapter
import com.example.learningsix.databinding.ActivityChatBinding
import com.example.learningsix.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlin.collections.set


class ChatActivity : AppCompatActivity(), MessageAdapter.ChatOnItemClickListener {
    private lateinit var binding: ActivityChatBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: MessageAdapter
    private lateinit var arrMessage: ArrayList<Message>
    private lateinit var arrUser: ArrayList<String>
    private lateinit var database: DatabaseReference
    private lateinit var mStorage: StorageReference
    private lateinit var senderName: String
    private lateinit var senderUid: String
    private lateinit var receiverName: String
    private lateinit var receiverUid: String
    private lateinit var conversationId: String
    private var reply: Boolean = false
    private var replyKey: String? = null
    private var replyMessage: String? = null
    private val IMAGE = 0
    private val PDF = 1
    private val AUDIO = 2

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        firebaseAuth = FirebaseAuth.getInstance()
        mStorage = FirebaseStorage.getInstance().reference
        database = FirebaseDatabase.getInstance().reference
        setSupportActionBar(binding.toolbar)
        arrMessage = arrayListOf()
        replyKey = ""
        replyMessage = ""
        receiverName = intent.getStringExtra("name")!!
        receiverUid = intent.getStringExtra("uid")!!
        senderUid = firebaseAuth.currentUser?.uid!!
        database.child("users").child(senderUid).get().addOnSuccessListener {
            senderName = it.child("userName").value.toString()
        }
        conversationId = intent.getStringExtra("ConversationId").toString()
        arrUser = arrayListOf(receiverUid, senderUid)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        binding.userName.text = receiverName

        binding.chatRecyclerview.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(this, arrMessage, this)
        binding.chatRecyclerview.adapter = adapter
        getChat()


        val messageSwipeController = MessageSwipeController(this, object : SwipeControllerActions {
            override fun showReplyUI(position: Int) {
                reply = true
                replyKey = arrMessage[position].key
                if (arrMessage[position].type == "0" || arrMessage[position].type == "1" || arrMessage[position].type == "2") {
                    replyMessage = "Attachment"
                } else {
                    replyMessage = arrMessage[position].data
                }
                showQuotedMessage(replyMessage!!)
            }
        })

        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(binding.chatRecyclerview)

        binding.cancelButton.setOnClickListener {
            reply = false
            hideReplyLayout()
        }

        val galleryImage = registerForActivityResult(
            ActivityResultContracts.GetContent(), ActivityResultCallback {
                val intent = Intent(applicationContext, ImageViewActivity::class.java)
                intent.putExtra("ConversationId", conversationId)
                intent.putExtra("UserArray", arrUser)
                intent.putExtra("SenderUid", senderUid)
                intent.putExtra("SenderName", senderName)
                intent.putExtra("ReceiverName", receiverName)
                intent.putExtra("ReceiverUid", receiverUid)
                intent.putExtra("imagePath", it.toString())
                startActivityForResult(intent, IMAGE)
            })
        binding.attachments.setOnClickListener {
            val builder = AlertDialog.Builder(it.context)
            builder.setTitle("Chat Options")
            val attachment = arrayOf("Select an Image", "Select a PDF", "Select a Audio")
            builder.setItems(attachment) { dialog, which ->
                when (which) {
                    0 -> {
                        galleryImage.launch("image/*")
                        dialog.dismiss()
                    }

                    1 -> {
                        val intent = Intent()
                        intent.type = "application/pdf"
                        intent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(Intent.createChooser(intent, "Select PDF File"), PDF)

                    }

                    2 -> {
                        val intent = Intent()
                        intent.type = "audio/*"
                        intent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(
                            Intent.createChooser(intent, "Select Audio File"),
                            AUDIO
                        )
                    }
                }
            }
            val dialog = builder.create()
            dialog.show()

        }

        binding.sendBtn.setOnClickListener {
            sendData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendData() {
        val message = binding.messageBox.text.toString()

        database.child("Messages").child(conversationId).child("Users").setValue(arrUser)

        val conversation = HashMap<String, Any>()
        conversation["lastMessage"] = message
        conversation["contactName"] = receiverName
        conversation["contactId"] = receiverUid
        conversation["conversationId"] = conversationId
        conversation["lastSenderId"] = senderUid
        conversation["timeStamp"] = ServerValue.TIMESTAMP

        database.child("Overview").child(senderUid).child(conversationId)
            .updateChildren(conversation)

        val conversation1 = HashMap<String, Any>()
        conversation1["lastMessage"] = message
        conversation1["contactName"] = senderName
        conversation1["contactId"] = senderUid
        conversation1["conversationId"] = conversationId
        conversation1["lastSenderId"] = senderUid
        conversation1["timeStamp"] = ServerValue.TIMESTAMP
        database.child("Overview").child(receiverUid).child(conversationId)
            .updateChildren(conversation1)

        if (reply) {
            val messageKey =
                database.child("Messages").child(conversationId).child("message").push().key
            val messageTime = HashMap<String, Any>()
            messageTime["data"] = message
            messageTime["reply"] = "$reply"
            messageTime["replyKey"] = "$replyKey"
            messageTime["replyMessage"] = "$replyMessage"
            messageTime["senderId"] = senderUid
            messageTime["timeStamp"] = ServerValue.TIMESTAMP

            database.child("Messages").child(conversationId).child("message")
                .child(messageKey.toString()).updateChildren(messageTime)
            reply = false

        } else {
            val messageKey =
                database.child("Messages").child(conversationId).child("message").push().key

            val messageTime = HashMap<String, Any>()
            messageTime["data"] = message
            messageTime["senderId"] = senderUid
            messageTime["timeStamp"] = ServerValue.TIMESTAMP
            database.child("Messages").child(conversationId).child("message")
                .child(messageKey.toString()).updateChildren(messageTime)

        }

        binding.replyLayout.visibility = View.GONE
        binding.messageBox.setText("")
        binding.chatRecyclerview.smoothScrollToPosition(adapter.itemCount)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGE -> {
                    val intent = Intent(applicationContext, ImageViewActivity::class.java)
                    intent.putExtra("imagePath", data?.data)
                    startActivity(intent)
                }

                PDF -> {
                    val messageKey =
                        database.child("Messages").child(conversationId!!).child("message")
                            .push().key.toString()
                    val fileUri = data?.data
                    val reference = mStorage.child("PDF").child(senderUid!!).child(messageKey)
                    reference.putFile(fileUri!!).addOnCompleteListener {
                        if (it.isSuccessful) {
                            reference.downloadUrl.addOnSuccessListener { file ->
                                database.child("Messages").child(conversationId).child("Users")
                                    .setValue(arrUser)

                                val messagePDF = HashMap<String, Any>()
                                messagePDF["data"] = "$file"
                                messagePDF["type"] = "1"
                                messagePDF["senderId"] = senderUid
                                messagePDF["timeStamp"] = ServerValue.TIMESTAMP
                                database.child("Messages").child(conversationId).child("message")
                                    .child(messageKey).updateChildren(messagePDF)


                                val conversationMessage = HashMap<String, Any>()
                                conversationMessage["lastMessage"] = "Attachment"
                                conversationMessage["contactName"] = receiverName
                                conversationMessage["contactId"] = receiverUid
                                conversationMessage["conversationId"] = conversationId
                                conversationMessage["lastSenderId"] = senderUid
                                conversationMessage["timeStamp"] = ServerValue.TIMESTAMP

                                database.child("Overview").child(senderUid).child(conversationId)
                                    .updateChildren(conversationMessage)
                                database.child("Overview").child(receiverUid).child(conversationId)
                                    .updateChildren(conversationMessage)

                            }
                        }
                    }
                }

                AUDIO -> {
                    val messageKey =
                        database.child("Messages").child(conversationId!!).child("message")
                            .push().key.toString()
                    val fileUri = data?.data
                    val reference = mStorage.child("Audio").child(senderUid!!).child(messageKey)
                    reference.putFile(fileUri!!).addOnCompleteListener {
                        if (it.isSuccessful) {
                            reference.downloadUrl.addOnSuccessListener { file ->
                                database.child("Messages").child(conversationId).child("Users")
                                    .setValue(arrUser)

                                val messageAudio = HashMap<String, Any>()
                                messageAudio["data"] = "$file"
                                messageAudio["type"] = "2"
                                messageAudio["senderId"] = senderUid
                                messageAudio["timeStamp"] = ServerValue.TIMESTAMP
                                database.child("Messages").child(conversationId).child("message")
                                    .child(messageKey).updateChildren(messageAudio)

                                val conversationMessage = HashMap<String, Any>()
                                conversationMessage["lastMessage"] = "Attachment"
                                conversationMessage["contactName"] = receiverName
                                conversationMessage["contactId"] = receiverUid
                                conversationMessage["conversationId"] = conversationId
                                conversationMessage["lastSenderId"] = senderUid
                                conversationMessage["timeStamp"] = ServerValue.TIMESTAMP

                                database.child("Overview").child(senderUid).child(conversationId)
                                    .updateChildren(conversationMessage)
                                database.child("Overview").child(receiverUid).child(conversationId)
                                    .updateChildren(conversationMessage)

                            }
                        }
                    }
                }
            }
        }
    }


    private fun getChat() {
        database.child("Messages").child(conversationId).child("message")
            .addChildEventListener(object : ChildEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        val chat = snapshot.getValue<Message>()
                        chat?.key = snapshot.key
                        arrMessage.add(chat!!)
                        adapter.notifyDataSetChanged()
                        binding.chatRecyclerview.post(Runnable { // Call smooth scroll
                            binding.chatRecyclerview.smoothScrollToPosition(adapter.itemCount - 1)
                        })
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        for (item in arrMessage.indices.reversed()) {
                            if (arrMessage[item].deleted == "true") {
                                var lastIndex = arrMessage.lastIndex
                                for (i in arrMessage.indices.reversed()) {
                                    if (arrMessage[i].deleted == "true") {
                                        continue
                                    } else {
                                        lastIndex = i
                                        break
                                    }
                                }
                                for (i in arrMessage.indices.reversed()) {
                                    val model = arrMessage[i]
                                    if (model.key == snapshot.key) {
                                        arrMessage[i] = snapshot.getValue<Message>()!!
                                        val message = HashMap<String, Any>()
                                        if (i - 1 == -1) {
                                            message["lastMessage"] = "Chatting not Started"
                                            message["lastSenderId"] = "null"
                                            message["timeStamp"] = ""
                                            database.child("Overview").child(senderUid)
                                                .child(conversationId).updateChildren(message)
                                            database.child("Overview").child(receiverUid)
                                                .child(conversationId).updateChildren(message)
                                        } else {
                                            if (i == lastIndex) {
                                                if (arrMessage[i - 1].deleted == "true") {
                                                    for (j in i downTo 0) {
                                                        if (arrMessage[j].deleted == "true") {
                                                            continue
                                                        } else {
                                                            if (arrMessage[j].type == "0" || arrMessage[j].type == "1" || arrMessage[j].type == "2") {
                                                                message["lastMessage"] =
                                                                    "Attachment"
                                                                message["lastSenderId"] =
                                                                    arrMessage[j].senderId!!
                                                                message["timeStamp"] =
                                                                    arrMessage[j].timeStamp!!
                                                                database.child("Overview")
                                                                    .child(senderUid)
                                                                    .child(conversationId)
                                                                    .updateChildren(message)
                                                                database.child("Overview")
                                                                    .child(receiverUid)
                                                                    .child(conversationId)
                                                                    .updateChildren(message)
                                                                break
                                                            } else {
                                                                message["lastMessage"] =
                                                                    arrMessage[j].data!!
                                                                message["lastSenderId"] =
                                                                    arrMessage[j].senderId!!
                                                                message["timeStamp"] =
                                                                    arrMessage[j].timeStamp!!
                                                                database.child("Overview")
                                                                    .child(senderUid)
                                                                    .child(conversationId)
                                                                    .updateChildren(message)
                                                                database.child("Overview")
                                                                    .child(receiverUid)
                                                                    .child(conversationId)
                                                                    .updateChildren(message)
                                                                break
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    if (arrMessage[i - 1].type == "0" || arrMessage[i - 1].type == "1" || arrMessage[i - 1].type == "2") {
                                                        message["lastMessage"] = "Attachment"
                                                        message["lastSenderId"] =
                                                            arrMessage[i - 1].senderId!!
                                                        message["timeStamp"] =
                                                            arrMessage[i - 1].timeStamp!!
                                                        database.child("Overview").child(senderUid)
                                                            .child(conversationId)
                                                            .updateChildren(message)
                                                        database.child("Overview")
                                                            .child(receiverUid)
                                                            .child(conversationId)
                                                            .updateChildren(message)
                                                    } else {
                                                        message["lastMessage"] =
                                                            arrMessage[i - 1].data!!
                                                        message["lastSenderId"] =
                                                            arrMessage[i - 1].senderId!!
                                                        message["timeStamp"] =
                                                            arrMessage[i - 1].timeStamp!!
                                                        database.child("Overview").child(senderUid)
                                                            .child(conversationId)
                                                            .updateChildren(message)
                                                        database.child("Overview")
                                                            .child(receiverUid)
                                                            .child(conversationId)
                                                            .updateChildren(message)
                                                    }

                                                }
                                            }
                                        }
                                        adapter.notifyItemChanged(i)
                                        break
                                    }

                                }
                            }
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


    private fun hideReplyLayout() {
        binding.replyLayout.visibility = View.GONE
    }

    private fun showQuotedMessage(replyMessage: String) {
        binding.messageBox.requestFocus()
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.messageBox, InputMethodManager.SHOW_IMPLICIT)
        binding.txtQuotedMsg.text = replyMessage
        binding.replyLayout.visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun chatDelete(messageId: String, position: Int) {
        val message = HashMap<String, Any>()
        message["deleted"] = "true"
        database.child("Messages").child(conversationId).child("message").child(messageId)
            .updateChildren(message)
    }

    override fun chatRipple(position: Int) {
        for (item in arrMessage.indices.reversed()) {
            if (arrMessage[item].key == arrMessage[position].replyKey) {
                binding.chatRecyclerview.post(Runnable { // Call smooth scroll
                    binding.chatRecyclerview.smoothScrollToPosition(item)
                })

                Handler().postDelayed({
                    adapter.notifyItemChanged(item)
                }, 800)

            }
        }

    }

}
