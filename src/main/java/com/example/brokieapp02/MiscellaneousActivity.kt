package com.example.brokieapp02
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MiscellaneousActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MiscellaneousAdapter
    private val miscellaneousItems = mutableListOf<MiscellaneousItem>()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.miscellaneous_layout)

        sharedPreferences = getSharedPreferences("MiscellaneousPrefs", Context.MODE_PRIVATE)

        loadItems()

        recyclerView = findViewById(R.id.recyclerViewMisc)
        adapter = MiscellaneousAdapter(miscellaneousItems)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.buttonaddNew).setOnClickListener {
            showAddNewItemDialog()
        }
    }

    private fun showAddNewItemDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.editTextItemName)
        val amountEditText = dialogView.findViewById<EditText>(R.id.editTextItemAmount)

        // Build the dialog
        AlertDialog.Builder(this)
            .setTitle("Add New Item")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val name = nameEditText.text.toString()
                val amount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0

                // Add new item to the list and notify the adapter
                if (name.isNotEmpty() && amount > 0) {
                    val newItem = MiscellaneousItem(name, amount)
                    miscellaneousItems.add(0, newItem) // Add at the beginning for newest on top
                    adapter.notifyItemInserted(0)
                    recyclerView.scrollToPosition(0)

                    // Save items to SharedPreferences
                    saveItems()
                } else {
                    Toast.makeText(this, "Please enter valid name and amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveItems() {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(miscellaneousItems)
        editor.putString("MiscellaneousItems", json)
        editor.apply()
    }

    private fun loadItems() {
        val gson = Gson()
        val json = sharedPreferences.getString("MiscellaneousItems", null)
        val type = object : TypeToken<List<MiscellaneousItem>>() {}.type
        val loadedItems: List<MiscellaneousItem>? = gson.fromJson(json, type)

        loadedItems?.let {
            miscellaneousItems.clear()
            miscellaneousItems.addAll(it)
            updateTotal()
        }
    }
//    private fun updatePercentageWasted(dailyLimit: Int, totalSpending: Int) {
//        val wastedAmount = totalSpending - dailyLimit
//        val percentageWasted = if (dailyLimit > 0) {
//            (wastedAmount.toDouble() / dailyLimit) * 100
//        } else {
//            0.0
//        }
//
//        val percentageWastedTextView: TextView = findViewById(R.id.percentageWastedTextView)
//        percentageWastedTextView.text = String.format("Percentage Wasted: %.2f%%", percentageWasted)
//    }
    private fun updateTotal() {
        val total = miscellaneousItems.sumOf { it.amount }
        val totalTextView: TextView = findViewById(R.id.TotalMisc)
        val totalMicsTextView: TextView = findViewById(R.id.TotalMics)

        totalTextView.text = total.toString()
        //totalMicsTextView.text = "Total\nMiscellaneous expenses: $total"
    }
}

