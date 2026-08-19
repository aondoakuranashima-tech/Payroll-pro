package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.AnomalyAlert
import com.example.data.model.AnomalySeverity
import com.example.data.model.ForecastPoint
import com.example.data.model.ReportFormat
import com.example.data.model.ReportType
import com.example.ui.components.CustomReportExportModal
import com.example.ui.components.EnterpriseExportModal
import com.example.ui.components.EnterpriseImportModal
import com.example.ui.components.TaxChartVisualizer
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@Composable
fun AnalyticsForecastScreen(
  viewModel: PayrollViewModel,
  onOpenUpgrade: () -> Unit
) {
  val employees by viewModel.employees.collectAsState()
  val payrollRuns by viewModel.payrollRuns.collectAsState()
  val forecastGrowthRate by viewModel.forecastGrowthRate.collectAsState()
  val forecastWageInflation by viewModel.forecastWageInflation.collectAsState()
  val forecastHorizon by viewModel.forecastHorizonMonths.collectAsState()
  val reportFilter by viewModel.reportFilter.collectAsState()

  val anomalies = remember(employees, payrollRuns) { viewModel.getAnomalies() }
  val forecastPoints = remember(payrollRuns, forecastGrowthRate, forecastWageInflation, forecastHorizon) {
    viewModel.getForecastPoints()
  }

  var selectedTab by remember { mutableStateOf(0) }
  var showExportModal by remember { mutableStateOf(false) }
  var showEnterpriseExportModal by remember { mutableStateOf(false) }
  var showEnterpriseImportModal by remember { mutableStateOf(false) }

  // Quick report filter selections
  var selectedDeptFilter by remember { mutableStateOf("All Departments") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("analytics_forecast_screen"),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Top Navigation TabRow
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.5f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.Transparent,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = VibrantPrimary
            )
          },
          modifier = Modifier.padding(4.dp)
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Reports", style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium) },
            icon = { Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Tax Chart", style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = { Text("AI Forecast", style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium) },
            icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
          Tab(
            selected = selectedTab == 3,
            onClick = { selectedTab = 3 },
            text = { Text("Tax 941", style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium) },
            icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
          Tab(
            selected = selectedTab == 4,
            onClick = { selectedTab = 4 },
            text = {
              val unresCount = anomalies.count { !it.isResolved && it.severity == AnomalySeverity.CRITICAL }
              Text(if (unresCount > 0) "Audits ($unresCount)" else "Audits", style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium)
            },
            icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
        }
      }
    }

    // 2. Tab Contents
    when (selectedTab) {
      0 -> {
        // TAB 0: CUSTOM REPORTING & ANALYTICS DASHBOARD
        item {
          Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(VibrantPrimary.copy(alpha = 0.3f), Color.Transparent))
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(Brush.linearGradient(listOf(VibrantPrimary, VibrantSecondary))),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.PieChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text("Executive Financial Reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                    Text("Real-time payroll burden & cost center analytics", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Button(
                    onClick = { showEnterpriseImportModal = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                  Button(
                    onClick = { showEnterpriseExportModal = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }

              // Key KPI Metrics Grid
              val totalGross = payrollRuns.sumOf { it.totalGross }
              val totalTaxes = payrollRuns.sumOf { it.totalEmployerTaxes }
              val totalBenefits = payrollRuns.sumOf { it.totalPreTaxDeductions }
              val totalBurden = totalGross + totalTaxes

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                ReportKpiBox(
                  title = "Direct Wages",
                  value = viewModel.formatCurrency(totalGross),
                  subtitle = "${employees.size} active employees",
                  color = VibrantPrimary,
                  modifier = Modifier.weight(1f)
                )
                ReportKpiBox(
                  title = "Employer Taxes",
                  value = viewModel.formatCurrency(totalTaxes),
                  subtitle = "FICA + FUTA + SUTA",
                  color = VibrantWarningAction,
                  modifier = Modifier.weight(1f)
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                ReportKpiBox(
                  title = "Benefits Overhead",
                  value = viewModel.formatCurrency(totalBenefits),
                  subtitle = "401(k) + Health care",
                  color = VibrantSecondary,
                  modifier = Modifier.weight(1f)
                )
                ReportKpiBox(
                  title = "Total Labor Burden",
                  value = viewModel.formatCurrency(totalBurden),
                  subtitle = "Total OpEx commitment",
                  color = VibrantSuccessText,
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }

        // Department Breakdown Card
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Department Labor Allocation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                Text("${employees.size} Staff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              val deptMap = employees.groupBy { it.department }
              val maxDeptSpend = deptMap.values.maxOfOrNull { emps -> emps.sumOf { it.baseRate } } ?: 1.0

              deptMap.forEach { (dept, emps) ->
                val deptTotal = emps.sumOf { it.baseRate }
                val pct = (deptTotal / maxDeptSpend).toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("$dept (${emps.size})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = VibrantTextPrimary)
                    Text(viewModel.formatCurrency(deptTotal), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantPrimary)
                  }
                  LinearProgressIndicator(
                    progress = { pct },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(CircleShape),
                    color = VibrantPrimary,
                    trackColor = VibrantPrimaryContainer
                  )
                }
              }
            }
          }
        }

        // Quick Export Presets Card
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = VibrantPrimaryContainer.copy(alpha = 0.35f)),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("One-Click Report Exporters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantOnPrimaryContainer)
              }
              Text("Generate certified compliance files formatted for external systems:", style = MaterialTheme.typography.bodySmall, color = VibrantSecondary)

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                QuickExportChip(label = "Executive PDF", icon = Icons.Default.PictureAsPdf) {
                  showExportModal = true
                }
                QuickExportChip(label = "Spreadsheet CSV", icon = Icons.Default.TableChart) {
                  showExportModal = true
                }
                QuickExportChip(label = "QuickBooks IIF", icon = Icons.Default.CloudSync) {
                  showExportModal = true
                }
                QuickExportChip(label = "Xero Journal CSV", icon = Icons.Default.CloudDone) {
                  showExportModal = true
                }
                QuickExportChip(label = "Developer JSON", icon = Icons.Default.Code) {
                  showExportModal = true
                }
              }
            }
          }
        }
      }

      1 -> {
        // TAB 1: INTERACTIVE TAX BRACKET & BURDEN CHART
        item {
          TaxChartVisualizer(viewModel = viewModel)
        }
      }

      2 -> {
        // TAB 2: AI BUDGET FORECASTING & SCENARIO MODELING
        item {
          Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(VibrantPrimary.copy(alpha = 0.3f), Color.Transparent))
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(Brush.linearGradient(listOf(VibrantPrimary, VibrantSecondary))),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text("AI Budget Scenario Modeler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                    Text("Multi-variable Monte Carlo cash outflow projection", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }

              // Interactive Sliders for Scenario Parameters
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Growth Rate
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Headcount Expansion Rate:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                  Text("+${"%.1f".format(forecastGrowthRate)}% / month", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = VibrantPrimary)
                }
                Slider(
                  value = forecastGrowthRate.toFloat(),
                  onValueChange = { viewModel.updateForecastScenario(growthRate = it.toDouble()) },
                  valueRange = 0f..25f,
                  colors = SliderDefaults.colors(thumbColor = VibrantPrimary, activeTrackColor = VibrantPrimary)
                )

                // Wage Inflation
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Wage & OpEx Inflation:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                  Text("+${"%.1f".format(forecastWageInflation)}% annual", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = VibrantSecondary)
                }
                Slider(
                  value = forecastWageInflation.toFloat(),
                  onValueChange = { viewModel.updateForecastScenario(wageInflation = it.toDouble()) },
                  valueRange = 0f..8f,
                  colors = SliderDefaults.colors(thumbColor = VibrantSecondary, activeTrackColor = VibrantSecondary)
                )

                // Horizon Chips
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Forecast Horizon:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(3 to "3 Mos", 6 to "6 Mos", 12 to "12 Mos").forEach { (months, label) ->
                      FilterChip(
                        selected = forecastHorizon == months,
                        onClick = { viewModel.updateForecastScenario(horizon = months) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(10.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Forecast Period Cards
        items(forecastPoints) { point ->
          ForecastProjectionCard(point = point, viewModel = viewModel)
        }
      }

      3 -> {
        // TAB 3: TAX COMPLIANCE & IRS 941 SCHEDULE
        item {
          Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VibrantPrimaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("IRS Form 941 Quarterly Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                  Text("Federal Deposit Rule: Semi-Weekly Depositor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }

              val run = payrollRuns.firstOrNull()
              val gross = run?.totalGross ?: 32000.0
              val ssTax = gross * 0.124 // 6.2% emp + 6.2% empr
              val medTax = gross * 0.029 // 1.45% emp + 1.45% empr
              val fedWithholding = (gross * 0.12)
              val total941 = fedWithholding + ssTax + medTax

              TaxScheduleRow(label = "Line 3: Federal Income Tax Withheld", amount = viewModel.formatCurrency(fedWithholding))
              TaxScheduleRow(label = "Line 5a: Taxable Social Security Wages (12.4%)", amount = viewModel.formatCurrency(ssTax))
              TaxScheduleRow(label = "Line 5c: Taxable Medicare Wages (2.9%)", amount = viewModel.formatCurrency(medTax))
              TaxScheduleRow(label = "Line 10: Total Taxes After Adjustments", amount = viewModel.formatCurrency(total941), isBold = true)

              Surface(
                shape = RoundedCornerShape(14.dp),
                color = VibrantSuccessContainer
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VibrantSuccessText, modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Automated IRS EFTPS & State DOR electronic deposits are synchronized.",
                    style = MaterialTheme.typography.labelSmall,
                    color = VibrantSuccessText,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }

      4 -> {
        // TAB 4: AI ANOMALY & FRAUD AUDIT CENTER
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("AI Anomaly & Fraud Audit Log", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = VibrantWarningContainer
            ) {
              Text(
                "${anomalies.count { !it.isResolved }} Pending Actions",
                style = MaterialTheme.typography.labelSmall,
                color = VibrantWarningAction,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        items(anomalies) { alert ->
          AnomalyResolutionCard(
            alert = alert,
            onResolve = { action ->
              viewModel.resolveAnomaly(alert.id, action)
            }
          )
        }
      }
    }
  }

  // Modals
  if (showExportModal || showEnterpriseExportModal) {
    EnterpriseExportModal(
      viewModel = viewModel,
      onDismiss = {
        showExportModal = false
        showEnterpriseExportModal = false
      }
    )
  }

  if (showEnterpriseImportModal) {
    EnterpriseImportModal(
      viewModel = viewModel,
      onDismiss = { showEnterpriseImportModal = false }
    )
  }
}

@Composable
fun ReportKpiBox(
  title: String,
  value: String,
  subtitle: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = color.copy(alpha = 0.08f),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(listOf(color.copy(alpha = 0.25f), Color.Transparent))
    ),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
      Text(subtitle, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = color.copy(alpha = 0.8f))
    }
  }
}

@Composable
fun QuickExportChip(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    border = CardDefaults.outlinedCardBorder()
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(icon, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(16.dp))
      Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = VibrantTextPrimary)
    }
  }
}

