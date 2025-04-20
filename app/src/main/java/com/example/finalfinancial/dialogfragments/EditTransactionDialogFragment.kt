package com.example.finalfinancial.dialogfragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.finalfinancial.models.Category
import com.example.finalfinancial.R
import com.example.finalfinancial.models.Transaction
import com.example.finalfinancial.Repositories.TransactionRepository
import com.example.finalfinancial.databinding.FragmentAddTransactionDialogBinding
import com.example.finalfinancial.utils.NotificationUtil
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionDialogFragment : DialogFragment() {

    private var _binding: FragmentAddTransactionDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionRepository: TransactionRepository

    private var transaction: Transaction? = null
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

        // Set dialog width to match parent
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Get transaction from arguments
        transaction = arguments?.getSerializable(ARG_TRANSACTION) as? Transaction
        if (transaction == null) {
            dismiss()
            return
        }

        // Set the title for editing
        binding.dialogTitleTv.text = getString(R.string.edit_transaction)

        setupTypeSelector()
        setupDatePicker()
        setupCategorySpinner()
        setupButtons()

        // Fill data with transaction values
        fillFormWithTransactionData()
    }

    private fun setupTypeSelector() {
        binding.typeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isIncome = checkedId == R.id.income_btn
                // Update category spinner based on transaction type
                updateCategorySpinner()
            }
        }
    }

    private fun setupDatePicker() {
        // Set initial date text
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

        // If we have a transaction, select its category
        transaction?.let {
            val position = categoryNames.indexOf(it.category.displayName)
            if (position >= 0) {
                binding.categorySpinner.setSelection(position)
            }
        }
    }

    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                updateTransaction()
            }
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun fillFormWithTransactionData() {
        transaction?.let {
            // Set transaction type
            isIncome = it.isIncome
            binding.incomeBtn.isChecked = it.isIncome
            binding.expenseBtn.isChecked = !it.isIncome

            // Set date
            selectedDate = it.date
            updateDateButtonText()

            // Set title and amount
            binding.titleEditText.setText(it.title)
            binding.amountEditText.setText(it.amount.toString())

            // Update category spinner and select current category
            updateCategorySpinner()
        }
    }

    private fun validateInputs(): Boolean {
        val title = binding.titleEditText.text.toString().trim()
        val amountText = binding.amountEditText.text.toString().trim()

        if (title.isEmpty()) {
            binding.titleEditText.error = getString(R.string.required_field)
            return false
        }

        if (amountText.isEmpty()) {
            binding.amountEditText.error = getString(R.string.required_field)
            return false
        }

        try {
            val amount = amountText.toDouble()
            if (amount <= 0) {
                binding.amountEditText.error = getString(R.string.amount_greater_than_zero)
                return false
            }
        } catch (e: NumberFormatException) {
            binding.amountEditText.error = getString(R.string.invalid_amount)
            return false
        }

        return true
    }

    private fun updateTransaction() {
        val currentTransaction = transaction ?: return
        val title = binding.titleEditText.text.toString().trim()
        val amount = binding.amountEditText.text.toString().toDouble()

        val categoryName = binding.categorySpinner.selectedItem.toString()
        val category = Category.values().first { it.displayName == categoryName }

        val updatedTransaction = Transaction(
            id = currentTransaction.id,
            title = title,
            amount = amount,
            category = category,
            date = selectedDate,
            isIncome = isIncome
        )

        transactionRepository.updateTransaction(updatedTransaction)

        // Check if budget notification should be shown
        NotificationUtil.updateBudgetNotificationIfNeeded(requireContext())

        Toast.makeText(
            requireContext(),
            getString(R.string.transaction_updated),
            Toast.LENGTH_SHORT
        ).show()

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TRANSACTION = "transaction"
        const val TAG = "EditTransactionDialog"

        fun newInstance(transaction: Transaction): EditTransactionDialogFragment {
            val fragment = EditTransactionDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_TRANSACTION, transaction)
            fragment.arguments = args
            return fragment
        }
    }
}