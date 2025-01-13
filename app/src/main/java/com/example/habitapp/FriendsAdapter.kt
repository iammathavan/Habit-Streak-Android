package com.example.habitapp

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference

class FriendsAdapter(
    private val Friends: MutableList<Friend>,
    private val onActionClick: (Friend, String) -> Unit
):
    RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>(){

    inner class FriendsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val tvFriendName: TextView = itemView.findViewById(R.id.tvFriendName)
        val btnViewHabits: Button = itemView.findViewById(R.id.btnViewHabits)
        val btnUnfriend: Button = itemView.findViewById(R.id.btnUnfriend)


        fun bind(friend: Friend) {
            tvFriendName.text = friend.name.toString()

            btnViewHabits.setOnClickListener {
                onActionClick(friend, "view_habits")
            }

            btnUnfriend.setOnClickListener {
                onActionClick(friend, "unfriend")
            }
        }

    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendsAdapter.FriendsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friends_item, parent, false)
        return FriendsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendsAdapter.FriendsViewHolder, position: Int) {
        val friend = Friends[position]
        holder.bind(friend)
    }

    override fun getItemCount(): Int {
        return Friends.size
    }

}