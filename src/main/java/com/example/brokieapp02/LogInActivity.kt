package com.example.brokieapp02

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var confirmRegistrationButton: Button
    private lateinit var requirementLength: TextView
    private lateinit var requirementUppercase: TextView
    private lateinit var requirementNumber: TextView
    private lateinit var requirementSpecial: TextView
    private lateinit var signInButton: Button
    private lateinit var googleSignInButton: SignInButton
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        initializeViews()
        configureGoogleSignIn()
        setupButtonListeners()
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        confirmRegistrationButton = findViewById(R.id.confirmRegistrationButton)
        requirementLength = findViewById(R.id.requirementLength)
        requirementUppercase = findViewById(R.id.requirementUppercase)
        requirementNumber = findViewById(R.id.requirementNumber)
        requirementSpecial = findViewById(R.id.requirementSpecial)
        signInButton = findViewById(R.id.signInButton)
        googleSignInButton = findViewById(R.id.googleSignInButton)
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupButtonListeners() {
        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        passwordEditText.addTextChangedListener(passwordWatcher)
        loginButton.setOnClickListener { handleLogin() }
        signInButton.setOnClickListener { showLoginUI() }
        registerButton.setOnClickListener { showRegistrationUI() }
        confirmRegistrationButton.setOnClickListener { handleRegistration() }
    }

    // Handle Google Sign-In result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                navigateToMainActivity()
            } else {
                Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        if (isEmailValid(email) && isPasswordValid(password)) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleRegistration() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if (isEmailValid(email) && isPasswordValid(password)) {
            if (password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                        handleLogin()  // Optionally log the user in after registration
                    } else {
                        Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoginUI() {
        confirmPasswordEditText.visibility = EditText.GONE
        confirmRegistrationButton.visibility = Button.GONE
        registerButton.visibility = Button.VISIBLE
        loginButton.visibility = Button.VISIBLE
        signInButton.visibility = Button.GONE
    }

    private fun showRegistrationUI() {
        confirmPasswordEditText.visibility = EditText.VISIBLE
        confirmRegistrationButton.visibility = Button.VISIBLE
        registerButton.visibility = Button.GONE
        loginButton.visibility = Button.GONE
        signInButton.visibility = Button.VISIBLE
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private val passwordWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val password = s.toString()
            updatePasswordRequirements(password)
            loginButton.isEnabled = isPasswordValid(password)
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun updatePasswordRequirements(password: String) {
        requirementLength.setTextColor(if (password.length >= 8) Color.GREEN else Color.RED)
        requirementUppercase.setTextColor(if (password.any { it.isUpperCase() }) Color.GREEN else Color.RED)
        requirementNumber.setTextColor(if (password.any { it.isDigit() }) Color.GREEN else Color.RED)
        requirementSpecial.setTextColor(if (password.any { !it.isLetterOrDigit() }) Color.GREEN else Color.RED)
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