@Composable
fun ForecastProjectionCard(
  point: ForecastPoint,
  viewModel: PayrollViewModel
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder(),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CalendarToday, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(point.periodLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
        }
        Text(
          text = viewModel.formatCurrency(point.totalCashOutflow),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = VibrantPrimary
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("Projected Wages", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(viewModel.formatCurrency(point.projectedGrossPayroll), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        Column {
          Text("Employer Taxes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(viewModel.formatCurrency(point.projectedEmployerTaxes), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        Column(horizontalAlignment = Alignment.End) {
          Text("Operating Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(viewModel.formatCurrency(point.projectedOperatingExpenses), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = VibrantPrimaryContainer.copy(alpha = 0.35f)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("95% Confidence Interval Band:", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
          Text(
            "${viewModel.formatCurrency(point.confidenceIntervalLow)} - ${viewModel.formatCurrency(point.confidenceIntervalHigh)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = VibrantPrimary
          )
        }
      }
    }
  }
}

@Composable
fun TaxScheduleRow(label: String, amount: String, isBold: Boolean = false) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
      color = if (isBold) VibrantTextNavy else VibrantTextPrimary,
      modifier = Modifier.weight(1f)
    )
    Text(
      text = amount,
      style = if (isBold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
      color = if (isBold) VibrantPrimary else VibrantTextPrimary
    )
  }
}

