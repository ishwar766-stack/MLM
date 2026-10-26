@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mlmshop.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List as ListIcon
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mlmshop.data.Member
import com.example.mlmshop.data.Order
import com.example.mlmshop.data.OrderStatus
import com.example.mlmshop.data.Product
import com.example.mlmshop.data.Store

private fun inr(x: Double) = "₹" + String.format("%.2f", x)

@Composable
fun App() {
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(Store.message) {
        Store.message?.let {
            snack.showSnackbar(it)
            Store.message = null
        }
    }
    if (Store.loggedIn) MainScaffold(snack) else LoginScreen(snack)
}

// ---------------------------------------------------------------- Login
@Composable
fun LoginScreen(snack: SnackbarHostState) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    Scaffold(snackbarHost = { SnackbarHost(snack) }) { pad ->
        Column(
            Modifier.padding(pad).padding(24.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            Text("Shop & Earn 🛍️", style = MaterialTheme.typography.headlineMedium)
            Text("Buy quality products. Share them. Earn from real product sales.")
            OutlinedTextField(name, { name = it }, label = { Text("Your name") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { phone = it.filter(Char::isDigit).take(10) },
                label = { Text("Mobile number") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(otp, { otp = it.take(4) }, label = { Text("OTP (demo: 1234)") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                if (phone.length == 10 && otp == "1234") Store.login(name)
                else Store.message = "Enter a 10-digit number and OTP 1234"
            }, modifier = Modifier.fillMaxWidth()) { Text("Login") }
            Text("Joining is free. Commissions are paid only on product sales.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ---------------------------------------------------------------- Shell
@Composable
fun MainScaffold(snack: SnackbarHostState) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var showCart by rememberSaveable { mutableStateOf(false) }
    val labels = listOf("Shop", "Orders", "Team", "Wallet", "Profile")
    val icons = listOf(Icons.Default.Home, Icons.Default.ListIcon, Icons.Default.Share,
        Icons.Default.AccountBox, Icons.Default.Person)

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text(if (showCart) "Cart & Checkout" else labels[tab]) },
                navigationIcon = { if (showCart) TextButton(onClick = { showCart = false }) { Text("Back") } },
                actions = {
                    if (!showCart) TextButton(onClick = { showCart = true }) {
                        Text("Cart (${Store.cart.sumOf { it.qty }})")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                labels.forEachIndexed { i, l ->
                    NavigationBarItem(
                        selected = tab == i && !showCart,
                        onClick = { tab = i; showCart = false },
                        icon = { Icon(icons[i], contentDescription = l) },
                        label = { Text(l) }
                    )
                }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            when {
                showCart -> CartScreen { showCart = false; tab = 1 }
                tab == 0 -> ShopScreen()
                tab == 1 -> OrdersScreen()
                tab == 2 -> TeamScreen()
                tab == 3 -> WalletScreen()
                else -> ProfileScreen()
            }
        }
    }
}

// ---------------------------------------------------------------- Shop
@Composable
fun ShopScreen() {
    var q by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("All") }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(q, { q = it }, label = { Text("Search products") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Store.categories.forEach { c ->
                FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) })
            }
        }
        val list = Store.products.filter {
            (cat == "All" || it.category == cat) && it.name.contains(q, ignoreCase = true)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) { items(list) { p -> ProductCard(p) } }
    }
}

@Composable
fun ProductCard(p: Product) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(p.emoji, fontSize = 40.sp)
            Text(p.name, fontWeight = FontWeight.SemiBold)
            Text(p.category, style = MaterialTheme.typography.bodySmall)
            Text("₹${p.price}", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { Store.addToCart(p) }, modifier = Modifier.fillMaxWidth()) { Text("Add to cart") }
        }
    }
}

