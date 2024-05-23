package com.example.habitapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference

class HabitAdapter(private val habits: MutableList<Habit>, private val database: DatabaseReference):
    RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textViewHabitName: TextView = itemView.findViewById(R.id.textViewHabitName)
        val textViewHabitDescription: TextView = itemView.findViewById(R.id.textViewHabitDescription)
        val textViewScore: TextView = itemView.findViewById(R.id.textViewScore)
        val textViewStreakScore: TextView = itemView.findViewById(R.id.textViewStreakScore)
        val checkBoxCompleted: CheckBox = itemView.findViewById(R.id.checkBoxCompleted)
        val userHabitRef: DatabaseReference = database
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.habit_item, parent, false)
        return HabitViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return habits.size
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.textViewHabitName.text = habit.name
        holder.textViewHabitDescription.text = habit.description
        holder.textViewScore.text = "Score: ${habit.score}"
        holder.textViewStreakScore.text = habit.streak.toString()
        holder.checkBoxCompleted.isChecked = habit.completion!!

        holder.checkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
            updateCheckBoxDB(habit.id, isChecked, holder.userHabitRef)
        }

    }

    private fun updateCheckBoxDB(id: String?, checked: Boolean, userHabitRef: DatabaseReference) {
        userHabitRef.child(id!!).child("completion").setValue(checked)
    }

}
