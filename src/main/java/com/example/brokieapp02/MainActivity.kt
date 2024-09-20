package com.example.brokieapp02

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var textViewToday: TextView
    private lateinit var textViewNet: TextView
    private lateinit var editLimit: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var textViewTitle: TextView
    private var drawableIndex = 1
    private val drawableCount = 3
    private val textColors = listOf(Color.parseColor("#FFB829"), Color.parseColor("#FFB829"), Color.parseColor("#D32F2F"))
    private lateinit var textViewUserInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        textViewUserInfo = findViewById(R.id.textViewUserInfo)
        checkUserSignIn()

        initializeViews()
        loadSavedValues()
        checkAndUpdateDailyValues()
        setupButtonListeners()

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

    }
    override fun onStart() {
        super.onStart()
        checkUserSignIn()
    }

    override fun onResume() {
        super.onResume()
        checkUserSignIn()
        checkAndUpdateDailyValues()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_daily -> {
                openDailyTotalsActivity()
                true
            }
            R.id.menu_limit_calculator -> {
                openLimitCalculatorActivity()
                true
            }
            R.id.menu_miscellaneous -> {
                val intent = Intent(this, MiscellaneousActivity::class.java)
                startActivity(intent)
                true
            }R.id.action_sign_out -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        // Clear any saved user data if necessary
        val sharedPref = getSharedPreferences("MainActivityPreferences", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply() // Clear saved preferences

        // Navigate back to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the back stack
        startActivity(intent)
        finish() // Optional: Finish the current activity
    }

    private fun openLimitCalculatorActivity() {
        val intent = Intent(this, LimitCalculatorActivity::class.java)
        startActivity(intent)
    }


    @SuppressLint("DefaultLocale")
    private fun setupButtonListeners() {
        val buttonPlus: Button = findViewById(R.id.buttonPlus)
        val buttonMinus: Button = findViewById(R.id.buttonMinus)

        buttonPlus.setOnClickListener {
            performCalculation(true)
        }
        buttonMinus.setOnClickListener {
            performCalculation(false)
        }

        textViewTitle.setOnClickListener {
            drawableIndex = (drawableIndex % drawableCount) + 1
            textViewTitle.setBackgroundResource(getDrawableResource(drawableIndex))
            textViewTitle.setTextColor(textColors[drawableIndex - 1])

            saveValue("selectedDrawableIndex", drawableIndex)
        }
    }

    private fun openDailyTotalsActivity() {
        val dailyTotal = textViewToday.text.toString().toIntOrNull() ?: 0
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        val formattedDate = String.format("%02d.%02d.%d", currentDay, currentMonth, currentYear)

        sharedPreferences.edit()
            .putInt("dailyTotalForDate_$formattedDate", dailyTotal)
            .apply()

        val intent = Intent(this, DailyTotalsActivity::class.java)
        val savedTotals = getAllSavedTotals()
        val dailyLimit = sharedPreferences.getInt("savedValueLimit", 200) // Retrieve limit from SharedPreferences or use default
        intent.putExtra("EXTRA_DAILY_TOTALS", savedTotals.toTypedArray())
        intent.putExtra("EXTRA_DAILY_LIMIT", dailyLimit)
        startActivity(intent)

    }

    private fun getAllSavedTotals(): List<Pair<String, Int>> {
        val totals = mutableListOf<Pair<String, Int>>()
        val allEntries = sharedPreferences.all

        for ((key, value) in allEntries) {
            if (key.startsWith("dailyTotalForDate_") && value is Int) {
                val date = key.removePrefix("dailyTotalForDate_")
                totals.add(Pair(date, value))
            }
        }
        return totals
    }

    private fun checkUserSignIn() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            val email = currentUser.email
            textViewUserInfo.text = "Logged in as: $email"
        }
    }

    private fun initializeViews() {
        textViewToday = findViewById(R.id.textViewToday)
        editLimit = findViewById(R.id.editLimit)
        textViewNet = findViewById(R.id.textViewNet)
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        textViewTitle = findViewById(R.id.textViewTitle)
        drawableIndex = sharedPreferences.getInt("selectedDrawableIndex", 1)
        textViewTitle.setBackgroundResource(getDrawableResource(drawableIndex))
        textViewTitle.setTextColor(textColors[drawableIndex - 1])
    }

    private fun getDrawableResource(index: Int): Int {
        return when (index) {
            1 -> R.drawable.rounded_corner_background
            2 -> R.drawable.borderonly
            3 -> R.drawable.borderonly
            else -> R.drawable.rounded_corner_background
        }
    }

    private fun performCalculation(isAddition: Boolean) {
        val editTextValue: EditText = findViewById(R.id.editValue)
        val value = editTextValue.text.toString().toIntOrNull() ?: 0
        val currentValue = textViewToday.text.toString().toInt()
        val newValue = if (isAddition) currentValue + value else currentValue - value

        editTextValue.text = null
        textViewToday.text = newValue.toString()

        saveValue("savedValueLimit", editLimit.text.toString().toInt())
        saveValue("savedValueToday", textViewToday.text.toString().toInt())

        val totalNet = editLimit.text.toString().toInt() - textViewToday.text.toString().toInt()
        updateNetValue(totalNet)
    }

    private fun updateNetValue(netValue: Int) {
        val previousTotalNet = sharedPreferences.getInt("previousTotalNet", 0)

        saveValue("savedValueNet", netValue + previousTotalNet)
        loadSavedValues()
    }

    @SuppressLint("SetTextI18n")
    private fun checkAndUpdateDailyValues() {
        val savedDay = sharedPreferences.getInt("savedDay", -1)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        if (savedDay != currentDay) {
            saveValue("savedValueToday", 0)
            saveValue("savedDay", currentDay)
        }

        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val date: TextView = findViewById(R.id.date)
        date.text = "$currentDay ${monthNames[currentMonth]} $currentYear"

        val savedDayForNet = sharedPreferences.getInt("savedDayForNet", -1)
        if (savedDayForNet != currentDay) {
            saveValue("savedDayForNet", currentDay)
            saveValue("savedValueNetForDay_$currentDay", textViewNet.text.toString().toInt())
            saveValue(
                "savedValueNet",
                textViewNet.text.toString().toInt() + editLimit.text.toString().toInt()
            )
            saveValue("previousTotalNet", textViewNet.text.toString().toInt())
            loadSavedValues()
        }
    }

    private fun saveValue(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    @SuppressLint("SetTextI18n")
    private fun loadSavedValues() {
        val savedValueToday = sharedPreferences.getInt("savedValueToday", 0)
        textViewToday.text = savedValueToday.toString()

        val savedValueLimit = sharedPreferences.getInt("savedValueLimit", 0)
        editLimit.setText(savedValueLimit.toString())

        if (savedValueToday >= savedValueLimit) {
            textViewToday.setTextColor(Color.parseColor("#FFB829"))
        } else {
            textViewToday.setTextColor(Color.WHITE)
        }

        val savedValueNet = sharedPreferences.getInt("savedValueNet", 0)
        if (savedValueNet > 0) {
            textViewNet.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            textViewNet.text = "+${savedValueNet}"
        } else {
            textViewNet.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            textViewNet.text = savedValueNet.toString()
        }
    }
}
