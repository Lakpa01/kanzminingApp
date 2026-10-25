
package com.example.kanzminingapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage

private val KanzGold = Color(0xFFE5B94F)
private val KanzDark = Color(0xFF11130F)
private val KanzPanel = Color(0xFF1B1E18)
private val KanzWhite = Color(0xFFF5F5F0)
private val KanzMuted = Color(0xFFB8B8AE)
private val KanzBorder = Color(0xFF33352D)

private data class KanzMarketplaceProduct(
    val name: String,
    val price: String,
    val subtitle: String,
    val category: String,
    val emoji: String,
    val details: String,
    val imageUrl: String
)

private val marketplaceProducts = listOf(
    KanzMarketplaceProduct(
        name = "Gold Washing Plant",
        price = "Sample: $50,000 USD",
        subtitle = "Gold washing and processing",
        category = "Processing",
        emoji = "⚙️",
        details = "Equipment designed to wash and process gold-bearing material. Confirm capacity, condition, water requirements, installation, shipping and warranty with the seller.",
        imageUrl = "https://www.zoomjomachine.com/uploadfile/20250305105406_7377.jpg"
    ),
    KanzMarketplaceProduct(
        name = "Gold Detector",
        price = "Sample: $5,000 USD",
        subtitle = "Gold exploration equipment",
        category = "Detection",
        emoji = "🔍",
        details = "A gold exploration detector. Check the exact model, detection capabilities, accessories and warranty before purchasing.",
        imageUrl = "https://seriousdetecting.com/cdn/shop/products/12-317_5b572ccd-22e2-4bb7-9ceb-f872b4d8742a.jpg?v=1684459985&width=1200"
    ),
    KanzMarketplaceProduct(
        name = "Electric Jack Hammer",
        price = "Sample: $3,000 USD",
        subtitle = "Rock breaking equipment",
        category = "Tools",
        emoji = "🔨",
        details = "An electric jack hammer for rock breaking. Verify power requirements, impact specifications, safety requirements and accessories.",
        imageUrl = "https://ifdalivestorage.blob.core.windows.net/user-uploads/5498/344513/00000000-0000-0000-0000-000000000000/01_main_entry_img.jpg"
    ),
    KanzMarketplaceProduct(
        name = "Mining Drill",
        price = "Contact seller",
        subtitle = "Drilling equipment",
        category = "Tools",
        emoji = "🛠️",
        details = "Drilling equipment for mining operations. Ask about drilling depth, compatible bits, power source, maintenance and delivery.",
        imageUrl = "https://img.d1cm.com/pro/202409291625279324.jpg"
    ),
    KanzMarketplaceProduct(
        name = "Gold Sluice Box",
        price = "Sample: $250 USD",
        subtitle = "Gold separation equipment",
        category = "Processing",
        emoji = "🪙",
        details = "A sluice box used to separate heavier material from sediment. Confirm dimensions, materials and water-flow requirements.",
        imageUrl = "https://goldibox.com/product/goldibox-fluid-bed-sluice/?v=84de8e2b14bb"
    ),
    KanzMarketplaceProduct(
        name = "Portable Gold Trommel",
        price = "Contact seller",
        subtitle = "Screening and washing",
        category = "Processing",
        emoji = "🔄",
        details = "A rotating screening system for processing material. Check throughput, screen size, power requirements and portability.",
        imageUrl = "https://www.cn-welltech.com/uploads/202134725/mobile-scrubber-trommel57177890032.jpg"
    ),
    KanzMarketplaceProduct(
        name = "Mining Generator",
        price = "Contact seller",
        subtitle = "Power supply equipment",
        category = "Power",
        emoji = "🔋",
        details = "A generator for mining operations. Confirm output capacity, fuel type, operating hours, maintenance history and shipping.",
        imageUrl = "https://img.jeawincdn.com/resource/upfiles/224/images/products/diesel-generator-set/1200kw-diesel-generator-perkins-4012-46tag2a-50hz-3-phase_PhrLQC.jpg"
    ),
    KanzMarketplaceProduct(
        name = "Industrial Water Pump",
        price = "Sample: $800 USD",
        subtitle = "Water transfer equipment",
        category = "Equipment",
        emoji = "💧",
        details = "A water pump for mining and processing. Check flow rate, pumping head, power source and operating conditions.",
        imageUrl = "https://cpimg.tistatic.com/05182487/b/4/Raw-Water-Pump.jpg"
    ),
    KanzMarketplaceProduct(
        name = "Mining Conveyor",
        price = "Contact seller",
        subtitle = "Material handling system",
        category = "Equipment",
        emoji = "🏗️",
        details = "A conveyor for moving material. Ask about length, belt width, load capacity, installation and maintenance.",
        imageUrl = "https://www.maithlogistics.com/products/underground-mining-belt-conveyor-system/"
    ),
    KanzMarketplaceProduct(
        name = "Hard Hat and Safety Kit",
        price = "Sample: $100 USD",
        subtitle = "Personal protective equipment",
        category = "Safety",
        emoji = "🦺",
        details = "A mining safety kit. Check the included protective equipment, available sizes and applicable safety certifications.",
        imageUrl = "https://lodworkwear.com.au/cdn/shop/files/65-791-BLK-Copy.jpg?v=1738156953"
    ),
    KanzMarketplaceProduct(
        name = "Mining Safety Boots",
        price = "Sample: $75 USD",
        subtitle = "Protective footwear",
        category = "Safety",
        emoji = "🥾",
        details = "Protective boots for mining environments. Check fit, toe protection, slip resistance and suitability for the worksite.",
        imageUrl = "https://lodworkwear.com.au/cdn/shop/files/65-791-BLK-Copy.jpg?v=1738156953"
    ),
    KanzMarketplaceProduct(
        name = "Rock Crusher",
        price = "Contact seller",
        subtitle = "Ore crushing equipment",
        category = "Processing",
        emoji = "⚒️",
        details = "A machine for reducing rock size. Confirm feed size, output size, capacity, power and installation requirements.",
        imageUrl = "https://gelenmakina.com/en/crushers/"
    ),
    KanzMarketplaceProduct(
        name = "Mining Excavator",
        price = "Contact seller",
        subtitle = "Heavy mining machinery",
        category = "Heavy Machinery",
        emoji = "🚜",
        details = "Heavy equipment for excavation and material handling. Check model year, operating hours, condition, transport costs and service history.",
        imageUrl = "https://s7d2.scene7.com/is/image/Caterpillar/CM20200513-7ac21-1057d?hei=467&op_sharpen=1&qlt=100&wid=700"
    ),
    KanzMarketplaceProduct(
        name = "Portable Air Compressor",
        price = "Sample: $1,200 USD",
        subtitle = "Compressed air equipment",
        category = "Power",
        emoji = "💨",
        details = "A portable air compressor for compatible mining tools. Verify air pressure, flow rate, power source and tool compatibility.",
        imageUrl = "https://d2hu48ign70s8n.cloudfront.net/images/products/20/2092802/B1D3D89C-CA8F-4860-841A-DDDE11CE61CB-large.jpg"
    )
)

