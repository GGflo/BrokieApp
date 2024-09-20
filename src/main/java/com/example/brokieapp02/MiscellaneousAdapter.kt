package com.example.brokieapp02

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MiscellaneousAdapter(private var items: List<MiscellaneousItem>) :
    RecyclerView.Adapter<MiscellaneousAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.textViewName)
        val amountTextView: TextView = view.findViewById(R.id.textViewAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_miscellaneous, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.name
        holder.amountTextView.text = item.amount.toString()
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<MiscellaneousItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
