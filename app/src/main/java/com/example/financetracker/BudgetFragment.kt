package com.example.financetracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class BudgetFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget, container, false)

        // References to UI elements
        val totalIncomeText: TextView = view.findViewById(R.id.total_income)
        val totalExpensesText: TextView = view.findViewById(R.id.total_expenses)
        val netBalanceText: TextView = view.findViewById(R.id.net_balance)

        // Load incomes from IncomePrefs
        val incomePrefs = requireContext().getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
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
        val expensePrefs = requireContext().getSharedPreferences("ExpensePrefs", Context.MODE_PRIVATE)
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

        // Calculate totals
        val totalIncome = incomes.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        val totalExpenses = expenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        val netBalance = totalIncome - totalExpenses

        // Update UI
        totalIncomeText.text = "$ ${String.format("%.2f", totalIncome)}"
        totalExpensesText.text = "$ ${String.format("%.2f", totalExpenses)}"
        netBalanceText.text = "$ ${String.format("%.2f", netBalance)}"
        // Set balance color based on value
        val balanceColor = when {
            netBalance > 0 -> R.color.green // #388E3C
            netBalance < 0 -> R.color.red   // #D32F2F
            else -> android.R.color.black   // Neutral
        }
        netBalanceText.setTextColor(requireContext().getColor(balanceColor))

        return view
    }
}