package com.example.financetracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class TransactionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.transactions_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

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
        val transactions = mutableListOf<Transaction>()
        transactions.addAll(incomes.map { Transaction.IncomeTransaction(it) })
        transactions.addAll(expenses.map { Transaction.ExpenseTransaction(it) })

        // Sort by date (assuming date is in a parseable format, e.g., "yyyy-MM-dd")
        val sortedTransactions = transactions.sortedByDescending {
            when (it) {
                is Transaction.IncomeTransaction -> it.income.date
                is Transaction.ExpenseTransaction -> it.expense.date
            }
        }

        // Set up the RecyclerView adapter
        recyclerView.adapter = TransactionAdapter(sortedTransactions)

        return view
    }
}