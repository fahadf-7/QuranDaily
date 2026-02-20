package com.example.qurandaily

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvSignIn: TextView
    private lateinit var btnTogglePassword: ImageView
    private lateinit var btnClose: ImageView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)   // activity_signup.xml

        auth = FirebaseAuth.getInstance()

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.et_fullname)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnSignUp = findViewById(R.id.btn_sign_up)
        tvSignIn = findViewById(R.id.tv_sign_in)
        btnTogglePassword = findViewById(R.id.btn_toggle_password)
        btnClose = findViewById(R.id.btn_close)
    }

    private fun setupListeners() {

        btnSignUp.setOnClickListener {
            attemptSignup()
        }

        tvSignIn.setOnClickListener {
            // Go back to Login screen
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        btnClose.setOnClickListener {
            // Just close signup, go back to previous screen (likely Login)
            finish()
        }
    }

    private fun attemptSignup() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (fullName.isEmpty()) {
            etFullName.error = "Enter your full name"
            etFullName.requestFocus()
            return
        }

        if (!isValidEmail(email)) {
            etEmail.error = "Enter a valid email"
            etEmail.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }

        btnSignUp.isEnabled = false
        btnSignUp.text = "Creating Account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    btnSignUp.isEnabled = true
                    btnSignUp.text = "Sign Up"
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Signup failed",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }

                // Set display name (full name)
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()

                user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener {
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        goToMain()
                    }
            }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        etPassword.setSelection(etPassword.text.length)
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
