package com.example.qurandaily

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etEmail: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var tvBackToSignIn: TextView
    private lateinit var btnClose: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // make sure this file name matches your layout file name
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.et_email)
        btnResetPassword = findViewById(R.id.btn_reset_password)
        tvBackToSignIn = findViewById(R.id.tv_back_to_signin)
        btnClose = findViewById(R.id.btn_close)
    }

    private fun setupListeners() {

        btnResetPassword.setOnClickListener {
            sendPasswordReset()
        }

        tvBackToSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun sendPasswordReset() {
        val email = etEmail.text.toString().trim()

        if (!isValidEmail(email)) {
            etEmail.error = "Enter a valid email"
            etEmail.requestFocus()
            return
        }

        btnResetPassword.isEnabled = false
        btnResetPassword.text = "Sending..."

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                btnResetPassword.isEnabled = true
                btnResetPassword.text = "Reset Password"

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Reset link sent to your email",
                        Toast.LENGTH_LONG
                    ).show()
                    // Optionally go back to login
                    // finish()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Failed to send reset email",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
