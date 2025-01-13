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

class HabitAdapter(private val habits: MutableList<Habit>, private val database: DatabaseReference):
    RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textViewHabitName: TextView = itemView.findViewById(R.id.textViewHabitName)
        val textViewHabitDescription: TextView = itemView.findViewById(R.id.textViewHabitDescription)
        val textViewScore: TextView = itemView.findViewById(R.id.textViewScore)
        val textViewStreakScore: TextView = itemView.findViewById(R.id.textViewStreakScore)
        val checkBoxCompleted: CheckBox = itemView.findViewById(R.id.checkBoxCompleted)
        val deleteHabitBtn: Button = itemView.findViewById(R.id.deleteHabitBtn)
        val userHabitRef: DatabaseReference = database
        val cardLayout: View = itemView.findViewById(R.id.Card)
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

        updateCardColor(holder.cardLayout, habit.completion!!)

        holder.checkBoxCompleted.setOnCheckedChangeListener(null)
        holder.checkBoxCompleted.isChecked = habit.completion!!

        holder.checkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
            habit.completion = isChecked
            updateCheckBoxDB(habit.id, isChecked, holder.userHabitRef)
            updateCardColor(holder.cardLayout, isChecked)
        }

        holder.deleteHabitBtn.setOnClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle("Delete Habit")
                .setMessage("Are you sure, you want to delete the ${holder.textViewHabitName.text} habit?")
                .setPositiveButton("Yes") { dialog, which ->
                    deleteHabit(habit, position, holder.userHabitRef, context)
                }
                .setNegativeButton("No", null)
                .show()
        }


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

    private fun deleteHabit(
        habit: Habit,
        position: Int,
        userHabitRef: DatabaseReference,
        context: Context
    ) {
        val habitID = habit.id
        if (habitID != null){
            userHabitRef.child(habitID).removeValue()
                .addOnSuccessListener {
                    habits.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, habits.size)
                    Toast.makeText(context, "Habit deleted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{e->
                    Toast.makeText(context, "Failed to delete habit: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun updateCheckBoxDB(id: String?, checked: Boolean, userHabitRef: DatabaseReference) {
        userHabitRef.child(id!!).child("completion").setValue(checked)
    }

}
