package com.example.habitapp

import DataStoreManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerViewHabits: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var logoutBtn: Button
    private lateinit var addHabitBtn: Button
    private lateinit var socialBtn: Button

    private lateinit var editTextHabitName: EditText
    private lateinit var editTextHabitDescription: EditText

    private var habitList : MutableList<Habit> = mutableListOf()

    private lateinit var dataStoreManager: DataStoreManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        logoutBtn = findViewById(R.id.logoutBtn)
        addHabitBtn = findViewById(R.id.addHabitBtn)
        socialBtn = findViewById(R.id.socialBtn)


        recyclerViewHabits = findViewById(R.id.habitsRecyclerView)
        recyclerViewHabits.layoutManager = LinearLayoutManager(this)

        database = Firebase.database.reference


        dataStoreManager = DataStoreManager(this)



        lifecycleScope.launch {
            dataStoreManager.userId.collect {
                currentUserID ->
                if (currentUserID != null){
                    //Display the User currently logged in at top
                    currentUserID?.let{uid->
                        val userRef = database.child(uid)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                for (snap in snapshot.child("habits").children){
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



                                val today = LocalDate.now()
                                val lastLogin = getDateFromDB(snapshot.child("userinfo").children.firstOrNull()!!.child("lastLogin"))
                                if (today > lastLogin){
                                    for (i in 0..<habitList.size){
                                        if (today == lastLogin!!.plusDays(1)){
                                            updateScore(habitList.get(i), userRef.child("habits"))
                                            updateStreak(habitList.get(i), userRef.child("habits"), true)
                                            resetCompletion(habitList.get(i), userRef.child("habits"))
                                        }else{
                                            updateScore(habitList.get(i), userRef.child("habits"))
                                            updateStreak(habitList.get(i), userRef.child("habits"), false)
                                            resetCompletion(habitList.get(i), userRef.child("habits"))
                                        }
                                    }
                                    val userInfoId = snapshot.child("userinfo").children.firstOrNull()!!.child("id").getValue(String::class.java)
                                    userRef.child("userinfo").child(userInfoId!!).child("lastLogin").setValue(today)
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO()

                            }

                        })

                    }
                }
                addHabitBtn.setOnClickListener {
                    showAddHabitDialog(currentUserID)
                }

                logoutBtn.setOnClickListener {
                    performLogout(this@MainActivity)
                }

                socialBtn.setOnClickListener {
                    Toast.makeText(this@MainActivity, "Feature is coming soon!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun performLogout(context: Context) {
        lifecycleScope.launch {
            dataStoreManager.clearUserData()
            val intent = Intent(context, LogInPage::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun resetCompletion(habit: Habit, userHabitRef: DatabaseReference) {
        val id = habit.id
        userHabitRef.child(id!!).child("completion").setValue(false)
        habit.completion = false
    }

    private fun updateStreak(habit: Habit, userHabitRef: DatabaseReference, isConsecutiveLogIn: Boolean) {
        val completion = habit.completion
        val id = habit.id

        if (isConsecutiveLogIn && completion == true){
            var streak = habit.streak
            streak = streak?.plus(1)
            userHabitRef.child(id!!).child("streak").setValue(streak)
            habit.streak = streak
        }else{
            userHabitRef.child(id!!).child("streak").setValue(0)
            habit.streak = 0
        }
    }

    private fun updateScore(habit: Habit, userHabitRef: DatabaseReference){
        val completion = habit.completion

        if (completion == true){
            val score = habit.score
            val streak = habit.streak
            val updatedScore = (score?.plus((1.5 * streak!!)))!!.toInt() + 10
            val id = habit.id

            userHabitRef.child(id!!).child("score").setValue(updatedScore)
            habit.score = updatedScore
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
                        habitList.add(habit)
                        habitAdapter.notifyItemInserted(habitList.size - 1)
                        Toast.makeText(this, "Habit added successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{
                        Toast.makeText(this, "Failed to add habit: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    }

}