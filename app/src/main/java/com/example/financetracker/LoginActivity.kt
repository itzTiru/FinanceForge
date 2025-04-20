package com.example.financetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // References to UI elements
        val usernameInput: TextInputEditText = findViewById(R.id.username_input)
        val passwordInput: TextInputEditText = findViewById(R.id.password_input)
        val loginButton: Button = findViewById(R.id.login_button)
        val createAccountButton: Button = findViewById(R.id.create_account_button)

        // Initialize SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Handle Create Account button click
        createAccountButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Basic validation
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the username already exists
            val savedUsername = sharedPreferences.getString("username", null)
            if (savedUsername != null && savedUsername == username) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the new credentials
            editor.putString("username", username)
            editor.putString("password", password)
            editor.apply()
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()

            // Clear the input fields
            usernameInput.text?.clear()
            passwordInput.text?.clear()
        }

        // Handle Login button click
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Basic validation
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check credentials against SharedPreferences
            val savedUsername = sharedPreferences.getString("username", null)
            val savedPassword = sharedPreferences.getString("password", null)

            if (savedUsername == null || savedPassword == null) {
                Toast.makeText(this, "No account found. Please create an account first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username == savedUsername && password == savedPassword) {
                // Successful login - navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Close LoginActivity so the user can't go back to it
            } else {
                // Failed login - show error
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}