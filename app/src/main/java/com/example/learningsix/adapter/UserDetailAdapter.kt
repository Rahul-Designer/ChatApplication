package com.example.learningsix.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.learningsix.databinding.UserdetailRecyclerviewLayoutBinding
import com.example.learningsix.model.User

class UserDetailAdapter(
    private val arrUser: ArrayList<User>,
    private val contactItemClickListener: ContactItemClickListener
) :
    RecyclerView.Adapter<UserDetailAdapter.ViewHolder>() {
    class ViewHolder(
        val binding: UserdetailRecyclerviewLayoutBinding,val contactItemClickListener: ContactItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: User) {
            binding.root.setOnClickListener {
                contactItemClickListener.openUserChat(adapterPosition,pos.userName!!,pos.userId!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            UserdetailRecyclerviewLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),contactItemClickListener
        )
    }

    override fun getItemCount(): Int {
        return arrUser.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pos = arrUser[position]
        holder.bind(pos)
        holder.binding.userName.text = pos.userName
    }

    interface ContactItemClickListener {
        fun openUserChat(position: Int, userName: String, userId: String)
    }
}