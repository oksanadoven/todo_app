package com.example.totolist.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R

class TaskItemDivider(context: Context) : RecyclerView.ItemDecoration() {

    private val resources = context.resources
    private var divider: Drawable? = ResourcesCompat.getDrawable(
        resources,
        R.drawable.task_item_divider,
        resources.newTheme()
    )

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft + convertPxToDp(parent.context, 280f).toInt()
        val right = parent.width - parent.paddingRight - convertPxToDp(parent.context, 40f).toInt()

        val childCount = parent.childCount
        var i = 0
        while (i < childCount) {
            val child = parent.getChildAt(i)
            val params: RecyclerView.LayoutParams = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + (divider?.intrinsicHeight ?: 0)
            divider?.setBounds(left, top, right, bottom)
            divider?.draw(c)
            i++
        }
    }

    private fun convertPxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }
}