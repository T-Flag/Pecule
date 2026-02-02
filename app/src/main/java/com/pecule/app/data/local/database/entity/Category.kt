package com.pecule.app.data.local.database.entity

enum class Category(val label: String, val icon: String) {
    SALARY("Salaire", "payments"),
    FOOD("Alimentation", "restaurant"),
    TRANSPORT("Transport", "directions_car"),
    HOUSING("Logement", "home"),
    UTILITIES("Factures", "receipt_long"),
    ENTERTAINMENT("Loisirs", "sports_esports"),
    HEALTH("Santé", "medical_services"),
    SHOPPING("Shopping", "shopping_bag"),
    OTHER("Autre", "more_horiz")
}
