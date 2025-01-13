package com.example.habitapp

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference

class FriendsHabitAdapter(private val habits: MutableList<Habit>):
    RecyclerView.Adapter<FriendsHabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textViewHabitName: TextView = itemView.findViewById(R.id.textViewHabitName)
        val textViewHabitDescription: TextView = itemView.findViewById(R.id.textViewHabitDescription)
        val textViewStreakScore: TextView = itemView.findViewById(R.id.textViewStreakScore)
        val textViewScore: TextView = itemView.findViewById(R.id.textViewScore)
        val checkBoxCompleted: CheckBox = itemView.findViewById(R.id.checkBoxCompleted)
        val cardLayout: View = itemView.findViewById(R.id.Card)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_habit_item, parent, false)
        return HabitViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return habits.size
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.textViewHabitName.text = habit.name
        holder.textViewHabitDescription.text = habit.description
        holder.textViewStreakScore.text = habit.streak.toString()
        holder.textViewScore.text = "Score: ${habit.score}"
        holder.checkBoxCompleted.isChecked = habit.completion!!

        updateCardColor(holder.cardLayout, habit.completion!!)


    }

    private fun updateCardColor(
        cardView: View,
        isChecked: Boolean,
    ) {
        if (isChecked) {
            cardView.setBackgroundResource(R.drawable.roundstyle_opp)

        } else {
            cardView.setBackgroundResource(R.drawable.roundstyle)
        }
    }


}
