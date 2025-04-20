package com.example.finalfinancial.dialogfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.finalfinancial.R
import com.example.finalfinancial.databinding.FragmentEditBudgetDialogBinding
import com.example.finalfinancial.utils.NotificationUtil
import com.example.finalfinancial.Repositories.PrefUtil

class EditBudgetDialogFragment : DialogFragment() {

    private var _binding: FragmentEditBudgetDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefUtil: PrefUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBudgetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefUtil = PrefUtil(requireContext())

        // Load current budget if set
        val currentBudget = prefUtil.getMonthlyBudget()
        if (currentBudget > 0) {
            binding.budgetEditText.setText(currentBudget.toString())
        }

        // Set dialog width to match parent
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.saveButton.setOnClickListener {
            saveBudget()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun saveBudget() {
        val budgetText = binding.budgetEditText.text.toString().trim()

        if (budgetText.isEmpty()) {
            binding.budgetEditText.error = getString(R.string.required_field)
            return
        }

        try {
            val budget = budgetText.toDouble()
            if (budget <= 0) {
                binding.budgetEditText.error = getString(R.string.amount_greater_than_zero)
                return
            }

            prefUtil.setMonthlyBudget(budget)

            // Reset budget warning threshold when budget changes
            prefUtil.lastBudgetWarningThreshold = 0

            // Update notification if needed
            NotificationUtil.updateBudgetNotificationIfNeeded(requireContext())

            Toast.makeText(
                requireContext(),
                getString(R.string.budget_updated),
                Toast.LENGTH_SHORT
            ).show()

            dismiss()
        } catch (e: NumberFormatException) {
            binding.budgetEditText.error = getString(R.string.invalid_amount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditBudgetDialog"
    }
}
