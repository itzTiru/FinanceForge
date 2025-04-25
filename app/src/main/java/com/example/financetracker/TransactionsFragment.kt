package com.example.financetracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class TransactionsFragment : Fragment() {

    private lateinit var adapter: TransactionAdapter
    private lateinit var allTransactions: List<Transaction>
    private var isShowingIncomes = false
    private var isShowingExpenses = false
    private var selectedIncomeCategory: String? = null
    private var selectedExpenseCategory: String? = null
    private var selectedIncomeButton: Button? = null
    private var selectedExpenseButton: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.transactions_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TransactionAdapter()
        recyclerView.adapter = adapter

        // Filter Buttons
        val incomeFilterButton: Button = view.findViewById(R.id.income_filter_button)
        val expensesFilterButton: Button = view.findViewById(R.id.expenses_filter_button)

        // Income Category Buttons
        val incomeCategoryButtonsLayout: LinearLayout = view.findViewById(R.id.income_category_buttons)
        val incomeAllButton: Button = view.findViewById(R.id.income_all_button)
        val incomeSalaryButton: Button = view.findViewById(R.id.income_salary_button)
        val incomeFreelanceButton: Button = view.findViewById(R.id.income_freelance_button)
        val incomeGiftButton: Button = view.findViewById(R.id.income_gift_button)
        val incomeInvestmentButton: Button = view.findViewById(R.id.income_investment_button)
        val incomeOtherButton: Button = view.findViewById(R.id.income_other_button)

        // Expense Category Buttons
        val expenseCategoryButtonsLayout: LinearLayout = view.findViewById(R.id.expense_category_buttons)
        val expenseAllButton: Button = view.findViewById(R.id.expense_all_button)
        val expenseGroceriesButton: Button = view.findViewById(R.id.expense_groceries_button)
        val expenseBillsButton: Button = view.findViewById(R.id.expense_bills_button)
        val expenseTransportButton: Button = view.findViewById(R.id.expense_transport_button)
        val expenseEntertainmentButton: Button = view.findViewById(R.id.expense_entertainment_button)
        val expenseOtherButton: Button = view.findViewById(R.id.expense_other_button)

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

        // Combine incomes and expenses into a single transaction list
        allTransactions = mutableListOf<Transaction>().apply {
            addAll(incomes.map { Transaction.IncomeTransaction(it) })
            addAll(expenses.map { Transaction.ExpenseTransaction(it) })
        }

        // Sort by date (assuming date is in "DD/MM/YYYY" format)
        allTransactions = allTransactions.sortedByDescending {
            when (it) {
                is Transaction.IncomeTransaction -> parseDate(it.income.date)
                is Transaction.ExpenseTransaction -> parseDate(it.expense.date)
            }
        }

        // Initially show all transactions
        adapter.updateTransactions(allTransactions)

        // Income Filter Button Click Listener
        incomeFilterButton.setOnClickListener {
            isShowingIncomes = !isShowingIncomes
            isShowingExpenses = false // Reset expenses filter
            selectedExpenseCategory = null
            selectedExpenseButton = null
            updateFilter(incomeFilterButton, expensesFilterButton, incomeCategoryButtonsLayout, expenseCategoryButtonsLayout)
            resetCategoryButtons(expenseAllButton, expenseGroceriesButton, expenseBillsButton, expenseTransportButton, expenseEntertainmentButton, expenseOtherButton)
            // Set "All" as default when activating Income filter
            if (isShowingIncomes) {
                selectedIncomeCategory = null
                selectedIncomeButton?.backgroundTintList = requireContext().getColorStateList(R.color.category_default)
                selectedIncomeButton = incomeAllButton
                incomeAllButton.backgroundTintList = requireContext().getColorStateList(R.color.green)
            }
            applyFilter()
        }

        // Expenses Filter Button Click Listener
        expensesFilterButton.setOnClickListener {
            isShowingExpenses = !isShowingExpenses
            isShowingIncomes = false // Reset income filter
            selectedIncomeCategory = null
            selectedIncomeButton = null
            updateFilter(expensesFilterButton, incomeFilterButton, incomeCategoryButtonsLayout, expenseCategoryButtonsLayout)
            resetCategoryButtons(incomeAllButton, incomeSalaryButton, incomeFreelanceButton, incomeGiftButton, incomeInvestmentButton, incomeOtherButton)
            // Set "All" as default when activating Expenses filter
            if (isShowingExpenses) {
                selectedExpenseCategory = null
                selectedExpenseButton?.backgroundTintList = requireContext().getColorStateList(R.color.category_default)
                selectedExpenseButton = expenseAllButton
                expenseAllButton.backgroundTintList = requireContext().getColorStateList(R.color.red)
            }
            applyFilter()
        }

        // Income Category Button Click Listeners
        val incomeButtons = listOf(
            incomeAllButton to null,
            incomeSalaryButton to "Salary",
            incomeFreelanceButton to "Freelance",
            incomeGiftButton to "Gift",
            incomeInvestmentButton to "Investment",
            incomeOtherButton to "Other"
        )
        incomeButtons.forEach { (button, category) ->
            button.setOnClickListener {
                selectedIncomeCategory = category
                selectedIncomeButton?.backgroundTintList = requireContext().getColorStateList(R.color.category_default)
                selectedIncomeButton = button
                button.backgroundTintList = requireContext().getColorStateList(R.color.green)
                applyFilter()
            }
        }

        // Expense Category Button Click Listeners
        val expenseButtons = listOf(
            expenseAllButton to null,
            expenseGroceriesButton to "Groceries",
            expenseBillsButton to "Bills",
            expenseTransportButton to "Transport",
            expenseEntertainmentButton to "Entertainment",
            expenseOtherButton to "Other"
        )
        expenseButtons.forEach { (button, category) ->
            button.setOnClickListener {
                selectedExpenseCategory = category
                selectedExpenseButton?.backgroundTintList = requireContext().getColorStateList(R.color.category_default)
                selectedExpenseButton = button
                button.backgroundTintList = requireContext().getColorStateList(R.color.red)
                applyFilter()
            }
        }

        return view
    }

    // Helper function to parse date in "DD/MM/YYYY" format for sorting
    private fun parseDate(date: String): Long {
        return try {
            val parts = date.split("/")
            if (parts.size != 3) return 0L
            val day = parts[0].toIntOrNull() ?: 0
            val month = parts[1].toIntOrNull() ?: 0
            val year = parts[2].toIntOrNull() ?: 0
            // Convert to a comparable long value (YYYYMMDD)
            (year * 10000L + month * 100L + day).toLong()
        } catch (e: Exception) {
            0L
        }
    }

    // Update button appearance and category buttons visibility based on filter state
    private fun updateFilter(
        selectedButton: Button,
        otherButton: Button,
        incomeCategoryButtonsLayout: LinearLayout,
        expenseCategoryButtonsLayout: LinearLayout
    ) {
        if (selectedButton.id == R.id.income_filter_button) {
            if (isShowingIncomes) {
                selectedButton.backgroundTintList = requireContext().getColorStateList(R.color.green)
                selectedButton.setTextColor(requireContext().getColor(android.R.color.white))
                incomeCategoryButtonsLayout.visibility = View.VISIBLE
            } else {
                selectedButton.backgroundTintList = requireContext().getColorStateList(android.R.color.transparent)
                selectedButton.setTextColor(requireContext().getColor(R.color.green))
                incomeCategoryButtonsLayout.visibility = View.GONE
            }
            otherButton.backgroundTintList = requireContext().getColorStateList(android.R.color.transparent)
            otherButton.setTextColor(requireContext().getColor(R.color.red))
            expenseCategoryButtonsLayout.visibility = View.GONE
        } else {
            if (isShowingExpenses) {
                selectedButton.backgroundTintList = requireContext().getColorStateList(R.color.red)
                selectedButton.setTextColor(requireContext().getColor(android.R.color.white))
                expenseCategoryButtonsLayout.visibility = View.VISIBLE
            } else {
                selectedButton.backgroundTintList = requireContext().getColorStateList(android.R.color.transparent)
                selectedButton.setTextColor(requireContext().getColor(R.color.red))
                expenseCategoryButtonsLayout.visibility = View.GONE
            }
            otherButton.backgroundTintList = requireContext().getColorStateList(android.R.color.transparent)
            otherButton.setTextColor(requireContext().getColor(R.color.green))
            incomeCategoryButtonsLayout.visibility = View.GONE
        }
    }

    // Reset category buttons to default state
    private fun resetCategoryButtons(vararg buttons: Button) {
        buttons.forEach { button ->
            button.backgroundTintList = requireContext().getColorStateList(R.color.category_default)
        }
    }

    // Apply the filter to the transactions list
    private fun applyFilter() {
        var filteredTransactions = when {
            isShowingIncomes -> allTransactions.filterIsInstance<Transaction.IncomeTransaction>()
            isShowingExpenses -> allTransactions.filterIsInstance<Transaction.ExpenseTransaction>()
            else -> allTransactions
        }

        // Apply category filter if applicable
        if (isShowingIncomes && selectedIncomeCategory != null) {
            filteredTransactions = filteredTransactions.filter {
                (it as Transaction.IncomeTransaction).income.category == selectedIncomeCategory
            }
        } else if (isShowingExpenses && selectedExpenseCategory != null) {
            filteredTransactions = filteredTransactions.filter {
                (it as Transaction.ExpenseTransaction).expense.category == selectedExpenseCategory
            }
        }

        adapter.updateTransactions(filteredTransactions)
    }
}