// ---------------------------------------------------------------- Cart
@Composable
fun CartScreen(onOrdered: () -> Unit) {
    var coupon by remember { mutableStateOf("") }
    var applied by remember { mutableStateOf("") }
    var pay by remember { mutableStateOf("UPI") }
    val discount = Store.discount(applied)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (Store.cart.isEmpty()) {
            Text("Your cart is empty.")
            return@Column
        }
        Store.cart.toList().forEach { line ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${line.product.emoji} ${line.product.name}", Modifier.weight(1f))
                TextButton(onClick = { Store.changeQty(line.product, -1) }) { Text("−") }
                Text("${line.qty}")
                TextButton(onClick = { Store.changeQty(line.product, 1) }) { Text("+") }
                Text("₹${line.product.price * line.qty}")
            }
        }
        Divider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(coupon, { coupon = it }, label = { Text("Coupon (try WELCOME10)") },
                singleLine = true, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                applied = coupon
                if (Store.discount(coupon) == 0) Store.message = "Invalid coupon"
            }) { Text("Apply") }
        }
        Text("Payment method", fontWeight = FontWeight.SemiBold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("UPI", "Card", "Net Banking", "Cash on Delivery").forEach {
                FilterChip(selected = pay == it, onClick = { pay = it }, label = { Text(it) })
            }
        }
        Divider()
        Text("Subtotal: ₹${Store.subtotal()}")
        if (discount > 0) Text("Coupon discount: −₹$discount")
        Text("Total: ₹${Store.subtotal() - discount}", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { Store.placeOrder(pay, discount); onOrdered() }, modifier = Modifier.fillMaxWidth()) {
            Text("Pay ₹${Store.subtotal() - discount} via $pay")
        }
        Text("Payment gateway (Razorpay/Cashfree etc.) is mocked in this demo.",
            style = MaterialTheme.typography.bodySmall)
    }
}

// ---------------------------------------------------------------- Orders
@Composable
fun OrdersScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (Store.orders.isEmpty()) Text("No orders yet. Add something from the Shop tab.")
        Store.orders.toList().forEach { OrderCard(it) }
    }
}

@Composable
fun OrderCard(o: Order) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Order #${o.id} · ₹${o.total} · ${o.payment}", fontWeight = FontWeight.SemiBold)
            Text(o.lines.joinToString { "${it.product.name} ×${it.qty}" }, style = MaterialTheme.typography.bodySmall)
            if (o.status == OrderStatus.RETURNED) {
                Text("Returned - commissions reversed", color = MaterialTheme.colorScheme.error)
            } else {
                val steps = listOf(OrderStatus.ORDERED, OrderStatus.SHIPPED, OrderStatus.OUT, OrderStatus.DELIVERED)
                LinearProgressIndicator(progress = { (o.status.ordinal + 1) / 4f }, modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    steps.forEach {
                        Text(it.label, fontSize = 11.sp,
                            color = if (it.ordinal <= o.status.ordinal) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            }
            when (o.status) {
                OrderStatus.DELIVERED -> OutlinedButton(onClick = { Store.returnOrder(o.id) }) { Text("Return order") }
                OrderStatus.RETURNED -> {}
                else -> Button(onClick = { Store.advance(o.id) }) { Text("Simulate next stage") }
            }
        }
    }
}

