package com.example.finalfinancial.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.finalfinancial.R

import com.example.finalfinancial.Repositories.TransactionRepository
import com.example.finalfinancial.databinding.FragmentDashboardBinding
import com.example.finalfinancial.utils.NotificationUtil
import com.example.finalfinancial.Repositories  .PrefUtil
import com.example.finalfinancial.dialogfragments.AddTransactionDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var prefUtil: PrefUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize repositories and utilities
        transactionRepository = TransactionRepository.getInstance(requireContext())
        prefUtil = PrefUtil(requireContext())

        // Setup FAB
        binding.addTransactionFab.setOnClickListener {
            openAddTransactionDialog()
        }

        // Update dashboard with current data
        updateDashboard()
    }

    private fun openAddTransactionDialog() {
        val dialog = AddTransactionDialogFragment()
        dialog.show(parentFragmentManager, "AddTransactionDialog")
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()

        // Check for budget notifications
        NotificationUtil.updateBudgetNotificationIfNeeded(requireContext())
    }

    private fun updateDashboard() {
        val currency = prefUtil.currency ?: "$"
        val monthlyIncome = transactionRepository.getTotalIncomeForMonth()
        val monthlyExpenses = transactionRepository.getTotalExpensesForMonth()
        val balance = monthlyIncome - monthlyExpenses

        // Update summary cards
        binding.incomeAmountTv.text = getString(R.string.currency_amount, currency, monthlyIncome)
        binding.expenseAmountTv.text = getString(R.string.currency_amount, currency, monthlyExpenses)
        binding.balanceAmountTv.text = getString(R.string.currency_amount, currency, balance)

        // Set balance text color
        binding.balanceAmountTv.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                when {
                    balance > 0 -> R.color.green
                    balance < 0 -> R.color.red
                    else -> R.color.text_primary     // Default text color
                }
            )
        )

        // Update budget progress
        val monthlyBudget = prefUtil.getMonthlyBudget()
        if (monthlyBudget > 0) {
            val budgetUsagePercent = if (monthlyBudget > 0) {
                ((monthlyExpenses / monthlyBudget) * 100).toInt().coerceIn(0, 100)
            } else 0

            binding.budgetProgressBar.progress = budgetUsagePercent
            binding.budgetPercentageTv.text = "$budgetUsagePercent%"
            binding.budgetAmountTv.text = getString(
                R.string.budget_status,
                currency,
                monthlyExpenses,
                monthlyBudget
            )

            // Update progress bar color based on budget status
            binding.budgetProgressBar.progressTintList = ContextCompat.getColorStateList(
                requireContext(),
                when {
                    budgetUsagePercent >= 100 -> R.color.red
                    budgetUsagePercent >= 80 -> R.color.orange
                    else -> R.color.green
                }
            )
        } else {
            binding.budgetProgressBar.progress = 0
            binding.budgetPercentageTv.text = "0%"
            binding.budgetAmountTv.text = getString(R.string.no_budget_set)
        }

        // Update current month
        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        binding.currentMonthTv.text = currentMonth

        // Show/hide appropriate views based on data
        if (monthlyIncome == 0.0 && monthlyExpenses == 0.0) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.dashboardContent.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.dashboardContent.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}