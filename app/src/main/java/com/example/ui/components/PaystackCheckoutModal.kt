package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Official Paystack & Regional Payment Brand Colors
val PaystackCyan = Color(0xFF0BA4DB)
val PaystackDarkCyan = Color(0xFF0083B0)
val PaystackDarkNavy = Color(0xFF011B33)
val PaystackLightCyan = Color(0xFFE6F7FD)
val PaystackSuccessGreen = Color(0xFF00C853)

val BrandGPay = Color(0xFF1F1F1F)
val BrandApplePay = Color(0xFF000000)
val BrandStripe = Color(0xFF635BFF)
val BrandPayPal = Color(0xFF003087)
val BrandPayPalYellow = Color(0xFFFFC439)
val BrandVenmo = Color(0xFF008CFF)
val BrandZelle = Color(0xFF7414CA)
val BrandMada = Color(0xFF006C35)
val BrandFawry = Color(0xFFFFB300)
val BrandBenefit = Color(0xFFD32F2F)
val BrandKnet = Color(0xFF1565C0)
val BrandTamara = Color(0xFFFF4E00)
val BrandGiftCard = Color(0xFF8E24AA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaystackCheckoutModal(
  tier: SubscriptionTier,
  isYearly: Boolean,
  paystackConfig: PaystackConfig,
  onPaymentSuccess: (PaystackTransaction) -> Unit,
  onDismiss: () -> Unit
) {
  val clipboardManager = LocalClipboardManager.current
  val coroutineScope = rememberCoroutineScope()

  val amount = if (isYearly) {
    when (tier) {
      SubscriptionTier.BASIC -> 579.18
      SubscriptionTier.PRO -> 1164.24
      SubscriptionTier.PREMIUM -> 1626.54
      SubscriptionTier.ENTERPRISE -> 1977.86
      SubscriptionTier.SOVEREIGN -> 2316.36
      SubscriptionTier.USERBASE -> 343.36
    }
  } else {
    when (tier) {
      SubscriptionTier.BASIC -> 49.0
      SubscriptionTier.PRO -> 99.0
      SubscriptionTier.PREMIUM -> 139.0
      SubscriptionTier.ENTERPRISE -> 169.0
      SubscriptionTier.SOVEREIGN -> 199.0
      SubscriptionTier.USERBASE -> 29.0
    }
  }

  var selectedCategory by remember { mutableStateOf(PaymentChannelCategory.CARDS_WALLETS) }
  var selectedChannel by remember { mutableStateOf(PaystackPaymentChannel.CARD) }
  var customerEmail by remember { mutableStateOf("stevecobbs357@gmail.com") }

  // Card details
  var cardNumber by remember { mutableStateOf("4084 0092 8401 2940") }
  var cardExpiry by remember { mutableStateOf("12/28") }
  var cardCvv by remember { mutableStateOf("382") }
  var cardHolderName by remember { mutableStateOf("Steve Cobbs") }
  var saveCardForRenewals by remember { mutableStateOf(true) }

  // PayPal / Venmo / Zelle State
  var venmoHandle by remember { mutableStateOf("@stevecobbs") }
  var zelleSenderName by remember { mutableStateOf("Steve Cobbs (Chase Checking)") }

  // Gift Card State
  var giftCardCode by remember { mutableStateOf("ENT-GIFT-9840-2819-3382") }
  var giftCardPin by remember { mutableStateOf("8819") }
  var isGiftCardValidated by remember { mutableStateOf(true) }

  // MENA Regional State
  var madaCardNumber by remember { mutableStateOf("5888 4920 1948 2019") }
  var fawryRefNumber by remember { mutableStateOf("982 410 923") }
  var benefitPhone by remember { mutableStateOf("+973 3912 3456") }
  var knetCivilId by remember { mutableStateOf("294019284019") }
  var stcPayPhone by remember { mutableStateOf("+966 50 123 4567") }

  // USSD & MoMo State
  var selectedBank by remember { mutableStateOf("GTBank (*737#)") }
  var momoPhone by remember { mutableStateOf("+234 803 123 4567") }
  var momoProvider by remember { mutableStateOf("MTN MoMo") }

  // Processing state machine
  var isProcessing by remember { mutableStateOf(false) }
  var processingMessage by remember { mutableStateOf("Connecting to Paystack Gateway...") }
  var showOtpChallenge by remember { mutableStateOf(false) }
  var otpInput by remember { mutableStateOf("582910") }
  var completedTransaction by remember { mutableStateOf<PaystackTransaction?>(null) }
  var countdownSeconds by remember { mutableStateOf(1794) } // 29:54 min

  LaunchedEffect(isProcessing) {
    while (countdownSeconds > 0) {
      delay(1000)
      countdownSeconds--
    }
  }

  AlertDialog(
    onDismissRequest = {
      if (!isProcessing) onDismiss()
    },
    shape = RoundedCornerShape(26.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp, vertical = 8.dp)
      .testTag("paystack_checkout_modal"),
    title = {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Paystack Branded Top Banner
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = PaystackDarkNavy,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(PaystackCyan),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Payment, contentDescription = "Paystack", tint = Color.White, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("paystack", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp, letterSpacing = (-0.5).sp)
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (paystackConfig.isLiveMode) PaystackSuccessGreen else Color(0xFFFF9800)
                  ) {
                    Text(
                      text = if (paystackConfig.isLiveMode) "LIVE" else "TEST",
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                      style = MaterialTheme.typography.labelSmall,
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                }
                Text("Unified Multi-Rail Global Gateway", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB0BEC5), fontSize = 10.sp)
              }
            }

            IconButton(
              onClick = onDismiss,
              enabled = !isProcessing,
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Order Summary Box
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = PaystackLightCyan,
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PaystackCyan, PaystackDarkCyan))),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(tier.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PaystackDarkNavy)
              Text(
                if (isYearly) "Annual Plan (17% Savings Applied)" else "Monthly Subscription",
                style = MaterialTheme.typography.labelSmall,
                color = PaystackDarkCyan,
                fontWeight = FontWeight.Medium
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${paystackConfig.settlementCurrency} $${String.format("%,.2f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = PaystackDarkNavy
              )
              Text("Direct to Paystack Merchant", style = MaterialTheme.typography.labelSmall, color = PaystackSuccessGreen, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (completedTransaction != null) {
          // Success State View
          val tx = completedTransaction!!
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = VibrantSuccessContainer),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PaystackSuccessGreen, PaystackSuccessGreen))),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(PaystackSuccessGreen),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(36.dp))
              }

              Text("Payment Settled to Paystack!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantSuccessText)
              Text(
                "Your payment via ${tx.channel.title} was confirmed and routed directly to your Paystack merchant balance.",
                style = MaterialTheme.typography.bodySmall,
                color = VibrantSuccessText.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
              )

              HorizontalDivider(color = VibrantSuccess.copy(alpha = 0.3f))

              Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ReceiptRow("Paystack Reference", tx.reference)
                ReceiptRow("Subscription Tier", tx.tierTitle)
                ReceiptRow("Amount Paid", "${tx.currency} $${String.format("%,.2f", tx.amount)}")
                ReceiptRow("Payment Channel", tx.channel.title)
                ReceiptRow("Merchant Settlement", tx.paystackSettlementStatus)
                ReceiptRow("Paystack Auth Code", tx.authorizationCode)
                ReceiptRow("Settlement Rail", tx.bankOrIssuer)
                ReceiptRow("Customer Email", tx.customerEmail)
              }
            }
          }
        } else if (showOtpChallenge) {
          // 3D Secure / OTP Challenge Screen
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PaystackCyan, PaystackDarkNavy))),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.VerifiedUser, contentDescription = "3DS OTP", tint = PaystackCyan, modifier = Modifier.size(40.dp))
              Text("Bank Security Verification", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              Text(
                "Please enter the 6-digit one-time code sent to your mobile phone to authorize the Paystack transaction.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
              )

              OutlinedTextField(
                value = otpInput,
                onValueChange = { if (it.length <= 6) otpInput = it },
                label = { Text("6-Digit Bank Verification OTP") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
              )

              Button(
                onClick = {
                  isProcessing = true
                  processingMessage = "Settling to Paystack balance..."
                  coroutineScope.launch {
                    delay(1200)
                    isProcessing = false
                    showOtpChallenge = false
                    val tx = PaystackTransaction(
                      reference = "pstk_ref_${System.currentTimeMillis() % 100000000}",
                      tierTitle = tier.title,
                      billingCycle = if (isYearly) "Yearly" else "Monthly",
                      amount = amount,
                      currency = paystackConfig.settlementCurrency,
                      channel = selectedChannel,
                      customerEmail = customerEmail,
                      status = "Success",
                      paidAt = "Just now",
                      authorizationCode = "AUTH_pstk_${(100000..999999).random()}",
                      bankOrIssuer = when (selectedChannel) {
                        PaystackPaymentChannel.CARD -> "Verified by Visa / Mastercard 3DS"
                        PaystackPaymentChannel.MADA -> "Saudi Mada Interbank Switch"
                        else -> "Paystack Direct Settlement"
                      },
                      feesDeducted = (amount * 0.015).coerceAtLeast(0.5),
                      paystackSettlementStatus = "Settled to Paystack Merchant Balance (Instant)"
                    )
                    completedTransaction = tx
                    onPaymentSuccess(tx)
                  }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PaystackCyan),
                modifier = Modifier.fillMaxWidth()
              ) {
                if (isProcessing) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(processingMessage)
                } else {
                  Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Submit & Settle to Paystack", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        } else {
          // Standard Multi-Rail Checkout Channels View

          // Customer Email input
          OutlinedTextField(
            value = customerEmail,
            onValueChange = { customerEmail = it },
            label = { Text("Customer Email (For Paystack Receipt)") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PaystackCyan) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          // Paystack Multi-Rail Routing Notice
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Hub, contentDescription = null, tint = PaystackCyan, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                "All payments below automatically route & settle in your Paystack Merchant Account (${paystackConfig.settlementCurrency}).",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
              )
            }
          }

          Text("CHOOSE PAYMENT METHOD CATEGORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

          // Category Selector Scrollable Tabs
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            PaymentChannelCategory.values().forEach { cat ->
              val isSelected = selectedCategory == cat
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) PaystackDarkNavy else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    selectedCategory = cat
                    // Set default channel for this category
                    selectedChannel = PaystackPaymentChannel.values().first { it.category == cat }
                  }
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(
                    imageVector = when (cat) {
                      PaymentChannelCategory.CARDS_WALLETS -> Icons.Default.CreditCard
                      PaymentChannelCategory.US_GLOBAL -> Icons.Default.AccountBalanceWallet
                      PaymentChannelCategory.MENA_REGIONAL -> Icons.Default.Public
                      PaymentChannelCategory.GIFT_VOUCHERS -> Icons.Default.CardGiftcard
                      PaymentChannelCategory.DIRECT_RAILS -> Icons.Default.AccountBalance
                    },
                    contentDescription = cat.title,
                    tint = if (isSelected) PaystackCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = when (cat) {
                      PaymentChannelCategory.CARDS_WALLETS -> "Cards/Pay"
                      PaymentChannelCategory.US_GLOBAL -> "US/Wallets"
                      PaymentChannelCategory.MENA_REGIONAL -> "MENA"
                      PaymentChannelCategory.GIFT_VOUCHERS -> "Gift Card"
                      PaymentChannelCategory.DIRECT_RAILS -> "Direct"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                  )
                }
              }
            }
          }

          // Specific Channels under selected category
          Text("SELECT RAIL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

          val availableChannels = PaystackPaymentChannel.values().filter { it.category == selectedCategory }

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            availableChannels.forEach { channel ->
              val isSelected = selectedChannel == channel
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) PaystackLightCyan else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PaystackCyan, PaystackDarkCyan))) else null,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedChannel = channel }
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                          when (channel) {
                            PaystackPaymentChannel.CARD -> PaystackDarkNavy
                            PaystackPaymentChannel.GOOGLE_PAY -> BrandGPay
                            PaystackPaymentChannel.APPLE_PAY -> BrandApplePay
                            PaystackPaymentChannel.STRIPE -> BrandStripe
                            PaystackPaymentChannel.PAYPAL -> BrandPayPal
                            PaystackPaymentChannel.VENMO -> BrandVenmo
                            PaystackPaymentChannel.ZELLE -> BrandZelle
                            PaystackPaymentChannel.MADA -> BrandMada
                            PaystackPaymentChannel.FAWRY -> BrandFawry
                            PaystackPaymentChannel.BENEFIT_PAY -> BrandBenefit
                            PaystackPaymentChannel.KNET -> BrandKnet
                            PaystackPaymentChannel.QPAY_NAPS -> Color(0xFF6B1736)
                            PaystackPaymentChannel.STC_PAY -> Color(0xFF4F008C)
                            PaystackPaymentChannel.TABBY_TAMARA -> BrandTamara
                            PaystackPaymentChannel.GIFT_CARD -> BrandGiftCard
                            else -> PaystackDarkNavy
                          }
                        ),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = when (channel) {
                          PaystackPaymentChannel.CARD -> Icons.Default.CreditCard
                          PaystackPaymentChannel.GOOGLE_PAY -> Icons.Default.Android
                          PaystackPaymentChannel.APPLE_PAY -> Icons.Default.PhoneIphone
                          PaystackPaymentChannel.STRIPE -> Icons.Default.FlashOn
                          PaystackPaymentChannel.PAYPAL -> Icons.Default.Payment
                          PaystackPaymentChannel.VENMO -> Icons.Default.Send
                          PaystackPaymentChannel.ZELLE -> Icons.Default.Speed
                          PaystackPaymentChannel.MADA -> Icons.Default.CreditCard
                          PaystackPaymentChannel.FAWRY -> Icons.Default.Storefront
                          PaystackPaymentChannel.BENEFIT_PAY -> Icons.Default.QrCode
                          PaystackPaymentChannel.KNET -> Icons.Default.AccountBalance
                          PaystackPaymentChannel.QPAY_NAPS -> Icons.Default.CreditCard
                          PaystackPaymentChannel.STC_PAY -> Icons.Default.Smartphone
                          PaystackPaymentChannel.TABBY_TAMARA -> Icons.Default.ViewTimeline
                          PaystackPaymentChannel.GIFT_CARD -> Icons.Default.CardGiftcard
                          PaystackPaymentChannel.BANK_TRANSFER -> Icons.Default.AccountBalance
                          PaystackPaymentChannel.USSD -> Icons.Default.PhoneAndroid
                          PaystackPaymentChannel.MOBILE_MONEY -> Icons.Default.PhonelinkRing
                          PaystackPaymentChannel.QR_CODE -> Icons.Default.QrCode2
                        },
                        contentDescription = channel.title,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(channel.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                      Text(channel.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    }
                  }

                  if (channel.badge.isNotEmpty()) {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = if (isSelected) PaystackCyan else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                      Text(
                        text = channel.badge,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }
              }
            }
          }

          // Channel Specific Views
          when (selectedChannel) {
            PaystackPaymentChannel.CARD -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = cardHolderName,
                  onValueChange = { cardHolderName = it },
                  label = { Text("Cardholder Name") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                  value = cardNumber,
                  onValueChange = { cardNumber = it },
                  label = { Text("Card Number (Visa / Mastercard / Amex / Verve)") },
                  leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = PaystackCyan) },
                  trailingIcon = {
                    Surface(shape = RoundedCornerShape(4.dp), color = PaystackDarkNavy) {
                      Text("3DS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                  },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  OutlinedTextField(
                    value = cardExpiry,
                    onValueChange = { cardExpiry = it },
                    label = { Text("Valid Thru") },
                    placeholder = { Text("MM/YY") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                  )
                  OutlinedTextField(
                    value = cardCvv,
                    onValueChange = { cardCvv = it },
                    label = { Text("CVV") },
                    placeholder = { Text("123") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                  )
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.clickable { saveCardForRenewals = !saveCardForRenewals }
                ) {
                  Checkbox(checked = saveCardForRenewals, onCheckedChange = { saveCardForRenewals = it })
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Securely tokenize for recurring subscriptions via Paystack", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }

            PaystackPaymentChannel.GOOGLE_PAY -> {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = BrandGPay,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF4285F4), modifier = Modifier.size(22.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Google Pay", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF333333)) {
                      Text("Tokenized", color = Color(0xFF34A853), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                  }
                  Text("Pay instantly with Google Wallet linked card • Biometric fingerprint auth", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCCCCCC))
                  Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF2B2B2B), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(8.dp))
                      Text("Chase Visa •••• 4242 ($customerEmail)", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                  }
                }
              }
            }

            PaystackPaymentChannel.APPLE_PAY -> {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = BrandApplePay,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Apple Pay", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF222222)) {
                      Text("Face ID Ready", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                  }
                  Text("Double-click side button & confirm with Face ID / Touch ID securely.", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBBBBBB))
                }
              }
            }

            PaystackPaymentChannel.STRIPE -> {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandStripe, BrandStripe)))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = BrandStripe, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stripe Direct & Link 1-Click", fontWeight = FontWeight.Bold, color = BrandStripe)
                  }
                  Text("Vaulted 1-Click checkout automatically bridged and settled into Paystack.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }

            PaystackPaymentChannel.PAYPAL -> {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF5F9FF),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandPayPal, Color(0xFF0079C1))))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = BrandPayPal, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PayPal Express Checkout", fontWeight = FontWeight.Bold, color = BrandPayPal, fontSize = 15.sp)
                  }
                  Text("Pay with PayPal Balance, linked Bank Account, or PayPal Credit.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2C3E50))
                  Surface(shape = RoundedCornerShape(8.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PaystackSuccessGreen, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("PayPal Buyer Protection Guaranteed", style = MaterialTheme.typography.labelSmall, color = Color(0xFF003087), fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }

            PaystackPaymentChannel.VENMO -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = venmoHandle,
                  onValueChange = { venmoHandle = it },
                  label = { Text("Venmo Username / Phone") },
                  leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, tint = BrandVenmo) },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
                Text("Tap 'Pay with Paystack' to confirm instant authorization from your Venmo balance.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }

            PaystackPaymentChannel.ZELLE -> {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF9F4FC),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandZelle, BrandZelle)))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = BrandZelle, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Zelle Direct Interbank Settlement", fontWeight = FontWeight.Bold, color = BrandZelle)
                  }
                  Text("Send via your US Mobile Banking App (Chase, BoA, Wells Fargo, Citi) with Zero Fees:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                  Surface(shape = RoundedCornerShape(10.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                      Column {
                        Text("Zelle Pay Recipient:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("payments@payflow.ai", fontWeight = FontWeight.Bold, color = BrandZelle, fontSize = 14.sp)
                      }
                      IconButton(onClick = { clipboardManager.setText(AnnotatedString("payments@payflow.ai")) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BrandZelle)
                      }
                    }
                  }
                }
              }
            }

            PaystackPaymentChannel.MADA -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color(0xFFE8F5E9),
                  border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandMada, BrandMada)))
                ) {
                  Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = BrandMada, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saudi National Payment Network (مدى Mada)", fontWeight = FontWeight.Bold, color = BrandMada, fontSize = 13.sp)
                  }
                }
                OutlinedTextField(
                  value = madaCardNumber,
                  onValueChange = { madaCardNumber = it },
                  label = { Text("Mada Debit Card Number") },
                  leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = BrandMada) },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }

            PaystackPaymentChannel.FAWRY -> {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF9C4),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandFawry, BrandFawry)))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Fawry Pay Egypt (فوري)", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
                  }
                  Text("Pay in Cash at 160,000+ Fawry retail kiosks or via myFawry wallet using Reference:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5D4037))

                  Surface(shape = RoundedCornerShape(10.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                      Column {
                        Text("Fawry Payment Reference Code:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(fawryRefNumber, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFFE65100))
                      }
                      IconButton(onClick = { clipboardManager.setText(AnnotatedString(fawryRefNumber.replace(" ", ""))) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFFE65100))
                      }
                    }
                  }
                }
              }
            }

            PaystackPaymentChannel.BENEFIT_PAY -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color(0xFFFFEBEE),
                  border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandBenefit, BrandBenefit)))
                ) {
                  Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = BrandBenefit, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bahrain BenefitPay (بنفت باي)", fontWeight = FontWeight.Bold, color = BrandBenefit)
                  }
                }
                OutlinedTextField(
                  value = benefitPhone,
                  onValueChange = { benefitPhone = it },
                  label = { Text("BenefitPay Registered Mobile") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }

            PaystackPaymentChannel.KNET -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color(0xFFE3F2FD),
                  border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandKnet, BrandKnet)))
                ) {
                  Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = BrandKnet, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kuwait Shared Electronic Banking (KNET كي نت)", fontWeight = FontWeight.Bold, color = BrandKnet)
                  }
                }
                OutlinedTextField(
                  value = knetCivilId,
                  onValueChange = { knetCivilId = it },
                  label = { Text("Kuwait Civil ID / Card Prefix") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }

            PaystackPaymentChannel.QPAY_NAPS -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Qatar National ATM & Debit Card Switch (QPay / NAPS)", fontWeight = FontWeight.Bold, color = Color(0xFF6B1736))
                Text("Supports all Qatari debit cards issued by QNB, CBQ, QIB, and Dukhan Bank.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }

            PaystackPaymentChannel.STC_PAY -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = stcPayPhone,
                  onValueChange = { stcPayPhone = it },
                  label = { Text("STC Pay Wallet Mobile Number") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
                Text("You will receive an instant approval push notification on your STC Pay app.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }

            PaystackPaymentChannel.TABBY_TAMARA -> {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF3E0),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandTamara, BrandTamara)))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ViewTimeline, contentDescription = null, tint = BrandTamara, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tamara & Tabby (Split in 4)", fontWeight = FontWeight.Bold, color = BrandTamara)
                  }
                  Text("Zero interest • Zero hidden fees. Pay in 4 monthly installments:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)

                  val installmentAmt = (amount / 4.0)
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..4).forEach { num ->
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (num == 1) BrandTamara else Color.White,
                        modifier = Modifier.weight(1f)
                      ) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                          Text(if (num == 1) "Today" else "Month $num", fontSize = 9.sp, color = if (num == 1) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                          Text("$${String.format("%.1f", installmentAmt)}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (num == 1) Color.White else Color.Black)
                        }
                      }
                    }
                  }
                }
              }
            }

            PaystackPaymentChannel.GIFT_CARD -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color(0xFFF3E5F5),
                  border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandGiftCard, BrandGiftCard)))
                ) {
                  Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = BrandGiftCard, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Redeem Gift Card or Enterprise Voucher", fontWeight = FontWeight.Bold, color = BrandGiftCard)
                  }
                }

                OutlinedTextField(
                  value = giftCardCode,
                  onValueChange = { giftCardCode = it },
                  label = { Text("Gift Card / Voucher Code") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  OutlinedTextField(
                    value = giftCardPin,
                    onValueChange = { giftCardPin = it },
                    label = { Text("4-Digit PIN") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                  )
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VibrantSuccessContainer,
                    modifier = Modifier.weight(1f)
                  ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                      Text("Available Balance", style = MaterialTheme.typography.labelSmall, color = VibrantSuccessText, fontSize = 9.sp)
                      Text("$2,500.00", fontWeight = FontWeight.Black, color = VibrantSuccessText, fontSize = 14.sp)
                    }
                  }
                }
              }
            }

            PaystackPaymentChannel.BANK_TRANSFER -> {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PaystackCyan, PaystackDarkCyan)))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Paystack Virtual Bank Account", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PaystackDarkCyan)
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFE082)) {
                      val mins = countdownSeconds / 60
                      val secs = countdownSeconds % 60
                      Text(
                        text = "⏱ ${String.format("%02d:%02d", mins, secs)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }

                  Text("Transfer exactly the subscription amount to this dedicated virtual account:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column {
                        Text("Bank Name: Titan Trust / Wema Bank", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("9948 1029 38", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 20.sp, color = PaystackDarkNavy)
                        Text("Beneficiary: Paystack / PayFlow Tech", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                      IconButton(
                        onClick = {
                          clipboardManager.setText(AnnotatedString("9948102938"))
                        }
                      ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PaystackCyan)
                      }
                    }
                  }
                }
              }
            }

            PaystackPaymentChannel.USSD -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select your mobile banking institution:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val banks = listOf("GTBank (*737#)", "Zenith Bank (*966#)", "Access Bank (*901#)", "UBA (*919#)", "First Bank (*894#)")
                banks.forEach { bank ->
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedBank == bank) PaystackLightCyan else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = if (selectedBank == bank) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PaystackCyan, PaystackCyan))) else null,
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { selectedBank = bank }
                  ) {
                    Row(
                      modifier = Modifier.padding(10.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(bank, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PaystackDarkNavy)
                      if (selectedBank == bank) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PaystackCyan, modifier = Modifier.size(16.dp))
                      }
                    }
                  }
                }

                val ussdCode = when {
                  selectedBank.contains("737") -> "*737*50*${amount.toInt()}*9481#"
                  selectedBank.contains("966") -> "*966*6*${amount.toInt()}*9481#"
                  selectedBank.contains("901") -> "*901*00*${amount.toInt()}*9481#"
                  else -> "*919*${amount.toInt()}*9481#"
                }

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = PaystackDarkNavy,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text("Dial on your phone:", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB0BEC5))
                      Text(ussdCode, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F), fontSize = 16.sp)
                    }
                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(ussdCode)) }) {
                      Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                    }
                  }
                }
              }
            }

            PaystackPaymentChannel.MOBILE_MONEY -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = momoPhone,
                  onValueChange = { momoPhone = it },
                  label = { Text("Mobile Money Phone Number") },
                  leadingIcon = { Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = PaystackCyan) },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
                Text("An instant STK push prompt will be triggered on your mobile handset to confirm payment.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }

            PaystackPaymentChannel.QR_CODE -> {
              Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Surface(
                  shape = RoundedCornerShape(16.dp),
                  color = Color.White,
                  border = CardDefaults.outlinedCardBorder(),
                  modifier = Modifier.size(160.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.QrCode2, contentDescription = "QR", modifier = Modifier.size(140.dp), tint = PaystackDarkNavy)
                  }
                }
                Text("Scan using your bank's mobile app or Visa QR to complete payment.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      if (completedTransaction != null) {
        Button(
          onClick = onDismiss,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PaystackSuccessGreen),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Done (Tier Activated)", fontWeight = FontWeight.Bold)
        }
      } else if (!showOtpChallenge) {
        Button(
          onClick = {
            if (selectedChannel == PaystackPaymentChannel.CARD || selectedChannel == PaystackPaymentChannel.MADA) {
              showOtpChallenge = true
            } else {
              isProcessing = true
              processingMessage = when (selectedChannel) {
                PaystackPaymentChannel.GOOGLE_PAY -> "Authorizing Google Pay Wallet..."
                PaystackPaymentChannel.APPLE_PAY -> "Verifying Apple Pay Face ID..."
                PaystackPaymentChannel.STRIPE -> "Authorizing Stripe Vault..."
                PaystackPaymentChannel.PAYPAL -> "Authorizing PayPal Express..."
                PaystackPaymentChannel.VENMO -> "Verifying Venmo Rail..."
                PaystackPaymentChannel.ZELLE -> "Verifying Zelle Instant Clearing..."
                PaystackPaymentChannel.FAWRY -> "Generating Fawry Reference..."
                PaystackPaymentChannel.BENEFIT_PAY -> "Connecting to BenefitPay..."
                PaystackPaymentChannel.KNET -> "Authorizing KNET Switch..."
                PaystackPaymentChannel.QPAY_NAPS -> "Verifying QPay Qatar..."
                PaystackPaymentChannel.STC_PAY -> "Sending STC Pay Prompt..."
                PaystackPaymentChannel.TABBY_TAMARA -> "Setting up Tamara Installments..."
                PaystackPaymentChannel.GIFT_CARD -> "Redeeming Voucher Balance..."
                else -> "Settling to Paystack Balance..."
              }
              coroutineScope.launch {
                delay(1500)
                isProcessing = false
                val tx = PaystackTransaction(
                  reference = "pstk_ref_${System.currentTimeMillis() % 100000000}",
                  tierTitle = tier.title,
                  billingCycle = if (isYearly) "Yearly" else "Monthly",
                  amount = amount,
                  currency = paystackConfig.settlementCurrency,
                  channel = selectedChannel,
                  customerEmail = customerEmail,
                  status = "Success",
                  paidAt = "Just now",
                  authorizationCode = "AUTH_pstk_${(100000..999999).random()}",
                  bankOrIssuer = when (selectedChannel) {
                    PaystackPaymentChannel.CARD -> "Mastercard / Visa 3DS Verified"
                    PaystackPaymentChannel.GOOGLE_PAY -> "Google Pay (Tokenized Chase Visa ****4242)"
                    PaystackPaymentChannel.APPLE_PAY -> "Apple Pay (Device Secure Element)"
                    PaystackPaymentChannel.STRIPE -> "Stripe Direct / Link Vault"
                    PaystackPaymentChannel.PAYPAL -> "PayPal Express Settlement"
                    PaystackPaymentChannel.VENMO -> "Venmo Instant Clearing"
                    PaystackPaymentChannel.ZELLE -> "Zelle Interbank Network (FedNow/ACH)"
                    PaystackPaymentChannel.MADA -> "Saudi Mada Debit Switch (SAMA)"
                    PaystackPaymentChannel.FAWRY -> "Fawry Banking & POS Network Egypt"
                    PaystackPaymentChannel.BENEFIT_PAY -> "BenefitPay EFTS Bahrain"
                    PaystackPaymentChannel.KNET -> "KNET Interbank Switch Kuwait"
                    PaystackPaymentChannel.QPAY_NAPS -> "QPay National Switch Qatar"
                    PaystackPaymentChannel.STC_PAY -> "STC Pay / SADAD Saudi Arabia"
                    PaystackPaymentChannel.TABBY_TAMARA -> "Tamara / Tabby 4x Installments"
                    PaystackPaymentChannel.GIFT_CARD -> "Enterprise Prepaid Gift Vault"
                    PaystackPaymentChannel.BANK_TRANSFER -> "Titan Virtual Bank Account"
                    PaystackPaymentChannel.USSD -> selectedBank
                    PaystackPaymentChannel.MOBILE_MONEY -> momoProvider
                    PaystackPaymentChannel.QR_CODE -> "Visa / EMVCo QR"
                  },
                  feesDeducted = (amount * 0.015).coerceAtLeast(0.5),
                  paystackSettlementStatus = "Settled to Paystack Merchant Balance (Instant)",
                  regionalMetadata = "${selectedChannel.category.title} -> Paystack Account Rail"
                )
                completedTransaction = tx
                onPaymentSuccess(tx)
              }
            }
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = when (selectedChannel) {
              PaystackPaymentChannel.GOOGLE_PAY -> BrandGPay
              PaystackPaymentChannel.APPLE_PAY -> BrandApplePay
              PaystackPaymentChannel.STRIPE -> BrandStripe
              PaystackPaymentChannel.PAYPAL -> BrandPayPal
              PaystackPaymentChannel.VENMO -> BrandVenmo
              PaystackPaymentChannel.ZELLE -> BrandZelle
              PaystackPaymentChannel.MADA -> BrandMada
              PaystackPaymentChannel.FAWRY -> Color(0xFFF57F17)
              PaystackPaymentChannel.BENEFIT_PAY -> BrandBenefit
              PaystackPaymentChannel.KNET -> BrandKnet
              PaystackPaymentChannel.TABBY_TAMARA -> BrandTamara
              PaystackPaymentChannel.GIFT_CARD -> BrandGiftCard
              else -> PaystackCyan
            }
          ),
          enabled = !isProcessing,
          modifier = Modifier.fillMaxWidth()
        ) {
          if (isProcessing) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(processingMessage)
          } else {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              "Pay ${paystackConfig.settlementCurrency} $${String.format("%,.2f", amount)} with ${selectedChannel.title}",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    },
    dismissButton = {
      if (completedTransaction == null && !isProcessing) {
        TextButton(onClick = onDismiss) {
          Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  )
}

@Composable
fun ReceiptRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = VibrantSuccessText.copy(alpha = 0.8f))
    Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSuccessText)
  }
}
