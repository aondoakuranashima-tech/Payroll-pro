package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.PayStubSummaryCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
  viewModel: PayrollViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.calculatorState.collectAsState()
  val taxSettings by viewModel.taxSettings.collectAsState()
  val activeCurrency by viewModel.activeCurrency.collectAsState()
  val clipboardManager = LocalClipboardManager.current

  var selectedContinentFilter by remember { mutableStateOf<GlobalContinent?>(null) }
  var showContinentPicker by remember { mutableStateOf(false) }

  val currentJurisdiction = state.selectedJurisdiction
  val currencySymbol = currentJurisdiction.currencySymbol

  val filteredJurisdictions = remember(selectedContinentFilter) {
    if (selectedContinentFilter == null) {
      GlobalJurisdiction.entries
    } else {
      GlobalJurisdiction.entries.filter { it.continent == selectedContinentFilter }
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("calculator_screen"),
    contentPadding = PaddingValues(bottom = 96.dp, top = 12.dp, start = 16.dp, end = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header Banner with Global Jurisdiction Overview
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VibrantPrimaryContainer),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(VibrantOutline, VibrantSecondaryContainer))
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
              Icon(Icons.Default.Public, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Global Payroll & Tax Simulator",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = VibrantOnPrimaryContainer
                )
                Text(
                  text = "${currentJurisdiction.countryName} • ${currentJurisdiction.continent.displayName}",
                  style = MaterialTheme.typography.labelSmall,
                  color = VibrantPrimary,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = VibrantPrimary.copy(alpha = 0.15f)
            ) {
              Text(
                text = currentJurisdiction.currencyCode,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = VibrantPrimary
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Compliant with ${currentJurisdiction.countryName} statutory tax laws, ${currentJurisdiction.pensionFundName} (${currentJurisdiction.employeePensionRate}% / ${currentJurisdiction.employerPensionRate}%), ${currentJurisdiction.healthInsuranceName}, and ${currentJurisdiction.clearingAndWpsFormat} clearing.",
            style = MaterialTheme.typography.bodySmall,
            color = VibrantSecondary.copy(alpha = 0.9f)
          )
        }
      }
    }

    // 2. Global Continent & Country Jurisdiction Selector
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "SELECT REGIONAL JURISDICTION",
              style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
              fontWeight = FontWeight.Bold,
              color = VibrantSecondary
            )
            Text(
              "28 Sovereign Regions",
              style = MaterialTheme.typography.labelSmall,
              color = VibrantPrimary,
              fontWeight = FontWeight.SemiBold
            )
          }

          // Continent Filter Chips
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

          // Country Chips
          Text("Active Country / Tax System:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            filteredJurisdictions.forEach { jur ->
              FilterChip(
                selected = state.selectedJurisdiction == jur,
                onClick = { viewModel.setGlobalJurisdiction(jur) },
                label = {
                  Text(
                    text = "${jur.countryCode} - ${jur.countryName}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (state.selectedJurisdiction == jur) FontWeight.Bold else FontWeight.Normal
                  )
                },
                leadingIcon = {
                  if (state.selectedJurisdiction == jur) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                  }
                }
              )
            }
          }

          // Statutory Features Badges
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (currentJurisdiction.hasThirteenthMonthBonus) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = VibrantTertiaryContainer,
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = VibrantTertiary, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Mandatory 13th-Month Pay", style = MaterialTheme.typography.labelSmall, color = VibrantTertiary, fontWeight = FontWeight.Bold)
                }
              }
            }
            if (currentJurisdiction.hasEndOfServiceGratuity) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = VibrantSecondaryContainer,
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.AccountBalance, contentDescription = null, tint = VibrantSecondary, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("End of Service Gratuity (EOSG)", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary, fontWeight = FontWeight.Bold)
                }
              }
            }
            if (currentJurisdiction.hasMandatoryWps) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = VibrantPrimaryContainer,
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("WPS File Rail Active", style = MaterialTheme.typography.labelSmall, color = VibrantPrimary, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // 3. Real-Time Result Paystub
    state.result?.let { calculation ->
      item {
        PayStubSummaryCard(
          calculation = calculation,
          currencySymbol = currencySymbol
        )
      }
    }

    // 4. Compensation & Timing Inputs
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("COMPENSATION & FREQUENCY", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = VibrantSecondary)

          // Pay Type Selector
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = state.payType == PayType.SALARY,
              onClick = { viewModel.updateCalculator(payType = PayType.SALARY, baseRate = if (state.baseRateInput == "65") "120000" else state.baseRateInput) },
              label = { Text("Annual / Periodic Salary") },
              modifier = Modifier.weight(1f)
            )
            FilterChip(
              selected = state.payType == PayType.HOURLY,
              onClick = { viewModel.updateCalculator(payType = PayType.HOURLY, baseRate = if (state.baseRateInput == "120000") "65" else state.baseRateInput) },
              label = { Text("Hourly Wage") },
              modifier = Modifier.weight(1f)
            )
          }

          // Base Rate Input
          OutlinedTextField(
            value = state.baseRateInput,
            onValueChange = { viewModel.updateCalculator(baseRate = it) },
            label = { Text(if (state.payType == PayType.SALARY) "Base Salary ($currencySymbol ${currentJurisdiction.currencyCode})" else "Hourly Rate ($currencySymbol ${currentJurisdiction.currencyCode}/hr)") },
            leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("calc_base_rate_input")
          )

          // Pay Frequency
          Text("Pay Frequency:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            PayFrequency.values().forEach { freq ->
              FilterChip(
                selected = state.payFrequency == freq,
                onClick = { viewModel.updateCalculator(payFrequency = freq) },
                label = { Text(freq.displayName, style = MaterialTheme.typography.labelSmall) }
              )
            }
          }

          // Hours Worked & Overtime (If hourly)
          if (state.payType == PayType.HOURLY) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedTextField(
                value = state.hoursWorkedInput,
                onValueChange = { viewModel.updateCalculator(hoursWorked = it) },
                label = { Text("Regular Hours") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = state.overtimeHoursInput,
                onValueChange = { viewModel.updateCalculator(overtimeHours = it) },
                label = { Text("Overtime Hours") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("Overtime Multiplier:", style = MaterialTheme.typography.labelMedium)
              Spacer(modifier = Modifier.width(8.dp))
              FilterChip(
                selected = state.overtimeMultiplier == 1.5,
                onClick = { viewModel.updateCalculator(overtimeMultiplier = 1.5) },
                label = { Text("1.5x") }
              )
              Spacer(modifier = Modifier.width(6.dp))
              FilterChip(
                selected = state.overtimeMultiplier == 2.0,
                onClick = { viewModel.updateCalculator(overtimeMultiplier = 2.0) },
                label = { Text("2.0x (Double Time)") }
              )
            }
          }
        }
      }
    }

    // 5. Statutory Tax Withholding & Regional Rules
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("STATUTORY DEDUCTIONS & VAT/GST/VAS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = VibrantSecondary)

          // Jurisdiction overview row
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pension / Social Security:", style = MaterialTheme.typography.bodySmall, color = VibrantSecondary)
                Text("${currentJurisdiction.pensionFundName} (${currentJurisdiction.employeePensionRate}% Emp / ${currentJurisdiction.employerPensionRate}% Co.)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Health / Medical Levy:", style = MaterialTheme.typography.bodySmall, color = VibrantSecondary)
                Text("${currentJurisdiction.healthInsuranceName} (${currentJurisdiction.employeeHealthRate}%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Standard VAT / GST / VAS:", style = MaterialTheme.typography.bodySmall, color = VibrantSecondary)
                Text("${currentJurisdiction.standardVatGstPercent}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bank Direct Clearing Format:", style = MaterialTheme.typography.bodySmall, color = VibrantSecondary)
                Text(currentJurisdiction.clearingAndWpsFormat, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = VibrantPrimary)
              }
            }
          }

          // Filing Status (if applicable)
          Text("Filing Status / Exemption Class:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FilingStatus.values().forEach { status ->
              FilterChip(
                selected = state.filingStatus == status,
                onClick = { viewModel.updateCalculator(filingStatus = status) },
                label = { Text(status.displayName, style = MaterialTheme.typography.labelSmall) }
              )
            }
          }

          // Custom VAT / GST override input
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = state.customVatPercentInput,
              onValueChange = { viewModel.updateCalculator(customVatPercent = it) },
              label = { Text("Custom VAT / GST (%) [Default: ${currentJurisdiction.standardVatGstPercent}%]") },
              trailingIcon = { Text("%", modifier = Modifier.padding(end = 12.dp)) },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // 6. Pre-Tax & Post-Tax Benefits
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("VOLUNTARY BENEFITS & DEDUCTIONS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = VibrantSecondary)

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = state.preTax401kPercentInput,
              onValueChange = { viewModel.updateCalculator(preTax401kPercent = it) },
              label = { Text("Voluntary Pension Pre-Tax (%)") },
              trailingIcon = { Text("%", modifier = Modifier.padding(end = 12.dp)) },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = state.preTaxHealthInput,
              onValueChange = { viewModel.updateCalculator(preTaxHealth = it) },
              label = { Text("Supplemental Health") },
              leadingIcon = { Text(currencySymbol, modifier = Modifier.padding(start = 8.dp)) },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.weight(1f)
            )
          }

          OutlinedTextField(
            value = state.postTaxDeductionsInput,
            onValueChange = { viewModel.updateCalculator(postTaxDeductions = it) },
            label = { Text("Post-Tax Deductions (Garnishments / Loan Repayments)") },
            leadingIcon = { Text(currencySymbol, modifier = Modifier.padding(start = 8.dp)) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // 7. Room Database Persistence & Actions
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
          onClick = {
            viewModel.saveTaxSettings(
              TaxSettingEntity(
                id = 1,
                activeJurisdictionCode = currentJurisdiction.countryCode,
                activeContinent = currentJurisdiction.continent.displayName,
                defaultCurrencyCode = currentJurisdiction.currencyCode,
                vatGstVasRatePercent = state.customVatPercentInput.toDoubleOrNull() ?: currentJurisdiction.standardVatGstPercent,
                employerPensionMatchPercent = currentJurisdiction.employerPensionRate,
                statutoryHealthPercent = currentJurisdiction.employerHealthRate,
                standardOvertimeMultiplier = state.overtimeMultiplier,
                localClearingFormat = currentJurisdiction.clearingAndWpsFormat,
                enforceWpsPayroll = currentJurisdiction.hasMandatoryWps
              )
            )
          },
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Save ${currentJurisdiction.countryName} as Default in Room DB")
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              state.result?.let { res ->
                val summaryText = """
                  PAYFLOW AI GLOBAL CALCULATION SUMMARY
                  Jurisdiction: ${currentJurisdiction.countryName} (${currentJurisdiction.continent.displayName})
                  Currency: ${currentJurisdiction.currencyCode} ($currencySymbol)
                  Gross Period Earnings: $currencySymbol${res.grossPay}
                  Income Tax / PAYE: -$currencySymbol${res.federalWithholding}
                  ${res.statutoryPensionLabel} (Employee): -$currencySymbol${res.statutoryPensionEmployee}
                  ${res.statutoryHealthLabel} (Employee): -$currencySymbol${res.statutoryHealthEmployee}
                  -------------------------------------
                  NET TAKE-HOME PAY: $currencySymbol${res.netTakeHomePay}
                  Effective Tax & Levy Rate: ${res.effectiveTaxRatePercent}%
                  Employer Total Cost: $currencySymbol${res.totalEmployerCost}
                  Local Clearing Rail: ${currentJurisdiction.clearingAndWpsFormat}
                """.trimIndent()
                clipboardManager.setText(AnnotatedString(summaryText))
                viewModel.showNotification("Global paystub summary copied to clipboard!")
              }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy Summary")
          }

          OutlinedButton(
            onClick = {
              viewModel.updateCalculator(
                baseRate = "120000",
                hoursWorked = "80",
                overtimeHours = "0",
                preTax401kPercent = "5.0",
                preTaxHealth = "120.00",
                postTaxDeductions = "0.00"
              )
              viewModel.showNotification("Calculator reset to baseline")
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset")
          }
        }
      }
    }
  }
}
