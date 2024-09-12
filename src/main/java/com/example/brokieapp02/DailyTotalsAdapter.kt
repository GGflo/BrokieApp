package com.example.brokieapp02

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DailyTotalsAdapter(private val dailyTotals: List<Pair<String, Int>>) : RecyclerView.Adapter<DailyTotalsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val totalTextView: TextView = view.findViewById(R.id.totalTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daily_total, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (date, total) = dailyTotals[position]
        holder.dateTextView.text = date // Display the formatted date
        holder.totalTextView.text = total.toString()
    }

    override fun getItemCount(): Int = dailyTotals.size
}

