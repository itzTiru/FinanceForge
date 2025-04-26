package com.example.financetracker

import kotlinx.serialization.Serializable

@Serializable
sealed class Transaction {
    data class IncomeTransaction(val income: Income) : Transaction()
    data class ExpenseTransaction(val expense: Expense) : Transaction()
}