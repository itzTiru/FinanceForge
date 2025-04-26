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
data class Income(val amount: String, val category: String, val date: String, val description: String? = null)

class AddIncomeActivity : AppCompatActivity() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var isEditMode = false
    private var editPosition = -1
    private var editIncome: Income? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

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
        val sharedPreferences = getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Check for edit mode
        intent.extras?.let { extras ->
            if (extras.containsKey("income") && extras.containsKey("position")) {
                isEditMode = true
                editPosition = extras.getInt("position", -1)
                editIncome = Json.decodeFromString<Income>(extras.getString("income") ?: return@let)
                titleText.text = "Edit Income"
                saveButton.text = "Update Income"
                editIncome?.let { income ->
                    amountInput.setText(income.amount)
                    categoryDropdown.setText(income.category, false)
                    dateInput.setText(income.date)
                    descriptionInput.setText(income.description ?: "")
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
            finish()
        }

        // Set up the category dropdown (income-specific categories)
        val categories = listOf("Salary", "Freelance", "Gift", "Investment", "Other")
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

        // Handle Saveundance button click
        saveButton.setOnClickListener {
            val amount = amountInput.text.toString()
            val category = categoryDropdown.text.toString()
            val date = dateInput.text.toString()
            val description = descriptionInput.text.toString().ifEmpty { null }

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

            val newIncome = Income(amount, category, date, description)
            if (isEditMode) {
                // Update existing income
                if (editPosition >= 0 && editPosition < incomes.size) {
                    incomes[editPosition] = newIncome
                }
                // Return updated income to TransactionsFragment
                val resultIntent = Intent()
                resultIntent.putExtra("updatedIncome", Json.encodeToString(newIncome))
                resultIntent.putExtra("position", editPosition)
                setResult(RESULT_OK, resultIntent)
            } else {
                // Add new income
                incomes.add(0, newIncome)
            }

            // Save the updated incomes list
            val incomesJson = Json.encodeToString(incomes)
            editor.putString("incomes", incomesJson)
            editor.apply()

            // Display success message
            val displayText = buildString {
                append(if (isEditMode) "Income updated successfully\n\n" else "Income added successfully\n\n")
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