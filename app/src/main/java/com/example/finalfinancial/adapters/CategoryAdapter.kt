package com.example.finalfinancial.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalfinancial.models.Category
import com.example.finalfinancial.R
import com.example.finalfinancial.databinding.CategoryItemBinding

class CategoryAdapter(
    private var categories: List<CategoryItem>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    data class CategoryItem(
        val category: Category,
        val amount: Double,
        val currency: String,
        val percentage: Int
    )

    inner class CategoryViewHolder(private val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryItem) {
            val context = binding.root.context

            binding.categoryNameTv.text = item.category.displayName
            binding.categoryIconIv.setImageResource(item.category.iconResId)

            binding.amountTv.text = context.getString(
                R.string.currency_amount,
                item.currency,
                item.amount
            )

            binding.percentageTv.text = context.getString(
                R.string.percentage_format,
                item.percentage
            )

            binding.progressBar.progress = item.percentage
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = CategoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<CategoryItem>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}