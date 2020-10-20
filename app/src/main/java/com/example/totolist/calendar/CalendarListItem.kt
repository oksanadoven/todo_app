package com.example.totolist.calendar

import androidx.annotation.LayoutRes

interface CalendarListItem: Comparable<CalendarListItem> {
    @LayoutRes
    fun getLayoutResId(): Int

}