@Composable
fun KanzHomeScreen(
    onOpenSection: (String) -> Unit,
    onLogout: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedProduct by remember {
        mutableStateOf<KanzMarketplaceProduct?>(null)
    }
    var showContactSeller by remember { mutableStateOf(false) }

    val categories = listOf(
        "All",
        "Equipment",
        "Detection",
        "Processing",
        "Tools",
        "Power",
        "Safety",
        "Heavy Machinery"
    )

    val filteredProducts = marketplaceProducts.filter { product ->
        val query = searchText.trim()

        val matchesSearch =
            query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.subtitle.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true) ||
                    product.details.contains(query, ignoreCase = true)

        val matchesCategory =
            selectedCategory == "All" ||
                    product.category == selectedCategory

        matchesSearch && matchesCategory
    }

    if (showContactSeller && selectedProduct != null) {
        SellerContactScreen(
            product = selectedProduct!!,
            onBack = { showContactSeller = false }
        )
        return
    }

    if (selectedProduct != null) {
        ProductDetailScreen(
            product = selectedProduct!!,
            onBack = { selectedProduct = null },
            onContactSeller = { showContactSeller = true }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KanzDark)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KANZ",
                        color = KanzGold,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Gold Mining Marketplace",
                        color = KanzMuted,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = KanzPanel
                    ) {
                        Text(
                            text = "⛏️",
                            modifier = Modifier.padding(10.dp),
                            fontSize = 22.sp
                        )
                    }

                    TextButton(onClick = onLogout) {
                        Text(
                            text = "Logout",
                            color = KanzGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Find what you need.",
                color = KanzWhite,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Explore equipment, tools and gold processing products.",
                color = KanzMuted,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                label = { Text("Search mining products") },
                placeholder = { Text("Try gold detector or drill") },
                leadingIcon = { Text("🔎", fontSize = 18.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = KanzWhite,
                    unfocusedTextColor = KanzWhite,
                    focusedLabelColor = KanzGold,
                    unfocusedLabelColor = KanzMuted,
                    focusedBorderColor = KanzGold,
                    unfocusedBorderColor = KanzBorder,
                    focusedContainerColor = KanzPanel,
                    unfocusedContainerColor = KanzPanel,
                    cursorColor = KanzGold
                )
            )

            Spacer(Modifier.height(22.dp))

            Text(
                text = "Browse categories",
                color = KanzWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KanzGold,
                            selectedLabelColor = KanzDark,
                            labelColor = KanzWhite
                        )
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategory == "All") {
                        "Mining products"
                    } else {
                        selectedCategory
                    },
                    color = KanzWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${filteredProducts.size} items",
                    color = KanzMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            if (filteredProducts.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = KanzPanel
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔎", fontSize = 32.sp)

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "No products found",
                            color = KanzWhite,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Try another search or select All.",
                            color = KanzMuted,
                            fontSize = 13.sp
                        )

                        TextButton(
                            onClick = {
                                searchText = ""
                                selectedCategory = "All"
                            }
                        ) {
                            Text("Clear filters", color = KanzGold)
                        }
                    }
                }
            } else {
                filteredProducts.forEach { product ->
                    ProductCard(
                        product = product,
                        onClick = { selectedProduct = product }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Demo catalogue: prices and availability are examples only. Images are illustrative product references. Confirm details and image usage rights before selling.",
                color = KanzMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(18.dp))
        }

        HorizontalDivider(color = KanzBorder)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KanzPanel)
                .navigationBarsPadding()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem("🏠", "Home") { onOpenSection("All") }
            BottomNavItem("💼", "Jobs") { onOpenSection("Jobs") }
            BottomNavItem("⛏️", "Mines") { onOpenSection("Mines") }
            BottomNavItem("💬", "Chats") { onOpenSection("Chats") }
            BottomNavItem("👤", "Profile") { onOpenSection("My Profile") }
        }
    }
}

