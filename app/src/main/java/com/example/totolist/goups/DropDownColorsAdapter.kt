package com.example.totolist.goups

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.totolist.R

class DropDownColorsAdapter(val context: Context) : BaseAdapter() {

    private val dataSource: ArrayList<String> = ArrayList()

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setColors(colors: List<String>) {
        dataSource.clear()
        dataSource.addAll(colors)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ItemHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.group_color_spinner_item, parent, false)
            viewHolder = ItemHolder(view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ItemHolder
        }
        val hex = dataSource[position]
        val currentColor = Color.parseColor(hex)
        viewHolder.image.setColorFilter(currentColor)
        return view
    }

    private class ItemHolder(view : View?) {
        val image = view?.findViewById(R.id.spinner_color_image) as ImageView

    }
}