package com.example.brokieapp02

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.core.content.ContextCompat

class DailyTotalsActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_totals)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve data from Intent
        val dailyTotals = intent.getSerializableExtra("EXTRA_DAILY_TOTALS") as? Array<Pair<String, Int>> ?: emptyArray()

        // Retrieve the limit (for example, from an Intent extra)
        val dailyLimit = intent.getIntExtra("EXTRA_DAILY_LIMIT", 200) // Default to 200 if not provided

        // Calculate average daily spending and surplus
        val totalSpending = dailyTotals.sumOf { it.second }
        val averageSpending = if (dailyTotals.isNotEmpty()) totalSpending / dailyTotals.size else 0
        val totalSurplus = dailyTotals.sumOf { dailyLimit - it.second }
        val averageSurplus = if (dailyTotals.isNotEmpty()) totalSurplus / dailyTotals.size else 0

        // Set up the adapter with dailyTotals and the actual daily limit
        val adapter = DailyTotalsAdapter(dailyTotals.sortedByDescending { it.first }.toMutableList(), dailyLimit)
        recyclerView.adapter = adapter

        // Update average spending and surplus views
        val averageSpendingTextView: TextView = findViewById(R.id.averageSpendingTextView)
        val averageSurplusTextView: TextView = findViewById(R.id.averageSurplusTextView)

        averageSpendingTextView.text = averageSpending.toString()
        averageSurplusTextView.text = averageSurplus.toString()

        // Change color based on conditions
        if (averageSpending > dailyLimit) {
            averageSpendingTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
        } else {
            averageSpendingTextView.setTextColor(ContextCompat.getColor(this, R.color.blue_purple))
        }

        if (averageSurplus < 0) {
            averageSurplusTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        } else {
            averageSurplusTextView.text="+$averageSurplus"
            averageSurplusTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        }
    }
}
