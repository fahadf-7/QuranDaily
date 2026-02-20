package com.example.qurandaily

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.qurandaily.R
import androidx.recyclerview.widget.RecyclerView


class HabitProgressAdapter(private val days: List<Int>) :
    RecyclerView.Adapter<HabitProgressAdapter.DayVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_progress_day, parent, false)
        return DayVH(view)
    }

    override fun onBindViewHolder(holder: DayVH, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount() = days.size

    inner class DayVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(progress: Int) {
            itemView.background.setTint(
                if (progress == 1) Color.parseColor("#1B5E20")
                else Color.parseColor("#C8E6C9")
            )
        }
    }
}