@Composable
private fun ProductImage(
    product: KanzMarketplaceProduct,
    modifier: Modifier = Modifier
) {
    SubcomposeAsyncImage(
        model = product.imageUrl,
        contentDescription = product.name,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF292B23)),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF292B23)),
                contentAlignment = Alignment.Center
            ) {
                Text(product.emoji, fontSize = 36.sp)
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF292B23)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(product.emoji, fontSize = 36.sp)
                    Text(
                        text = "Image unavailable",
                        color = KanzMuted,
                        fontSize = 10.sp
                    )
                }
            }
        },
        success = {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    )
}

@Composable
private fun ProductCard(
    product: KanzMarketplaceProduct,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = KanzPanel
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImage(
                product = product,
                modifier = Modifier.size(100.dp)
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.category.uppercase(),
                    color = KanzGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = product.name,
                    color = KanzWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = product.subtitle,
                    color = KanzMuted,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = product.price,
                    color = KanzGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = "View details →",
                    color = KanzWhite,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ProductDetailScreen(
    product: KanzMarketplaceProduct,
    onBack: () -> Unit,
    onContactSeller: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KanzDark)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back to products", color = KanzGold)
        }

        Spacer(Modifier.height(12.dp))

        ProductImage(
            product = product,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        )

        Spacer(Modifier.height(22.dp))

        Text(
            text = product.category.uppercase(),
            color = KanzGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = product.name,
            color = KanzWhite,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = product.price,
            color = KanzGold,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(22.dp))

        Text(
            text = "Product description",
            color = KanzWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = product.details,
            color = KanzMuted,
            fontSize = 15.sp,
            lineHeight = 23.sp
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Seller information",
            color = KanzWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Seller profiles and verified offers will be added in a future version.",
            color = KanzMuted,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onContactSeller,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = KanzGold,
                contentColor = KanzDark
            )
        ) {
            Text("Contact seller", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Demo listing: price and availability have not been verified.",
            color = KanzMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun SellerContactScreen(
    product: KanzMarketplaceProduct,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val productMessages = KanzChatStore.messages.filter {
        it.productName == product.name
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KanzDark)
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back to product", color = KanzGold)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Contact seller",
            color = KanzWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = product.name,
            color = KanzGold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = product.price,
            color = KanzMuted,
            fontSize = 13.sp
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = KanzBorder
        )

        if (productMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💬", fontSize = 42.sp)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Start a conversation",
                        color = KanzWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Ask about price, availability, delivery or product details.",
                        color = KanzMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                productMessages.forEach { chatMessage ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (chatMessage.isFromUser) {
                            Arrangement.End
                        } else {
                            Arrangement.Start
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (chatMessage.isFromUser) {
                                KanzGold
                            } else {
                                KanzPanel
                            },
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Text(
                                text = chatMessage.message,
                                modifier = Modifier.padding(
                                    horizontal = 14.dp,
                                    vertical = 10.dp
                                ),
                                color = if (chatMessage.isFromUser) {
                                    KanzDark
                                } else {
                                    KanzWhite
                                },
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            LaunchedEffect(productMessages.size) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Write a message...", color = KanzMuted)
                },
                shape = RoundedCornerShape(14.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = KanzWhite,
                    unfocusedTextColor = KanzWhite,
                    focusedBorderColor = KanzGold,
                    unfocusedBorderColor = KanzBorder,
                    focusedContainerColor = KanzPanel,
                    unfocusedContainerColor = KanzPanel,
                    cursorColor = KanzGold
                )
            )

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    val textToSend = messageText.trim()

                    if (textToSend.isNotEmpty()) {
                        KanzChatStore.sendMessage(
                            product.name,
                            textToSend
                        )
                        messageText = ""
                    }
                },
                enabled = messageText.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KanzGold,
                    contentColor = KanzDark
                ),
                contentPadding = PaddingValues(
                    horizontal = 14.dp,
                    vertical = 12.dp
                )
            ) {
                Text("Send", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Demo messaging: messages are stored locally on this device.",
            color = KanzMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun BottomNavItem(
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 20.sp)

        Spacer(Modifier.height(4.dp))

        Text(
            text = label,
            color = if (label == "Home") KanzGold else KanzMuted,
            fontSize = 10.sp,
            fontWeight = if (label == "Home") {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
    }
}
