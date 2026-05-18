package com.spendsnap.app.shared

object Constants {

    // ---- Category kind (khớp với BE schema: CATEGORY_KINDS) ----
    const val KIND_EXPENSE = "expense"
    const val KIND_INCOME = "income"

    // ---- UI fallback khi không lookup được emoji từ slug ----
    const val FALLBACK_ICON = "📦"

    // ---- Default icon slug (khớp với BE CATEGORY_ICONS "other") ----
    const val DEFAULT_ICON_SLUG = "other"

    // ---- Input limits ----
    const val MAX_AMOUNT_LENGTH = 10
}
