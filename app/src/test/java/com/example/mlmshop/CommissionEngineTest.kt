package com.example.mlmshop

import com.example.mlmshop.data.CommissionEngine
import com.example.mlmshop.data.Member
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommissionEngineTest {
    // chain: 1 <- 2 <- 3 <- 4 <- 5 <- 6 <- 7  (7 is the buyer)
    private val chain = (1..7).map { Member(it, "M$it", if (it == 1) null else it - 1) }

    @Test fun paysFiveLevelsOnly() {
        val c = CommissionEngine.forSale(7, 1000.0, chain)
        assertEquals(5, c.size)
        assertEquals(6, c.first().memberId)
        assertEquals(100.0, c.first().amount, 0.001)
        assertEquals(2, c.last().memberId)
        assertEquals(10.0, c.last().amount, 0.001)
        assertTrue(c.none { it.memberId == 1 })
    }

    @Test fun reversalNegatesAmounts() {
        val c = CommissionEngine.forSale(7, 1000.0, chain)
        val r = CommissionEngine.reversal(c)
        assertEquals(0.0, c.sumOf { it.amount } + r.sumOf { it.amount }, 0.001)
    }
}
