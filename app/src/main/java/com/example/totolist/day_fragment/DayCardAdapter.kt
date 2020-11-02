package com.example.totolist.day_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R

class DayCardAdapter : ListAdapter<CalendarDay, DayCardAdapter.DayCardViewHolder>(
    DaysDiffCallback()
) {

    interface OnItemClickListener {
        fun onDaySelected(day: CalendarDay)
    }

    class DayCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardDate: TextView = itemView.findViewById(R.id.date_number)
        private val cardDayOfWeek: TextView = itemView.findViewById(R.id.day_of_week)
        fun bind(date: String, dayOfWeek: String) {
            cardDate.text = date
            cardDayOfWeek.text = dayOfWeek
        }
    }

    private class DaysDiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(
            oldItem: CalendarDay,
            newItem: CalendarDay
        ): Boolean {
            return oldItem.databaseDate == newItem.databaseDate
        }

        override fun areContentsTheSame(
            oldItem: CalendarDay,
            newItem: CalendarDay
        ): Boolean {
            return oldItem == newItem
        }

    }

    var listener: OnItemClickListener? = null
    private val itemsLimit = 1825

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.calendar_day_card, parent, false)
        val holder = DayCardViewHolder(itemView)
        itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val previousSelected = currentList.find { it.isSelected }
                if (previousSelected != null) {
                    previousSelected.isSelected = false
                    val index = currentList.indexOf(previousSelected)
                    notifyItemChanged(index)
                }
                val item = getItem(position)
                item.isSelected = true
                notifyItemChanged(position)
                listener?.onDaySelected(item)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: DayCardViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem.currentDate, currentItem.dayOfWeek)
        val color = holder.itemView.resources.getColor(
            if (currentItem.isSelected) {
                R.color.colorPrimaryDarkTransparent
            } else {
                R.color.colorPrimaryDark
            }
        )
        val cardView: CardView = holder.itemView.findViewById(R.id.day_card)
        cardView.setCardBackgroundColor(color)
    }

    override fun getItemCount(): Int {
        return if (currentList.size > itemsLimit) {
            itemsLimit
        } else {
            currentList.size
        }
    }

}