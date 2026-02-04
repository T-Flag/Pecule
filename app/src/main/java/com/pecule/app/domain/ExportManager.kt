package com.pecule.app.domain

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExportManager(
    private val context: Context,
    private val csvExporter: CsvExporter = CsvExporter(),
    private val pdfExporter: PdfExporter = PdfExporter()
) {

    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM", Locale.FRANCE)

    fun exportCsv(
        cycle: BudgetCycle,
        expenses: List<Expense>,
        incomes: List<Income>,
        categories: Map<Long, CategoryEntity>
    ): Intent {
        val csvContent = csvExporter.export(cycle, expenses, incomes, categories)
        val fileName = generateFileName(cycle, "csv")
        val file = saveToFile(fileName, csvContent.toByteArray())

        return createShareIntent(file, "text/csv", "Export CSV - Pécule")
    }

    fun exportPdf(
        cycle: BudgetCycle,
        expenses: List<Expense>,
        incomes: List<Income>,
        categories: Map<Long, CategoryEntity>
    ): Intent {
        val pdfBytes = pdfExporter.export(cycle, expenses, incomes, categories)
        val fileName = generateFileName(cycle, "pdf")
        val file = saveToFile(fileName, pdfBytes)

        return createShareIntent(file, "application/pdf", "Export PDF - Pécule")
    }

    private fun generateFileName(cycle: BudgetCycle, extension: String): String {
        val monthYear = cycle.startDate.format(monthFormatter)
        return "pecule_$monthYear.$extension"
    }

    private fun saveToFile(fileName: String, content: ByteArray): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val file = File(exportDir, fileName)
        file.writeBytes(content)
        return file
    }

    private fun createShareIntent(file: File, mimeType: String, title: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
