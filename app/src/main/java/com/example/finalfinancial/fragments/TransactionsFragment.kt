package com.example.finalfinancial.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalfinancial.R
import com.example.finalfinancial.Repositories.TransactionRepository
import com.example.finalfinancial.adapters.TransactionAdapter
import com.example.finalfinancial.databinding.FragmentTransactionsBinding
import com.example.finalfinancial.dialogfragments.AddTransactionDialogFragment
import com.example.finalfinancial.dialogfragments.EditTransactionDialogFragment
import com.example.finalfinancial.models.Transaction
import java.util.*

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var transactionAdapter: TransactionAdapter

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionRepository = TransactionRepository.getInstance(requireContext())

        // Setup MonthYear Spinner
        setupMonthYearSpinner()

        // Setup RecyclerView
        setupRecyclerView()

        // Setup FAB
        binding.addTransactionFab.setOnClickListener {
            AddTransactionDialogFragment().show(
                parentFragmentManager,
                "AddTransactionDialog"
            )
        }
    }

    private fun setupMonthYearSpinner() {
        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH)

        val monthYearItems = ArrayList<String>()
        val monthYearCalendar = Calendar.getInstance()

        monthYearItems.add("All Transactions")

        for (i in 0 until 12) {
            monthYearCalendar.set(Calendar.YEAR, currentYear)
            monthYearCalendar.set(Calendar.MONTH, currentMonth - i)
            val monthYearStr = android.text.format.DateFormat.format("MMMM yyyy", monthYearCalendar).toString()
            monthYearItems.add(monthYearStr)
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            monthYearItems
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.monthYearSpinner.adapter = adapter

        binding.monthYearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    loadAllTransactions()
                } else {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, -(position - 1))
                    currentYear = cal.get(Calendar.YEAR)
                    currentMonth = cal.get(Calendar.MONTH)
                    loadTransactionsForMonth(currentYear, currentMonth)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                loadAllTransactions()
            }
        }
    }


    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            ArrayList(),
            onItemClick = { transaction ->
                EditTransactionDialogFragment.newInstance(transaction).show(
                    parentFragmentManager,
                    "EditTransactionDialog"
                )
            },
            onDeleteClick = { transaction ->
                transactionRepository.deleteTransaction(transaction.id)
                updateTransactionsList()
            }
        )

        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }

        // Initial load
        loadAllTransactions()
    }

    private fun loadAllTransactions() {
        val transactions = transactionRepository.getAllTransactions().sortedByDescending { it.date }
        updateTransactionsList(transactions)
    }

    private fun loadTransactionsForMonth(year: Int, month: Int) {
        val transactions = transactionRepository.getTransactionsForMonth(year, month)
            .sortedByDescending { it.date }
        updateTransactionsList(transactions)
    }

    private fun updateTransactionsList(transactions: List<Transaction>? = null)
    {
        val transactionsList = transactions ?: if (binding.monthYearSpinner.selectedItemPosition == 0) {
            transactionRepository.getAllTransactions()
        } else {
            transactionRepository.getTransactionsForMonth(currentYear, currentMonth)
        }.sortedByDescending { it.date }

        transactionAdapter.updateTransactions(transactionsList)

        // Show empty state if needed
        if (transactionsList.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.transactionsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.transactionsRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        updateTransactionsList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}