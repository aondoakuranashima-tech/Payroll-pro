package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomReportFilter
import com.example.data.model.GeneratedReportData
import com.example.data.model.ReportFormat
import com.example.data.model.ReportType
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReportExportModal(
  viewModel: PayrollViewModel,
  onDismiss: () -> Unit
) {
  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val currentFilter by viewModel.reportFilter.collectAsState()

  var selectedType by remember { mutableStateOf(currentFilter.reportType) }
  var selectedFormat by remember { mutableStateOf(ReportFormat.PDF) }
  var selectedDept by remember { mutableStateOf(currentFilter.selectedDepartment) }
  var selectedPayType by remember { mutableStateOf(currentFilter.selectedPayType) }
  var selectedDateRange by remember { mutableStateOf(currentFilter.dateRange) }
  var isCopied by remember { mutableStateOf(false) }

  // Update filter on changes
  LaunchedEffect(selectedType, selectedDept, selectedPayType, selectedDateRange) {
    viewModel.updateReportFilter(
      reportType = selectedType,
      selectedDepartment = selectedDept,
      selectedPayType = selectedPayType,
      dateRange = selectedDateRange
    )
  }

  val generatedReport = remember(selectedType, selectedFormat, selectedDept, selectedPayType, selectedDateRange) {
    viewModel.generateReport(selectedFormat)
  }
  val exportedString = remember(generatedReport) {
    viewModel.exportReport(generatedReport)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(26.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .testTag("custom_report_export_modal"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(VibrantPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Summarize,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Custom Report & Data Export", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
            Text("GAAP & IRS 941 Compliant Export Engine", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Report Type Selector
        Text("Select Report Template", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ReportType.values().forEach { rType ->
            FilterChip(
              selected = selectedType == rType,
              onClick = { selectedType = rType },
              label = { Text(rType.title, maxLines = 1) },
              shape = RoundedCornerShape(12.dp)
            )
          }
        }

        // Target Export Format
        Text("Export Destination Format", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ReportFormat.values().forEach { fmt ->
            FilterChip(
              selected = selectedFormat == fmt,
              onClick = { selectedFormat = fmt },
              label = { Text(fmt.displayName) },
              shape = RoundedCornerShape(12.dp)
            )
          }
        }

        // Filter Controls (Department & Date Range)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Department", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            val depts = listOf("All Departments", "Engineering", "Design", "Product", "Operations", "Sales")
            var deptExpanded by remember { mutableStateOf(false) }
            Box {
              OutlinedButton(
                onClick = { deptExpanded = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(selectedDept, style = MaterialTheme.typography.labelSmall, maxLines = 1)
              }
              DropdownMenu(expanded = deptExpanded, onDismissRequest = { deptExpanded = false }) {
                depts.forEach { dept ->
                  DropdownMenuItem(
                    text = { Text(dept) },
                    onClick = {
                      selectedDept = dept
                      deptExpanded = false
                    }
                  )
                }
              }
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Text("Period", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            val periods = listOf("Q3 2026 (Jul 1 - Sep 30)", "Q2 2026 (Apr 1 - Jun 30)", "YTD 2026 (Jan 1 - Sep 30)")
            var periodExpanded by remember { mutableStateOf(false) }
            Box {
              OutlinedButton(
                onClick = { periodExpanded = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(selectedDateRange.split(" ").first(), style = MaterialTheme.typography.labelSmall, maxLines = 1)
              }
              DropdownMenu(expanded = periodExpanded, onDismissRequest = { periodExpanded = false }) {
                periods.forEach { period ->
                  DropdownMenuItem(
                    text = { Text(period) },
                    onClick = {
                      selectedDateRange = period
                      periodExpanded = false
                    }
                  )
                }
              }
            }
          }
        }

        // Live Generated Report Content Preview Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Generated ${selectedFormat.name} Stream Output", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = VibrantPrimary
              ) {
                Text("Valid & Ready", color = Color.White, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
              }
            }

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
            ) {
              Text(
                text = exportedString,
                color = Color(0xFF38BDF8),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                lineHeight = 14.sp
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = {
            clipboardManager.setText(AnnotatedString(exportedString))
            isCopied = true
            viewModel.showNotification("Report copied to clipboard in ${selectedFormat.name} format!")
          },
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (isCopied) "Copied!" else "Copy Payload")
        }

        Button(
          onClick = {
            viewModel.showNotification("Exporting ${generatedReport.title}.${selectedFormat.extension.lowercase()} to downloads...")
            onDismiss()
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Download ${selectedFormat.name}")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}