@Composable
fun AnomalyResolutionCard(
  alert: AnomalyAlert,
  onResolve: (action: String) -> Unit
) {
  val (cardBg, titleColor, icon) = when (alert.severity) {
    AnomalySeverity.CRITICAL -> Triple(VibrantErrorContainer, VibrantError, Icons.Default.Warning)
    AnomalySeverity.WARNING -> Triple(VibrantWarningContainer, VibrantWarningAction, Icons.Default.ReportProblem)
    AnomalySeverity.INFO -> Triple(VibrantSuccessContainer, VibrantSuccessText, Icons.Default.Info)
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(listOf(titleColor.copy(alpha = 0.4f), Color.Transparent))
    )
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
              .background(cardBg),
            contentAlignment = Alignment.Center
          ) {
            Icon(icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(18.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(alert.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
            Text("${alert.category} • ${alert.detectedAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }

      Text(alert.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

      if (alert.isResolved) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = VibrantSuccessContainer
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VibrantSuccessText, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Resolved: ${alert.resolutionAction ?: "Verified & Closed"}", style = MaterialTheme.typography.labelSmall, color = VibrantSuccessText, fontWeight = FontWeight.Bold)
          }
        }
      } else {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = { onResolve("Dismissed without action") },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text("Dismiss", style = MaterialTheme.typography.labelSmall)
          }
          Spacer(modifier = Modifier.width(6.dp))
          Button(
            onClick = { onResolve("Verified with Identity & CPA Audit") },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Mark Verified", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
