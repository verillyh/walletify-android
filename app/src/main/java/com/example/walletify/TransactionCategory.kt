package com.example.walletify

enum class TransactionCategory(val displayName: String, val imageResId: Int) {
    FOOD("Food", R.drawable.food_icon),
    HOUSING("Housing", R.drawable.housing_icon),
    TRANSPORTATION("Transportation", R.drawable.transportation_icon),
    SHOPPING("Shopping", R.drawable.shopping_icon),
    GROCERIES("Groceries", R.drawable.groceries_icon),
    UTILITIES("Utilities", R.drawable.utilities_icon),
    INSURANCE("Insurance", R.drawable.insurance_icon),
    HEALTH("Health", R.drawable.health_icon),
    ENTERTAINMENT("Entertainment", R.drawable.entertainment_icon),
    INVESTMENTS("Investments", R.drawable.investment_icon),
    EDUCATION("Education", R.drawable.education_icon),
    CLOTHING("Clothing", R.drawable.clothing_icon),
    MISCELLANEOUS("Miscellaneous", R.drawable.miscellaneous_icon),
    INCOME("Income", R.drawable.income_icon),
    TRANSFER("Transfer", R.drawable.transfer_icon)
}
