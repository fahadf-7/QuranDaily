package com.example.qurandaily

import Habit
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateHabitDialog(
    private val existingHabit: Habit? = null,     // null = new, non-null = edit
    private val onHabitSaved: (Habit) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_create_habit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val title = view.findViewById<EditText>(R.id.et_title)
        val desc = view.findViewById<EditText>(R.id.et_description)
        val category = view.findViewById<Spinner>(R.id.spinner_category)
        val priority = view.findViewById<Spinner>(R.id.spinner_priority)
        val time = view.findViewById<Spinner>(R.id.spinner_time)
        val dailyTarget = view.findViewById<EditText>(R.id.et_daily_target)
        val unit = view.findViewById<Spinner>(R.id.spinner_unit)
        val reminder = view.findViewById<EditText>(R.id.et_reminder)
        val btnCreate = view.findViewById<Button>(R.id.btn_create)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        val ctx = requireContext()

        // Spinners
        ArrayAdapter.createFromResource(
            ctx, R.array.habit_categories, android.R.layout.simple_spinner_dropdown_item
        ).also { category.adapter = it }

        ArrayAdapter.createFromResource(
            ctx, R.array.habit_priorities, android.R.layout.simple_spinner_dropdown_item
        ).also { priority.adapter = it }

        ArrayAdapter.createFromResource(
            ctx, R.array.habit_times, android.R.layout.simple_spinner_dropdown_item
        ).also { time.adapter = it }

        ArrayAdapter.createFromResource(
            ctx, R.array.habit_units, android.R.layout.simple_spinner_dropdown_item
        ).also { unit.adapter = it }

        // If editing, pre-fill fields
        existingHabit?.let { h ->
            title.setText(h.title)
            desc.setText(h.description)
            category.setSelection((category.adapter as ArrayAdapter<String>).getPosition(h.category))
            priority.setSelection((priority.adapter as ArrayAdapter<String>).getPosition(h.priority))
            time.setSelection((time.adapter as ArrayAdapter<String>).getPosition(h.preferredTime))
            if (h.dailyTarget > 0) dailyTarget.setText(h.dailyTarget.toString())
            unit.setSelection((unit.adapter as ArrayAdapter<String>).getPosition(h.targetUnit))
            reminder.setText(h.reminderNote)
            btnCreate.text = "Update Habit"
        }

        btnCreate.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            val collRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("habits")

            val docRef = if (existingHabit == null) {
                collRef.document()                          // New habit
            } else {
                collRef.document(existingHabit.id)          // Update existing
            }

            val habit = Habit(
                id = docRef.id,
                title = title.text.toString(),
                description = desc.text.toString(),
                category = category.selectedItem.toString(),
                priority = priority.selectedItem.toString(),
                preferredTime = time.selectedItem.toString(),
                dailyTarget = dailyTarget.text.toString().toIntOrNull() ?: 0,
                targetUnit = unit.selectedItem.toString(),
                reminderNote = reminder.text.toString(),
                progress = existingHabit?.progress ?: MutableList(7) { 0 } // keep past progress if editing
            )

            docRef.set(habit)
                .addOnSuccessListener {
                    onHabitSaved(habit)
                    dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to save habit", Toast.LENGTH_SHORT).show()
                }
        }

        btnCancel.setOnClickListener { dismiss() }
    }
}
