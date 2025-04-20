package com.example.finalfinancial.dialogfragments

import android.Manifest
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.example.finalfinancial.models.Category
import com.example.finalfinancial.R
import com.example.finalfinancial.models.Transaction
import com.example.finalfinancial.Repositories.TransactionRepository
import com.example.finalfinancial.databinding.FragmentAddTransactionDialogBinding
import com.example.finalfinancial.utils.NotificationUtil
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionDialogFragment : DialogFragment() {

    private var _binding: FragmentAddTransactionDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionRepository: TransactionRepository

    private var selectedDate = Calendar.getInstance().timeInMillis
    private var isIncome = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionRepository = TransactionRepository.getInstance(requireContext())

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setupTypeSelector()
        setupDatePicker()
        setupCategorySpinner()
        setupButtons()
        clearErrorsOnTyping()
    }

    private fun setupTypeSelector() {
        binding.typeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isIncome = checkedId == R.id.income_btn
                updateCategorySpinner()
            }
        }
        binding.expenseBtn.isChecked = true
    }

    private fun setupDatePicker() {
        updateDateButtonText()

        binding.datePickerBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate

            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.timeInMillis
                    updateDateButtonText()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateButtonText() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.datePickerBtn.text = dateFormat.format(Date(selectedDate))
    }

    private fun setupCategorySpinner() {
        updateCategorySpinner()
    }

    private fun updateCategorySpinner() {
        val categories = if (isIncome) {
            Category.getIncomeCategories()
        } else {
            Category.getExpenseCategories()
        }

        val categoryNames = categories.map { it.displayName }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter
    }

    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun validateInputs(): Boolean {
        val title = binding.titleEditText.text.toString().trim()
        val amountText = binding.amountEditText.text.toString().trim()

        var isValid = true

        if (title.isEmpty()) {
            showError(binding.titleInputLayout, getString(R.string.required_field))
            isValid = false
        }

        if (amountText.isEmpty()) {
            showError(binding.amountInputLayout, getString(R.string.required_field))
            isValid = false
        } else {
            try {
                val amount = amountText.toDouble()
                if (amount <= 0) {
                    showError(binding.amountInputLayout, getString(R.string.amount_greater_than_zero))
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                showError(binding.amountInputLayout, getString(R.string.invalid_amount))
                isValid = false
            }
        }

        return isValid
    }

    private fun showError(layout: com.google.android.material.textfield.TextInputLayout, message: String) {
        layout.error = message
        layout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
    }

    private fun clearErrorsOnTyping() {
        binding.titleEditText.addTextChangedListener {
            binding.titleInputLayout.error = null
        }

        binding.amountEditText.addTextChangedListener {
            binding.amountInputLayout.error = null
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun saveTransaction() {
        val title = binding.titleEditText.text.toString().trim()
        val amount = binding.amountEditText.text.toString().toDouble()

        val categoryName = binding.categorySpinner.selectedItem.toString()
        val category = Category.values().first { it.displayName == categoryName }

        val transaction = Transaction(
            title = title,
            amount = amount,
            category = category,
            date = selectedDate,
            isIncome = isIncome
        )

        transactionRepository.addTransaction(transaction)
        NotificationUtil.updateBudgetNotificationIfNeeded(requireContext())

        Toast.makeText(
            requireContext(),
            getString(R.string.transaction_added),
            Toast.LENGTH_SHORT
        ).show()

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
