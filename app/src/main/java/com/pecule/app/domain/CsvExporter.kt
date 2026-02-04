package com.pecule.app.domain

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import java.time.format.DateTimeFormatter
import java.util.Locale

class CsvExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE)

    fun export(
        cycle: BudgetCycle,
        expenses: List<Expense>,
        incomes: List<Income>,
        categories: Map<Long, CategoryEntity>
    ): String {
        val sb = StringBuilder()

        // Header
        sb.appendLine("Type,Libellé,Catégorie,Montant,Date,Fixe")

        // Expenses
        expenses.forEach { expense ->
            val categoryName = expense.categoryId?.let { categories[it]?.name } ?: ""
            val fixedLabel = if (expense.isFixed) "Oui" else "Non"
            sb.appendLine(
                "Dépense,${escapeCSV(expense.label)},$categoryName,${formatAmount(expense.amount)},${expense.date.format(dateFormatter)},$fixedLabel"
            )
        }

        // Incomes
        incomes.forEach { income ->
            val fixedLabel = if (income.isFixed) "Oui" else "Non"
            sb.appendLine(
                "Revenu,${escapeCSV(income.label)},,${formatAmount(income.amount)},${income.date.format(dateFormatter)},$fixedLabel"
            )
        }

        return sb.toString().trimEnd()
    }

    private fun formatAmount(amount: Double): String {
        return String.format(Locale.US, "%.2f", amount)
    }

    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
