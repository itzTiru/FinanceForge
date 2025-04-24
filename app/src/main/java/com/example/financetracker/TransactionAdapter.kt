package com.example.financetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.transaction_icon)
        val title: TextView = itemView.findViewById(R.id.transaction_title)
        val date: TextView = itemView.findViewById(R.id.transaction_date)
        val amount: TextView = itemView.findViewById(R.id.transaction_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        when (transaction) {
            is Transaction.IncomeTransaction -> {
                val income = transaction.income
                holder.title.text = income.category
                holder.date.text = income.date
                // Format amount as a number, assuming it's a valid number string
                val amountValue = income.amount.toDoubleOrNull() ?: 0.0
                holder.amount.text = "+ $${String.format("%.2f", amountValue)}"
                holder.amount.setTextColor(
                    holder.itemView.context.getColor(R.color.green) // #388E3C
                )
                holder.icon.setImageResource(R.drawable.ic_plus)
            }
            is Transaction.ExpenseTransaction -> {
                val expense = transaction.expense
                holder.title.text = expense.category
                holder.date.text = expense.date
                // Format amount as a number, assuming it's a valid number string
                val amountValue = expense.amount.toDoubleOrNull() ?: 0.0
                holder.amount.text = "- $${String.format("%.2f", amountValue)}"
                holder.amount.setTextColor(
                    holder.itemView.context.getColor(R.color.red) // #D32F2F
                )
                holder.icon.setImageResource(R.drawable.ic_minus)
            }
        }
    }

    override fun getItemCount(): Int = transactions.size
}