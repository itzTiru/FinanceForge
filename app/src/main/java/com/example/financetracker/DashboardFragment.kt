package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class DashboardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Reference to UI elements
        val balanceValueText: TextView = view.findViewById(R.id.balance_value)
        val addExpenseButton: Button = view.findViewById(R.id.add_expense_button)
        val addIncomeButton: Button = view.findViewById(R.id.add_income_button)

        // Load incomes from IncomePrefs
        val incomePrefs = requireContext().getSharedPreferences("IncomePrefs", android.content.Context.MODE_PRIVATE)
        val incomesJson = incomePrefs.getString("incomes", null)
        val incomes: List<Income> = if (incomesJson != null) {
            try {
                Json.decodeFromString(incomesJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        // Load expenses from ExpensePrefs
        val expensePrefs = requireContext().getSharedPreferences("ExpensePrefs", android.content.Context.MODE_PRIVATE)
        val expensesJson = expensePrefs.getString("expenses", null)
        val expenses: List<Expense> = if (expensesJson != null) {
            try {
                Json.decodeFromString(expensesJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        // Calculate net balance
        val totalIncome = incomes.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        val totalExpenses = expenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        val netBalance = totalIncome - totalExpenses

        // Update balance_value TextView
        balanceValueText.text = "$ ${String.format("%.2f", netBalance)}"

        // Set balance color based on value
        val balanceColor = when {
            netBalance > 0 -> R.color.white // #388E3C
            netBalance < 0 -> R.color.red   // #D32F2F
            else -> android.R.color.white   // Neutral (matches card text color)
        }
        balanceValueText.setTextColor(requireContext().getColor(balanceColor))

        // Click listener for Add Expense button
        addExpenseButton.setOnClickListener {
            val intent = Intent(requireContext(), AddExpenseActivity::class.java)
            startActivity(intent)
        }

        // Click listener for Add Income button
        addIncomeButton.setOnClickListener {
            val intent = Intent(requireContext(), AddIncomeActivity::class.java)
            startActivity(intent)
        }
    }
}