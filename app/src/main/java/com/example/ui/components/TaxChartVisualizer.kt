package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxChartVisualizer(
  viewModel: PayrollViewModel,
  modifier: Modifier = Modifier,
  onNavigateToCalculator: () -> Unit = {}
) {
  var selectedJurisdiction by remember { mutableStateOf(GlobalJurisdiction.USA) }
  var annualGrossSalary by remember { mutableStateOf(120000.0) }
  var filingStatus by remember { mutableStateOf(FilingStatus.SINGLE) }
  var chartMode by remember { mutableStateOf(0) } // 0: Breakdown Stack, 1: Progressive Brackets, 2: Employer vs Employee, 3: Global Radar

  // Dynamic Calculation
  val calcResult = remember(selectedJurisdiction, annualGrossSalary, filingStatus) {
    viewModel.calculatePaycheck(
      jurisdiction = selectedJurisdiction,
      payType = PayType.SALARY,
      baseRate = annualGrossSalary,
      payFrequency = PayFrequency.MONTHLY,
      filingStatus = filingStatus,
      hoursWorked = 160.0
    )
  }

  val grossAnnual = annualGrossSalary
  val incomeTaxAnnual = calcResult.federalWithholding * 12.0 + calcResult.stateWithholding * 12.0
  val socialSecurityAnnual = calcResult.socialSecurityWithholding * 12.0
  val medicareHealthAnnual = calcResult.medicareWithholding * 12.0
  val pensionAnnual = calcResult.preTaxDeductions * 12.0
  val netAnnual = calcResult.netTakeHomePay * 12.0
  val employerCostAnnual = calcResult.totalEmployerCost * 12.0
  val effectiveTaxRate = if (grossAnnual > 0) ((grossAnnual - netAnnual) / grossAnnual) * 100.0 else 0.0

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("tax_chart_visualizer"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header & Interactive Jurisdiction Selector
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder().copy(
        brush = Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFF6366F1)))
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Interactive Statutory Tax Chart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
              Text("Marginal brackets, statutory deductions & employer burden", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
            }
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFEEF2FF)
          ) {
            Text(
              text = "${selectedJurisdiction.flagEmoji} ${selectedJurisdiction.currencyCode}",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF4F46E5)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Region Selector Chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          GlobalJurisdiction.entries.forEach { j ->
            val isSelected = j == selectedJurisdiction
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) Color(0xFF4F46E5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              border = BorderStroke(1.dp, if (isSelected) Color(0xFF4338CA) else Color.Transparent),
              modifier = Modifier.clickable { selectedJurisdiction = j }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(j.flagEmoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = j.countryName,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // 2. Interactive Wage Slider & Effective Rate Gauge
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder(),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Simulated Annual Gross Salary", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
            Text(
              text = "${selectedJurisdiction.currencySymbol}${String.format(Locale.US, "%,d", annualGrossSalary.toInt())}",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = VibrantTextNavy
            )
          }

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (effectiveTaxRate > 35) VibrantErrorContainer else VibrantSuccessContainer
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              horizontalAlignment = Alignment.End
            ) {
              Text(
                text = "${String.format(Locale.US, "%.1f", effectiveTaxRate)}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (effectiveTaxRate > 35) VibrantError else VibrantSuccessText
              )
              Text(
                text = "Effective Tax Rate",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.5.sp,
                color = if (effectiveTaxRate > 35) VibrantError else VibrantSuccessText
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Slider for Salary ($20k to $300k)
        Slider(
          value = annualGrossSalary.toFloat(),
          onValueChange = { annualGrossSalary = ((it / 5000).roundToInt() * 5000).toDouble() },
          valueRange = 20000f..300000f,
          steps = 55,
          colors = SliderDefaults.colors(
            thumbColor = Color(0xFF4F46E5),
            activeTrackColor = Color(0xFF6366F1),
            inactiveTrackColor = Color(0xFFE0E7FF)
          ),
          modifier = Modifier.testTag("tax_chart_salary_slider")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("${selectedJurisdiction.currencySymbol}20k", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${selectedJurisdiction.currencySymbol}150k", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${selectedJurisdiction.currencySymbol}300k+", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    // 3. Tab Selector: Breakdown Stack, Progressive Ladder, Employer Burden, Global Radar
    TabRow(
      selectedTabIndex = chartMode,
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      shape = RoundedCornerShape(16.dp),
      indicator = {},
      divider = {}
    ) {
      listOf("Tax Breakdown", "Progressive Brackets", "Employer vs Net", "Global Radar").forEachIndexed { idx, title ->
        val selected = chartMode == idx
        Tab(
          selected = selected,
          onClick = { chartMode = idx },
          modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFF4F46E5) else Color.Transparent)
        ) {
          Text(
            text = title,
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.5.sp
          )
        }
      }
    }

    // 4. Chart Views according to selected Mode
    when (chartMode) {
      0 -> TaxBreakdownStackView(
        jurisdiction = selectedJurisdiction,
        grossAnnual = grossAnnual,
        incomeTax = incomeTaxAnnual,
        socialSecurity = socialSecurityAnnual,
        medicareHealth = medicareHealthAnnual,
        pension = pensionAnnual,
        netPay = netAnnual,
        viewModel = viewModel
      )
      1 -> ProgressiveBracketsLadderView(
        jurisdiction = selectedJurisdiction,
        grossAnnual = grossAnnual,
        filingStatus = filingStatus
      )
      2 -> EmployerVsNetBurdenView(
        jurisdiction = selectedJurisdiction,
        grossAnnual = grossAnnual,
        netAnnual = netAnnual,
        employerTotalCost = employerCostAnnual,
        calcResult = calcResult,
        viewModel = viewModel
      )
      3 -> GlobalRadarComparisonView(
        grossAmount = grossAnnual,
        viewModel = viewModel
      )
    }
  }
}

// -------------------------------------------------------------
// TAB 0: STACKED TAX BREAKDOWN VIEW
// -------------------------------------------------------------
@Composable
fun TaxBreakdownStackView(
  jurisdiction: GlobalJurisdiction,
  grossAnnual: Double,
  incomeTax: Double,
  socialSecurity: Double,
  medicareHealth: Double,
  pension: Double,
  netPay: Double,
  viewModel: PayrollViewModel
) {
  val totalTaxesDeductions = incomeTax + socialSecurity + medicareHealth + pension
  val netPct = if (grossAnnual > 0) (netPay / grossAnnual).toFloat().coerceIn(0f, 1f) else 0.7f
  val taxPct = if (grossAnnual > 0) (incomeTax / grossAnnual).toFloat().coerceIn(0f, 1f) else 0.15f
  val socSecPct = if (grossAnnual > 0) (socialSecurity / grossAnnual).toFloat().coerceIn(0f, 1f) else 0.08f
  val healthPct = if (grossAnnual > 0) (medicareHealth / grossAnnual).toFloat().coerceIn(0f, 1f) else 0.04f
  val pensionPct = if (grossAnnual > 0) (pension / grossAnnual).toFloat().coerceIn(0f, 1f) else 0.03f

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text("Annual Distribution Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)

      // Visual Stacked Bar
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(24.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFFE2E8F0))
      ) {
        Row(modifier = Modifier.fillMaxSize()) {
          if (netPct > 0) {
            Box(
              modifier = Modifier
                .weight(netPct)
                .fillMaxHeight()
                .background(Color(0xFF10B981)) // Green Net
            )
          }
          if (taxPct > 0) {
            Box(
              modifier = Modifier
                .weight(taxPct)
                .fillMaxHeight()
                .background(Color(0xFFEF4444)) // Red Income Tax
            )
          }
          if (socSecPct > 0) {
            Box(
              modifier = Modifier
                .weight(socSecPct)
                .fillMaxHeight()
                .background(Color(0xFF3B82F6)) // Blue Social Security
            )
          }
          if (healthPct > 0) {
            Box(
              modifier = Modifier
                .weight(healthPct)
                .fillMaxHeight()
                .background(Color(0xFFF59E0B)) // Amber Health
            )
          }
          if (pensionPct > 0) {
            Box(
              modifier = Modifier
                .weight(pensionPct)
                .fillMaxHeight()
                .background(Color(0xFF8B5CF6)) // Purple Pension
            )
          }
        }
      }

      // Legend & Itemized Cards
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TaxLegendRow(
          color = Color(0xFF10B981),
          title = "Net Take-Home Pay",
          amount = "${jurisdiction.currencySymbol}${String.format(Locale.US, "%,.2f", netPay)}",
          percent = "${String.format(Locale.US, "%.1f", (netPct * 100))}% of Gross",
          isHighlight = true
        )
        TaxLegendRow(
          color = Color(0xFFEF4444),
          title = "Income Tax Withholding (PAYE)",
          amount = "${jurisdiction.currencySymbol}${String.format(Locale.US, "%,.2f", incomeTax)}",
          percent = "${String.format(Locale.US, "%.1f", (taxPct * 100))}%"
        )
        TaxLegendRow(
          color = Color(0xFF3B82F6),
          title = "${jurisdiction.pensionFundName} / Social Security",
          amount = "${jurisdiction.currencySymbol}${String.format(Locale.US, "%,.2f", socialSecurity)}",
          percent = "${String.format(Locale.US, "%.1f", (socSecPct * 100))}%"
        )
        TaxLegendRow(
          color = Color(0xFFF59E0B),
          title = "${jurisdiction.healthInsuranceName} / Healthcare",
          amount = "${jurisdiction.currencySymbol}${String.format(Locale.US, "%,.2f", medicareHealth)}",
          percent = "${String.format(Locale.US, "%.1f", (healthPct * 100))}%"
        )
        if (pension > 0) {
          TaxLegendRow(
            color = Color(0xFF8B5CF6),
            title = "Voluntary Pre-Tax Retirement / 401(k)",
            amount = "${jurisdiction.currencySymbol}${String.format(Locale.US, "%,.2f", pension)}",
            percent = "${String.format(Locale.US, "%.1f", (pensionPct * 100))}%"
          )
        }
      }
    }
  }
}

