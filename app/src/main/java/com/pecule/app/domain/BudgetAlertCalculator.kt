package com.pecule.app.domain

enum class BudgetAlertLevel {
    NONE,
    WARNING,
    DANGER,
    EXCEEDED
}

data class BudgetAlert(
    val level: BudgetAlertLevel,
    val message: String,
    val percentage: Int
)

class BudgetAlertCalculator {

    fun calculate(percentageUsed: Double): BudgetAlert {
        val roundedPercentage = percentageUsed.toInt()

        return when {
            percentageUsed < 0 -> BudgetAlert(
                level = BudgetAlertLevel.NONE,
                message = "",
                percentage = 0
            )
            percentageUsed < 50 -> BudgetAlert(
                level = BudgetAlertLevel.NONE,
                message = "",
                percentage = roundedPercentage
            )
            percentageUsed < 80 -> BudgetAlert(
                level = BudgetAlertLevel.WARNING,
                message = "Attention, vous avez utilisé ${roundedPercentage}% de votre budget",
                percentage = roundedPercentage
            )
            percentageUsed < 100 -> BudgetAlert(
                level = BudgetAlertLevel.DANGER,
                message = "Budget critique : ${roundedPercentage}% utilisé",
                percentage = roundedPercentage
            )
            else -> BudgetAlert(
                level = BudgetAlertLevel.EXCEEDED,
                message = "Budget dépassé ! ${roundedPercentage}% utilisé",
                percentage = roundedPercentage
            )
        }
    }
}
