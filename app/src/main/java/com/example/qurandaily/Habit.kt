data class Habit(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val priority: String = "",
    val preferredTime: String = "",
    val dailyTarget: Int = 0,
    val targetUnit: String = "",
    val reminderNote: String = "",
    var progress: MutableList<Int> = MutableList(7) { 0 }   // 0/1 for last 7 days
)