@Composable
fun TaxLegendRow(
  color: Color,
  title: String,
  amount: String,
  percent: String,
  isHighlight: Boolean = false
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isHighlight) Color(0xFFECFDF5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
          color = if (isHighlight) Color(0xFF065F46) else VibrantTextPrimary
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = amount,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold,
          color = if (isHighlight) Color(0xFF065F46) else VibrantTextPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = percent,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 10.sp
        )
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 1: PROGRESSIVE MARGINAL TAX BRACKETS LADDER
// -------------------------------------------------------------
@Composable
fun ProgressiveBracketsLadderView(
  jurisdiction: GlobalJurisdiction,
  grossAnnual: Double,
  filingStatus: FilingStatus
) {
  val brackets = remember(jurisdiction, grossAnnual) {
    when (jurisdiction) {
      GlobalJurisdiction.USA -> listOf(
        TaxBracketBand("Tier 1 (10%)", 0.0, 11925.0, 10.0, 11925.0.coerceAtMost(grossAnnual), (11925.0.coerceAtMost(grossAnnual) * 0.10), 0xFF10B981),
        TaxBracketBand("Tier 2 (12%)", 11925.0, 48475.0, 12.0, max(0.0, grossAnnual - 11925.0).coerceAtMost(36550.0), (max(0.0, grossAnnual - 11925.0).coerceAtMost(36550.0) * 0.12), 0xFF3B82F6),
        TaxBracketBand("Tier 3 (22%)", 48475.0, 103350.0, 22.0, max(0.0, grossAnnual - 48475.0).coerceAtMost(54875.0), (max(0.0, grossAnnual - 48475.0).coerceAtMost(54875.0) * 0.22), 0xFFF59E0B),
        TaxBracketBand("Tier 4 (24%)", 103350.0, 197300.0, 24.0, max(0.0, grossAnnual - 103350.0).coerceAtMost(93950.0), (max(0.0, grossAnnual - 103350.0).coerceAtMost(93950.0) * 0.24), 0xFFEC4899),
        TaxBracketBand("Tier 5 (32%)", 197300.0, 250525.0, 32.0, max(0.0, grossAnnual - 197300.0).coerceAtMost(53225.0), (max(0.0, grossAnnual - 197300.0).coerceAtMost(53225.0) * 0.32), 0xFF8B5CF6),
        TaxBracketBand("Tier 6 (35%+)", 250525.0, 999999.0, 35.0, max(0.0, grossAnnual - 250525.0), (max(0.0, grossAnnual - 250525.0) * 0.35), 0xFFEF4444)
      )
      GlobalJurisdiction.UK -> listOf(
        TaxBracketBand("Personal Allowance (0%)", 0.0, 12570.0, 0.0, 12570.0.coerceAtMost(grossAnnual), 0.0, 0xFF10B981),
        TaxBracketBand("Basic Rate (20%)", 12570.0, 50270.0, 20.0, max(0.0, grossAnnual - 12570.0).coerceAtMost(37700.0), (max(0.0, grossAnnual - 12570.0).coerceAtMost(37700.0) * 0.20), 0xFF3B82F6),
        TaxBracketBand("Higher Rate (40%)", 50270.0, 125140.0, 40.0, max(0.0, grossAnnual - 50270.0).coerceAtMost(74870.0), (max(0.0, grossAnnual - 50270.0).coerceAtMost(74870.0) * 0.40), 0xFFF59E0B),
        TaxBracketBand("Additional Rate (45%)", 125140.0, 999999.0, 45.0, max(0.0, grossAnnual - 125140.0), (max(0.0, grossAnnual - 125140.0) * 0.45), 0xFFEF4444)
      )
      GlobalJurisdiction.GERMANY -> listOf(
        TaxBracketBand("Grundfreibetrag (0%)", 0.0, 11604.0, 0.0, 11604.0.coerceAtMost(grossAnnual), 0.0, 0xFF10B981),
        TaxBracketBand("Zone 1 (14% - 24%)", 11604.0, 17005.0, 18.0, max(0.0, grossAnnual - 11604.0).coerceAtMost(5401.0), (max(0.0, grossAnnual - 11604.0).coerceAtMost(5401.0) * 0.18), 0xFF3B82F6),
        TaxBracketBand("Zone 2 (24% - 42%)", 17005.0, 66760.0, 32.0, max(0.0, grossAnnual - 17005.0).coerceAtMost(49755.0), (max(0.0, grossAnnual - 17005.0).coerceAtMost(49755.0) * 0.32), 0xFFF59E0B),
        TaxBracketBand("Spitzensteuersatz (42%)", 66760.0, 277825.0, 42.0, max(0.0, grossAnnual - 66760.0).coerceAtMost(211065.0), (max(0.0, grossAnnual - 66760.0).coerceAtMost(211065.0) * 0.42), 0xFFEC4899),
        TaxBracketBand("Reichensteuer (45%)", 277825.0, 999999.0, 45.0, max(0.0, grossAnnual - 277825.0), (max(0.0, grossAnnual - 277825.0) * 0.45), 0xFFEF4444)
      )
      GlobalJurisdiction.BRAZIL -> listOf(
        TaxBracketBand("Faixa 1 Isento (0%)", 0.0, 27110.0, 0.0, 27110.0.coerceAtMost(grossAnnual), 0.0, 0xFF10B981),
        TaxBracketBand("Faixa 2 (7.5%)", 27110.0, 33919.0, 7.5, max(0.0, grossAnnual - 27110.0).coerceAtMost(6809.0), (max(0.0, grossAnnual - 27110.0).coerceAtMost(6809.0) * 0.075), 0xFF3B82F6),
        TaxBracketBand("Faixa 3 (15.0%)", 33919.0, 45012.0, 15.0, max(0.0, grossAnnual - 33919.0).coerceAtMost(11093.0), (max(0.0, grossAnnual - 33919.0).coerceAtMost(11093.0) * 0.15), 0xFFF59E0B),
        TaxBracketBand("Faixa 4 (22.5%)", 45012.0, 55976.0, 22.5, max(0.0, grossAnnual - 45012.0).coerceAtMost(10964.0), (max(0.0, grossAnnual - 45012.0).coerceAtMost(10964.0) * 0.225), 0xFFEC4899),
        TaxBracketBand("Faixa 5 (27.5%)", 55976.0, 999999.0, 27.5, max(0.0, grossAnnual - 55976.0), (max(0.0, grossAnnual - 55976.0) * 0.275), 0xFFEF4444)
      )
      else -> listOf(
        TaxBracketBand("Statutory Band 1", 0.0, (grossAnnual * 0.4), 10.0, grossAnnual * 0.4, (grossAnnual * 0.04), 0xFF10B981),
        TaxBracketBand("Statutory Band 2", (grossAnnual * 0.4), (grossAnnual * 0.8), 20.0, grossAnnual * 0.4, (grossAnnual * 0.08), 0xFF3B82F6),
        TaxBracketBand("Statutory Band 3 (Top)", (grossAnnual * 0.8), grossAnnual, 30.0, grossAnnual * 0.2, (grossAnnual * 0.06), 0xFFEF4444)
      )
    }
  }

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder()
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
        Column {
          Text("Marginal Tax Brackets & Tiers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
          Text("${jurisdiction.countryName} Progressive Tax Scale", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFF3E8FF)
        ) {
          Text(
            text = "${brackets.size} Brackets",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7E22CE)
          )
        }
      }

      brackets.forEach { b ->
        val isReached = b.taxableInThisTier > 0
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (isReached) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, if (isReached) Color(b.colorHex).copy(alpha = 0.4f) else Color.Transparent)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isReached) Color(b.colorHex) else Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(b.tierLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
              }
              Text(
                text = "${jurisdiction.currencySymbol}${String.format(Locale.US, "%,.2f", b.taxOwedInThisTier)} tax",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isReached) Color(b.colorHex) else Color.Gray
              )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Band: ${jurisdiction.currencySymbol}${String.format(Locale.US, "%,d", b.lowerLimit.toInt())} - ${if (b.upperLimit > 500000) "Above" else "${jurisdiction.currencySymbol}${String.format(Locale.US, "%,d", b.upperLimit.toInt())}"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Taxable: ${jurisdiction.currencySymbol}${String.format(Locale.US, "%,.2f", b.taxableInThisTier)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 2: EMPLOYER BURDEN VS EMPLOYEE TAKE-HOME
// -------------------------------------------------------------
@Composable
fun EmployerVsNetBurdenView(
  jurisdiction: GlobalJurisdiction,
  grossAnnual: Double,
  netAnnual: Double,
  employerTotalCost: Double,
  calcResult: CalculationResult,
  viewModel: PayrollViewModel
) {
  val employerExtraBurden = max(0.0, employerTotalCost - grossAnnual)
  val employeeDeductionsTotal = max(0.0, grossAnnual - netAnnual)

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Column {
        Text("Total Employer Cost vs Employee Net", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
        Text("True cost to company including statutory on-costs and taxes", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
      }

      // 3 Comparative Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        BurdenStatCard(
          title = "Employer Cost",
          amount = "${jurisdiction.currencySymbol}${String.format(Locale.US, "%,d", employerTotalCost.toInt())}",
          subtitle = "+${String.format(Locale.US, "%.1f", (employerExtraBurden / grossAnnual * 100))}% On-Cost",
          badgeColor = Color(0xFFEF4444),
          modifier = Modifier.weight(1f)
        )
        BurdenStatCard(
          title = "Contract Gross",
          amount = "${jurisdiction.currencySymbol}${String.format(Locale.US, "%,d", grossAnnual.toInt())}",
          subtitle = "Agreed Base",
          badgeColor = Color(0xFF3B82F6),
          modifier = Modifier.weight(1f)
        )
        BurdenStatCard(
          title = "Employee Net",
          amount = "${jurisdiction.currencySymbol}${String.format(Locale.US, "%,d", netAnnual.toInt())}",
          subtitle = "${String.format(Locale.US, "%.1f", (netAnnual / grossAnnual * 100))}% In Pocket",
          badgeColor = Color(0xFF10B981),
          modifier = Modifier.weight(1f)
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      // Visual Cost Gap Bar
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Statutory Overhead Leakage", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE2E8F0))
        ) {
          Row(modifier = Modifier.fillMaxSize()) {
            Box(
              modifier = Modifier
                .weight((netAnnual / employerTotalCost).toFloat().coerceIn(0.1f, 0.9f))
                .fillMaxHeight()
                .background(Color(0xFF10B981))
            )
            Box(
              modifier = Modifier
                .weight(((employerTotalCost - netAnnual) / employerTotalCost).toFloat().coerceIn(0.1f, 0.9f))
                .fillMaxHeight()
                .background(Color(0xFFEF4444))
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Employee Net: ${String.format(Locale.US, "%.1f", (netAnnual / employerTotalCost * 100))}%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF059669))
          Text("Tax & Benefit Overhead: ${String.format(Locale.US, "%.1f", ((employerTotalCost - netAnnual) / employerTotalCost * 100))}%", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDC2626))
        }
      }
    }
  }
}

