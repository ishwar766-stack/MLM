package com.example.mlmshop.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * In-memory demo state. Replace the functions below with calls to your
 * backend (REST API + PostgreSQL) when you are ready for production.
 */
object Store {
    const val ME = 1
    const val TDS_RATE = 0.05
    const val MIN_WITHDRAW = 100.0
    const val RETAIL_PROFIT_RATE = 0.10

    val products = listOf(
        Product(1, "Herbal Immunity Tonic", "Wellness", 799, "🌿"),
        Product(2, "Vitamin C Gummies", "Wellness", 499, "🍊"),
        Product(3, "Aloe Vera Face Gel", "Beauty", 349, "🧴"),
        Product(4, "Argan Hair Serum", "Beauty", 599, "💧"),
        Product(5, "Soy Wax Candle Set", "Lifestyle", 899, "🕯️"),
        Product(6, "Steel Water Bottle", "Lifestyle", 649, "🥤"),
        Product(7, "Wellness App - 1 Year", "Digital", 1499, "📱"),
        Product(8, "Meditation Pack - 6 Months", "Digital", 999, "🧘")
    )
    val categories = listOf("All") + products.map { it.category }.distinct()

    val members = mutableStateListOf(
        Member(1, "You", null), Member(2, "Asha", 1), Member(3, "Ravi", 1),
        Member(4, "Priya", 2), Member(5, "Imran", 2), Member(6, "Neha", 3),
        Member(7, "Karan", 4), Member(8, "Sunita", 7), Member(9, "Deepak", 8),
        Member(10, "Meera", 9)
    )
    val volume = mutableStateMapOf(
        1 to 0, 2 to 8400, 3 to 5200, 4 to 12000, 5 to 3100,
        6 to 2700, 7 to 15500, 8 to 4300, 9 to 2200, 10 to 900
    )
    val cart = mutableStateListOf<CartLine>()
    val orders = mutableStateListOf<Order>()
    val ledger = mutableStateListOf<LedgerEntry>()

    var loggedIn by mutableStateOf(false)
    var userName by mutableStateOf("You")
    var isDistributor by mutableStateOf(false)
    var kycVerified by mutableStateOf(false)
    var walletBalance by mutableStateOf(0.0)
    var message by mutableStateOf<String?>(null)
    val referralCode = "SHOP1001"

    private var nextOrderId = 1001

    // ---------- auth ----------
    fun login(name: String) {
        userName = name.ifBlank { "You" }
        members[0] = members[0].copy(name = userName)
        loggedIn = true
    }

    // ---------- cart & orders ----------
    fun addToCart(p: Product) {
        val i = cart.indexOfFirst { it.product.id == p.id }
        if (i >= 0) cart[i] = cart[i].copy(qty = cart[i].qty + 1) else cart.add(CartLine(p, 1))
        message = "${p.name} added to cart"
    }

    fun changeQty(p: Product, delta: Int) {
        val i = cart.indexOfFirst { it.product.id == p.id }
        if (i < 0) return
        val q = cart[i].qty + delta
        if (q <= 0) cart.removeAt(i) else cart[i] = cart[i].copy(qty = q)
    }

    fun subtotal(): Int = cart.sumOf { it.product.price * it.qty }

    fun discount(code: String): Int =
        if (code.trim().equals("WELCOME10", ignoreCase = true)) subtotal() * 10 / 100 else 0

    fun placeOrder(payment: String, discount: Int) {
        if (cart.isEmpty()) return
        orders.add(0, Order(nextOrderId++, cart.toList(), subtotal() - discount, OrderStatus.ORDERED, payment))
        cart.clear()
        message = "Order placed"
    }

    fun advance(id: Int) {
        val i = orders.indexOfFirst { it.id == id }
        if (i < 0) return
        val o = orders[i]
        val next = when (o.status) {
            OrderStatus.ORDERED -> OrderStatus.SHIPPED
            OrderStatus.SHIPPED -> OrderStatus.OUT
            OrderStatus.OUT -> OrderStatus.DELIVERED
            else -> return
        }
        orders[i] = o.copy(status = next)
        if (next == OrderStatus.DELIVERED) {
            volume[ME] = (volume[ME] ?: 0) + o.total
            if (isDistributor) credit("Retail profit - order #${o.id}", o.total * RETAIL_PROFIT_RATE)
        }
    }

