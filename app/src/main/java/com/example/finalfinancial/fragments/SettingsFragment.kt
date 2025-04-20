package com.example.finalfinancial.fragments
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.finalfinancial.R
import com.example.finalfinancial.Repositories.TransactionRepository
import com.example.finalfinancial.databinding.FragmentSettingsBinding
import com.example.finalfinancial.Repositories.FileUtil
import com.example.finalfinancial.utils.NotificationUtil
import com.example.finalfinancial.Repositories.PrefUtil

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefUtil: PrefUtil
    private lateinit var transactionRepository: TransactionRepository

    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                restoreFromBackup(uri)
            }
        }
    }

    private val exportFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                exportToFile(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefUtil = PrefUtil(requireContext())
        transactionRepository = TransactionRepository.getInstance(requireContext())

        setupCurrencySpinner()
        setupBudgetSettings()
        setupNotificationSettings()
        setupBackupOptions()
    }

    private fun setupCurrencySpinner() {
        val currencies = arrayOf("$", "€", "£", "¥", "₹", "₽", "₩", "฿", "₺", "₴","Rs")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            currencies
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner.adapter = adapter

        // Set current selection
        val currentCurrency = prefUtil.currency ?: "$"
        val currencyIndex = currencies.indexOf(currentCurrency)
        if (currencyIndex != -1) {
            binding.currencySpinner.setSelection(currencyIndex)
        }

        binding.currencySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCurrency = currencies[position]
                if (selectedCurrency != prefUtil.currency) {
                    prefUtil.currency = selectedCurrency
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.currency_updated),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupBudgetSettings() {
        val currentBudget = prefUtil.getMonthlyBudget()
        binding.budgetEditText.setText(if (currentBudget > 0) currentBudget.toString() else "")

        binding.saveBudgetBtn.setOnClickListener {
            val budgetText = binding.budgetEditText.text.toString()
            if (budgetText.isNotEmpty()) {
                try {
                    val budget = budgetText.toDouble()
                    if (budget > 0) {
                        prefUtil.setMonthlyBudget(budget)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.budget_updated),
                            Toast.LENGTH_SHORT
                        ).show()

                        // Reset budget warning threshold when budget changes
                        prefUtil.lastBudgetWarningThreshold = 0

                        // Update notification if needed
                        NotificationUtil.updateBudgetNotificationIfNeeded(requireContext())
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.enter_valid_budget),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.enter_valid_budget),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Clear budget
                prefUtil.setMonthlyBudget(0.0)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.budget_cleared),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupNotificationSettings() {
        binding.notificationSwitch.isChecked = prefUtil.shouldShowBudgetWarning

        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefUtil.shouldShowBudgetWarning = isChecked

            if (isChecked) {
                // Reset threshold to allow notifications to trigger again
                prefUtil.lastBudgetWarningThreshold = 0

                // Check if notifications should be shown immediately
                NotificationUtil.updateBudgetNotificationIfNeeded(requireContext())

                Toast.makeText(
                    requireContext(),
                    getString(R.string.notifications_enabled),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.notifications_disabled),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupBackupOptions() {
        binding.exportDataBtn.setOnClickListener {
            createExportIntent()
        }

        binding.importDataBtn.setOnClickListener {
            createImportIntent()
        }
    }

    private fun createExportIntent() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, FileUtil.getBackupFilename())
        }
        exportFileLauncher.launch(intent)
    }

    private fun exportToFile(uri: Uri) {
        try {
            val transactions = transactionRepository.getAllTransactions()
            if (transactions.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_data_to_export),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val outputStream = requireContext().contentResolver.openOutputStream(uri)
            outputStream?.use { stream ->
                val jsonData = com.google.gson.Gson().toJson(transactions)
                stream.write(jsonData.toByteArray())
                stream.flush()
            }

            Toast.makeText(
                requireContext(),
                getString(R.string.data_exported_successfully),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                getString(R.string.export_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createImportIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        importFileLauncher.launch(intent)
    }

    private fun restoreFromBackup(uri: Uri) {
        val success = FileUtil.restoreFromBackup(requireContext(), transactionRepository, uri)

        if (success) {
            Toast.makeText(
                requireContext(),
                getString(R.string.data_imported_successfully),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.import_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
