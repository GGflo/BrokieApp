package com.example.brokieapp02

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DailyTotalsAdapter(private val dailyTotals: List<Pair<String, Int>>, private val dailyLimit: Int) : RecyclerView.Adapter<DailyTotalsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val totalTextView: TextView = itemView.findViewById(R.id.totalTextView)
        val surplusTextView: TextView = itemView.findViewById(R.id.surplusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daily_total, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (date, total) = dailyTotals[position]
        val surplus = dailyLimit - total

        holder.dateTextView.text = date
        holder.totalTextView.text = total.toString()
        holder.surplusTextView.text = surplus.toString()

        holder.surplusTextView.text = if (surplus >= 0) {
            "+$surplus"
        } else {
            surplus.toString()
        }
        holder.surplusTextView.setTextColor(
            if (surplus >= 0)holder.itemView.context.getColor(android.R.color.holo_green_dark)
            else holder.itemView.context.getColor(android.R.color.holo_red_dark)
        )
        holder.totalTextView.setTextColor(
            if (total>=dailyLimit) holder.itemView.context.getColor(android.R.color.holo_orange_light)
            else holder.itemView.context.getColor(android.R.color.white)
        )
    }

    override fun getItemCount(): Int {
        return dailyTotals.size
    }
}