@Composable
fun BurdenStatCard(
  title: String,
  amount: String,
  subtitle: String,
  badgeColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
      Spacer(modifier = Modifier.height(2.dp))
      Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
      Spacer(modifier = Modifier.height(2.dp))
      Text(subtitle, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = badgeColor, fontSize = 9.sp)
    }
  }
}

// -------------------------------------------------------------
// TAB 3: GLOBAL RADAR COMPARISON
// -------------------------------------------------------------
@Composable
fun GlobalRadarComparisonView(
  grossAmount: Double,
  viewModel: PayrollViewModel
) {
  val radarItems = remember(grossAmount) {
    listOf(
      TaxComparisonJurisdiction("United States", "🇺🇸", "USD", "$", 22.4, 7.65, 4.0, 65.95, 9.65, grossAmount, grossAmount * 0.6595, grossAmount * 1.0965),
      TaxComparisonJurisdiction("United Kingdom", "🇬🇧", "GBP", "£", 20.0, 8.0, 3.0, 69.0, 13.80, grossAmount * 0.79, grossAmount * 0.79 * 0.69, grossAmount * 0.79 * 1.138),
      TaxComparisonJurisdiction("Germany", "🇩🇪", "EUR", "€", 26.5, 9.3, 9.3, 54.9, 21.0, grossAmount * 0.92, grossAmount * 0.92 * 0.549, grossAmount * 0.92 * 1.21),
      TaxComparisonJurisdiction("Australia", "🇦🇺", "AUD", "A$", 24.5, 2.0, 0.0, 73.5, 11.5, grossAmount * 1.54, grossAmount * 1.54 * 0.735, grossAmount * 1.54 * 1.115),
      TaxComparisonJurisdiction("Brazil (CLT)", "🇧🇷", "BRL", "R$", 18.2, 11.0, 0.0, 70.8, 35.8, grossAmount * 5.45, grossAmount * 5.45 * 0.708, grossAmount * 5.45 * 1.358),
      TaxComparisonJurisdiction("Saudi Arabia", "🇸🇦", "SAR", "SAR", 0.0, 10.0, 0.0, 90.0, 12.0, grossAmount * 3.75, grossAmount * 3.75 * 0.90, grossAmount * 3.75 * 1.12),
      TaxComparisonJurisdiction("Nigeria", "🇳🇬", "NGN", "₦", 16.5, 8.0, 0.0, 75.5, 12.0, grossAmount * 1580.0, grossAmount * 1580.0 * 0.755, grossAmount * 1580.0 * 1.12)
    )
  }

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text("Multi-Country Statutory Take-Home Radar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)

      radarItems.forEach { item ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.flagEmoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.countryName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
              }
              Text(
                text = "${item.currencySymbol}${String.format(Locale.US, "%,.0f", item.sampleNetAnnual)} Net",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF059669)
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Proportional Bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFE2E8F0))
            ) {
              Box(
                modifier = Modifier
                  .weight((item.employeeNetTakeHomePercent / 100.0).toFloat())
                  .fillMaxHeight()
                  .background(Color(0xFF10B981))
              )
              Box(
                modifier = Modifier
                  .weight(((100.0 - item.employeeNetTakeHomePercent) / 100.0).toFloat())
                  .fillMaxHeight()
                  .background(Color(0xFFEF4444))
              )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Take-Home: ${String.format(Locale.US, "%.1f", item.employeeNetTakeHomePercent)}%", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("Employer Burden: +${String.format(Locale.US, "%.1f", item.employerOverheadPercent)}%", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color(0xFFB91C1C))
            }
          }
        }
      }
    }
  }
}
