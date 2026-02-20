package com.example.qurandaily

import Habit
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(
    private val list: MutableList<Habit>,
    private val onToggleComplete: (Habit) -> Unit,
    private val onEdit: (Habit) -> Unit,
    private val onDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitVH(view)
    }

    override fun onBindViewHolder(holder: HabitVH, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    inner class HabitVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(habit: Habit) {
            val tvTitle = itemView.findViewById<TextView>(R.id.tv_habit_title)
            val tvDesc = itemView.findViewById<TextView>(R.id.tv_description)
            val tvPriority = itemView.findViewById<TextView>(R.id.tv_priority)
            val tvStreak = itemView.findViewById<TextView>(R.id.tv_streak)
            val tvSuccess = itemView.findViewById<TextView>(R.id.tv_success_rate)
            val ivCheck = itemView.findViewById<ImageView>(R.id.iv_check)
            val ivEdit = itemView.findViewById<ImageView>(R.id.iv_edit)
            val ivDelete = itemView.findViewById<ImageView>(R.id.iv_delete)
            val rvDays = itemView.findViewById<RecyclerView>(R.id.rv_last7days)

            tvTitle.text = habit.title
            tvDesc.text = habit.description
            tvPriority.text = habit.priority

            when (habit.priority.lowercase()) {
                "low" -> tvPriority.setBackgroundResource(R.drawable.bg_priority_low)
                "medium" -> tvPriority.setBackgroundResource(R.drawable.bg_priority_medium)
                "high" -> tvPriority.setBackgroundResource(R.drawable.bg_priority_high)
                else -> tvPriority.setBackgroundResource(R.drawable.bg_priority_medium)
            }

            // horizontal 7-day dots
            rvDays.layoutManager = LinearLayoutManager(
                itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            rvDays.adapter = HabitProgressAdapter(habit.progress)

            // ----- success rate over last 7 days -----
            val doneCount = habit.progress.count { it == 1 }
            val successPercent =
                if (habit.progress.isNotEmpty()) (doneCount * 100) / habit.progress.size else 0

            // ----- streak: consecutive 1s from the most recent day backwards -----
            var streak = 0
            for (i in habit.progress.size - 1 downTo 0) {
                if (habit.progress[i] == 1) streak++ else break
            }

            tvStreak.text = "$streak\nDay Streak"
            tvSuccess.text = "$successPercent%\nSuccess Rate"

            // today completed? (to style the tick)
            val todayDone = habit.progress.lastOrNull() == 1
            ivCheck.alpha = if (todayDone) 1f else 0.4f

            ivCheck.setOnClickListener { onToggleComplete(habit) }
            ivEdit.setOnClickListener { onEdit(habit) }
            ivDelete.setOnClickListener { onDelete(habit) }
        }
    }

}
