package com.example.brokieapp02

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LimitCalculatorActivity : AppCompatActivity() {

    private lateinit var enterIncome: EditText
    private lateinit var buttonCalculate: Button
    private lateinit var buttonApplySS: Button
    private lateinit var buttonApplyS: Button
    private lateinit var buttonApplyE: Button
    private lateinit var textViewSSLimit: TextView
    private lateinit var textViewSaverLimit: TextView
    private lateinit var textViewExactLimit: TextView
    private lateinit var textViewSSaverAmount: TextView
    private lateinit var textViewSaverAmount: TextView
    private lateinit var textViewEAmount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_limit_calculator)

        // Initialize views
        enterIncome = findViewById(R.id.enterIncome)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        buttonApplySS = findViewById(R.id.buttonApplySS)
        buttonApplyS = findViewById(R.id.buttonApplyS)
        buttonApplyE = findViewById(R.id.buttonApplyE)
        textViewSSLimit = findViewById(R.id.textViewSSLimit)
        textViewSaverLimit = findViewById(R.id.textViewSaverLimit)
        textViewExactLimit = findViewById(R.id.textViewExactLimit)
        textViewSSaverAmount = findViewById(R.id.textViewSSaverAmount)
        textViewSaverAmount = findViewById(R.id.textViewSaverAmount)
        textViewEAmount = findViewById(R.id.textViewEAmount)

        // Set Calculate button click listener
        buttonCalculate.setOnClickListener {
            calculateLimits()
        }

        // Set Apply button listeners
        buttonApplySS.setOnClickListener {
            applyLimit(textViewSSLimit.text.toString().toInt())
        }

        buttonApplyS.setOnClickListener {
            applyLimit(textViewSaverLimit.text.toString().toInt())
        }

        buttonApplyE.setOnClickListener {
            applyLimit(textViewExactLimit.text.toString().toInt())
        }
    }

    private fun calculateLimits() {
        val incomeStr = enterIncome.text.toString()
        if (incomeStr.isEmpty()) {
            Toast.makeText(this, "Please enter your income", Toast.LENGTH_SHORT).show()
            return
        }

        val income = incomeStr.toInt()
        val limit = income / 30
        val Slimit = (limit * 0.9).toInt() // Calculating Saver Limit as Integer
        val SSlimit = (limit * 0.85).toInt() // Calculating Super Saver Limit as Integer

        // Update the limits in the TextViews
        textViewSSLimit.text = SSlimit.toString()
        textViewSaverLimit.text = Slimit.toString()
        textViewExactLimit.text = limit.toString()

        val Ssaving = (limit - Slimit) * 30 // Total savings for Saver
        val SSsaving = (limit - SSlimit) * 30 // Total savings for Super Saver

        textViewSSaverAmount.text = SSsaving.toString()
        textViewSaverAmount.text = Ssaving.toString()
    }

    private fun applyLimit(limitValue: Int) {
        // Use SharedPreferences to store the limit in MainActivity
        val sharedPref = getSharedPreferences("MainActivityPreferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val currentLimit = sharedPref.getInt("MainActivityLimit", 0)
        val newLimit = currentLimit + limitValue
        var editlimit = getSharedPreferences("editLimit",0)
        editor.putInt("MainActivityLimit", newLimit)

        editor.apply()

        Toast.makeText(this, "Limit applied: $limitValue", Toast.LENGTH_SHORT).show()
    }
}
