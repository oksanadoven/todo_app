package com.example.totolist.search_fragment

import com.example.totolist.R
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

data class SearchHeaderItem (
    val date: Long
) : SearchItem {
    override fun getLayoutResId(): Int {
        return R.layout.search_card_date_header
    }

    fun getDateString(date: Long) : String {
        return Instant.ofEpochMilli(date).atZone(ZoneId.of(ZoneOffset.UTC.toString()))
            .toLocalDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d")).toUpperCase(Locale.ROOT)
    }
}