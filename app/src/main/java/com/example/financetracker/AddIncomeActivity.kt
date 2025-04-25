package com.example.financetracker

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class Income(val amount: String, val category: String, val date: String, val description: String? = null)

class AddIncomeActivity : AppCompatActivity() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        // References to UI elements
        val exitButton: ImageButton = findViewById(R.id.exit_button)
        val amountInput: TextInputEditText = findViewById(R.id.amount_input)
        val categoryDropdown: AutoCompleteTextView = findViewById(R.id.category_dropdown)
        val dateInput: TextInputEditText = findViewById(R.id.date_input)
        val dateInputLayout: TextInputLayout = findViewById(R.id.date_input_layout)
        val descriptionInput: TextInputEditText = findViewById(R.id.description_input)
        val saveButton: Button = findViewById(R.id.save_button)
        val successMessage: TextView = findViewById(R.id.success_message)

        // Initialize SharedPreferences
        val sharedPreferences = getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Set default date to today
        val today = Calendar.getInstance()
        dateInput.setText(dateFormat.format(today.time))

        // Set up DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                dateInput.setText(dateFormat.format(selectedDate.time))
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )

        // Show DatePickerDialog when date input or calendar icon is clicked
        dateInputLayout.setEndIconOnClickListener {
            datePickerDialog.show()
        }
        dateInput.setOnClickListener {
            datePickerDialog.show()
        }

        // Load and display all incomes (if any)
        val savedIncomesJson = sharedPreferences.getString("incomes", null)
        val incomes: MutableList<Income> = if (savedIncomesJson != null) {
            try {
                Json.decodeFromString(savedIncomesJson)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
        if (incomes.isNotEmpty()) {
            val displayText = buildString {
                append("Recent Incomes:\n")
                incomes.take(3).forEachIndexed { index, income ->
                    append("Income ${index + 1}:\n")
                    append("Amount: $${income.amount}\n")
                    append("Category: ${income.category}\n")
                    append("Date: ${income.date}\n")
                    if (!income.description.isNullOrEmpty()) {
                        append("Description: ${income.description}\n")
                    }
                    append("\n")
                }
            }
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
            val description = descriptionInput.text.toString().ifEmpty { null }

            // Basic validation
            if (amount.isEmpty() || category.isEmpty() || date.isEmpty()) {
                successMessage.text = "Error: Please fill in all required fields"
                successMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }

            // Additional validation: Ensure the category is one of the predefined options
            if (!categories.contains(category)) {
                successMessage.text = "Error: Please select a valid category"
                successMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }

            // Additional validation: Ensure amount is a valid positive number
            if (amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
                successMessage.text = "Error: Please enter a valid positive number for amount"
                successMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }

            // Additional validation: Ensure date is valid
            try {
                dateFormat.isLenient = false
                dateFormat.parse(date)
            } catch (e: Exception) {
                successMessage.text = "Error: Please enter a valid date (DD/MM/YYYY)"
                successMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }

            // Add new income to the list
            val newIncome = Income(amount, category, date, description)
            incomes.add(0, newIncome) // Add to the beginning for newest first

            // Save the updated incomes list to SharedPreferences
            val incomesJson = Json.encodeToString(incomes)
            editor.putString("incomes", incomesJson)
            editor.apply()

            // Display success message and updated income list
            val displayText = buildString {
                append("Income added successfully\n\n")
                append("Recent Incomes:\n")
                incomes.take(3).forEachIndexed { index, income ->
                    append("Income ${index + 1}:\n")
                    append("Amount: $${income.amount}\n")
                    append("Category: ${income.category}\n")
                    append("Date: ${income.date}\n")
                    if (!income.description.isNullOrEmpty()) {
                        append("Description: ${income.description}\n")
                    }
                    append("\n")
                }
            }
            successMessage.text = displayText
            successMessage.setTextColor(getColor(R.color.green)) // Match the green color used in the layout (#388E3C)

            // Clear the input fields
            amountInput.text?.clear()
            categoryDropdown.text?.clear()
            dateInput.setText(dateFormat.format(today.time)) // Reset to today's date
            descriptionInput.text?.clear()
        }
    }
}