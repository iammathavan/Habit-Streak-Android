package com.example.habitapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class UserInfoFragment : Fragment() {

    private lateinit var userId: String
    private lateinit var database: DatabaseReference




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID) ?: ""
        }
        database = Firebase.database.reference



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_info, container, false)

        if (userId.isNotEmpty()) {
            fetchUserInfo(view, userId)
        }


        val btnCopyUserId: ImageButton = view.findViewById(R.id.btnCopyUserId)
        btnCopyUserId.setOnClickListener {
            val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val userIdText = view.findViewById<TextView>(R.id.tvUserId)?.text.toString()
            val clip = ClipData.newPlainText("User ID", userIdText)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(activity, "User ID copied to clipboard", Toast.LENGTH_SHORT).show()
        }


        return view
    }

    private fun fetchUserInfo(view: View?, userId: String) {
        val tvUserName = view?.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = view?.findViewById<TextView>(R.id.tvUserEmail)
        val tvUserId = view?.findViewById<TextView>(R.id.tvUserId)

        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val name = snapshot.child("userinfo").children.firstOrNull()!!.child("name").getValue(String::class.java)
                val email = snapshot.child("userinfo").children.firstOrNull()!!.child("email").getValue(String::class.java)

                tvUserName?.text = "Name: $name"
                tvUserEmail?.text = "Email: $email"
                tvUserId?.text = userId

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    companion object {
        private const val ARG_USER_ID = "USER_ID"

        fun newInstance(userId: String): UserInfoFragment {
            val fragment = UserInfoFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

}