package com.example.totolist.utils

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R

class TaskListDivider(context: Context) : RecyclerView.ItemDecoration() {

    private val divider = ContextCompat.getDrawable(context, R.drawable.task_item_divider)!!

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft + convertPxToDp(parent.context, 50f).toInt()
        val right = parent.width - parent.paddingRight - convertPxToDp(parent.context, 50f).toInt()

        val childCount = parent.childCount
        var i = 0
        while (i < childCount) {
            if (parent.adapter?.getItemViewType(i) == R.layout.group_item) {
                val child = parent.getChildAt(i)
                val params: RecyclerView.LayoutParams =
                    child.layoutParams as RecyclerView.LayoutParams
                val top = child.bottom + params.bottomMargin
                val bottom = top + divider.intrinsicHeight
                divider.setBounds(left, top, right, bottom)
                divider.draw(c)
            }
            i++
        }
    }

    private fun convertPxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }
}