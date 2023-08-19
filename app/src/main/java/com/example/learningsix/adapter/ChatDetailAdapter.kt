package com.example.learningsix.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.learningsix.ChatActivity
import com.example.learningsix.databinding.ChatdetailItemLayoutBinding
import com.example.learningsix.model.Conversation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatDetailAdapter(private val context: Context, private val arrChat: ArrayList<Conversation>) :
    RecyclerView.Adapter<ChatDetailAdapter.ViewHolder>() {
    class ViewHolder(val binding: ChatdetailItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ChatdetailItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrChat.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pos = arrChat[position]

        holder.binding.userName.text = pos.contactName
        if (pos.lastMessage == "Chatting not Started"){
            holder.binding.userMsg.setTypeface(holder.binding.userMsg.typeface,Typeface.ITALIC)
            holder.binding.userMsg.setTextColor(Color.parseColor("#A09F9F"))
        }
        holder.binding.userMsg.text = pos.lastMessage
//        val time = getTimeDate(pos.timeStamp)
        holder.binding.timeStamp.text = getTimeDate(pos.timeStamp)


        holder.binding.root.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", pos.contactName)
            intent.putExtra("uid", pos.contactId)
            intent.putExtra("ConversationId",pos.conversationId)
            it.context.startActivity(intent)
        }
    }

    private fun getTimeDate(timestamp: Long?): String {
        return try {
            val netDate = Date(timestamp!!)
            val sfd = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault())
            sfd.format(netDate)
        } catch (e: Exception) {
            "date"
        }
    }
}