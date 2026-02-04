package com.pecule.app.domain

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class PdfExporter {

    // Pécule theme colors (lazy to avoid initialization issues in tests)
    private val primaryColor by lazy { Color.parseColor("#80CBC4") }
    private val textPrimaryColor by lazy { Color.parseColor("#00695C") }
    private val textSecondaryColor by lazy { Color.parseColor("#666666") }
    private val errorColor by lazy { Color.parseColor("#E53935") }
    private val successColor by lazy { Color.parseColor("#43A047") }

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE)

    // Page dimensions (A4 in points: 595 x 842)
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40f
    private val contentWidth = pageWidth - 2 * margin

    fun export(
        cycle: BudgetCycle,
        expenses: List<Expense>,
        incomes: List<Income>,
        categories: Map<Long, CategoryEntity>
    ): ByteArray {
        val document = PdfDocument()
        val summary = calculateSummary(cycle, expenses, incomes)
        val categoryBreakdown = calculateCategoryBreakdown(expenses, categories)

        // Page 1: Title, summary, category breakdown
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var yPosition = margin

        // Draw title
        yPosition = drawTitle(canvas, cycle, yPosition)
        yPosition += 20f

        // Draw period
        yPosition = drawPeriod(canvas, cycle, yPosition)
        yPosition += 30f

        // Draw summary card
        yPosition = drawSummary(canvas, summary, yPosition)
        yPosition += 30f

        // Draw donut chart
        if (categoryBreakdown.isNotEmpty()) {
            yPosition = drawDonutChart(canvas, categoryBreakdown, yPosition)
            yPosition += 20f
        }

        // Draw category breakdown table
        if (categoryBreakdown.isNotEmpty()) {
            yPosition = drawCategoryTable(canvas, categoryBreakdown, yPosition)
        }

        document.finishPage(page)

        // Page 2 (if needed): Transaction list
        val allTransactions = prepareTransactionList(expenses, incomes, categories)
        if (allTransactions.isNotEmpty()) {
            pageNumber = 2
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPosition = margin

            yPosition = drawTransactionList(canvas, allTransactions, yPosition)
            document.finishPage(page)
        }

        // Write to byte array
        val outputStream = ByteArrayOutputStream()
        document.writeTo(outputStream)
        document.close()

        return outputStream.toByteArray()
    }

    fun generateTitle(cycle: BudgetCycle): String {
        val monthYear = cycle.startDate.format(monthFormatter).replaceFirstChar {
            it.titlecase(Locale.FRANCE)
        }
        return "Pécule - Rapport $monthYear"
    }

    fun calculateSummary(
        cycle: BudgetCycle,
        expenses: List<Expense>,
        incomes: List<Income>
    ): PdfSummary {
        val totalExpenses = expenses.sumOf { it.amount }
        val totalIncomes = incomes.sumOf { it.amount }
        val balance = cycle.amount + totalIncomes - totalExpenses

        return PdfSummary(
            budgetAmount = cycle.amount,
            totalExpenses = totalExpenses,
            totalIncomes = totalIncomes,
            balance = balance
        )
    }

    fun calculateCategoryBreakdown(
        expenses: List<Expense>,
        categories: Map<Long, CategoryEntity>
    ): List<CategoryBreakdownItem> {
        val totalExpenses = expenses.sumOf { it.amount }
        if (totalExpenses == 0.0) return emptyList()

        return expenses
            .groupBy { expense -> expense.categoryId?.let { categories[it] } }
            .filterKeys { it != null }
            .map { (category, expenseList) ->
                val amount = expenseList.sumOf { it.amount }
                val percentage = (amount / totalExpenses) * 100
                CategoryBreakdownItem(
                    categoryName = category!!.name,
                    categoryColor = category.color,
                    amount = amount,
                    percentage = percentage
                )
            }
            .sortedByDescending { it.amount }
    }

    private fun drawTitle(canvas: Canvas, cycle: BudgetCycle, startY: Float): Float {
        val paint = Paint().apply {
            color = textPrimaryColor
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val title = generateTitle(cycle)
        canvas.drawText(title, margin, startY + 24f, paint)

        return startY + 40f
    }

    private fun drawPeriod(canvas: Canvas, cycle: BudgetCycle, startY: Float): Float {
        val paint = Paint().apply {
            color = textSecondaryColor
            textSize = 12f
            isAntiAlias = true
        }

        val startDate = cycle.startDate.format(dateFormatter)
        val endDate = cycle.endDate?.format(dateFormatter) ?: "En cours"
        val period = "Période: $startDate - $endDate"
        canvas.drawText(period, margin, startY + 12f, paint)

        return startY + 20f
    }

    private fun drawSummary(canvas: Canvas, summary: PdfSummary, startY: Float): Float {
        // Background card
        val cardPaint = Paint().apply {
            color = Color.parseColor("#E0F2F1")
            style = Paint.Style.FILL
        }
        val cardRect = RectF(margin, startY, pageWidth - margin, startY + 120f)
        canvas.drawRoundRect(cardRect, 12f, 12f, cardPaint)

        // Labels and values
        val labelPaint = Paint().apply {
            color = textSecondaryColor
            textSize = 12f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        var y = startY + 30f
        val labelX = margin + 20f
        val valueX = pageWidth - margin - 20f

        // Budget initial
        canvas.drawText("Budget initial", labelX, y, labelPaint)
        valuePaint.color = textPrimaryColor
        valuePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(currencyFormat.format(summary.budgetAmount), valueX, y, valuePaint)
        y += 25f

        // Total revenus
        canvas.drawText("Total revenus", labelX, y, labelPaint)
        valuePaint.color = successColor
        canvas.drawText("+ ${currencyFormat.format(summary.totalIncomes)}", valueX, y, valuePaint)
        y += 25f

        // Total dépenses
        canvas.drawText("Total dépenses", labelX, y, labelPaint)
        valuePaint.color = errorColor
        canvas.drawText("- ${currencyFormat.format(summary.totalExpenses)}", valueX, y, valuePaint)
        y += 25f

        // Divider
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#B2DFDB")
            strokeWidth = 1f
        }
        canvas.drawLine(labelX, y - 10f, valueX, y - 10f, dividerPaint)

        // Solde
        labelPaint.isFakeBoldText = true
        canvas.drawText("Solde", labelX, y + 5f, labelPaint)
        valuePaint.color = if (summary.balance >= 0) successColor else errorColor
        canvas.drawText(currencyFormat.format(summary.balance), valueX, y + 5f, valuePaint)

        return startY + 130f
    }

    private fun drawDonutChart(
        canvas: Canvas,
        categoryBreakdown: List<CategoryBreakdownItem>,
        startY: Float
    ): Float {
        val centerX = pageWidth / 2f
        val centerY = startY + 80f
        val outerRadius = 60f
        val innerRadius = 35f

        val rect = RectF(
            centerX - outerRadius,
            centerY - outerRadius,
            centerX + outerRadius,
            centerY + outerRadius
        )

        var startAngle = -90f
        categoryBreakdown.forEach { item ->
            val sweepAngle = (item.percentage / 100f * 360f).toFloat()
            val paint = Paint().apply {
                color = item.categoryColor.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)
            startAngle += sweepAngle
        }

        // Draw inner circle (to make it a donut)
        val innerPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, innerRadius, innerPaint)

        return startY + 170f
    }

    private fun drawCategoryTable(
        canvas: Canvas,
        categoryBreakdown: List<CategoryBreakdownItem>,
        startY: Float
    ): Float {
        val titlePaint = Paint().apply {
            color = textPrimaryColor
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        canvas.drawText("Dépenses par catégorie", margin, startY + 14f, titlePaint)
        var y = startY + 35f

        val labelPaint = Paint().apply {
            color = textSecondaryColor
            textSize = 11f
            isAntiAlias = true
        }

        val valuePaint = Paint().apply {
            color = textPrimaryColor
            textSize = 11f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        val percentPaint = Paint().apply {
            color = textSecondaryColor
            textSize = 10f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        categoryBreakdown.forEach { item ->
            // Color dot
            val dotPaint = Paint().apply {
                color = item.categoryColor.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(margin + 5f, y - 4f, 5f, dotPaint)

            // Category name
            canvas.drawText(item.categoryName, margin + 20f, y, labelPaint)

            // Amount
            canvas.drawText(
                currencyFormat.format(item.amount),
                pageWidth - margin - 60f,
                y,
                valuePaint
            )

            // Percentage
            canvas.drawText(
                String.format(Locale.FRANCE, "%.1f%%", item.percentage),
                pageWidth - margin,
                y,
                percentPaint
            )

            y += 22f
        }

        return y
    }

    private fun prepareTransactionList(
        expenses: List<Expense>,
        incomes: List<Income>,
        categories: Map<Long, CategoryEntity>
    ): List<TransactionListItem> {
        val expenseItems = expenses.map { expense ->
            TransactionListItem(
                type = "Dépense",
                label = expense.label,
                category = expense.categoryId?.let { categories[it]?.name } ?: "",
                amount = expense.amount,
                date = expense.date,
                isExpense = true
            )
        }

        val incomeItems = incomes.map { income ->
            TransactionListItem(
                type = "Revenu",
                label = income.label,
                category = "",
                amount = income.amount,
                date = income.date,
                isExpense = false
            )
        }

        return (expenseItems + incomeItems).sortedByDescending { it.date }
    }

    private fun drawTransactionList(
        canvas: Canvas,
        transactions: List<TransactionListItem>,
        startY: Float
    ): Float {
        val titlePaint = Paint().apply {
            color = textPrimaryColor
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }

        canvas.drawText("Détail des transactions", margin, startY + 16f, titlePaint)
        var y = startY + 45f

        // Headers
        val headerPaint = Paint().apply {
            color = textSecondaryColor
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        canvas.drawText("Date", margin, y, headerPaint)
        canvas.drawText("Type", margin + 70f, y, headerPaint)
        canvas.drawText("Libellé", margin + 130f, y, headerPaint)
        canvas.drawText("Catégorie", margin + 280f, y, headerPaint)
        headerPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Montant", pageWidth - margin, y, headerPaint)

        y += 15f

        // Divider
        val dividerPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.5f
        }
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
        y += 15f

        // Transactions
        val textPaint = Paint().apply {
            textSize = 9f
            isAntiAlias = true
        }

        transactions.take(30).forEach { transaction ->
            textPaint.color = textSecondaryColor
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(transaction.date.format(dateFormatter), margin, y, textPaint)
            canvas.drawText(transaction.type, margin + 70f, y, textPaint)

            textPaint.color = Color.BLACK
            val labelText = if (transaction.label.length > 20) {
                transaction.label.take(17) + "..."
            } else {
                transaction.label
            }
            canvas.drawText(labelText, margin + 130f, y, textPaint)

            textPaint.color = textSecondaryColor
            canvas.drawText(transaction.category, margin + 280f, y, textPaint)

            textPaint.color = if (transaction.isExpense) errorColor else successColor
            textPaint.textAlign = Paint.Align.RIGHT
            val sign = if (transaction.isExpense) "-" else "+"
            canvas.drawText(
                "$sign ${currencyFormat.format(transaction.amount)}",
                pageWidth - margin,
                y,
                textPaint
            )

            y += 18f

            // Check if we're near the bottom of the page
            if (y > pageHeight - margin - 20f) {
                return y
            }
        }

        return y
    }

    data class PdfSummary(
        val budgetAmount: Double,
        val totalExpenses: Double,
        val totalIncomes: Double,
        val balance: Double
    )

    data class CategoryBreakdownItem(
        val categoryName: String,
        val categoryColor: Long,
        val amount: Double,
        val percentage: Double
    )

    private data class TransactionListItem(
        val type: String,
        val label: String,
        val category: String,
        val amount: Double,
        val date: java.time.LocalDate,
        val isExpense: Boolean
    )
}
