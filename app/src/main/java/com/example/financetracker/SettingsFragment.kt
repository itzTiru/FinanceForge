package com.example.financetracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private val REQUEST_CODE_STORAGE_PERMISSION = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize SharedPreferences for settings
        val settingsPrefs = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

        // Currency Dropdown
        val currencyDropdown: AutoCompleteTextView = view.findViewById(R.id.currency_dropdown)
        val currencies = listOf("USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)", "INR (₹)")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies)
        currencyDropdown.setAdapter(adapter)

        // Load saved currency
        val savedCurrency = settingsPrefs.getString("currency", "USD ($)") ?: "USD ($)"
        currencyDropdown.setText(savedCurrency, false)

        // Configure dropdown behavior
        currencyDropdown.setOnClickListener {
            currencyDropdown.showDropDown()
        }
        currencyDropdown.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) currencyDropdown.showDropDown()
        }
        currencyDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedCurrency = currencies[position]
            settingsPrefs.edit().putString("currency", selectedCurrency).apply()
            Toast.makeText(context, "Currency changed to $selectedCurrency", Toast.LENGTH_SHORT).show()
        }

        // Backup Button
        val backupButton: Button = view.findViewById(R.id.backup_button)
        backupButton.setOnClickListener {
            val incomePrefs = requireContext().getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
            val expensePrefs = requireContext().getSharedPreferences("ExpensePrefs", Context.MODE_PRIVATE)
            backupData(incomePrefs, expensePrefs)
        }

        // Restore Button
        val restoreButton: Button = view.findViewById(R.id.restore_button)
        restoreButton.setOnClickListener {
            restoreData()
        }

        return view
    }

    private fun backupData(incomePrefs: SharedPreferences, expensePrefs: SharedPreferences) {
        try {
            // Check storage permission for Android API 28 and below
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
                return
            }

            // Retrieve incomes and expenses
            val incomesJson = incomePrefs.getString("incomes", null)
            val expensesJson = expensePrefs.getString("expenses", null)

            // Create JSON object for backup
            val json = mutableMapOf<String, String?>()
            json["incomes"] = incomesJson
            json["expenses"] = expensesJson

            // Generate file name with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "FinanceTrackerBackup_$timestamp.json"

            // Save to Documents directory
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }
            val backupFile = File(documentsDir, fileName)
            FileOutputStream(backupFile).use { outputStream ->
                outputStream.write(Json.encodeToString(json).toByteArray())
            }

            Toast.makeText(context, "Backup saved to Documents/$fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to backup data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restoreData() {
        try {
            // Check storage permission for Android API 28 and below
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
                return
            }

            // Open file picker for JSON files
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/json"
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain"))
            }
            restoreFileLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to initiate restore: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val restoreFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                        val jsonString = inputStream.bufferedReader().use { it.readText() }
                        val json = Json.decodeFromString<Map<String, String?>>(jsonString)

                        // Update SharedPreferences
                        val incomePrefs = requireContext().getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
                        val expensePrefs = requireContext().getSharedPreferences("ExpensePrefs", Context.MODE_PRIVATE)
                        incomePrefs.edit().putString("incomes", json["incomes"]).apply()
                        expensePrefs.edit().putString("expenses", json["expenses"]).apply()

                        Toast.makeText(context, "Data restored successfully.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to restore data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    val incomePrefs = requireContext().getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
                    val expensePrefs = requireContext().getSharedPreferences("ExpensePrefs", Context.MODE_PRIVATE)
                    backupData(incomePrefs, expensePrefs)
                } else if (permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    restoreData()
                }
            } else {
                Toast.makeText(context, "Storage permission denied. Cannot backup/restore data.", Toast.LENGTH_LONG).show()
            }
        }
    }
}