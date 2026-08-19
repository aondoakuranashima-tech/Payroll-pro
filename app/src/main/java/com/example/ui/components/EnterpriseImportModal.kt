package com.example.ui.components

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
import androidx.compose.ui.platform.testTag
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
fun EnterpriseImportModal(
  viewModel: PayrollViewModel,
  initialTarget: ImportTargetType = ImportTargetType.EMPLOYEES,
  onDismiss: () -> Unit
) {
  val clipboardManager = LocalClipboardManager.current

  var selectedTarget by remember { mutableStateOf(initialTarget) }
  var rawCsvInput by remember { mutableStateOf(ExportImportEngine.getSampleEmployeesCsv()) }

  // Parse and validate live
  val validationResult = remember(selectedTarget, rawCsvInput) {
    when (selectedTarget) {
      ImportTargetType.EMPLOYEES -> ExportImportEngine.parseEmployeesCsv(rawCsvInput)
      ImportTargetType.PAYROLL_RUNS -> ExportImportEngine.parsePayrollRunsCsv(rawCsvInput)
      ImportTargetType.EXPENSES -> ExportImportEngine.parseExpensesCsv(rawCsvInput)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)
      .testTag("enterprise_import_modal"),
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
              .background(Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1)))),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Bulk Data Importer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
            Text("Import CSV datasets into local Room SQLite database", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
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
        // 1. Target Selector
        Text("1. Select Destination Table", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ImportTargetType.entries.forEach { target ->
            val isSelected = target == selectedTarget
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = BorderStroke(1.dp, if (isSelected) Color(0xFF0369A1) else Color.Transparent),
              modifier = Modifier.clickable {
                selectedTarget = target
                rawCsvInput = when (target) {
                  ImportTargetType.EMPLOYEES -> ExportImportEngine.getSampleEmployeesCsv()
                  ImportTargetType.PAYROLL_RUNS -> ExportImportEngine.getSamplePayrollRunsCsv()
                  ImportTargetType.EXPENSES -> ExportImportEngine.getSampleExpensesCsv()
                }
              }
            ) {
              Text(
                text = target.title,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // 2. Preloaded Preset Datasets
        if (selectedTarget == ImportTargetType.EMPLOYEES) {
          Text("2. Quick Preset Workforce Datasets", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(
              "US Tech Team (10 Staff)" to "US_TECH_10",
              "Europe Remote (8 Devs)" to "EUROPE_REMOTE_8",
              "LATAM & MENA (6 Staff)" to "LATAM_MENA_6"
            ).forEach { (label, key) ->
              OutlinedButton(
                onClick = { rawCsvInput = ExportImportEngine.getPresetDataset(key) },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 11.sp)
              }
            }
          }
        }

        // 3. Raw CSV Text Editor Input
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("3. Paste CSV Data (or edit below)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
          TextButton(
            onClick = {
              val clip = clipboardManager.getText()?.text
              if (!clip.isNullOrBlank()) {
                rawCsvInput = clip
                viewModel.showNotification("Pasted data from clipboard")
              }
            },
            contentPadding = PaddingValues(0.dp)
          ) {
            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Paste Clipboard", fontSize = 11.sp)
          }
        }

        OutlinedTextField(
          value = rawCsvInput,
          onValueChange = { rawCsvInput = it },
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .testTag("import_csv_text_input"),
          textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 14.sp),
          shape = RoundedCornerShape(12.dp),
          placeholder = { Text("first_name,last_name,email,department,role,base_salary...", fontSize = 10.sp) }
        )

        // 4. Real-time Pre-Flight Validation Bar
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (validationResult.invalidRowsCount > 0) VibrantWarningContainer else VibrantSuccessContainer,
          border = BorderStroke(1.dp, if (validationResult.invalidRowsCount > 0) VibrantWarningBorder else Color(0xFFA7F3D0))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                if (validationResult.invalidRowsCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (validationResult.invalidRowsCount > 0) VibrantWarningTitle else VibrantSuccessText,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "${validationResult.validRowsCount} Valid Records Ready",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = if (validationResult.invalidRowsCount > 0) VibrantWarningTitle else VibrantSuccessText
                )
                if (validationResult.invalidRowsCount > 0) {
                  Text(
                    text = "${validationResult.invalidRowsCount} invalid rows will be skipped",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = VibrantWarningBody
                  )
                }
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.White.copy(alpha = 0.8f)
            ) {
              Text(
                text = "${validationResult.totalRows} Rows",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = VibrantTextNavy
              )
            }
          }
        }

        // Error callouts if any
        if (validationResult.errorMessages.isNotEmpty()) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            validationResult.errorMessages.take(3).forEach { err ->
              Text("• $err", style = MaterialTheme.typography.labelSmall, color = VibrantError, fontSize = 10.5.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          when (selectedTarget) {
            ImportTargetType.EMPLOYEES -> {
              if (validationResult.parsedEmployees.isNotEmpty()) {
                viewModel.importEmployeesBulk(validationResult.parsedEmployees)
                viewModel.showNotification("Successfully imported ${validationResult.parsedEmployees.size} employees into Room DB!")
                onDismiss()
              } else {
                viewModel.showNotification("No valid employee records to import", true)
              }
            }
            ImportTargetType.PAYROLL_RUNS -> {
              if (validationResult.parsedPayrollRuns.isNotEmpty()) {
                viewModel.importPayrollRunsBulk(validationResult.parsedPayrollRuns)
                viewModel.showNotification("Successfully imported ${validationResult.parsedPayrollRuns.size} payroll runs into Room DB!")
                onDismiss()
              } else {
                viewModel.showNotification("No valid payroll runs to import", true)
              }
            }
            ImportTargetType.EXPENSES -> {
              if (validationResult.parsedExpenses.isNotEmpty()) {
                viewModel.importExpensesBulk(validationResult.parsedExpenses)
                viewModel.showNotification("Successfully imported ${validationResult.parsedExpenses.size} expense line items!")
                onDismiss()
              } else {
                viewModel.showNotification("No valid expense records to import", true)
              }
            }
          }
        },
        enabled = validationResult.validRowsCount > 0,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.testTag("import_confirm_commit_button")
      ) {
        Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Import ${validationResult.validRowsCount} Records to Database", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
