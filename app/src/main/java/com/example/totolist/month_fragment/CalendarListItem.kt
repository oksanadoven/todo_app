package com.example.totolist.month_fragment

import androidx.annotation.LayoutRes

interface CalendarListItem: Comparable<CalendarListItem> {
    @LayoutRes
    fun getLayoutResId(): Int
}