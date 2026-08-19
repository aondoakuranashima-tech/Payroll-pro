package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  viewModel: PayrollViewModel,
  onNavigateToCalculator: () -> Unit,
  onNavigateToPayrollRuns: () -> Unit,
  onNavigateToEmployees: () -> Unit,
  onNavigateToExpenses: () -> Unit,
  onNavigateToAnalytics: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onNavigateToAiCopilot: () -> Unit = {}
) {
  val employees by viewModel.employees.collectAsState()
  val payrollRuns by viewModel.payrollRuns.collectAsState()
  val expenses by viewModel.expenses.collectAsState()
  val companyProfile by viewModel.companyProfile.collectAsState()
  val activeCurrency by viewModel.activeCurrency.collectAsState()
  val taxSettings by viewModel.taxSettings.collectAsState()
  val paystackConfig by viewModel.paystackConfig.collectAsState()

  var showUpgradeModal by remember { mutableStateOf(false) }
  var showPaystackCheckout by remember { mutableStateOf(false) }
  var checkoutTier by remember { mutableStateOf(companyProfile.selectedTier) }
  var checkoutIsYearly by remember { mutableStateOf(false) }

  var showScanReceiptModal by remember { mutableStateOf(false) }
  var showQuickBooksModal by remember { mutableStateOf(false) }
  var showOnboardingModal by remember { mutableStateOf(false) }
  var showVideoTutorialsModal by remember { mutableStateOf(false) }
  var showExportReportModal by remember { mutableStateOf(false) }
  var showCurrencyModal by remember { mutableStateOf(false) }

  val latestRun = payrollRuns.firstOrNull()
  val totalGrossPayroll = payrollRuns.sumOf { it.totalGross }
  val totalNetPayroll = payrollRuns.sumOf { it.totalNet }
  val totalEmployerTaxes = payrollRuns.sumOf { it.totalEmployerTaxes }
  val totalExpensesAmount = expenses.sumOf { it.amount }
  val totalMonthlyExpenditure = totalGrossPayroll + totalEmployerTaxes + totalExpensesAmount
  val anomalies = remember(employees, payrollRuns) { viewModel.getAnomalies() }

  val currentJurCode = taxSettings?.activeJurisdictionCode ?: "USA"
  val activeJurisdiction = GlobalJurisdiction.entries.find { it.countryCode == currentJurCode } ?: GlobalJurisdiction.USA

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(bottom = 96.dp, top = 12.dp, start = 16.dp, end = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 0. Top Enterprise Status Bar & Jurisdiction Nexus
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = companyProfile.companyName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${activeJurisdiction.flagEmoji} ${activeJurisdiction.countryName} Nexus",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(VibrantSuccess)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Live Sync Active",
              style = MaterialTheme.typography.labelSmall,
              color = VibrantSuccessText,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.clickable { showCurrencyModal = true }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CurrencyExchange, contentDescription = "Switch Global Currency", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "$activeCurrency (${viewModel.getCurrencySymbol()})",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }

    // 1. Setup Assistant (if onboarding is incomplete)
    if (!companyProfile.isOnboardingCompleted) {
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = VibrantPrimaryContainer),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(VibrantPrimary, VibrantSecondary))
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(VibrantPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text("Enterprise Setup Incomplete", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                Text("Configure EIN, QuickBooks sync & tax nexus in 4 quick steps.", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
              }
            }
            Button(
              onClick = { showOnboardingModal = true },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text("Start Wizard", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 2. Current Payroll Cycle Summary Card (PRIMARY REQUIREMENT)
    item {
      Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(VibrantOutline, VibrantSecondaryContainer))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("current_payroll_cycle_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Header Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Autorenew, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "CURRENT PAYROLL CYCLE",
                  style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                  fontWeight = FontWeight.Bold,
                  color = VibrantSecondary.copy(alpha = 0.9f)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = latestRun?.title ?: "Bi-Weekly Standard Cycle",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = VibrantTextNavy
              )
              Text(
                text = "Period: ${latestRun?.periodStart ?: "Aug 16, 2026"} – ${latestRun?.periodEnd ?: "Aug 31, 2026"}",
                style = MaterialTheme.typography.bodySmall,
                color = VibrantSecondary
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = VibrantSuccessContainer,
              border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(VibrantSuccess, VibrantSuccess)))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(VibrantSuccessText)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = latestRun?.status ?: "Ready to Disburse",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = VibrantSuccessText
                )
              }
            }
          }

          // Cycle Progress Bar (Day 11 of 14)
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Cycle Timeline: Day 12 of 14",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = VibrantSecondary
              )
              Text(
                text = "Disbursement in 2 days",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = VibrantPrimary
              )
            }
            LinearProgressIndicator(
              progress = { 0.85f },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = VibrantPrimary,
              trackColor = VibrantSecondary.copy(alpha = 0.2f)
            )
          }

          HorizontalDivider(color = VibrantOutline.copy(alpha = 0.4f))

          // 3-Column Metrics
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Active Headcount", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
              Text(
                text = "${employees.size} Employees",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VibrantTextPrimary
              )
            }
            Column {
              Text("Gross Wages", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
              Text(
                text = viewModel.formatCurrency(latestRun?.totalGross ?: 17200.0),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VibrantTextPrimary
              )
            }
            Column {
              Text("Net Take-Home", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
              Text(
                text = viewModel.formatCurrency(latestRun?.totalNet ?: 12840.50),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VibrantPrimary
              )
            }
          }

          // Actions Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = onNavigateToPayrollRuns,
              modifier = Modifier
                .weight(1f)
                .testTag("hero_run_payroll_button"),
              shape = RoundedCornerShape(16.dp),
              colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Process Run", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = onNavigateToCalculator,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(16.dp)
            ) {
              Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Tax Calculator")
            }
          }
        }
      }
    }

    // 2.5 Live Global AI Tax Copilot & Market Analysis Quick Hub
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
          1.dp,
          Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFFC084FC)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToAiCopilot() }
          .testTag("dashboard_ai_copilot_banner")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  Brush.linearGradient(
                    listOf(Color(0xFF6366F1), Color(0xFF9333EA))
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Psychology,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Enterprise AI Tax & Market Copilot",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = VibrantTextNavy
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Color(0xFFEEF2FF)
                ) {
                  Text(
                    text = "Live",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4F46E5)
                  )
                }
              }
              Text(
                text = "Statutory compliance, VAT/VAS rules & 8-region labor index",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp
              )
            }
          }

          IconButton(
            onClick = onNavigateToAiCopilot,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Color(0xFFF3E8FF))
          ) {
            Icon(
              imageVector = Icons.Default.ArrowForward,
              contentDescription = "Open AI Copilot",
              tint = Color(0xFF9333EA),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    // 3. Upcoming Payment Dates & Schedule (PRIMARY REQUIREMENT)
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("upcoming_payment_dates_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "UPCOMING PAYMENT DATES",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                fontWeight = FontWeight.Bold,
                color = VibrantSecondary
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = "Next: Aug 31",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          Text(
            text = "Scheduled banking disbursements, tax remittance deadlines, and statutory pension deposits.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Payment Dates Timeline List
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PaymentDateTimelineItem(
              title = "Direct Deposit Banking Execution",
              date = "Aug 31, 2026",
              daysLeft = "in 2 days",
              amount = viewModel.formatCurrency(latestRun?.totalNet ?: 12840.50),
              rail = activeJurisdiction.clearingAndWpsFormat,
              badgeStatus = "Scheduled",
              badgeColor = VibrantPrimary,
              icon = Icons.Default.AccountBalance
            )

            PaymentDateTimelineItem(
              title = "Federal & State Tax Withholding Deposit",
              date = "Sep 15, 2026",
              daysLeft = "in 17 days",
              amount = viewModel.formatCurrency(totalEmployerTaxes + (latestRun?.totalEmployeeTaxes ?: 2120.0)),
              rail = "IRS EFTPS / Form 941 Remittance",
              badgeStatus = "Auto-Debit",
              badgeColor = VibrantSuccess,
              icon = Icons.Default.Gavel
            )

            PaymentDateTimelineItem(
              title = "${activeJurisdiction.pensionFundName} Deposit",
              date = "Sep 15, 2026",
              daysLeft = "in 17 days",
              amount = viewModel.formatCurrency((latestRun?.totalGross ?: 17200.0) * (activeJurisdiction.employerPensionRate / 100.0)),
              rail = "Statutory Pension Vault Match",
              badgeStatus = "Compliant",
              badgeColor = VibrantSecondary,
              icon = Icons.Default.Shield
            )

            PaymentDateTimelineItem(
              title = "Employee Expense Reimbursements",
              date = "Sep 05, 2026",
              daysLeft = "in 7 days",
              amount = viewModel.formatCurrency(totalExpensesAmount),
              rail = "Direct ACH Reimbursement",
              badgeStatus = "Approved",
              badgeColor = VibrantTertiary,
              icon = Icons.Default.Receipt
            )
          }
        }
      }
    }

    // 4. Total Monthly Expenditure Overview (WITH GLOBAL CURRENCY SWITCHER)
    item {
      Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("monthly_expenditure_overview_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Card Header with Currency Selector Trigger
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "TOTAL MONTHLY EXPENDITURE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                fontWeight = FontWeight.Bold,
                color = VibrantSecondary
              )
            }
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier
                .clickable { showCurrencyModal = true }
                .testTag("open_currency_selector_chip")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "$activeCurrency (${viewModel.getCurrencySymbol()})",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }

          // Quick Currency Switcher Carousel
          val quickCurrencies = remember {
            listOf(
              "USD" to "🇺🇸",
              "EUR" to "🇪🇺",
              "GBP" to "🇬🇧",
              "CAD" to "🇨🇦",
              "AUD" to "🇦🇺",
              "JPY" to "🇯🇵",
              "INR" to "🇮🇳",
              "BRL" to "🇧🇷",
              "SAR" to "🇸🇦",
              "AED" to "🇦🇪",
              "NGN" to "🇳🇬",
              "ZAR" to "🇿🇦",
              "KES" to "🇰🇪",
              "SGD" to "🇸🇬",
              "CHF" to "🇨🇭"
            )
          }

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(quickCurrencies) { (code, flag) ->
              val isSelected = activeCurrency.equals(code, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                  .clickable { viewModel.setActiveCurrency(code) }
                  .testTag("currency_chip_$code")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = flag, fontSize = 13.sp)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = code,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                  )
                  if (isSelected) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onPrimary,
                      modifier = Modifier.size(12.dp)
                    )
                  }
                }
              }
            }
            item {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                modifier = Modifier
                  .clickable { showCurrencyModal = true }
                  .testTag("all_currencies_chip")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "All Rates",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                  )
                }
              }
            }
          }

          // Real-Time Exchange Rate Banner
          val currentRate = viewModel.getExchangeRate()
          val currentSymbol = viewModel.getCurrencySymbol()
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (activeCurrency.equals("USD", ignoreCase = true)) {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                  imageVector = if (activeCurrency.equals("USD", ignoreCase = true)) Icons.Default.CheckCircle else Icons.Default.SwapHoriz,
                  contentDescription = null,
                  tint = if (activeCurrency.equals("USD", ignoreCase = true)) VibrantSuccessText else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (activeCurrency.equals("USD", ignoreCase = true)) {
                  Text(
                    text = "Base Currency (USD) • Real-time Federal Reserve Parity",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                } else {
                  Column {
                    Text(
                      text = "Real-Time Rate: 1 USD = $currentRate $currentSymbol ($activeCurrency)",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface,
                      fontSize = 11.sp
                    )
                    Text(
                      text = "Converted from \$${"%,.2f".format(totalMonthlyExpenditure)} USD base • Central Bank Feed",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 10.sp
                    )
                  }
                }
              }

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = VibrantSuccessContainer
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(5.dp)
                      .clip(CircleShape)
                      .background(VibrantSuccessText)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Live FX",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = VibrantSuccessText,
                    fontSize = 10.sp
                  )
                }
              }
            }
          }

          // Master Outflow Headline (Converted to active currency)
          Column {
            Text(
              text = viewModel.formatConvertedCurrency(totalMonthlyExpenditure, activeCurrency),
              style = MaterialTheme.typography.headlineLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Total Combined Outflow: Gross Wages + Employer Taxes & Match + Business Expenses",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Visual Categorical Distribution Bar
          val netRatio = if (totalMonthlyExpenditure > 0) (totalNetPayroll / totalMonthlyExpenditure).toFloat() else 0.6f
          val taxesRatio = if (totalMonthlyExpenditure > 0) ((totalEmployerTaxes + (latestRun?.totalEmployeeTaxes ?: 0.0)) / totalMonthlyExpenditure).toFloat() else 0.25f
          val expensesRatio = if (totalMonthlyExpenditure > 0) (totalExpensesAmount / totalMonthlyExpenditure).toFloat() else 0.15f

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
            ) {
              Box(
                modifier = Modifier
                  .weight(netRatio.coerceAtLeast(0.05f))
                  .fillMaxHeight()
                  .background(VibrantPrimary)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Box(
                modifier = Modifier
                  .weight(taxesRatio.coerceAtLeast(0.05f))
                  .fillMaxHeight()
                  .background(VibrantSecondary)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Box(
                modifier = Modifier
                  .weight(expensesRatio.coerceAtLeast(0.05f))
                  .fillMaxHeight()
                  .background(VibrantTertiary)
              )
            }

            // Legend Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VibrantPrimary))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Net Wages (${"%.0f".format(netRatio * 100)}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VibrantSecondary))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Taxes & Match (${"%.0f".format(taxesRatio * 100)}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VibrantTertiary))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Expenses (${"%.0f".format(expensesRatio * 100)}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          // 4-Quadrant Cost Breakdown Grid (With Converted Amounts)
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              ExpenditureStatMiniCard(
                title = "Gross Salaries",
                amount = viewModel.formatConvertedCurrency(totalGrossPayroll, activeCurrency),
                subtitle = "${employees.size} active headcount",
                icon = Icons.Default.Payments,
                color = VibrantPrimary,
                modifier = Modifier.weight(1f)
              )
              ExpenditureStatMiniCard(
                title = "Net Disbursed",
                amount = viewModel.formatConvertedCurrency(totalNetPayroll, activeCurrency),
                subtitle = "Direct banking transfers",
                icon = Icons.Default.AccountBalanceWallet,
                color = VibrantTertiary,
                modifier = Modifier.weight(1f)
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              ExpenditureStatMiniCard(
                title = "Employer Taxes",
                amount = viewModel.formatConvertedCurrency(totalEmployerTaxes, activeCurrency),
                subtitle = "FICA, statutory & match",
                icon = Icons.Default.AccountBalance,
                color = VibrantSecondary,
                modifier = Modifier.weight(1f)
              )
              ExpenditureStatMiniCard(
                title = "Ops & Expenses",
                amount = viewModel.formatConvertedCurrency(totalExpensesAmount, activeCurrency),
                subtitle = "${expenses.size} business receipts",
                icon = Icons.Default.ReceiptLong,
                color = Color(0xFFD97706),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }

    // 5. Role Switcher Pill Bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "ACTIVE ROLE: ${companyProfile.currentUserRole.roleName.uppercase()}",
          style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
          fontWeight = FontWeight.Bold,
          color = VibrantSecondary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          UserRole.values().take(3).forEach { role ->
            val isSelected = companyProfile.currentUserRole == role
            Surface(
              onClick = { viewModel.updateUserRole(role) },
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) VibrantPrimary else Color.Transparent,
              border = if (isSelected) null else CardDefaults.outlinedCardBorder()
            ) {
              Text(
                text = role.name.take(3),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }

    // 6. Quick Action Chips
    item {
      Text(
        text = "ENTERPRISE ACTIONS & TOOLS",
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
        fontWeight = FontWeight.Bold,
        color = VibrantSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item {
          ActionChipButton(
            icon = Icons.Default.Summarize,
            label = "Custom Reports",
            onClick = { showExportReportModal = true },
            containerColor = VibrantPrimaryContainer,
            contentColor = VibrantPrimary
          )
        }
        item {
          ActionChipButton(
            icon = Icons.Default.AutoAwesome,
            label = "AI Expense Sync",
            onClick = onNavigateToExpenses,
            containerColor = VibrantSuccessContainer,
            contentColor = VibrantSuccessText
          )
        }
        item {
          ActionChipButton(
            icon = Icons.Default.PlayLesson,
            label = "Video Masterclass",
            onClick = { showVideoTutorialsModal = true },
            containerColor = VibrantWarningContainer,
            contentColor = VibrantWarningTitle
          )
        }
        item {
          ActionChipButton(
            icon = Icons.Default.RocketLaunch,
            label = "Setup Wizard",
            onClick = { showOnboardingModal = true },
            containerColor = VibrantSecondaryContainer,
            contentColor = VibrantSecondary
          )
        }
        item {
          ActionChipButton(
            icon = Icons.Default.DocumentScanner,
            label = "Scan Receipt",
            onClick = { showScanReceiptModal = true },
            containerColor = VibrantPrimaryContainer,
            contentColor = VibrantPrimary
          )
        }
      }
    }

    // 7. Anomaly Alert Section
    if (anomalies.isNotEmpty()) {
      item {
        AnomalyAlertCard(alert = anomalies.first())
      }
    }

    // 8. Monetization Banner
    item {
      MonetizationBanner(
        tier = companyProfile.selectedTier,
        isDismissed = companyProfile.isAdBannerDismissed,
        onUpgradeClick = { showUpgradeModal = true },
        onDismissClick = { viewModel.dismissAdBanner() }
      )
    }

    // 9. Recent Payroll Cycles
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "RECENT PAYROLL CYCLES",
          style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
          fontWeight = FontWeight.Bold,
          color = VibrantSecondary
        )
        TextButton(onClick = onNavigateToPayrollRuns) {
          Text("Manage All Runs →", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantPrimary)
        }
      }
    }

    if (payrollRuns.isNotEmpty()) {
      items(payrollRuns.take(3)) { run ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              viewModel.selectPayrollRun(run)
              onNavigateToPayrollRuns()
            },
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.4f)))
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = run.title,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = VibrantTextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = VibrantSuccessContainer
                ) {
                  Text(
                    text = run.status,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = VibrantSuccessText,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${run.periodStart} – ${run.periodEnd} • ${run.employeeCount} Staff",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = viewModel.formatCurrency(run.totalNet),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VibrantTertiary
              )
              Text(
                text = "Gross: ${viewModel.formatCurrency(run.totalGross)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }

  // Modals
  if (showOnboardingModal) {
    OnboardingFlowModal(
      currentProfile = companyProfile,
      onComplete = { name, ein, state, qb, xero ->
        viewModel.completeOnboarding(name, ein, state, qb, xero)
        showOnboardingModal = false
      },
      onDismiss = { showOnboardingModal = false },
      onOpenVideoTutorials = {
        showOnboardingModal = false
        showVideoTutorialsModal = true
      }
    )
  }

  if (showVideoTutorialsModal) {
    VideoTutorialModal(
      tutorials = viewModel.getVideoTutorials(),
      onDismiss = { showVideoTutorialsModal = false }
    )
  }

  if (showExportReportModal) {
    CustomReportExportModal(
      viewModel = viewModel,
      onDismiss = { showExportReportModal = false }
    )
  }

  if (showUpgradeModal) {
    UpgradeSubscriptionModal(
      currentTier = companyProfile.selectedTier,
      onSelectTier = {
        viewModel.upgradeSubscription(it)
      },
      onPayWithPaystack = { tier, isYearly ->
        checkoutTier = tier
        checkoutIsYearly = isYearly
        showUpgradeModal = false
        showPaystackCheckout = true
      },
      onDismiss = { showUpgradeModal = false }
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

  if (showScanReceiptModal) {
    ScanReceiptModal(
      onDismiss = { showScanReceiptModal = false },
      onScanComplete = { vendor, amt ->
        viewModel.autoScanAndAddReceipt(vendor, amt)
        showScanReceiptModal = false
      }
    )
  }

  if (showQuickBooksModal && latestRun != null) {
    QuickBooksXeroModal(
      journalEntries = viewModel.getJournalEntries(latestRun),
      runTitle = latestRun.title,
      onDismiss = { showQuickBooksModal = false },
      onExport = { format ->
        viewModel.showNotification("Exported General Ledger to $format format successfully!")
        showQuickBooksModal = false
      }
    )
  }

  if (showCurrencyModal) {
    GlobalCurrencySwitcherModal(
      activeCurrency = activeCurrency,
      totalMonthlyExpenditureUsd = totalMonthlyExpenditure,
      currencies = viewModel.availableCurrencies,
      onSelectCurrency = { code ->
        viewModel.setActiveCurrency(code)
        showCurrencyModal = false
      },
      onDismiss = { showCurrencyModal = false }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalCurrencySwitcherModal(
  activeCurrency: String,
  totalMonthlyExpenditureUsd: Double,
  currencies: List<CurrencyInfo>,
  onSelectCurrency: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedContinent by remember { mutableStateOf("All") }

  val continents = remember {
    listOf("All", "North America", "Europe", "Asia & Pacific", "MENA", "Africa", "South America")
  }

  val filteredCurrencies = remember(searchQuery, selectedContinent, currencies) {
    currencies.filter { currency ->
      val matchesSearch = searchQuery.isBlank() ||
          currency.code.contains(searchQuery, ignoreCase = true) ||
          currency.name.contains(searchQuery, ignoreCase = true) ||
          currency.symbol.contains(searchQuery, ignoreCase = true) ||
          currency.continent.contains(searchQuery, ignoreCase = true)

      val matchesContinent = selectedContinent == "All" ||
          currency.continent.equals(selectedContinent, ignoreCase = true)

      matchesSearch && matchesContinent
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp)
      .testTag("global_currency_switcher_modal"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Global Currency Switcher",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Toggle dashboard expenditure view across live FX rates",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 480.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Search field
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search currency, country or symbol...", style = MaterialTheme.typography.bodySmall) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("currency_search_input")
        )

        // Continent Filter Pills
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(continents) { continent ->
            val isSelected = selectedContinent == continent
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
              modifier = Modifier.clickable { selectedContinent = continent }
            ) {
              Text(
                text = continent,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Currency Items List
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredCurrencies) { curr ->
            val isSelected = activeCurrency.equals(curr.code, ignoreCase = true)
            val convertedTotal = totalMonthlyExpenditureUsd * curr.rateToUsd

            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
              border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectCurrency(curr.code) }
                .testTag("currency_item_${curr.code}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Text(text = curr.flagEmoji, fontSize = 24.sp)
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = "${curr.code} (${curr.symbol})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                      ) {
                        Text(
                          text = curr.continent,
                          modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                          style = MaterialTheme.typography.labelSmall,
                          fontSize = 9.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }
                    Text(
                      text = curr.name,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 11.sp
                    )
                    Text(
                      text = "1 USD = ${curr.rateToUsd} ${curr.symbol}",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.primary,
                      fontSize = 10.sp
                    )
                  }
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "${curr.symbol}${"%,.2f".format(convertedTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Monthly Outflow",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                  )
                  if (isSelected) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = VibrantSuccessContainer
                    ) {
                      Text(
                        text = "ACTIVE",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VibrantSuccessText,
                        fontSize = 9.sp
                      )
                    }
                  }
                }
              }
            }
          }

          if (filteredCurrencies.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No currencies match \"$searchQuery\"",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        // Live Exchange Rate Feed Disclaimer
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Real-time feeds: Federal Reserve, ECB, Bank of England, SAMA, SARB, CBN & BoJ.",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

@Composable
fun PaymentDateTimelineItem(
  title: String,
  date: String,
  daysLeft: String,
  amount: String,
  rail: String,
  badgeStatus: String,
  badgeColor: Color,
  icon: ImageVector
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(badgeColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
          Text("$date ($daysLeft) • $rail", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = badgeColor.copy(alpha = 0.15f)
        ) {
          Text(
            text = badgeStatus,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = badgeColor,
            fontSize = 10.sp
          )
        }
      }
    }
  }
}

@Composable
fun ExpenditureStatMiniCard(
  title: String,
  amount: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
      }
      Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
    }
  }
}

