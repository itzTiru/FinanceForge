package com.example.financetracker

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class AddIncomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        // References to UI elements
        val exitButton: ImageButton = findViewById(R.id.exit_button)
        val amountInput: TextInputEditText = findViewById(R.id.amount_input)
        val categoryDropdown: AutoCompleteTextView = findViewById(R.id.category_dropdown)
        val dateInput: TextInputEditText = findViewById(R.id.date_input)
        val saveButton: Button = findViewById(R.id.save_button)
        val successMessage: TextView = findViewById(R.id.success_message)

        // Initialize SharedPreferences
        val sharedPreferences = getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Load previously saved income (if any) and display it
        val savedAmount = sharedPreferences.getString("amount", null)
        val savedCategory = sharedPreferences.getString("category", null)
        val savedDate = sharedPreferences.getString("date", null)
        if (savedAmount != null && savedCategory != null && savedDate != null) {
            val displayText = "Last Added Income:\nAmount: $$savedAmount\nCategory: $savedCategory\nDate: $savedDate"
            successMessage.text = displayText
        }

        // Handle Exit button click
        exitButton.setOnClickListener {
            finish() // Close the activity and return to Dashboard
        }

        // Set up the category dropdown (income-specific categories)
        val categories = listOf("Salary", "Freelance", "Gift", "Investment", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categoryDropdown.setAdapter(adapter)

        // Configure the AutoCompleteTextView to show the dropdown on click
        categoryDropdown.setOnClickListener {
            categoryDropdown.showDropDown()
        }

        // Optional: Show dropdown when the user focuses on the field
        categoryDropdown.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                categoryDropdown.showDropDown()
            }
        }

        // Handle Save button click
        saveButton.setOnClickListener {
            val amount = amountInput.text.toString()
            val category = categoryDropdown.text.toString()
            val date = dateInput.text.toString()

            // Basic validation
            if (amount.isEmpty() || category.isEmpty() || date.isEmpty()) {
                successMessage.text = "Error: Please fill in all fields"
                successMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }

            // Additional validation: Ensure the category is one of the predefined options
            if (!categories.contains(category)) {
                successMessage.text = "Error: Please select a valid category"
                successMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }

            // Save the income details to SharedPreferences
            editor.putString("amount", amount)
            editor.putString("category", category)
            editor.putString("date", date)
            editor.apply()

            // Display success message and income details
            val displayText = "Income added successfully\nAmount: $$amount\nCategory: $category\nDate: $date"
            successMessage.text = displayText
            successMessage.setTextColor(getColor(R.color.green))  // Match the green color used in the layout (#388E3C)

            // Clear the input fields
            amountInput.text?.clear()
            categoryDropdown.text?.clear()
            dateInput.text?.clear()
        }
    }
}