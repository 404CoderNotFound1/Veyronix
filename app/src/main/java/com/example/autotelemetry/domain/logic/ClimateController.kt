package com.example.autotelemetry.domain.logic

class ClimateController(
    private val minimumC: Float = 16f,
    private val maximumC: Float = 30f,
    private val stepC: Float = 0.5f,
) {
    fun increase(currentC: Float): Float = (currentC + stepC).coerceAtMost(maximumC)
    fun decrease(currentC: Float): Float = (currentC - stepC).coerceAtLeast(minimumC)
}
