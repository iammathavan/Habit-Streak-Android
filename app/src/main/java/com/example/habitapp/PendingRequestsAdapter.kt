package com.example.habitapp

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import java.time.LocalDate

class PendingRequestsAdapter(
    private val pendingRequests: MutableList<Request>,
    private val database: DatabaseReference
) :
    RecyclerView.Adapter<PendingRequestsAdapter.RequestViewHolder>(){

    inner class RequestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
        val dbRef: DatabaseReference = database
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingRequestsAdapter.RequestViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_requests_item, parent, false)
        return RequestViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return pendingRequests.size
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = pendingRequests[position]
        holder.tvUserName.text = "${request.sname.toString()} wants to be your friend \uD83D\uDC4B"
        val context = holder.itemView.context

        holder.btnAccept.setOnClickListener {
            addFriend(request.sender, request.receiver, holder.dbRef, request.rname, context, true)
            addFriend(request.receiver, request.sender, holder.dbRef, request.sname, context, false)
            deleteRequest(request, position, holder.dbRef, context, false)

        }

        holder.btnReject.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Friend Request")
                .setMessage("Are you sure you do not want to be ${request.sname}'s friend? \uD83E\uDD7A")
                .setPositiveButton("Yes") { dialog, which ->
                    deleteRequest(request, position, holder.dbRef, context, true)
                }
                .setNegativeButton("No", null)
                .show()
        }


    }

    private fun addFriend(
        who: String?,
        friending: String?,
        dbRef: DatabaseReference,
        name: String?,
        context: Context,
        forUser: Boolean
    ) {
        val friendRef = dbRef.child(who.toString()).child("friends")


        val friend = Friend(friending, name, LocalDate.now())

        friending.let {
            friendRef.child(it.toString()).setValue(friend)
                .addOnSuccessListener {
                    if (forUser) {
                        Toast.makeText(context, "Friend Added", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(context, "Failed to add friend: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteRequest(
        request: Request, position: Int, dbRef: DatabaseReference,
        context: Context,
        forUser: Boolean
    ) {
        val requestID = request.id
        val userId = request.receiver
        if (requestID != null){
            dbRef.child(userId.toString()).child("requests").child(requestID).removeValue()
                .addOnSuccessListener {
                    pendingRequests.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, pendingRequests.size)
                    if (forUser){
                        Toast.makeText(context, "Request deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener{e->
                    Toast.makeText(context, "Failed to delete request: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


}

