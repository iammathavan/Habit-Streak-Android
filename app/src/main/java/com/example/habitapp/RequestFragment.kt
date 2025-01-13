package com.example.habitapp

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.LocalDate

class RequestFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    private var friendRequests : MutableList<Request> = mutableListOf()
    private lateinit var requestAdapter: PendingRequestsAdapter
    private lateinit var recyclerViewRequests: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(RequestFragment.ARG_USER_ID) ?: ""
        }
        database = Firebase.database.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_request, container, false)

        val etUserId: EditText = view.findViewById(R.id.etUserId)
        val btnPaste: ImageButton = view.findViewById(R.id.btnPaste)
        val btnAddFriend: Button = view.findViewById(R.id.btnAddFriend)


        btnPaste.setOnClickListener {
            val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = clipboard?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val pastedText = clipData.getItemAt(0).text.toString()
                etUserId.setText(pastedText)
            } else {
                Toast.makeText(activity, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            }
        }

        btnAddFriend.setOnClickListener {
            val friendId = etUserId.text.toString().trim()
            if (friendId.isNotEmpty()){


                if (friendId == userId){
                    Toast.makeText(activity, "You can not be your own friend :(", Toast.LENGTH_SHORT).show()
                }

                else {
                    database
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.child(friendId).exists()) {

                                    if (!alreadySend(userId, friendId, snapshot) && !alreadySend(friendId, userId, snapshot)
                                        && !alreadyFriend(userId, friendId, snapshot)) {
                                        addRequest(database, userId, friendId,
                                            snapshot.child(userId).child("userinfo").children.firstOrNull()!!
                                                .child("name").getValue(String::class.java),
                                            snapshot.child(friendId).child("userinfo").children.firstOrNull()!!
                                                .child("name").getValue(String::class.java))
                                    }
                                    else if(alreadySend(friendId, userId, snapshot)){
                                        Snackbar.make(view, "They already sent you a friend request, check Pending Requests \uD83E\uDEC2",
                                            Snackbar.LENGTH_LONG)
                                            .setAction("OK", null).show()

                                    }
                                    else if (alreadyFriend(userId, friendId, snapshot)){
                                        Snackbar.make(view, "You guys are already friends \uD83E\uDD26",
                                            Snackbar.LENGTH_LONG)
                                            .setAction("OK", null).show()
                                    }

                                    else{
                                        Snackbar.make(view, "You have already sent a request, please wait for them to decide if they want to be your friend \uD83D\uDE44",
                                            Snackbar.LENGTH_LONG)
                                            .setAction("OK", null).show()

                                    }
                                } else {
                                    Toast.makeText(activity, "User does not exist", Toast.LENGTH_SHORT).show()
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }


                        })
                }
            }

        }

        recyclerViewRequests = view.findViewById(R.id.rvPendingRequests)
        recyclerViewRequests.layoutManager = LinearLayoutManager(activity)

        database.child(userId).child("requests").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children){
                    val requestID = snap.child("id").getValue(String::class.java)
                    val sname = snap.child("sname").getValue(String::class.java)
                    val rname = snap.child("rname").getValue(String::class.java)
                    val sender = snap.child("sender").getValue(String::class.java)
                    val receiver = snap.child("receiver").getValue(String::class.java)
                    val date = getDateFromDB(snap.child("date"))

                    val request = Request(requestID, sender, receiver, date, sname, rname)
                    request.let { friendRequests.add(it) }
                }
                requestAdapter = PendingRequestsAdapter(friendRequests, database)
                recyclerViewRequests.adapter = requestAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })


        return view
    }


    private fun alreadySend(sender: String, receiver: String, snapshot: DataSnapshot): Boolean {
        if (!snapshot.child(receiver).child("requests").exists()){
            return false
        }
        for (snap in snapshot.child(receiver).child("requests").children){
            if(snap.child("sender").getValue(String::class.java) == sender){
                return true
            }
        }
        return false
    }

    private fun alreadyFriend(sender: String, receiver: String, snap: DataSnapshot): Boolean{
        if (snap.child(sender).child("friends").child(receiver).exists()){
            return true
        }
        return false
    }

    private fun addRequest(
        database: DatabaseReference,
        sender: String,
        receiver: String,
        sname: String?,
        rname: String?
    ) {
        val requestRef = database.child(receiver).child("requests")
        val requestID = requestRef.push().key

        val request = Request(requestID, sender, receiver, LocalDate.now(), sname, rname)
        requestID?.let {
            requestRef.child(it).setValue(request)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Sent Request", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(activity, "Failed to send request: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
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

    companion object {
        private const val ARG_USER_ID = "USER_ID"

        fun newInstance(userId: String): RequestFragment {
            val fragment = RequestFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }


}