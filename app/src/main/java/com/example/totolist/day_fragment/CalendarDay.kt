package com.example.totolist.day_fragment

data class CalendarDay(
    val currentDate: String,
    val dayOfWeek: String,
    val timeInMillis: Long,
    var isSelected: Boolean = false
)