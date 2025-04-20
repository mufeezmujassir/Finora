package com.example.finalfinancial.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.finalfinancial.R
import com.example.finalfinancial.models.Transaction
import com.example.finalfinancial.databinding.TransactionItemBinding
import com.example.finalfinancial.Repositories.PrefUtil
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(private val binding: TransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            val context = binding.root.context
            val prefUtil = PrefUtil(context)
            val currency = prefUtil.currency ?: "$"

            binding.titleTv.text = transaction.title
            binding.categoryTv.text = transaction.category.displayName

            // Format date
            val date = Date(transaction.date)
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.dateTv.text = dateFormat.format(date)

            // Set amount with appropriate color
            val amountText = context.getString(
                R.string.currency_amount,
                currency,
                transaction.amount
            )
            binding.amountTv.text = amountText

            binding.amountTv.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (transaction.isIncome) R.color.green else R.color.red
                )
            )

            // Set category icon
            binding.categoryIconIv.setImageResource(transaction.category.iconResId)

            // Set click listeners
            binding.root.setOnClickListener {
                onItemClick(transaction)
            }

            binding.deleteBtn.setOnClickListener {
                onDeleteClick(transaction)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = TransactionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}