package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.PaystackCheckoutModal
import com.example.ui.components.UpgradeSubscriptionModal
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseSettingsScreen(
  viewModel: PayrollViewModel,
  modifier: Modifier = Modifier
) {
  val companyProfile by viewModel.companyProfile.collectAsState()
  val globalConfig by viewModel.globalConfig.collectAsState()
  val paystackConfig by viewModel.paystackConfig.collectAsState()
  val paystackTransactions by viewModel.paystackTransactions.collectAsState()
  val taxSettings by viewModel.taxSettings.collectAsState()
  val activeCurrency by viewModel.activeCurrency.collectAsState()

  val clipboardManager = LocalClipboardManager.current
  var showSubscriptionModal by remember { mutableStateOf(false) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }
  var showPaystackCheckout by remember { mutableStateOf(false) }
  var checkoutTier by remember { mutableStateOf(companyProfile.selectedTier) }
  var checkoutIsYearly by remember { mutableStateOf(false) }
  var selectedContinentFilter by remember { mutableStateOf<GlobalContinent?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("enterprise_settings_screen"),
    contentPadding = PaddingValues(bottom = 96.dp, top = 12.dp, start = 16.dp, end = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Subscription Tier & Monetization Center
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Diamond, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Text("Subscription & Pro Upgrades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (companyProfile.selectedTier.isPro) VibrantSuccessContainer else VibrantSecondaryContainer
            ) {
              Text(
                text = companyProfile.selectedTier.title,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (companyProfile.selectedTier.isPro) VibrantSuccessText else VibrantSecondary,
                fontWeight = FontWeight.Bold
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Active Plan: ${companyProfile.selectedTier.title} (${companyProfile.selectedTier.priceMonthly})",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Includes direct cloud accounting export, machine learning forecasting, and third-party developer API access.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { showSubscriptionModal = true },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Compare Plans", style = MaterialTheme.typography.labelSmall)
            }

            Button(
              onClick = {
                checkoutTier = companyProfile.selectedTier
                checkoutIsYearly = false
                showPaystackCheckout = true
              },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0BA4DB)),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Pay with Paystack", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 1.5. Dedicated Paystack Gateway & Settlements Hub
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(Color(0xFF0BA4DB), Color(0xFF0083B0)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF0BA4DB)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Paystack", tint = Color.White, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("PAYSTACK UNIFIED GLOBAL GATEWAY", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = Color(0xFF0083B0))
                Text("Cards, GPay, Apple Pay, PayPal, Zelle & MENA rails settle to Paystack", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (paystackConfig.isLiveMode) VibrantSuccessContainer else VibrantSecondaryContainer
            ) {
              Text(
                text = if (paystackConfig.isLiveMode) "LIVE HUB" else "TEST MODE",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (paystackConfig.isLiveMode) VibrantSuccessText else VibrantSecondary,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Total Settled Revenue Banner
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFE6F7FD),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF0BA4DB), Color(0xFF0083B0)))),
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
                Text("Total Settled Revenue (Paystack Balance)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF011B33), fontWeight = FontWeight.Medium)
                Text(
                  text = "${paystackConfig.settlementCurrency} $${String.format("%,.2f", paystackConfig.totalSettledRevenueUsd)}",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF011B33)
                )
              }
              Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF00C853)) {
                Text("Instant 0-FX", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              }
            }
          }

          Text(
            text = "Accept customer payments via Credit Cards, Google Pay, Apple Pay, Stripe, PayPal, Venmo, Zelle, MENA Gateways (Mada, Fawry, BenefitPay, KNET, STC Pay, Tamara), Gift Cards, and Direct Bank Transfers — all automatically routing to your Paystack merchant balance.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Live / Test Mode Switch & Currency Selector
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Paystack Environment Mode", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              Text(if (paystackConfig.isLiveMode) "Live Production Merchant Keys" else "Sandbox / Test Demo Keys", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = paystackConfig.isLiveMode,
              onCheckedChange = { viewModel.updatePaystackConfig(paystackConfig.copy(isLiveMode = it)) }
            )
          }

          // Settlement Currency Selector
          StandardSettingRow(
            title = "Merchant Settlement Currency",
            currentValue = paystackConfig.settlementCurrency,
            options = listOf("USD ($)", "NGN (₦)", "GHS (GH₵)", "ZAR (R)", "KES (KSh)", "AED (د.إ)", "SAR (﷼)"),
            onSelect = {
              val cur = it.take(3)
              viewModel.updatePaystackConfig(paystackConfig.copy(settlementCurrency = cur))
            }
          )

          // Public Key Display
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Paystack Public Key (${if (paystackConfig.isLiveMode) "Live" else "Test"})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = if (paystackConfig.isLiveMode) paystackConfig.publicKey else paystackConfig.testPublicKey,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0BA4DB)
                )
              }
              IconButton(
                onClick = {
                  val key = if (paystackConfig.isLiveMode) paystackConfig.publicKey else paystackConfig.testPublicKey
                  clipboardManager.setText(AnnotatedString(key))
                  viewModel.showNotification("Paystack Public Key copied to clipboard!")
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF0BA4DB), modifier = Modifier.size(16.dp))
              }
            }
          }

          // Webhook Endpoint
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Webhook Listener Endpoint (charge.success)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = paystackConfig.webhookEndpoint,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              IconButton(
                onClick = {
                  clipboardManager.setText(AnnotatedString(paystackConfig.webhookEndpoint))
                  viewModel.showNotification("Webhook endpoint copied to clipboard!")
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF0BA4DB), modifier = Modifier.size(16.dp))
              }
            }
          }

          // Recent Paystack Transactions Table
          Text("RECENT SETTLED PAYMENTS & SUBSCRIBER RECEIPTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

          paystackTransactions.take(5).forEach { tx ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tx.tierTitle, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = VibrantSuccessContainer) {
                      Text("Paystack Settled", style = MaterialTheme.typography.labelSmall, color = VibrantSuccessText, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                  }
                  Text("${tx.reference} • ${tx.paidAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                  Text("Auth: ${tx.authorizationCode} • ${tx.bankOrIssuer}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0BA4DB), fontSize = 10.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "${tx.currency} $${String.format("%,.2f", tx.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(tx.billingCycle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
              }
            }
          }
        }
      }
    }


    // 2. Comprehensive Enterprise Theme & Custom Color Palettes
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("THEME & COLOR CONFIGURATION", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = globalConfig.activeThemeMode.title,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Text(
            text = "Select your preferred visual mode: Light, Dark, Device default, or Custom corporate color palettes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Theme Modes Grid (Device, Light, Dark, Custom)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ThemeMode.values().forEach { mode ->
              val isSelected = globalConfig.activeThemeMode == mode
              val modeIcon = when (mode) {
                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                ThemeMode.LIGHT -> Icons.Default.LightMode
                ThemeMode.DARK -> Icons.Default.DarkMode
                ThemeMode.CUSTOM -> Icons.Default.ColorLens
              }

              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))) else null,
                modifier = Modifier
                  .weight(1f)
                  .clickable { viewModel.setThemeMode(mode) }
                  .testTag("theme_mode_${mode.name.lowercase()}")
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = modeIcon,
                    contentDescription = mode.title,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = mode.title.replace(" Theme", ""),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                }
              }
            }
          }

          // Custom Enterprise Color Themes Swatches
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "ENTERPRISE COLOR PALETTE",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.secondary
            )

            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              contentPadding = PaddingValues(vertical = 4.dp)
            ) {
              items(EnterpriseColorTheme.values()) { palette ->
                val isSelected = globalConfig.activeColorTheme == palette
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                  border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(palette.primaryHex), Color(palette.primaryHex)))) else null,
                  modifier = Modifier
                    .width(130.dp)
                    .clickable { viewModel.setColorTheme(palette) }
                    .testTag("palette_${palette.name.lowercase()}")
                ) {
                  Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Box(
                      modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(palette.primaryHex)),
                      contentAlignment = Alignment.Center
                    ) {
                      if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                      }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = palette.displayName,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1
                    )
                  }
                }
              }
            }

            // Description of active color palette
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(globalConfig.activeColorTheme.primaryHex))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "${globalConfig.activeColorTheme.displayName}: ${globalConfig.activeColorTheme.description}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          // Display Toggles (Dynamic Colors, High Contrast, High Density)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Dynamic Material You (Android 12+)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              Text("Sample wallpaper colors automatically", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = globalConfig.dynamicMonetColors,
              onCheckedChange = { viewModel.setDynamicMonetColors(it) }
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("High Contrast Accessibility", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              Text("Elevate text borders and visibility contours", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = globalConfig.highContrastMode,
              onCheckedChange = { viewModel.setHighContrast(it) }
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Compact Information Density", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              Text("Fit more financial ledgers & payroll entries", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = globalConfig.highInformationDensity,
              onCheckedChange = { viewModel.setHighInformationDensity(it) }
            )
          }
        }
      }
    }

    // 2.5. Global Sovereign Jurisdictions & Room Tax Database
    item {
      val currentJurCode = taxSettings?.activeJurisdictionCode ?: "USA"
      val activeJur = GlobalJurisdiction.entries.find { it.countryCode == currentJurCode } ?: GlobalJurisdiction.USA
      val filteredJurs = remember(selectedContinentFilter) {
        if (selectedContinentFilter == null) GlobalJurisdiction.entries
        else GlobalJurisdiction.entries.filter { it.continent == selectedContinentFilter }
      }

      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("GLOBAL SOVEREIGN TAX ENGINE (ROOM DB)", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = VibrantPrimaryContainer
            ) {
              Text(
                text = "${activeJur.flagEmoji} ${activeJur.countryCode}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = VibrantPrimary
              )
            }
          }

          Text(
            text = "Active Country: ${activeJur.countryName} (${activeJur.continent.displayName}). Local statutory laws: ${activeJur.statutoryLaws}.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Continent Tabs
          ScrollableTabRow(
            selectedTabIndex = if (selectedContinentFilter == null) 0 else GlobalContinent.entries.indexOf(selectedContinentFilter) + 1,
            edgePadding = 0.dp,
            divider = {},
            containerColor = MaterialTheme.colorScheme.surface
          ) {
            Tab(
              selected = selectedContinentFilter == null,
              onClick = { selectedContinentFilter = null },
              text = { Text("All Continents", style = MaterialTheme.typography.labelSmall, fontWeight = if (selectedContinentFilter == null) FontWeight.Bold else FontWeight.Normal) }
            )
            GlobalContinent.entries.forEach { continent ->
              Tab(
                selected = selectedContinentFilter == continent,
                onClick = { selectedContinentFilter = continent },
                text = { Text(continent.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = if (selectedContinentFilter == continent) FontWeight.Bold else FontWeight.Normal) }
              )
            }
          }

          // Horizontal scroll of jurisdictions
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
          ) {
            items(filteredJurs) { jur ->
              val isSelected = activeJur == jur
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))) else null,
                modifier = Modifier.clickable { viewModel.setGlobalJurisdiction(jur) }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(jur.flagEmoji, style = MaterialTheme.typography.bodyMedium)
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(jur.countryName, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                    Text("${jur.currencyCode} (${jur.currencySymbol}) • VAT: ${jur.standardVatGstPercent}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                  }
                }
              }
            }
          }

          // Active Jurisdiction Detail Summary
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pension / Retirement:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${activeJur.pensionFundName} (${activeJur.employeePensionRate}% / ${activeJur.employerPensionRate}%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Medical / Health Care:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${activeJur.healthInsuranceName} (${activeJur.employeeHealthRate}% / ${activeJur.employerHealthRate}%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bank Direct Clearing Format:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(activeJur.clearingAndWpsFormat, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
      }
    }

    // 3. Enterprise Payroll & Global Standards Configuration
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("ENTERPRISE STANDARDS & POLICIES", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = VibrantSuccessContainer
            ) {
              Text("GAAP Compliant", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = VibrantSuccessText, fontWeight = FontWeight.Bold)
            }
          }

          Text(
            text = "Configure global corporate parameters, tax nexus rules, and statutory accounting audit standards.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Fiscal Year Start Month
          StandardSettingRow(
            title = "Fiscal Year Start Month",
            currentValue = globalConfig.fiscalYearStartMonth,
            options = listOf("January", "April", "July", "October"),
            onSelect = { viewModel.updateGlobalConfiguration(globalConfig.copy(fiscalYearStartMonth = it)) }
          )

          // Default Payroll Cycle
          StandardSettingRow(
            title = "Standard Payroll Cycle",
            currentValue = globalConfig.defaultPayrollCycle,
            options = listOf("Bi-Weekly (Every other Friday)", "Semi-Monthly (15th & Last)", "Monthly", "Weekly"),
            onSelect = { viewModel.updateGlobalConfiguration(globalConfig.copy(defaultPayrollCycle = it)) }
          )

          // FLSA Overtime Multiplier
          StandardSettingRow(
            title = "FLSA Overtime Multiplier",
            currentValue = "${globalConfig.standardOvertimeMultiplier}x",
            options = listOf("1.5x", "2.0x"),
            onSelect = {
              val mult = if (it == "2.0x") 2.0 else 1.5
              viewModel.updateGlobalConfiguration(globalConfig.copy(standardOvertimeMultiplier = mult))
            }
          )

          // 401(k) Safe Harbor Match Limit
          StandardSettingRow(
            title = "401(k) Company Match Cap",
            currentValue = "${globalConfig.standard401kMatchLimitPercent.toInt()}%",
            options = listOf("3%", "4%", "5%", "6%"),
            onSelect = {
              val pct = it.replace("%", "").toDoubleOrNull() ?: 4.0
              viewModel.updateGlobalConfiguration(globalConfig.copy(standard401kMatchLimitPercent = pct))
            }
          )

          // Primary Tax Nexus
          StandardSettingRow(
            title = "Primary Tax Nexus Jurisdiction",
            currentValue = globalConfig.primaryAccountingNexusState,
            options = listOf("California (CA)", "New York (NY)", "Texas (TX)", "Washington (WA)", "Florida (FL)"),
            onSelect = { viewModel.updateGlobalConfiguration(globalConfig.copy(primaryAccountingNexusState = it)) }
          )

          // IRS / SEC Record Retention
          StandardSettingRow(
            title = "IRS / SEC Audit Retention Policy",
            currentValue = "${globalConfig.auditLogRetentionYears} Years Statutory",
            options = listOf("7 Years Statutory", "10 Years Enterprise", "Permanent Vault"),
            onSelect = {
              val yrs = if (it.startsWith("10")) 10 else if (it.startsWith("Permanent")) 99 else 7
              viewModel.updateGlobalConfiguration(globalConfig.copy(auditLogRetentionYears = yrs))
            }
          )

          Spacer(modifier = Modifier.height(4.dp))

          OutlinedButton(
            onClick = { showResetConfirmDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantError),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset Configuration to Standard Defaults", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }

    // 4. User Role & Team Collaboration Access
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ROLE PERMISSIONS & COLLABORATION", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
          }
          Text("Select active session access level to test role-based permission boundaries:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

          UserRole.values().forEach { role ->
            val isSelected = companyProfile.currentUserRole == role
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))) else null,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.updateUserRole(role) }
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(role.roleName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                  Text(role.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                RadioButton(
                  selected = isSelected,
                  onClick = { viewModel.updateUserRole(role) },
                  colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
              }
            }
          }
        }
      }
    }

    // 5. Enterprise Security & Authentication (MFA, Passkeys, Session Controls)
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("ENTERPRISE SECURITY & IDENTITY", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = VibrantSuccessContainer
            ) {
              Text("98/100 SOC 2", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = VibrantSuccessText, fontWeight = FontWeight.Bold)
            }
          }

          Text("Zero-Trust multi-factor authentication, hardware biometric sensors, and FIDO2 passkeys.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { viewModel.lockApp() },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Lock Shield", style = MaterialTheme.typography.labelSmall)
            }

            Button(
              onClick = { viewModel.logout() },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = VibrantError.copy(alpha = 0.9f)),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Sign Out", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 6. Developer RESTful API Hub & Third-Party Extensions
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("DEVELOPER REST API & INTEGRATIONS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = VibrantSuccessContainer
            ) {
              Text("REST v1", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = VibrantSuccessText, fontWeight = FontWeight.Bold)
            }
          }
          Text("Connect custom ERPs, internal HR software, or Google Apps Script via secure token-authenticated endpoints.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

          // Live API Key
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Production API Key (Live)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                IconButton(
                  onClick = { viewModel.regenerateApiKey(isLive = true) },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = companyProfile.liveApiKey,
                  fontFamily = FontFamily.Monospace,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                  onClick = {
                    clipboardManager.setText(AnnotatedString(companyProfile.liveApiKey))
                    viewModel.showNotification("Live API key copied to clipboard!")
                  },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
              }
            }
          }

          // Sample cURL command
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = VibrantDarkSurface
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("cURL Request Example", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                IconButton(
                  onClick = {
                    val curl = "curl -X POST https://api.payflow.ai/v1/calculate \\\n  -H \"Authorization: Bearer ${companyProfile.liveApiKey}\" \\\n  -d '{\"baseRate\": 120000, \"state\": \"CA\"}'"
                    clipboardManager.setText(AnnotatedString(curl))
                    viewModel.showNotification("cURL request snippet copied!")
                  },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp), tint = Color.White)
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "curl -X POST https://api.payflow.ai/v1/calculate \\\n  -H \"Authorization: Bearer ${companyProfile.liveApiKey}\" \\\n  -d '{\"baseRate\": 120000, \"state\": \"CA\"}'",
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFA5D6A7)
              )
            }
          }
        }
      }
    }

    // 7. Cloud Accounting & Backup Sync
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("CLOUD INTEGRATIONS & BACKUPS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
          }

          IntegrationRow(
            title = "QuickBooks Online Integration",
            subtitle = "Automatic double-entry wage & tax journal sync",
            status = "Connected",
            icon = Icons.Default.AccountBalance
          )
          IntegrationRow(
            title = "Xero Cloud Accounting",
            subtitle = "Direct bank feed & reconciliation mapping",
            status = "Connected",
            icon = Icons.Default.SyncAlt
          )
          IntegrationRow(
            title = "Google Drive Cloud Backup",
            subtitle = "Encrypted daily AES-256 snapshot archive",
            status = "Enabled",
            icon = Icons.Default.CloudQueue
          )
        }
      }
    }
  }

  if (showSubscriptionModal) {
    UpgradeSubscriptionModal(
      currentTier = companyProfile.selectedTier,
      onSelectTier = {
        viewModel.upgradeSubscription(it)
      },
      onPayWithPaystack = { tier, isYearly ->
        checkoutTier = tier
        checkoutIsYearly = isYearly
        showSubscriptionModal = false
        showPaystackCheckout = true
      },
      onDismiss = { showSubscriptionModal = false }
    )
  }

  if (showPaystackCheckout) {
    PaystackCheckoutModal(
      tier = checkoutTier,
      isYearly = checkoutIsYearly,
      paystackConfig = paystackConfig,
      onPaymentSuccess = { tx ->
        viewModel.recordPaystackPayment(
          tier = checkoutTier,
          isYearly = checkoutIsYearly,
          channel = tx.channel,
          customerEmail = tx.customerEmail
        )
      },
      onDismiss = { showPaystackCheckout = false }
    )
  }

  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = VibrantError) },
      title = { Text("Reset Enterprise Configuration?") },
      text = { Text("This will restore default visual theme, corporate payroll parameters, fiscal schedules, and retention policies to factory enterprise defaults.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetConfigurationToDefaults()
            showResetConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = VibrantError)
        ) {
          Text("Reset Defaults")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun StandardSettingRow(
  title: String,
  currentValue: String,
  options: List<String>,
  onSelect: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
          Text(currentValue, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        TextButton(
          onClick = { expanded = !expanded },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(if (expanded) "Close" else "Change", style = MaterialTheme.typography.labelSmall)
          Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
        }
      }

      AnimatedVisibility(visible = expanded) {
        Column(
          modifier = Modifier.padding(top = 8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          options.forEach { opt ->
            val isSelected = opt == currentValue
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSelect(opt)
                  expanded = false
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = opt,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (isSelected) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun IntegrationRow(
  title: String,
  subtitle: String,
  status: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = VibrantSuccessContainer
    ) {
      Text(
        text = status,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = VibrantSuccessText,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

