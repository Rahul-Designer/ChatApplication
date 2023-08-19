package com.example.learningsix.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.learningsix.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MessageAdapter(
    private val context: Context,
    private val arrMessage: ArrayList<com.example.learningsix.model.Message>,
    private val chatOnItemClickListener: ChatOnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVE = 1
    private val ITEM_START = 2

    class SentViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val sendLayout: LinearLayout = itemView.findViewById(R.id.sendChatLayout)
        val sendImage: ImageView = itemView.findViewById(R.id.send_image)
        val sendFileAttachment: LinearLayout = itemView.findViewById(R.id.send_file_attachment)
        val sendAudioAttachment: LinearLayout = itemView.findViewById(R.id.send_audio_attachment)
        val sendReply: TextView = itemView.findViewById(R.id.sendTxtQuotedMsg)
        val sendMessage: TextView = itemView.findViewById(R.id.send_txt)
        val sendTime: TextView = itemView.findViewById(R.id.send_time)
    }

    class ReceiverViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val receiveLayout: LinearLayout = itemView.findViewById(R.id.receiveChatLayout)
        val receiveImage: ImageView = itemView.findViewById(R.id.receive_image)
        val receiveFileAttachment: LinearLayout = itemView.findViewById(R.id.receive_file_attachment)
        val receiveAudioAttachment: LinearLayout = itemView.findViewById(R.id.receive_audio_attachment)
        val receiveReply: TextView = itemView.findViewById(R.id.receiveTxtQuotedMsg)
        val receiveMessage: TextView = itemView.findViewById(R.id.receive_txt)
        val receiveTime: TextView = itemView.findViewById(R.id.receive_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            ReceiverViewHolder(
                LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
            )
        } else {
            SentViewHolder(
                LayoutInflater.from(context).inflate(R.layout.send, parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (FirebaseAuth.getInstance().currentUser?.uid.equals(arrMessage[position].senderId)) {
            ITEM_START
        } else {
            ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return arrMessage.size
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            when(arrMessage[position].type){
                "0" -> {
                    Glide.with(context).load(arrMessage[position].data).into(viewHolder.sendImage)
                    viewHolder.sendImage.visibility = View.VISIBLE
                    viewHolder.sendMessage.visibility = View.GONE
                }
                "1" -> {
                    viewHolder.sendFileAttachment.visibility = View.VISIBLE
                    viewHolder.sendMessage.visibility = View.GONE
                }
                "2" -> {
                    viewHolder.sendAudioAttachment.visibility = View.VISIBLE
                    viewHolder.sendMessage.visibility = View.GONE
                }
                else ->{
                    viewHolder.sendImage.visibility = View.GONE
                    viewHolder.sendFileAttachment.visibility = View.GONE
                    viewHolder.sendAudioAttachment.visibility = View.GONE
                    viewHolder.sendMessage.visibility = View.VISIBLE
                }
            }

            if (arrMessage[position].deleted == "true") {
                viewHolder.sendLayout.visibility = View.GONE
            }else{
                viewHolder.sendLayout.visibility = View.VISIBLE
            }

            if (arrMessage[position].reply == "true") {
                viewHolder.sendReply.visibility = View.VISIBLE
                viewHolder.sendReply.text = arrMessage[position].replyMessage
            } else{
                viewHolder.sendReply.visibility = View.GONE
                viewHolder.sendReply.text = ""
            }

            viewHolder.sendMessage.text = arrMessage[position].data
            viewHolder.sendTime.text = getTimeDate(arrMessage[position].timeStamp)

            viewHolder.sendLayout.setOnLongClickListener {
                val popupMenu = PopupMenu(context, viewHolder.sendLayout)
                popupMenu.inflate(R.menu.sender_option_menu)
                popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        when (item?.itemId) {
                            R.id.delete -> {
                                chatOnItemClickListener.chatDelete(
                                    arrMessage[position].key!!,
                                    position
                                )
                                return true
                            }
                        }
                        return false
                    }
                })
                popupMenu.show()
                return@setOnLongClickListener true
            }

            viewHolder.sendLayout.setOnClickListener {
                when(arrMessage[position].type){
                    "0" -> {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setDataAndType(Uri.parse(arrMessage[position].data.toString()), "image/*")
                        it.context.startActivity(intent)
                    }
                    "1" -> {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setDataAndType(Uri.parse(arrMessage[position].data.toString()), "application/pdf")
                        it.context.startActivity(intent)
                    }
                    "2" -> {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setDataAndType(Uri.parse(arrMessage[position].data.toString()), "audio/*")
                        it.context.startActivity(intent)
                    }
                }
            }

            viewHolder.sendReply.setOnClickListener {
                chatOnItemClickListener.chatRipple(position)
            }

        } else {
            val viewHolder = holder as ReceiverViewHolder
            when(arrMessage[position].type){
                "0" -> {
                    Glide.with(context).load(arrMessage[position].data).into(viewHolder.receiveImage)
                    viewHolder.receiveImage.visibility = View.VISIBLE
                    viewHolder.receiveMessage.visibility = View.GONE
                }
                "1" -> {
                    viewHolder.receiveFileAttachment.visibility = View.VISIBLE
                    viewHolder.receiveMessage.visibility = View.GONE
                }
                "2" -> {
                    viewHolder.receiveAudioAttachment.visibility = View.VISIBLE
                    viewHolder.receiveMessage.visibility = View.GONE
                }
                else -> {
                    viewHolder.receiveImage.visibility = View.GONE
                    viewHolder.receiveFileAttachment.visibility = View.GONE
                    viewHolder.receiveAudioAttachment.visibility = View.GONE
                    viewHolder.receiveMessage.visibility = View.VISIBLE
                }
            }

            if (arrMessage[position].deleted == "true") {
                viewHolder.receiveLayout.visibility = View.GONE
            }else{
                viewHolder.receiveLayout.visibility = View.VISIBLE
            }
            if (arrMessage[position].reply == "true") {
                viewHolder.receiveReply.visibility = View.VISIBLE
                viewHolder.receiveReply.text = arrMessage[position].replyMessage
            }else{
                viewHolder.receiveReply.visibility = View.GONE
                viewHolder.receiveReply.text = ""
            }
            viewHolder.receiveMessage.text = arrMessage[position].data
            viewHolder.receiveTime.text = getTimeDate(arrMessage[position].timeStamp)

            viewHolder.receiveLayout.setOnLongClickListener {
                val popupMenu = PopupMenu(context, viewHolder.receiveLayout)
                popupMenu.inflate(R.menu.sender_option_menu)
                popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        when (item?.itemId) {
                            R.id.delete -> {
                                chatOnItemClickListener.chatDelete(
                                    arrMessage[position].key!!,
                                    position
                                )
                                return true
                            }
                        }
                        return false
                    }
                })
                popupMenu.show()
                return@setOnLongClickListener true
            }

            viewHolder.receiveLayout.setOnClickListener {
                when(arrMessage[position].type){
                    "0" ->{
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setDataAndType(Uri.parse(arrMessage[position].data.toString()), "image/*")
                        it.context.startActivity(intent)
                    }
                    "1" -> {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setDataAndType(Uri.parse(arrMessage[position].data.toString()), "application/pdf")
                        it.context.startActivity(intent)
                    }
                    "2" -> {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setDataAndType(Uri.parse(arrMessage[position].data.toString()), "audio/*")
                        it.context.startActivity(intent)
                    }
                }
            }

            viewHolder.receiveReply.setOnClickListener {
                chatOnItemClickListener.chatRipple(position)
            }
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


    interface ChatOnItemClickListener {
        fun chatDelete(messageId: String, position: Int)
        fun chatRipple(position: Int)
    }
}