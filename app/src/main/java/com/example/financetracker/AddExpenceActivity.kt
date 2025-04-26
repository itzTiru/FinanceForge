package com.example.financetracker

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
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
data class Expense(val amount: String, val category: String, val date: String, val description: String? = null)

class AddExpenseActivity : AppCompatActivity() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var isEditMode = false
    private var editPosition = -1
    private var editExpense: Expense? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // References to UI elements
        val exitButton: ImageButton = findViewById(R.id.exit_button)
        val titleText: TextView = findViewById(R.id.title_text)
        val amountInput: TextInputEditText = findViewById(R.id.amount_input)
        val categoryDropdown: AutoCompleteTextView = findViewById(R.id.category_dropdown)
        val dateInput: TextInputEditText = findViewById(R.id.date_input)
        val dateInputLayout: TextInputLayout = findViewById(R.id.date_input_layout)
        val descriptionInput: TextInputEditText = findViewById(R.id.description_input)
        val saveButton: Button = findViewById(R.id.save_button)
        val successMessage: TextView = findViewById(R.id.success_message)

        // Initialize SharedPreferences
        val sharedPreferences = getSharedPreferences("ExpensePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Check for edit mode
        intent.extras?.let { extras ->
            if (extras.containsKey("expense") && extras.containsKey("position")) {
                isEditMode = true
                editPosition = extras.getInt("position", -1)
                editExpense = Json.decodeFromString<Expense>(extras.getString("expense") ?: return@let)
                titleText.text = "Edit Expense"
                saveButton.text = "Update Expense"
                editExpense?.let { expense ->
                    amountInput.setText(expense.amount)
                    categoryDropdown.setText(expense.category, false)
                    dateInput.setText(expense.date)
                    descriptionInput.setText(expense.description ?: "")
                }
            }
        }

        // Set default date to today if not in edit mode
        if (!isEditMode) {
            val today = Calendar.getInstance()
            dateInput.setText(dateFormat.format(today.time))
        }

        // Set up DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                dateInput.setText(dateFormat.format(selectedDate.time))
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )

        // Show DatePickerDialog when date input or calendar icon is clicked
        dateInputLayout.setEndIconOnClickListener {
            datePickerDialog.show()
        }
        dateInput.setOnClickListener {
            datePickerDialog.show()
        }

        // Load and display all expenses (if any)
        val savedExpensesJson = sharedPreferences.getString("expenses", null)
        val expenses: MutableList<Expense> = if (savedExpensesJson != null) {
            try {
                Json.decodeFromString(savedExpensesJson)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
        if (expenses.isNotEmpty()) {
            val displayText = buildString {
                append("Recent Expenses:\n")
                expenses.take(3).forEachIndexed { index, expense ->
                    append("Expense ${index + 1}:\n")
                    append("Amount: $${expense.amount}\n")
                    append("Category: ${expense.category}\n")
                    append("Date: ${expense.date}\n")
                    if (!expense.description.isNullOrEmpty()) {
                        append("Description: ${expense.description}\n")
                    }
                    append("\n")
                }
            }
            successMessage.text = displayText
        }

        // Handle Exit button click
        exitButton.setOnClickListener {
            finish()
        }

        // Set up the category dropdown (expense-specific categories)
        val categories = listOf("Groceries", "Bills", "Transport", "Entertainment", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categoryDropdown.setAdapter(adapter)

        // Configure the AutoCompleteTextView to show the dropdown
        categoryDropdown.setOnClickListener {
            categoryDropdown.showDropDown()
        }
        categoryDropdown.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                categoryDropdown.showDropDown()
            }
        }

        // Handle Save/Update button click
        saveButton.setOnClickListener {
            val amount = amountInput.text.toString()
            val category = categoryDropdown.text.toString()
            val date = dateInput.text.toString()
            var description = descriptionInput.text.toString().ifEmpty { null }

            // Validation
            if (amount.isEmpty() || category.isEmpty() || date.isEmpty()) {
                successMessage.text = "Error: Please fill in all required fields"
                successMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }
            if (!categories.contains(category)) {
                successMessage.text = "Error: Please select a valid category"
                successMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }
            if (amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
                successMessage.text = "Error: Please enter a valid positive number for amount"
                successMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }
            try {
                dateFormat.isLenient = false
                dateFormat.parse(date)
            } catch (e: Exception) {
                successMessage.text = "Error: Please enter a valid date (DD/MM/YYYY)"
                successMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }

            val newExpense = Expense(amount, category, date, description)
            if (isEditMode) {
                // Update existing expense
                if (editPosition >= 0 && editPosition < expenses.size) {
                    expenses[editPosition] = newExpense
                }
                // Return updated expense to TransactionsFragment
                val resultIntent = Intent()
                resultIntent.putExtra("updatedExpense", Json.encodeToString(newExpense))
                resultIntent.putExtra("position", editPosition)
                setResult(RESULT_OK, resultIntent)
            } else {
                // Add new expense
                expenses.add(0, newExpense)
            }

            // Save the updated expenses list
            val expensesJson = Json.encodeToString(expenses)
            editor.putString("expenses", expensesJson)
            editor.apply()

            // Display success message
            val displayText = buildString {
                append(if (isEditMode) "Expense updated successfully\n\n" else "Expense added successfully\n\n")
                append("Recent Expenses:\n")
                expenses.take(3).forEachIndexed { index, expense ->
                    append("Expense ${index + 1}:\n")
                    append("Amount: $${expense.amount}\n")
                    append("Category: ${expense.category}\n")
                    append("Date: ${expense.date}\n")
                    if (!expense.description.isNullOrEmpty()) {
                        append("Description: ${expense.description}\n")
                    }
                    append("\n")
                }
            }
            successMessage.text = displayText
            successMessage.setTextColor(getColor(R.color.green))

            // Clear fields or finish in edit mode
            if (isEditMode) {
                finish()
            } else {
                amountInput.text?.clear()
                categoryDropdown.text?.clear()
                dateInput.setText(dateFormat.format(Calendar.getInstance().time))
                descriptionInput.text?.clear()
            }
        }
    }
}