package com.example.habitapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.LocalDate

class FriendsFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    private var friends : MutableList<Friend> = mutableListOf()
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var recyclerViewFriends: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(FriendsFragment.ARG_USER_ID) ?: ""
        }
        database = Firebase.database.reference
    }

    private fun getDateFromDB(snap: DataSnapshot): LocalDate? {
        val day = snap.child("dayOfMonth").getValue(Int::class.java)
        val month = snap.child("monthValue").getValue(Int::class.java)
        val year = snap.child("year").getValue(Int::class.java)


        if (day != null && month != null && year != null) {
            return LocalDate.of(year, month, day)
        }
        return null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        recyclerViewFriends = view.findViewById(R.id.rvFriendsList)
        recyclerViewFriends.layoutManager = LinearLayoutManager(activity)

        database.child(userId).child("friends").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children){
                    val id = snap.child("id").getValue(String::class.java)
                    val name = snap.child("name").getValue(String::class.java)
                    val date = getDateFromDB(snap.child("date"))

                    val friend = Friend(id, name, date)
                    friend.let { friends.add(it) }
                    Log.d("Test", "Hmm ${snap}")
                    Log.d("Test", "Hmm ${friends}")
                }
                friendsAdapter = FriendsAdapter(friends) { friend, action ->
                    handleFriendAction(friend, action)
                }
                recyclerViewFriends.adapter = friendsAdapter

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        return view
    }

    private fun handleFriendAction(friend: Friend, action: String) {
        when (action) {
            "view_habits" -> {
                val intent = Intent(activity, FriendHabitsActivity::class.java)
                intent.putExtra("FRIEND_ID", friend.id)
                intent.putExtra("FRIEND_NAME", friend.name)
                startActivity(intent)
            }
            "unfriend" -> {
                AlertDialog.Builder(context)
                    .setTitle("Unfriend")
                    .setMessage("Are you sure you no longer want to be friend with ${friend.name}? \uD83D\uDE2E")
                    .setPositiveButton("Yes") { dialog, which ->
                        val friendId = friend.id
                        val position = friends.indexOf(friend)
                        if (friendId != null){
                            unfriend(context, userId, friendId, position, false)
                            unfriend(context, friendId, userId, position, true)

                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    private fun unfriend(
        context: Context?,
        friendId: String,
        userId: String,
        position: Int,
        forUser: Boolean
    ) {
        database.child(userId).child("friends").child(friendId).removeValue()
            .addOnSuccessListener {
                if (forUser){
                    friends.removeAt(position)
                    friendsAdapter.notifyItemRemoved(position)
                    friendsAdapter.notifyItemRangeChanged(position, friendsAdapter.itemCount)
                    Toast.makeText(context, "Successfully Unfriended", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener{e->
                Toast.makeText(context, "Failed to unfriend: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    companion object {
        private const val ARG_USER_ID = "USER_ID"

        fun newInstance(userId: String): FriendsFragment {
            val fragment = FriendsFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }
}