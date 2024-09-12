package com.example.brokieapp02

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DailyTotalsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_totals)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve data from Intent
        val dailyTotals = intent.getSerializableExtra("EXTRA_DAILY_TOTALS") as? Array<Pair<String, Int>> ?: emptyArray()

        // Set up the adapter
        val adapter = DailyTotalsAdapter(dailyTotals.toList())
        recyclerView.adapter = adapter
    }
}


