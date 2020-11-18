package com.example.totolist.search_fragment

import androidx.annotation.LayoutRes

interface SearchItem {
    @LayoutRes
    fun getLayoutResId(): Int
}