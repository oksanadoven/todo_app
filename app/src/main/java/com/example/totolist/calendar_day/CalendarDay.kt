package com.example.totolist.calendar_day

data class CalendarDay(
    val currentDate: String,
    val dayOfWeek: String,
    val dateInMs: Long,
    val databaseDate: String,
    var isSelected: Boolean = false
) {
}