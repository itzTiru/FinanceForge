package com.example.financetracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class AddExpenseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // References to UI elements
        val exitButton: ImageButton = findViewById(R.id.exit_button)
        val amountInput: TextInputEditText = findViewById(R.id.amount_input)
        val categoryDropdown: AutoCompleteTextView = findViewById(R.id.category_dropdown)
        val dateInput: TextInputEditText = findViewById(R.id.date_input)
        val saveButton: Button = findViewById(R.id.save_button)

        // Handle Exit button click
        exitButton.setOnClickListener {
            finish() // Close the activity and return to Dashboard
        }

        // Set up the category dropdown (expense-specific categories)
        val categories = listOf("Food", "Transport", "Entertainment", "Bills", "Shopping", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categoryDropdown.setAdapter(adapter)

        // Handle Save button click
        saveButton.setOnClickListener {
            val amount = amountInput.text.toString()
            val category = categoryDropdown.text.toString()
            val date = dateInput.text.toString()

            // Basic validation
            if (amount.isEmpty() || category.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // For now, just show a Toast with the input
            val message = "Expense Saved\nAmount: $amount\nCategory: $category\nDate: $date"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            // Finish the activity (go back to Dashboard)
            finish()
        }
    }
}