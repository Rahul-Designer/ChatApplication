package com.example.learningsix

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learningsix.adapter.UserDetailAdapter
import com.example.learningsix.databinding.ActivityContactBinding
import com.example.learningsix.model.Conversation
import com.example.learningsix.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ContactActivity : AppCompatActivity(),UserDetailAdapter.ContactItemClickListener {
    private lateinit var binding: ActivityContactBinding
    private lateinit var database: DatabaseReference
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: UserDetailAdapter
    lateinit var arrUser: ArrayList<User>
    private lateinit var arrConversation: ArrayList<Conversation>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact)
        firebaseAuth = FirebaseAuth.getInstance()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Contact List"
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        arrUser = arrayListOf()
        arrConversation = arrayListOf()
        adapter = UserDetailAdapter(arrUser, this)
        binding.userDetailRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.userDetailRecyclerview.adapter = adapter

        getUserData()
    }

    private fun getUserData() {
        database = FirebaseDatabase.getInstance().getReference("users")
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait..")
        progressDialog.setMessage("Application is loading, please wait")
        progressDialog.show()
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                arrUser.clear()
                if (snapshot.exists()) {
                    progressDialog.dismiss()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (firebaseAuth.currentUser?.uid != user?.userId) {
                            arrUser.add(user!!)
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    progressDialog.dismiss()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun openUserChat(position: Int, userName: String, userId: String) {
        database = FirebaseDatabase.getInstance().reference.child("Overview").child(firebaseAuth.currentUser!!.uid)
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                arrConversation.clear()
                if (snapshot.exists()) {
                    for (conversationSnapshot in snapshot.children) {
                        val conversation = conversationSnapshot.getValue(Conversation::class.java)
                        arrConversation.add(conversation!!)
                    }
                    Log.d("RAHUL",arrConversation.toString())
                    updateConversation(arrConversation,userName,userId)
                }
                else{
                    val emptyArray : ArrayList<Conversation> = arrayListOf()
                    updateConversation(emptyArray,userName,userId)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    private fun updateConversation(
        arrConversation: ArrayList<Conversation>,
        userName: String,
        userId: String) {

        var conversationId : String? = null
        if (arrConversation.isEmpty()) {
            val conversation = database.child("Overview").child(userId).push()
            conversationId = conversation.key.toString()
        } else {
            for (item in arrConversation) {
                if (item.contactId == userId) {
                    conversationId = item.conversationId!!
                    break
                }
                val conversation = database.child("Overview").child(userId).push()
                conversationId = conversation.key.toString()
            }

        }
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("name", userName)
        intent.putExtra("uid", userId)
        intent.putExtra("ConversationId", conversationId)
        startActivity(intent)
    }

}