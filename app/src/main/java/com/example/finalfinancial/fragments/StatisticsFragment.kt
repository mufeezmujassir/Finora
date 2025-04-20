package com.example.finalfinancial.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalfinancial.models.Category
import com.example.finalfinancial.R
import com.example.finalfinancial.Repositories.TransactionRepository
import com.example.finalfinancial.adapters.CategoryAdapter
import com.example.finalfinancial.databinding.FragmentStatisticsBinding
import com.example.finalfinancial.Repositories.PrefUtil
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var prefUtil: PrefUtil

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionRepository = TransactionRepository.getInstance(requireContext())
        prefUtil = PrefUtil(requireContext())

        setupMonthYearSpinner()
        setupCategoryRecyclerView()
        setupPieChart()
        updateStatistics()
    }

    private fun setupMonthYearSpinner() {
        val calendar = Calendar.getInstance()
         currentYear = calendar.get(Calendar.YEAR)
         currentMonth = calendar.get(Calendar.MONTH)

        // Create list of last 12 months
        val monthYearItems = ArrayList<String>()
        val monthYearCalendar = Calendar.getInstance()

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
                // Calculate year and month for the selected item
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -position)
                currentYear = cal.get(Calendar.YEAR)
                currentMonth = cal.get(Calendar.MONTH)
                updateStatistics()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Use current month/year by default
                updateStatistics()
            }
        }
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter(ArrayList())
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
        }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            legend.isEnabled = false
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }
    }

    private fun updateStatistics() {
        val expensesByCategory = transactionRepository.getExpensesByCategory(currentYear, currentMonth)
        val totalExpenses = transactionRepository.getTotalExpensesForMonth(currentYear, currentMonth)

        if (totalExpenses <= 0) {
            showEmptyState()
            return
        }

        showChartState()

        // Update currency display
        val currency = prefUtil.currency ?: "$"
        binding.totalExpensesTv.text = getString(R.string.currency_amount, currency, totalExpenses)

        // Update pie chart
        updatePieChart(expensesByCategory, totalExpenses)

        // Update categories list
        updateCategoriesList(expensesByCategory, totalExpenses, currency)
    }

    private fun updatePieChart(expensesByCategory: Map<Category, Double>, totalExpenses: Double) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        // Sort categories by amount (descending)
        val sortedCategories = expensesByCategory.entries
            .sortedByDescending { it.value }

        // Add entries for each category
        for (entry in sortedCategories) {
            val percentage = (entry.value / totalExpenses).toFloat()
            entries.add(PieEntry(percentage, entry.key.displayName))

            // Assign different colors to different categories
            colors.add(ColorTemplate.MATERIAL_COLORS[colors.size % ColorTemplate.MATERIAL_COLORS.size])
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(binding.pieChart))
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.BLACK)

        binding.pieChart.data = data
        binding.pieChart.centerText = getString(R.string.expenses_by_category)
        binding.pieChart.invalidate() // refresh
    }

    private fun updateCategoriesList(
        expensesByCategory: Map<Category, Double>,
        totalExpenses: Double,
        currency: String
    ) {
        val categoryItems = expensesByCategory.entries
            .sortedByDescending { it.value }
            .map { entry ->
                val percentage = (entry.value / totalExpenses * 100).toInt()
                CategoryAdapter.CategoryItem(
                    entry.key,
                    entry.value,
                    currency,
                    percentage
                )
            }

        categoryAdapter.updateCategories(categoryItems)
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.statisticsContent.visibility = View.GONE
    }

    private fun showChartState() {
        binding.emptyStateLayout.visibility = View.GONE
        binding.statisticsContent.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        updateStatistics()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}