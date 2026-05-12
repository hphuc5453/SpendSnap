package com.spendsnap.app.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class StatisticsOverviewResponse(
    val yearMonth: String,
    val totalSpent: Double = 0.0,
    val totalSpentLastMonth: Double = 0.0,
    val spentChangePercent: Double? = null,
    val totalIncome: Double = 0.0,
    val totalIncomeLastMonth: Double = 0.0,
    val incomeChangePercent: Double? = null,
    val totalBudget: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val safeToSpendPerDay: Double? = null,
    val daysLeftInMonth: Int = 0,
    val breakdown: List<BreakdownItemResponse> = emptyList(),
    val insights: InsightsResponse = InsightsResponse()
)

@Serializable
data class BreakdownItemResponse(
    val categoryId: String,
    val name: String,
    val icon: String,
    val color: String? = null,
    val kind: String,
    val amount: Double = 0.0,
    val percentage: Double = 0.0,
    val amountLastMonth: Double = 0.0,
    val changePercent: Double? = null
)

@Serializable
data class InsightsResponse(
    val topImprovement: TopImprovement? = null,
    val topRegression: TopRegression? = null,
    val topSpending: TopSpending? = null
)

@Serializable
data class TopImprovement(
    val categoryId: String,
    val name: String,
    val icon: String,
    val savedAmount: Double = 0.0,
    val changePercent: Double? = null
)

@Serializable
data class TopRegression(
    val categoryId: String,
    val name: String,
    val icon: String,
    val extraAmount: Double = 0.0,
    val changePercent: Double? = null
)

@Serializable
data class TopSpending(
    val categoryId: String,
    val name: String,
    val icon: String,
    val amount: Double = 0.0,
    val percentage: Double = 0.0
)
