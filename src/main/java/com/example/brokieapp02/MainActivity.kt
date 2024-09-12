package com.example.brokieapp02

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var textViewToday: TextView
    private lateinit var textViewNet: TextView
    private lateinit var editLimit: EditText
    private lateinit var GoogleSignInClient: GoogleSignInClient
    private lateinit var Auth: FirebaseAuth
    private lateinit var signInButton: Button
    private lateinit var dailyButton: Button

    private lateinit var textViewTitle: TextView
    private var drawableIndex = 1
    private val drawableCount = 3
    private val textColors = listOf(Color.parseColor("#FFB829"), Color.parseColor("#FFB829"), Color.parseColor("#D32F2F"))

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        signingIn()
        initializeViews()
        loadSavedValues()
        checkAndUpdateDailyValues()
        setupButtonListeners()
    }

    private fun signingIn() {
        Auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignInClient = GoogleSignIn.getClient(this, gso)

        signInButton = findViewById(R.id.signinButton)
        updateUI()
        signInButton.setOnClickListener {
            if (Auth.currentUser == null) {
                signIn()
            } else {
                signOut()
            }
        }
    }
    private fun signIn() {
        val signInIntent = GoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    private fun signOut() {
        Auth.signOut()
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
        updateUI()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        val user = Auth.currentUser
        if (user != null) {
            signInButton.text= "Sign out"
        } else {
            signInButton.text = "Sign in"
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Handle sign-in failure
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in succeeded
                    updateUI()
                } else {
                    // Sign-in failed
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private fun initializeViews() {
        textViewToday = findViewById(R.id.textViewToday)
        editLimit = findViewById(R.id.editLimit)
        textViewNet = findViewById(R.id.textViewNet)
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        dailyButton = findViewById(R.id.dailyButton)


        textViewTitle = findViewById(R.id.textViewTitle)// New CODE******************
        drawableIndex = sharedPreferences.getInt("selectedDrawableIndex", 1)
        textViewTitle.setBackgroundResource(getDrawableResource(drawableIndex))
        textViewTitle.setTextColor(textColors[drawableIndex - 1])
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

            saveValue("selectedDrawableIndex",drawableIndex)
        }

        dailyButton.setOnClickListener {
            val user = Auth.currentUser
            if (user != null) { // Check if the user is signed in
                val dailyTotal = textViewToday.text.toString()

                val calendar = Calendar.getInstance()
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                val currentMonth = calendar.get(Calendar.MONTH) + 1
                val currentYear = calendar.get(Calendar.YEAR)
                val formattedDate = String.format("%02d.%02d.%d", currentDay, currentMonth, currentYear)

                saveValue("dailyTotalForDate_$formattedDate", dailyTotal.toInt())

                val intent = Intent(this, DailyTotalsActivity::class.java)
                val savedTotals = getAllSavedTotals()

                intent.putExtra("EXTRA_DAILY_TOTALS", savedTotals.toTypedArray())
                startActivity(intent)
            } else {
                // User is not signed in, prompt them to sign in
                signIn()
            }
        }
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
        val previousTotalNet= sharedPreferences.getInt("previousTotalNet",0)

        saveValue("savedValueNet", netValue+previousTotalNet)
        loadSavedValues()
    }

    @SuppressLint("SetTextI18n")
    private fun checkAndUpdateDailyValues() {
        val savedDay = sharedPreferences.getInt("savedDay", -1)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        if (savedDay != currentDay) {
            saveValue("savedValueToday",0)
            saveValue("savedDay", currentDay)
        }
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )//on update not on on create but OK
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)//for stat keepin'
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
            saveValue("previousTotalNet",textViewNet.text.toString().toInt())
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
            textViewToday.setTextColor(Color.parseColor("#FFB829")) // Set color to FFB829 if true
        } else {
            textViewToday.setTextColor(Color.WHITE) // Set default color to white if false
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

