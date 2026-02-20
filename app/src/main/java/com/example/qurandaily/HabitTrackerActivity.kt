package com.example.qurandaily

import Habit
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HabitTrackerActivity : AppCompatActivity() {

    private lateinit var habitListRecycler: RecyclerView
    private lateinit var emptyHabitsLayout: View
    private lateinit var btnAddHabit: Button
    private lateinit var tvTodayStats: TextView

    private val habits = mutableListOf<Habit>()
    private lateinit var adapter: HabitAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_tracker)

        habitListRecycler = findViewById(R.id.habitListRecycler)
        emptyHabitsLayout = findViewById(R.id.emptyHabitsLayout)
        btnAddHabit = findViewById(R.id.btnAddHabit)
        tvTodayStats = findViewById(R.id.habitTrackerTodayStats)

        adapter = HabitAdapter(
            habits,
            onToggleComplete = { toggleHabitCompletion(it) },
            onEdit = { openEditHabitDialog(it) },
            onDelete = { deleteHabit(it) }
        )

        habitListRecycler.adapter = adapter
        habitListRecycler.layoutManager = LinearLayoutManager(this)

        btnAddHabit.setOnClickListener { openCreateHabitDialog() }
        emptyHabitsLayout.findViewById<Button>(R.id.btn_create_first_habit)
            .setOnClickListener { openCreateHabitDialog() }

        loadHabitsFromFirestore()
    }

    private fun refreshUI() {
        if (habits.isEmpty()) {
            emptyHabitsLayout.visibility = View.VISIBLE
            habitListRecycler.visibility = View.GONE
        } else {
            emptyHabitsLayout.visibility = View.GONE
            habitListRecycler.visibility = View.VISIBLE
        }
        updateSummary()
    }

    private fun openCreateHabitDialog() {
        val dialog = CreateHabitDialog { habit ->
            habits.add(habit)
            adapter.notifyItemInserted(habits.lastIndex)
            refreshUI()
        }
        dialog.show(supportFragmentManager, "CREATE_HABIT")
    }

    private fun openEditHabitDialog(habit: Habit) {
        val dialog = CreateHabitDialog(existingHabit = habit) { updated ->
            val index = habits.indexOfFirst { it.id == updated.id }
            if (index != -1) {
                habits[index] = updated
                adapter.notifyItemChanged(index)
                refreshUI()
            }
        }
        dialog.show(supportFragmentManager, "EDIT_HABIT")
    }

    private fun toggleHabitCompletion(habit: Habit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        val newProgress = habit.progress.toMutableList()
        val lastIndex = newProgress.lastIndex
        newProgress[lastIndex] = if (newProgress[lastIndex] == 1) 0 else 1

        firestore.collection("users")
            .document(userId)
            .collection("habits")
            .document(habit.id)
            .update("progress", newProgress)
            .addOnSuccessListener {
                habit.progress = newProgress
                adapter.notifyDataSetChanged()
                updateSummary()
            }
    }

    private fun deleteHabit(habit: Habit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .document(userId)
            .collection("habits")
            .document(habit.id)
            .delete()
            .addOnSuccessListener {
                val index = habits.indexOf(habit)
                if (index != -1) {
                    habits.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    refreshUI()
                }
            }
    }

    private fun loadHabitsFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("habits")
            .get()
            .addOnSuccessListener { snapshot ->
                habits.clear()
                for (doc in snapshot.documents) {
                    val habit = doc.toObject(Habit::class.java)
                    if (habit != null) habits.add(habit)
                }
                adapter.notifyDataSetChanged()
                refreshUI()
            }
    }

    private fun updateSummary() {
        val total = habits.size
        if (total == 0) {
            tvTodayStats.text = "0/0 completed today • 0% progress"
            return
        }

        val completedToday = habits.count { it.progress.lastOrNull() == 1 }
        val percent = (completedToday * 100) / total

        tvTodayStats.text = "$completedToday/$total completed today • $percent% progress"
    }
}
