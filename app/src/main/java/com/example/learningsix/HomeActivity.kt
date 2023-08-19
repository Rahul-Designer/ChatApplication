package com.example.learningsix


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learningsix.adapter.ChatDetailAdapter
import com.example.learningsix.auth.SignInActivity
import com.example.learningsix.databinding.ActivityHomeBinding
import com.example.learningsix.model.Conversation
import com.example.learningsix.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeActivity : AppCompatActivity(){
    private lateinit var binding : ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database : DatabaseReference
    private lateinit var adapter: ChatDetailAdapter
    private lateinit var arrUser : ArrayList<User>
    private lateinit var arrConversation : ArrayList<Conversation>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        firebaseAuth = FirebaseAuth.getInstance()
        setSupportActionBar(binding.toolbar)
        arrConversation = arrayListOf()
        arrUser = arrayListOf()
        adapter = ChatDetailAdapter(this,arrConversation)
        binding.userDetailRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.userDetailRecyclerview.adapter = adapter
        getData()
    }

    private fun getData() {
        database = FirebaseDatabase.getInstance().reference.child("Overview").child(firebaseAuth.currentUser!!.uid)
        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                arrConversation.clear()
                if (snapshot.exists()){
                    for (conversationSnapshot in snapshot.children){
                        Log.d("RAHUL","${conversationSnapshot.child("timeStamp").value}")
                        val conversationDetail = conversationSnapshot.getValue(Conversation::class.java)
                        arrConversation.add(conversationDetail!!)
                    }
                    val size = arrConversation.size.toString()
                    Log.d("RAHUL",size)
                    for (i in arrConversation.indices){
                        if (arrConversation[0].timeStamp!! < arrConversation[i].timeStamp!!){
                            swap(arrConversation,0,i)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                firebaseAuth.signOut()
                val pref = getSharedPreferences("login", MODE_PRIVATE)
                val editor = pref?.edit()
                editor?.putBoolean("flag", false)
                editor?.apply()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
                true
            }
            R.id.action_add ->{
                val intent = Intent(applicationContext,ContactActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun swap(list: ArrayList<Conversation>, i: Int, j: Int) {
        val t = list[i]
        list[i] = list[j]
        list[j] = t
    }

}