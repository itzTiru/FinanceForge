package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Reference to the Add Expense and Add Income buttons
        val addExpenseButton: Button = view.findViewById(R.id.add_expense_button)
        val addIncomeButton: Button = view.findViewById(R.id.add_income_button)

        // Click listener for Add Expense button
        addExpenseButton.setOnClickListener {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply {
                putExtra("TRANSACTION_TYPE", "expense")
            }
            startActivity(intent)
        }

        // Click listener for Add Income button
        addIncomeButton.setOnClickListener {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply {
                putExtra("TRANSACTION_TYPE", "income")
            }
            startActivity(intent)
        }
    }
}