    fun returnOrder(id: Int) {
        val i = orders.indexOfFirst { it.id == id }
        if (i < 0 || orders[i].status != OrderStatus.DELIVERED) return
        val o = orders[i]
        orders[i] = o.copy(status = OrderStatus.RETURNED)
        volume[ME] = (volume[ME] ?: 0) - o.total
        if (isDistributor) debit("Reversal - retail profit #${o.id}", o.total * RETAIL_PROFIT_RATE)
        message = "Order #${o.id} returned, commission reversed"
    }

    // ---------- wallet ----------
    fun credit(title: String, amount: Double) {
        val a = CommissionEngine.round2(amount)
        walletBalance += a
        ledger.add(0, LedgerEntry(title, a, true))
    }

    fun debit(title: String, amount: Double) {
        val a = CommissionEngine.round2(amount)
        walletBalance -= a
        ledger.add(0, LedgerEntry(title, a, false))
    }

    fun withdraw(amount: Double) {
        message = when {
            !kycVerified -> "Complete KYC (Profile tab) before withdrawing"
            amount < MIN_WITHDRAW -> "Minimum withdrawal is ₹${MIN_WITHDRAW.toInt()}"
            amount > walletBalance -> "Insufficient wallet balance"
            else -> {
                val tds = CommissionEngine.round2(amount * TDS_RATE)
                debit("Withdrawal to bank (TDS ₹$tds, net ₹${CommissionEngine.round2(amount - tds)})", amount)
                "Withdrawal requested"
            }
        }
    }

    // ---------- network ----------
    val teamVolume: Int
        get() = members.filter { it.id != ME }.sumOf { volume[it.id] ?: 0 }

    fun totalVolume(): Int = (volume[ME] ?: 0) + teamVolume
    fun currentRank(): Rank = Rank.values().last { totalVolume() >= it.minVolume }
    fun nextRank(): Rank? = Rank.values().firstOrNull { it.minVolume > totalVolume() }

    fun addMember(name: String) {
        if (name.isBlank()) return
        val id = members.maxOf { it.id } + 1
        members.add(Member(id, name.trim(), ME))
        volume[id] = 0
        message = "${name.trim()} joined your team (joining is free)"
    }

    fun simulateDownlineSale() {
        if (!isDistributor) { message = "Become a distributor (Profile tab) to earn commissions"; return }
        val candidates = members.filter { it.id != ME }
        if (candidates.isEmpty()) return
        val buyer = candidates.random()
        val p = products.random()
        volume[buyer.id] = (volume[buyer.id] ?: 0) + p.price
        val mine = CommissionEngine.forSale(buyer.id, p.price.toDouble(), members).firstOrNull { it.memberId == ME }
        message = if (mine != null) {
            credit("L${mine.level} commission - ${buyer.name} bought ${p.name}", mine.amount)
            "+₹${mine.amount} (level ${mine.level}) from ${buyer.name}'s purchase"
        } else {
            "${buyer.name} bought ${p.name} - beyond level 5, no commission"
        }
    }

    // ---------- KYC ----------
    fun submitKyc(pan: String, aadhaar: String, account: String, ifsc: String) {
        message = when {
            !Regex("[A-Z]{5}[0-9]{4}[A-Z]").matches(pan.trim().uppercase()) -> "Invalid PAN"
            !Regex("[0-9]{12}").matches(aadhaar.trim()) -> "Aadhaar must be 12 digits"
            !Regex("[0-9]{9,18}").matches(account.trim()) -> "Invalid bank account number"
            !Regex("[A-Z]{4}0[A-Z0-9]{6}").matches(ifsc.trim().uppercase()) -> "Invalid IFSC"
            else -> { kycVerified = true; "KYC verified (demo auto-approval)" }
        }
    }
}