// ---------------------------------------------------------------- Team
@Composable
fun TeamScreen() {
    var newName by remember { mutableStateOf("") }
    val rank = Store.currentRank()
    val next = Store.nextRank()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!Store.isDistributor) {
            Card(Modifier.fillMaxWidth()) {
                Text("Upgrade to Distributor (free) in the Profile tab to start earning.",
                    Modifier.padding(16.dp))
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Rank: ${rank.label}", style = MaterialTheme.typography.titleMedium)
                Text("Total volume: ₹${Store.totalVolume()}")
                if (next != null) {
                    LinearProgressIndicator(
                        progress = { (Store.totalVolume().toFloat() / next.minVolume).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth())
                    Text("₹${next.minVolume - Store.totalVolume()} more to reach ${next.label}",
                        style = MaterialTheme.typography.bodySmall)
                } else Text("Top rank achieved 🎉")
            }
        }
        Text("Genealogy tree (tap to expand)", style = MaterialTheme.typography.titleMedium)
        Store.members.firstOrNull { it.id == Store.ME }?.let { TreeNode(it, 0) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(newName, { newName = it }, label = { Text("New member name") },
                singleLine = true, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = { Store.addMember(newName); newName = "" }) { Text("Add") }
        }
        Button(onClick = { Store.simulateDownlineSale() }, modifier = Modifier.fillMaxWidth()) {
            Text("Simulate a downline purchase")
        }
        Divider()
        Text("Leaderboard (by sales volume)", style = MaterialTheme.typography.titleMedium)
        Store.members.sortedByDescending { Store.volume[it.id] ?: 0 }.take(5).forEachIndexed { i, m ->
            Text("${i + 1}. ${m.name} - ₹${Store.volume[m.id] ?: 0}")
        }
        Divider()
        Text("Unilevel plan: L1 10% · L2 5% · L3 3% · L4 2% · L5 1% of product sales value. " +
            "No commission on joining.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun TreeNode(m: Member, depth: Int) {
    val children = Store.members.filter { it.sponsorId == m.id }
    var open by remember { mutableStateOf(depth < 2) }
    Row(
        Modifier.fillMaxWidth().clickable { open = !open }
            .padding(start = (depth * 16).dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (children.isEmpty()) "•" else if (open) "▾" else "▸")
        Spacer(Modifier.width(8.dp))
        Column {
            Text(m.name, fontWeight = FontWeight.SemiBold)
            Text(if (depth == 0) "You" else "Level $depth · ₹${Store.volume[m.id] ?: 0}",
                style = MaterialTheme.typography.bodySmall)
        }
    }
    if (open) children.forEach { TreeNode(it, depth + 1) }
}

// ---------------------------------------------------------------- Wallet
@Composable
fun WalletScreen() {
    var amount by remember { mutableStateOf("") }
    val credits = Store.ledger.filter { it.credit }
    val retail = credits.filter { it.title.startsWith("Retail") }.sumOf { it.amount }
    val direct = credits.filter { it.title.startsWith("L1 ") }.sumOf { it.amount }
    val team = credits.filter { it.title.startsWith("L") && !it.title.startsWith("L1 ") }.sumOf { it.amount }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Wallet balance", style = MaterialTheme.typography.bodySmall)
                Text(inr(Store.walletBalance), style = MaterialTheme.typography.headlineMedium)
                Text("Retail profit: ${inr(retail)}")
                Text("Direct referral (L1): ${inr(direct)}")
                Text("Team commission (L2-L5): ${inr(team)}")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Withdraw amount (₹)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = { Store.withdraw(amount.toDoubleOrNull() ?: 0.0) }) { Text("Withdraw") }
        }
        Text("Min ₹${Store.MIN_WITHDRAW.toInt()} · TDS ${(Store.TDS_RATE * 100).toInt()}% deducted · KYC required",
            style = MaterialTheme.typography.bodySmall)
        Divider()
        Text("Transactions", style = MaterialTheme.typography.titleMedium)
        if (Store.ledger.isEmpty()) Text("No transactions yet.")
        Store.ledger.toList().forEach {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(it.title, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Text((if (it.credit) "+" else "−") + inr(it.amount),
                    color = if (it.credit) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ---------------------------------------------------------------- Profile
@Composable
fun ProfileScreen() {
    val ctx = LocalContext.current
    var pan by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(Store.userName, style = MaterialTheme.typography.headlineSmall)
        Text("Rank: ${Store.currentRank().label} · Referral code: ${Store.referralCode}")
        if (Store.isDistributor) Text("✅ Distributor")
        else Button(onClick = { Store.isDistributor = true; Store.message = "You are now a distributor" },
            modifier = Modifier.fillMaxWidth()) { Text("Become a Distributor (free)") }

        OutlinedButton(onClick = {
            val i = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,
                    "Shop quality wellness & lifestyle products on Shop & Earn. Use my code ${Store.referralCode} when you join!")
            }
            ctx.startActivity(Intent.createChooser(i, "Share via"))
        }, modifier = Modifier.fillMaxWidth()) { Text("Share invite (WhatsApp, Instagram, ...)") }

        Divider()
        Text("KYC verification", style = MaterialTheme.typography.titleMedium)
        if (Store.kycVerified) {
            Text("✅ KYC verified - payouts enabled")
        } else {
            OutlinedTextField(pan, { pan = it.uppercase().take(10) }, label = { Text("PAN") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(aadhaar, { aadhaar = it.filter(Char::isDigit).take(12) }, label = { Text("Aadhaar (12 digits)") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(account, { account = it.filter(Char::isDigit).take(18) }, label = { Text("Bank account number") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(ifsc, { ifsc = it.uppercase().take(11) }, label = { Text("IFSC") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { Store.submitKyc(pan, aadhaar, account, ifsc) }, modifier = Modifier.fillMaxWidth()) {
                Text("Submit KYC")
            }
            Text("Demo only: real KYC must use a licensed verification API and encrypted storage.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
