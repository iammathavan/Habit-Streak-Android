package com.example.habitapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class FriendHabitsActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var name: String

    private lateinit var tvUserName: TextView

    private lateinit var homeBtn: Button
    private lateinit var friendsBtn: Button

    private lateinit var database: DatabaseReference
    private lateinit var recyclerViewHabits: RecyclerView
    private lateinit var friendHabitAdapter: FriendsHabitAdapter
    private var habitList : MutableList<Habit> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_habits)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.friendsHabitActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userId = intent.getStringExtra("FRIEND_ID") ?: ""
        name = intent.getStringExtra("FRIEND_NAME") ?: ""

        tvUserName = findViewById(R.id.userNameTextView)
        homeBtn = findViewById(R.id.btnHome)
        friendsBtn = findViewById(R.id.btnFriends)

        database = Firebase.database.reference
        recyclerViewHabits = findViewById(R.id.habitsRecyclerView)
        recyclerViewHabits.layoutManager = LinearLayoutManager(this)

        tvUserName.text = "${name}'s habits"

        database.child(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.child("habits").children){
                    val habitID = snap.child("id").getValue(String::class.java)
                    val habitName = snap.child("name").getValue(String::class.java)
                    val habitDesc = snap.child("description").getValue(String::class.java)
                    val habitScore = snap.child("score").getValue(Int::class.java)
                    val habitStreak = snap.child("streak").getValue(Int::class.java)
                    val habitCompletion = snap.child("completion").getValue(Boolean::class.java)
                    val habitStartDate = null

                    val habit = Habit(habitID, habitName, habitStartDate, habitStreak, habitScore, habitDesc, habitCompletion)
                    habit.let { habitList.add(it) }
                }
                friendHabitAdapter = FriendsHabitAdapter(habitList)
                recyclerViewHabits.adapter = friendHabitAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })



        homeBtn.setOnClickListener {
            val intent = Intent(this@FriendHabitsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        friendsBtn.setOnClickListener {
            finish()
        }






    }
}