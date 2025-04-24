package com.example.financetracker

import kotlinx.serialization.Serializable

@Serializable
sealed class Transaction {
    @Serializable
    data class IncomeTransaction(val income: Income) : Transaction()
    @Serializable
    data class ExpenseTransaction(val expense: Expense) : Transaction()
}