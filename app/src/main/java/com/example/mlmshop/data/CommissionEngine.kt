package com.example.mlmshop.data

/**
 * Unilevel commission engine. Commissions are paid ONLY on product sales value
 * (never on joining fees) and only up to [levelRates].size levels above the buyer.
 */
object CommissionEngine {
    /** Level 1 = direct sponsor of the buyer. */
    val levelRates = listOf(0.10, 0.05, 0.03, 0.02, 0.01)

    fun round2(v: Double): Double = Math.round(v * 100.0) / 100.0

    fun forSale(buyerId: Int, saleValue: Double, members: List<Member>): List<Commission> {
        val byId = members.associateBy { it.id }
        val result = mutableListOf<Commission>()
        var current = byId[buyerId]?.sponsorId
        var level = 1
        while (current != null && level <= levelRates.size) {
            result += Commission(current, level, round2(saleValue * levelRates[level - 1]))
            current = byId[current]?.sponsorId
            level++
        }
        return result
    }

    /** Returns reverse the commissions: same rows, negated amounts. */
    fun reversal(commissions: List<Commission>): List<Commission> =
        commissions.map { it.copy(amount = -it.amount) }
}
