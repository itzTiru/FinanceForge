package com.example.financetracker

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class BudgetFragment : Fragment() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget, container, false)

        // References to UI elements
        val totalIncomeText: TextView = view.findViewById(R.id.total_income)
        val totalExpensesText: TextView = view.findViewById(R.id.total_expenses)
        val netBalanceText: TextView = view.findViewById(R.id.net_balance)
        val incomeExpensePieChart: CustomPieChart = view.findViewById(R.id.income_expense_pie_chart)
        val categoryPieChart: CustomPieChart = view.findViewById(R.id.category_pie_chart)
        val monthlyBalanceBarChart: CustomBarChart = view.findViewById(R.id.monthly_balance_bar_chart)

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

        // Convert to Transaction list
        val transactions: List<Transaction> = mutableListOf<Transaction>().apply {
            addAll(incomes.map { Transaction.IncomeTransaction(it) })
            addAll(expenses.map { Transaction.ExpenseTransaction(it) })
        }

        // Calculate totals for summary
        val totalIncome = incomes.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        val totalExpenses = expenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        val netBalance = totalIncome - totalExpenses

        // Update summary UI
        totalIncomeText.text = "$ ${String.format("%.2f", totalIncome)}"
        totalExpensesText.text = "$ ${String.format("%.2f", totalExpenses)}"
        netBalanceText.text = "$ ${String.format("%.2f", netBalance)}"
        val balanceColor = when {
            netBalance > 0 -> R.color.green
            netBalance < 0 -> R.color.red
            else -> android.R.color.black
        }
        netBalanceText.setTextColor(requireContext().getColor(balanceColor))

        // Setup charts
        setupIncomeExpensePieChart(transactions, incomeExpensePieChart)
        setupCategoryPieChart(transactions, categoryPieChart)
        setupMonthlyBalanceBarChart(transactions, monthlyBalanceBarChart)

        return view
    }

    private fun setupIncomeExpensePieChart(transactions: List<Transaction>, pieChart: CustomPieChart) {
        var totalIncome = 0.0
        var totalExpenses = 0.0

        transactions.forEach { transaction ->
            when (transaction) {
                is Transaction.IncomeTransaction -> {
                    totalIncome += transaction.income.amount.toDoubleOrNull() ?: 0.0
                }
                is Transaction.ExpenseTransaction -> {
                    totalExpenses += transaction.expense.amount.toDoubleOrNull() ?: 0.0
                }
            }
        }

        val entries = listOf(
            ChartEntry("Income", totalIncome.toFloat(), Color.parseColor("#4CAF50")), // Green
            ChartEntry("Expenses", totalExpenses.toFloat(), Color.parseColor("#F44336")) // Red
        ).filter { it.value > 0 } // Only include non-zero values

        pieChart.setData(entries)
    }

    private fun setupCategoryPieChart(transactions: List<Transaction>, pieChart: CustomPieChart) {
        val categoryMap = mutableMapOf<String, Double>()
        val predefinedCategories = listOf("Groceries", "Bills", "Transport", "Entertainment", "Other")
        val colors = listOf(
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#FFC107")  // Amber
        )

        // Initialize category map with zeros for all predefined categories
        predefinedCategories.forEach { category ->
            categoryMap[category] = 0.0
        }

        // Aggregate expenses by category
        transactions.filterIsInstance<Transaction.ExpenseTransaction>().forEach { transaction ->
            val expense = transaction.expense
            val amount = expense.amount.toDoubleOrNull() ?: 0.0
            val category = if (expense.category in predefinedCategories) expense.category else "Other"
            categoryMap[category] = categoryMap.getOrDefault(category, 0.0) + amount
        }

        // Create chart entries, including categories with zero amounts for consistency
        val entries = predefinedCategories.mapIndexed { index, category ->
            ChartEntry(category, categoryMap[category]!!.toFloat(), colors[index % colors.size])
        }.filter { it.value > 0 } // Only show non-zero categories in the chart

        pieChart.setData(entries)
    }

    private fun setupMonthlyBalanceBarChart(transactions: List<Transaction>, barChart: CustomBarChart) {
        val monthlyBalances = mutableMapOf<String, Float>()

        transactions.forEach { transaction ->
            try {
                val dateStr = when (transaction) {
                    is Transaction.IncomeTransaction -> transaction.income.date
                    is Transaction.ExpenseTransaction -> transaction.expense.date
                }
                val date = dateFormat.parse(dateStr) ?: return@forEach // Skip invalid dates
                val monthKey = monthFormat.format(date)
                val amount = when (transaction) {
                    is Transaction.IncomeTransaction -> transaction.income.amount.toDoubleOrNull() ?: 0.0
                    is Transaction.ExpenseTransaction -> transaction.expense.amount.toDoubleOrNull() ?: 0.0
                }
                val currentBalance = monthlyBalances.getOrDefault(monthKey, 0f)
                monthlyBalances[monthKey] = currentBalance + when (transaction) {
                    is Transaction.IncomeTransaction -> amount.toFloat()
                    is Transaction.ExpenseTransaction -> -amount.toFloat()
                }
            } catch (e: Exception) {
                // Skip invalid dates
            }
        }

        val entries = monthlyBalances.entries
            .sortedBy { monthFormat.parse(it.key) } // Sort by date
            .map { entry ->
                BarChartEntry(entry.key, entry.value, Color.parseColor("#2196F3")) // Blue
            }

        barChart.setData(entries)
    }
}