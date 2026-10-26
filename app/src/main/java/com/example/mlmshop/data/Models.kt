package com.example.mlmshop.data

data class Product(val id: Int, val name: String, val category: String, val price: Int, val emoji: String)
data class CartLine(val product: Product, val qty: Int)

enum class OrderStatus(val label: String) {
    ORDERED("Ordered"), SHIPPED("Shipped"), OUT("Out for delivery"),
    DELIVERED("Delivered"), RETURNED("Returned")
}

data class Order(
    val id: Int,
    val lines: List<CartLine>,
    val total: Int,
    val status: OrderStatus,
    val payment: String
)

data class Member(val id: Int, val name: String, val sponsorId: Int?)
data class Commission(val memberId: Int, val level: Int, val amount: Double)
data class LedgerEntry(val title: String, val amount: Double, val credit: Boolean)

enum class Rank(val label: String, val minVolume: Int) {
    BRONZE("Bronze", 0), SILVER("Silver", 20000), GOLD("Gold", 50000), DIAMOND("Diamond", 100000)
}
