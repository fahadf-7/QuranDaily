package com.example.qurandaily

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnTogglePassword: ImageView
    private lateinit var btnClose: ImageView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        initViews()
        setupListeners()
    }

    // 🔴 IMPORTANT:
    // Removed onStart() auto-redirect so login screen ALWAYS shows first
    // even if Firebase has a cached user.

    private fun initViews() {
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnSignIn = findViewById(R.id.btn_sign_in)
        tvSignUp = findViewById(R.id.tv_sign_up)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        btnTogglePassword = findViewById(R.id.btn_toggle_password)
        btnClose = findViewById(R.id.btn_close)
    }

    private fun setupListeners() {

        btnSignIn.setOnClickListener {
            attemptLogin()
        }

        tvSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        btnClose.setOnClickListener {
            // Close app / activity
            finish()
        }
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (!isValidEmail(email)) {
            etEmail.error = "Enter a valid email"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Enter your password"
            etPassword.requestFocus()
            return
        }

        btnSignIn.isEnabled = false
        btnSignIn.text = "Signing In..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                btnSignIn.isEnabled = true
                btnSignIn.text = "Sign In"

                if (task.isSuccessful) {
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Login failed",
                        Toast.LENGTH_LONG
                    ).show()
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
