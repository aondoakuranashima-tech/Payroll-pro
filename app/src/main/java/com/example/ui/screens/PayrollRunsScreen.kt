package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PayrollItemEntity
import com.example.data.model.PayrollRunEntity
import com.example.ui.components.QuickBooksXeroModal
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollRunsScreen(
  viewModel: PayrollViewModel,
  modifier: Modifier = Modifier
) {
  val payrollRuns by viewModel.payrollRuns.collectAsState()
  val employees by viewModel.employees.collectAsState()
  val selectedRun by viewModel.selectedPayrollRun.collectAsState()
  val selectedRunItems by viewModel.selectedRunItems.collectAsState()

  var showNewRunDialog by remember { mutableStateOf(false) }
  var showAccountingModalForRun by remember { mutableStateOf<PayrollRunEntity?>(null) }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showNewRunDialog = true },
        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Execute Payroll Run") },
        text = { Text("New Payroll Run", fontWeight = FontWeight.Bold) },
        containerColor = VibrantPrimary,
        contentColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
          .padding(bottom = 60.dp)
          .testTag("new_payroll_run_fab")
      )
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("payroll_runs_screen"),
      contentPadding = PaddingValues(bottom = 96.dp, top = 12.dp, start = 16.dp, end = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Header Information
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Text("Payroll Runs & Batch Cycles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantOnPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Automate company-wide wage calculations, tax withholding schedules, and export balanced GL journal entries.",
              style = MaterialTheme.typography.bodySmall,
              color = VibrantSecondary.copy(alpha = 0.85f)
            )
          }
        }
      }

      // 2. Selected Run Detail View (If any)
      selectedRun?.let { run ->
        item {
          Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.5f)))
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(run.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                  Text("Period: ${run.periodStart} – ${run.periodEnd}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { viewModel.clearSelectedRun() }) {
                  Icon(Icons.Default.Close, contentDescription = "Close details")
                }
              }

              Spacer(modifier = Modifier.height(12.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = VibrantSuccessContainer,
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Text("TOTAL NET PAYOUT", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp), fontWeight = FontWeight.Bold, color = VibrantSuccessText)
                    Text(viewModel.formatCurrency(run.totalNet), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantSuccessText)
                  }
                }
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = VibrantPrimaryContainer,
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Text("TOTAL TAXES", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp), fontWeight = FontWeight.Bold, color = VibrantSecondary)
                    Text(viewModel.formatCurrency(run.totalEmployeeTaxes + run.totalEmployerTaxes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantOnPrimaryContainer)
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = { showAccountingModalForRun = run },
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("QuickBooks GL", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                  onClick = {
                    viewModel.showNotification("Payslips sent via secure email to ${run.employeeCount} employees!")
                  },
                  shape = RoundedCornerShape(14.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Send Payslips", style = MaterialTheme.typography.labelSmall)
                }
              }

              // Employee Itemized Breakdown
              if (selectedRunItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text("ITEMIZED EMPLOYEE PAYOUTS (${selectedRunItems.size})", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp), fontWeight = FontWeight.Bold, color = VibrantSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                selectedRunItems.forEach { item ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(item.employeeName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = VibrantTextPrimary)
                      Text("${item.department} • Gross: ${viewModel.formatCurrency(item.grossPay)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(viewModel.formatCurrency(item.netPay), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = VibrantTertiary)
                  }
                  HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
              }
            }
          }
        }
      }

      // 3. Historical Runs List Header
      item {
        Text("PAYROLL HISTORY (${payrollRuns.size} COMPLETED)", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), fontWeight = FontWeight.Bold, color = VibrantSecondary)
      }

      if (payrollRuns.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
              Text("No payroll history yet. Tap 'New Payroll Run' to execute.", style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      } else {
        items(payrollRuns, key = { it.id }) { run ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.selectPayrollRun(run) }
              .testTag("payroll_run_card_${run.id}"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.5f)))
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(run.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextPrimary)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "${run.periodStart} – ${run.periodEnd} • ${run.employeeCount} Payees",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(viewModel.formatCurrency(run.totalNet), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTertiary)
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = VibrantSuccessContainer
                  ) {
                    Text(
                      text = "Reconciled",
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      style = MaterialTheme.typography.labelSmall,
                      color = VibrantSuccessText,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(8.dp))
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(14.dp), tint = VibrantPrimary)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("QuickBooks & Xero Synced", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(
                  onClick = { showAccountingModalForRun = run },
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                  modifier = Modifier.height(28.dp)
                ) {
                  Text("View GL Entries →", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantPrimary)
                }
              }
            }
          }
        }
      }
    }
  }

  // New Payroll Run Wizard Dialog
  if (showNewRunDialog) {
    var periodStart by remember { mutableStateOf("Aug 15, 2026") }
    var periodEnd by remember { mutableStateOf("Aug 28, 2026") }

    AlertDialog(
      onDismissRequest = { showNewRunDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = VibrantPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Execute Batch Payroll", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Calculates exact gross wages, federal/state tax withholdings, and FICA for all ${employees.size} active employees.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = periodStart,
            onValueChange = { periodStart = it },
            label = { Text("Period Start Date") },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = periodEnd,
            onValueChange = { periodEnd = it },
            label = { Text("Period End Date") },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
          )

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = VibrantPrimaryContainer.copy(alpha = 0.6f)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text("Summary of Eligible Payees:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantOnPrimaryContainer)
              employees.take(4).forEach { emp ->
                Text("• ${emp.fullName} (${emp.role})", style = MaterialTheme.typography.bodySmall, color = VibrantOnPrimaryContainer)
              }
              if (employees.size > 4) {
                Text("• ...and ${employees.size - 4} more", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.processBulkPayrollRun(periodStart, periodEnd)
            showNewRunDialog = false
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
        ) {
          Text("Calculate & Submit Run")
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewRunDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  showAccountingModalForRun?.let { run ->
    QuickBooksXeroModal(
      journalEntries = viewModel.getJournalEntries(run),
      runTitle = run.title,
      onDismiss = { showAccountingModalForRun = null },
      onExport = { format ->
        viewModel.showNotification("Exported General Ledger to $format format successfully!")
        showAccountingModalForRun = null
      }
    )
  }
}
