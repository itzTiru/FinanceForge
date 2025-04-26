package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Update currency for balance, budget, and transaction
        val balanceValue: TextView = view.findViewById(R.id.balance_value)
        val budgetProgress: TextView = view.findViewById(R.id.budget_progress)
        val transactionAmount1: TextView = view.findViewById(R.id.transaction_amount_1)

        // Example values (replace with actual data logic)
        val balance = 22950.00
        val budgetSpent = 500.00
        val budgetTotal = 1000.00
        val transaction1Amount = 2950.00

        val currencySymbol = Utils.getCurrencySymbol(requireContext())
        balanceValue.text = "$currencySymbol${String.format("%.2f", balance)}"
        budgetProgress.text = "Budget: $currencySymbol${String.format("%.2f", budgetSpent)} / $currencySymbol${String.format("%.2f", budgetTotal)}"
        transactionAmount1.text = "- $currencySymbol${String.format("%.2f", transaction1Amount)}"

        // Add Expense Button
        val addExpenseButton: Button = view.findViewById(R.id.add_expense_button)
        addExpenseButton.setOnClickListener {
            startActivity(Intent(context, AddExpenseActivity::class.java))
        }

        // Add Income Button
        val addIncomeButton: Button = view.findViewById(R.id.add_income_button)
        addIncomeButton.setOnClickListener {
            startActivity(Intent(context, AddIncomeActivity::class.java))
        }

        // Quick Action Buttons (placeholders)
        view.findViewById<ImageButton>(R.id.action_send).setOnClickListener { /* Handle send */ }
        view.findViewById<ImageButton>(R.id.action_pay).setOnClickListener { /* Handle pay */ }
        view.findViewById<ImageButton>(R.id.action_recharge).setOnClickListener { /* Handle recharge */ }
        view.findViewById<ImageButton>(R.id.action_electricity).setOnClickListener { /* Handle electricity */ }

        return view
    }
}