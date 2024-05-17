package com.example.habitapp

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var userNameTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var recyclerViewHabits: RecyclerView
    private lateinit var habitAdapter: HabitAdapter

    private lateinit var editTextHabitName: EditText
    private lateinit var editTextHabitDescription: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        floatingActionButton = findViewById(R.id.floatingActionButton)
        userNameTextView = findViewById(R.id.userNameTextView)


        recyclerViewHabits = findViewById(R.id.habitsRecyclerView)
        recyclerViewHabits.layoutManager = LinearLayoutManager(this)

        database = Firebase.database.reference
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid



        //Display the User currently logged in at top
        currentUserID?.let{uid->
            val userRef = database.child(uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val userName = snapshot.child("userinfo").children.firstOrNull()!!.child("name").getValue(String::class.java)
                    if (userName != null){
                        userNameTextView.text = "$userName" + "'s Habits"
                    }
                    val today = LocalDate.now()
                    val lastLogin = getDateFromDB(snapshot.child("userinfo").children.firstOrNull()!!.child("lastLogin"))
                    Log.d("Ronaldo", "${snapshot.child("userinfo")}")

                    for (snap in snapshot.child("habits").children){
                        Log.d("Ronaldo", "Ok -> ${snap}")
                        if (today > lastLogin){
                            if (today == lastLogin!!.plusDays(1)){
                                //Log.d("Ronaldo", "${snapshot.child("habits")} vs ${userRef.child("habits")}")
                                updateScore(snap, userRef.child("habits"))
                                updateStreak(snap, userRef.child("habits"), true)
                                resetCompletion(snap, userRef.child("habits"))
                            }else{
                                updateScore(snap, userRef.child("habits"))
                                updateStreak(snap, userRef.child("habits"), false)
                                resetCompletion(snap, userRef.child("habits"))
                            }
                        }
                    }
                    val userInfoId = snapshot.child("userinfo").children.firstOrNull()!!.child("id").getValue(String::class.java)
                    userRef.child("userinfo").child(userInfoId!!).child("lastLogin").setValue(today)

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO()
                }

            })

            userRef.child("habits").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val habitList = mutableListOf<Habit>()
                    for (snap in snapshot.children){
                        val habitID = snap.child("id").getValue(String::class.java)
                        val habitName = snap.child("name").getValue(String::class.java)
                        val habitDesc = snap.child("description").getValue(String::class.java)
                        val habitScore = snap.child("score").getValue(Int::class.java)
                        val habitStreak = snap.child("streak").getValue(Int::class.java)
                        val habitCompletion = snap.child("completion").getValue(Boolean::class.java)
                        val habitStartDate = getDateFromDB(snap.child("startDate"))

                        val habit = Habit(habitID, habitName, habitStartDate, habitStreak, habitScore, habitDesc, habitCompletion)
                        habit.let { habitList.add(it) }
                    }
                    habitAdapter = HabitAdapter(habitList, userRef.child("habits"))
                    recyclerViewHabits.adapter = habitAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }


        floatingActionButton.setOnClickListener {
            showAddHabitDialog(currentUserID)
        }


    }


    private fun resetCompletion(snap: DataSnapshot, userHabitRef: DatabaseReference) {
        val id = snap.child("id").getValue(String::class.java)
        userHabitRef.child(id!!).child("completion").setValue(false)
    }

    private fun updateStreak(snap: DataSnapshot, userHabitRef: DatabaseReference, isConsecutiveLogIn: Boolean) {
        val completion = snap.child("completion").getValue(Boolean::class.java)
        val id = snap.child("id").getValue(String::class.java)

        if (isConsecutiveLogIn && completion == true){
            var streak = snap.child("streak").getValue(Int::class.java)
            streak = streak?.plus(1)
            userHabitRef.child(id!!).child("streak").setValue(streak)
        }else{
            userHabitRef.child(id!!).child("streak").setValue(0)
        }
    }

    private fun updateScore(snap: DataSnapshot, userHabitRef: DatabaseReference){
        val completion = snap.child("completion").getValue(Boolean::class.java)

        if (completion == true){
            val score = snap.child("score").getValue(Int::class.java)
            val streak = snap.child("streak").getValue(Int::class.java)
            val updatedScore = (score?.plus((1.5 * streak!!)))!!.toInt() + 10
            val id = snap.child("id").getValue(String::class.java)

            userHabitRef.child(id!!).child("score").setValue(updatedScore)
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

    private fun showAddHabitDialog(currentUserID: String?){
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)

        editTextHabitName = dialogView.findViewById(R.id.editTextHabitName)
        editTextHabitDescription = dialogView.findViewById(R.id.editTextHabitDescription)

        val dialog = AlertDialog.Builder(this, R.style.CustomDialog)
            .setTitle("Add new Habit")
            .setView(dialogView)
            .setPositiveButton("Add"){
                    dialog, which ->
                val habitName = editTextHabitName.text.toString()
                val habitDescription = editTextHabitDescription.text.toString()

                addHabit(habitName, habitDescription, currentUserID)

            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            positiveButton.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            negativeButton.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }

        dialog.show()
    }

    private fun addHabit(name: String, description: String, currentUserID: String?){
        val currentDate = LocalDate.now()

        currentUserID?.let {uid->
            val habitRef = database.child(uid).child("habits")
            val habitID = habitRef.push().key

            val habit = Habit(habitID, name, currentDate, 0, 0, description, false)
            habitID?.let {
                habitRef.child(it).setValue(habit)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Habit added successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{
                        Toast.makeText(this, "Failed to add habit: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    }

}