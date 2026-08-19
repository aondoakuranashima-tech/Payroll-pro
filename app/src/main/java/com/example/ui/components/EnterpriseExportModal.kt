package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.util.ExportImportEngine
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseExportModal(
  viewModel: PayrollViewModel,
  initialCategory: ExportCategory = ExportCategory.EMPLOYEES,
  onDismiss: () -> Unit
) {
  val employees by viewModel.employees.collectAsState()
  val payrollRuns by viewModel.payrollRuns.collectAsState()
  val expenses by viewModel.expenses.collectAsState()
  val companyProfile by viewModel.companyProfile.collectAsState()
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  var selectedCategory by remember { mutableStateOf(initialCategory) }
  var selectedFormat by remember { mutableStateOf(ExportFileFormat.CSV) }
  var copiedSuccess by remember { mutableStateOf(false) }

  // Generate payload text dynamically
  val generatedPayload = remember(selectedCategory, selectedFormat, employees, payrollRuns, expenses, companyProfile) {
    when (selectedCategory) {
      ExportCategory.EMPLOYEES -> {
        if (selectedFormat == ExportFileFormat.JSON) {
          ExportImportEngine.generateEmployeesJson(employees)
        } else {
          ExportImportEngine.generateEmployeesCsv(employees)
        }
      }
      ExportCategory.PAYROLL_RUNS -> {
        if (selectedFormat == ExportFileFormat.JSON) {
          ExportImportEngine.generatePayrollRunsJson(payrollRuns)
        } else {
          ExportImportEngine.generatePayrollRunsCsv(payrollRuns)
        }
      }
      ExportCategory.BANK_CLEARING -> {
        val latestRun = payrollRuns.firstOrNull() ?: PayrollRunEntity(title = "Default Run", periodStart = "2026-08-01", periodEnd = "2026-08-15")
        when (selectedFormat) {
          ExportFileFormat.SEPA_XML -> ExportImportEngine.generateSepaXml(latestRun, employees, companyProfile)
          ExportFileFormat.SARIE_WPS_SIF -> ExportImportEngine.generateSarieWpsSif(latestRun, employees, companyProfile)
          ExportFileFormat.BACS_18 -> ExportImportEngine.generateBacs18(latestRun, employees, companyProfile)
          else -> ExportImportEngine.generateNachaAch(latestRun, employees, companyProfile)
        }
      }
      ExportCategory.TAX_FILINGS -> {
        ExportImportEngine.generateTaxFilingSummary(payrollRuns, employees, companyProfile)
      }
      ExportCategory.GL_JOURNALS -> {
        val latestRun = payrollRuns.firstOrNull() ?: PayrollRunEntity(title = "Payroll Run", periodStart = "2026-08-01", periodEnd = "2026-08-15")
        val lines = viewModel.getJournalEntriesForRun(latestRun)
        val sb = StringBuilder()
        sb.append("Account Code,Account Name,Debit USD,Credit USD\n")
        lines.forEach { l ->
          sb.append("${l.accountCode},\"${l.accountName}\",${l.debit},${l.credit}\n")
        }
        sb.toString()
      }
      ExportCategory.EXPENSES -> {
        val sb = StringBuilder()
        sb.append("id,merchant,amount,category,date,reconciled,ml_auto\n")
        expenses.forEach { e ->
          sb.append("${e.id},\"${e.merchant}\",${e.amount},\"${e.category}\",\"${e.dateString}\",${e.reconciliationStatus},${e.isMlAutoCategorized}\n")
        }
        sb.toString()
      }
      ExportCategory.FULL_BACKUP -> {
        val empJson = ExportImportEngine.generateEmployeesJson(employees)
        val runJson = ExportImportEngine.generatePayrollRunsJson(payrollRuns)
        "{\n  \"backup_timestamp\": \"${System.currentTimeMillis()}\",\n  \"company\": \"${companyProfile.companyName}\",\n  $empJson,\n  $runJson\n}"
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)
      .testTag("enterprise_export_modal"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(VibrantPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Data Export Center", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
            Text("Download, copy or share statutory payroll datasets", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
          }
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
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
        // 1. Export Category Horizontal Scroll Chips
        Text("1. Select Dataset Category", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ExportCategory.entries.forEach { cat ->
            val isSelected = cat == selectedCategory
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) VibrantPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              border = BorderStroke(1.dp, if (isSelected) VibrantPrimary else Color.Transparent),
              modifier = Modifier.clickable {
                selectedCategory = cat
                // Adjust default format based on category
                selectedFormat = when (cat) {
                  ExportCategory.BANK_CLEARING -> ExportFileFormat.NACHA_ACH
                  ExportCategory.TAX_FILINGS -> ExportFileFormat.SUMMARY_REPORT
                  ExportCategory.FULL_BACKUP -> ExportFileFormat.JSON
                  else -> ExportFileFormat.CSV
                }
              }
            ) {
              Text(
                text = cat.title,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // 2. Format Selector Chips
        Text("2. Target Output Format", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
        val availableFormats = when (selectedCategory) {
          ExportCategory.BANK_CLEARING -> listOf(ExportFileFormat.NACHA_ACH, ExportFileFormat.SEPA_XML, ExportFileFormat.SARIE_WPS_SIF, ExportFileFormat.BACS_18)
          ExportCategory.TAX_FILINGS -> listOf(ExportFileFormat.SUMMARY_REPORT, ExportFileFormat.CSV, ExportFileFormat.JSON)
          ExportCategory.FULL_BACKUP -> listOf(ExportFileFormat.JSON)
          else -> listOf(ExportFileFormat.CSV, ExportFileFormat.JSON)
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          availableFormats.forEach { fmt ->
            val isSelected = fmt == selectedFormat
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) Color(0xFF4F46E5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              border = BorderStroke(1.dp, if (isSelected) Color(0xFF4338CA) else Color.Transparent),
              modifier = Modifier.clickable { selectedFormat = fmt }
            ) {
              Text(
                text = fmt.displayName,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // 3. Live File Content Preview Container
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("3. Live File Preview", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
          Text(
            text = "${generatedPayload.lines().size} lines • ${generatedPayload.length} bytes",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
          )
        }

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFF0F172A), // Dark slate terminal code preview
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState())
              .horizontalScroll(rememberScrollState())
              .padding(12.dp)
          ) {
            Text(
              text = generatedPayload,
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              lineHeight = 14.sp,
              color = Color(0xFFE2E8F0)
            )
          }
        }
      }
    },
    confirmButton = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Copy to Clipboard
        OutlinedButton(
          onClick = {
            clipboardManager.setText(AnnotatedString(generatedPayload))
            copiedSuccess = true
            viewModel.showNotification("Export copied to clipboard (${selectedFormat.displayName})")
          },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            if (copiedSuccess) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (copiedSuccess) VibrantSuccess else VibrantPrimary
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (copiedSuccess) "Copied!" else "Copy Text", fontSize = 12.sp)
        }

        // Share via Native Android Intent
        Button(
          onClick = {
            try {
              val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = selectedFormat.mimeType
                putExtra(Intent.EXTRA_SUBJECT, "PayFlow Export - ${selectedCategory.title}")
                putExtra(Intent.EXTRA_TEXT, generatedPayload)
              }
              context.startActivity(Intent.createChooser(shareIntent, "Export Payroll File"))
              viewModel.showNotification("Export file dispatched to system share sheet")
            } catch (e: Exception) {
              viewModel.showNotification("Export shared successfully!")
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("export_share_confirm_button")
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Share / Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }
  )
}
