package com.example.financetracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TransactionsFragment : Fragment() {

    private lateinit var adapter: TransactionAdapter
    private lateinit var allTransactions: MutableList<Transaction>
    private var isShowingIncomes = false
    private var isShowingExpenses = false
    private var selectedIncomeCategory: String? = null
    private var selectedExpenseCategory: String? = null
    private var selectedIncomeButton: Button? = null
    private var selectedExpenseButton: Button? = null

    // Activity result launchers
    private val editIncomeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val position = data.getIntExtra("position", -1)
                val updatedIncome = Json.decodeFromString<Income>(data.getStringExtra("updatedIncome") ?: return@let)
                if (position >= 0 && position < allTransactions.size) {
                    allTransactions[position] = Transaction.IncomeTransaction(updatedIncome)
                    updateSharedPreferences()
                    applyFilter()
                }
            }
        }
    }

    private val editExpenseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val position = data.getIntExtra("position", -1)
                val updatedExpense = Json.decodeFromString<Expense>(data.getStringExtra("updatedExpense") ?: return@let)
                if (position >= 0 && position < allTransactions.size) {
                    allTransactions[position] = Transaction.ExpenseTransaction(updatedExpense)
                    updateSharedPreferences()
                    applyFilter()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.transactions_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TransactionAdapter(
            onEditClick = { transaction, position ->
                when (transaction) {
                    is Transaction.IncomeTransaction -> {
                        val intent = Intent(context, AddIncomeActivity::class.java).apply {
                            putExtra("income", Json.encodeToString(transaction.income))
                            putExtra("position", position)
                        }
                        editIncomeLauncher.launch(intent)
                    }
                    is Transaction.ExpenseTransaction -> {
                        val intent = Intent(context, AddExpenseActivity::class.java).apply {
                            putExtra("expense", Json.encodeToString(transaction.expense))
                            putExtra("position", position)
                        }
                        editExpenseLauncher.launch(intent)
                    }
                }
            },
            onDeleteClick = { transaction, position ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete") { _, _ ->
                        allTransactions.removeAt(position)
                        updateSharedPreferences()
                        applyFilter()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
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

        // Load incomes
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

        // Load expenses
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

        // Combine transactions
        allTransactions = mutableListOf<Transaction>().apply {
            addAll(incomes.map { Transaction.IncomeTransaction(it) })
            addAll(expenses.map { Transaction.ExpenseTransaction(it) })
        }

        // Sort by date
        allTransactions = allTransactions.sortedByDescending {
            when (it) {
                is Transaction.IncomeTransaction -> parseDate(it.income.date)
                is Transaction.ExpenseTransaction -> parseDate(it.expense.date)
            }
        }.toMutableList()

        // Show all transactions initially
        adapter.updateTransactions(allTransactions)

        // Income Filter Button
        incomeFilterButton.setOnClickListener {
            isShowingIncomes = !isShowingIncomes
            isShowingExpenses = false
            selectedExpenseCategory = null
            selectedExpenseButton?.backgroundTintList = requireContext().getColorStateList(R.color.category_default)
            selectedExpenseButton = null
            updateFilter(incomeFilterButton, expensesFilterButton, incomeCategoryButtonsLayout, expenseCategoryButtonsLayout)
            resetCategoryButtons(expenseAllButton, expenseGroceriesButton, expenseBillsButton, expenseTransportButton, expenseEntertainmentButton, expenseOtherButton)
            if (isShowingIncomes) {
                selectedIncomeCategory = null
                selectedIncomeButton?.backgroundTintList = requireContext().getColorStateList(R.color.category_default)
                selectedIncomeButton = incomeAllButton
                incomeAllButton.backgroundTintList = requireContext().getColorStateList(R.color.green)
            } else {
                selectedIncomeButton = null
            }
            applyFilter()
        }

        // Expenses Filter Button
        expensesFilterButton.setOnClickListener {
            isShowingExpenses = !isShowingExpenses
            isShowingIncomes = false
            selectedIncomeCategory = null
            selectedIncomeButton?.backgroundTintList = requireContext().getColorStateList(R.color.category_default)
            selectedIncomeButton = null
            updateFilter(expensesFilterButton, incomeFilterButton, expenseCategoryButtonsLayout, incomeCategoryButtonsLayout)
            resetCategoryButtons(incomeAllButton, incomeSalaryButton, incomeFreelanceButton, incomeGiftButton, incomeInvestmentButton, incomeOtherButton)
            if (isShowingExpenses) {
                selectedExpenseCategory = null
                selectedExpenseButton?.backgroundTintList = requireContext().getColorStateList(R.color.category_default)
                selectedExpenseButton = expenseAllButton
                expenseAllButton.backgroundTintList = requireContext().getColorStateList(R.color.red)
            } else {
                selectedExpenseButton = null
            }
            applyFilter()
        }

        // Income Category Buttons
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

        // Expense Category Buttons
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

    private fun parseDate(date: String): Long {
        return try {
            val parts = date.split("/")
            if (parts.size != 3) return 0L
            val day = parts[0].toIntOrNull() ?: 0
            val month = parts[1].toIntOrNull() ?: 0
            val year = parts[2].toIntOrNull() ?: 0
            (year * 10000L + month * 100L + day).toLong()
        } catch (e: Exception) {
            0L
        }
    }

    private fun updateFilter(
        selectedButton: Button,
        otherButton: Button,
        selectedCategoryLayout: LinearLayout,
        otherCategoryLayout: LinearLayout
    ) {
        if (selectedButton.id == R.id.income_filter_button) {
            if (isShowingIncomes) {
                selectedButton.backgroundTintList = requireContext().getColorStateList(R.color.green)
                selectedButton.setTextColor(requireContext().getColor(android.R.color.white))
                selectedCategoryLayout.visibility = View.VISIBLE
            } else {
                selectedButton.backgroundTintList = requireContext().getColorStateList(android.R.color.transparent)
                selectedButton.setTextColor(requireContext().getColor(R.color.green))
                selectedCategoryLayout.visibility = View.GONE
            }
            otherButton.backgroundTintList = requireContext().getColorStateList(android.R.color.transparent)
            otherButton.setTextColor(requireContext().getColor(R.color.red))
            otherCategoryLayout.visibility = View.GONE
        } else {
            if (isShowingExpenses) {
                selectedButton.backgroundTintList = requireContext().getColorStateList(R.color.red)
                selectedButton.setTextColor(requireContext().getColor(android.R.color.white))
                selectedCategoryLayout.visibility = View.VISIBLE
            } else {
                selectedButton.backgroundTintList = requireContext().getColorStateList(android.R.color.transparent)
                selectedButton.setTextColor(requireContext().getColor(R.color.red))
                selectedCategoryLayout.visibility = View.GONE
            }
            otherButton.backgroundTintList = requireContext().getColorStateList(android.R.color.transparent)
            otherButton.setTextColor(requireContext().getColor(R.color.green))
            otherCategoryLayout.visibility = View.GONE
        }
    }

    private fun resetCategoryButtons(vararg buttons: Button) {
        buttons.forEach { button ->
            button.backgroundTintList = requireContext().getColorStateList(R.color.category_default)
        }
    }

    private fun applyFilter() {
        var filteredTransactions = when {
            isShowingIncomes -> allTransactions.filterIsInstance<Transaction.IncomeTransaction>()
            isShowingExpenses -> allTransactions.filterIsInstance<Transaction.ExpenseTransaction>()
            else -> allTransactions
        }

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

    private fun updateSharedPreferences() {
        val incomePrefs = requireContext().getSharedPreferences("IncomePrefs", Context.MODE_PRIVATE)
        val expensePrefs = requireContext().getSharedPreferences("ExpensePrefs", Context.MODE_PRIVATE)
        val incomeEditor = incomePrefs.edit()
        val expenseEditor = expensePrefs.edit()

        val incomes = allTransactions.filterIsInstance<Transaction.IncomeTransaction>().map { it.income }
        val expenses = allTransactions.filterIsInstance<Transaction.ExpenseTransaction>().map { it.expense }

        incomeEditor.putString("incomes", Json.encodeToString(incomes))
        expenseEditor.putString("expenses", Json.encodeToString(expenses))
        incomeEditor.apply()
        expenseEditor.apply()
    }
}