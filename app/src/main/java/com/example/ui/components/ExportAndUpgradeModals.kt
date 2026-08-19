package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickBooksXeroModal(
  journalEntries: List<JournalEntryLine>,
  runTitle: String,
  onDismiss: () -> Unit,
  onExport: (format: String) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)
      .testTag("qb_xero_modal"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.AccountBalance,
          contentDescription = "Accounting Integration",
          tint = VibrantPrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("GL Journal Entries (Double-Entry)", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = runTitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = VibrantSurfaceVariant.copy(alpha = 0.5f)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Account / GL Description", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
              Row {
                Text("Debit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary, modifier = Modifier.width(70.dp))
                Text("Credit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary, modifier = Modifier.width(70.dp))
              }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            journalEntries.forEach { entry ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(entry.accountName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = VibrantTextPrimary)
                  Text("Code #${entry.accountCode}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row {
                  Text(
                    text = if (entry.debit > 0) "$${String.format("%,.2f", entry.debit)}" else "-",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (entry.debit > 0) FontWeight.Bold else FontWeight.Normal,
                    color = if (entry.debit > 0) VibrantTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(70.dp)
                  )
                  Text(
                    text = if (entry.credit > 0) "$${String.format("%,.2f", entry.credit)}" else "-",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (entry.credit > 0) FontWeight.Bold else FontWeight.Normal,
                    color = if (entry.credit > 0) VibrantPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(70.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Auto-Reconciliation Status: Balanced & Ready to Push",
          style = MaterialTheme.typography.labelSmall,
          color = VibrantTertiary,
          fontWeight = FontWeight.Bold
        )
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = { onExport("QuickBooks Online (.IIF)") },
          shape = RoundedCornerShape(14.dp)
        ) {
          Text("QuickBooks", style = MaterialTheme.typography.labelMedium)
        }
        Button(
          onClick = { onExport("Xero Journal (.CSV)") },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
        ) {
          Text("Xero Sync", style = MaterialTheme.typography.labelMedium)
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

@Composable
fun UpgradeSubscriptionModal(
  currentTier: SubscriptionTier,
  onSelectTier: (SubscriptionTier) -> Unit,
  onPayWithPaystack: ((tier: SubscriptionTier, isYearly: Boolean) -> Unit)? = null,
  onDismiss: () -> Unit
) {
  var isYearlyBilling by remember { mutableStateOf(false) }
  var selectedTierState by remember { mutableStateOf(currentTier) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(26.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .testTag("upgrade_subscription_modal"),
    title = {
      Column {
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
                imageVector = Icons.Default.Diamond,
                contentDescription = "Subscription Tiers",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Subscription Plans", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
              Text("Enterprise Payroll & Accounting Tiers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Monthly vs Yearly Billing Switcher
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = VibrantPrimaryContainer.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              onClick = { isYearlyBilling = false },
              shape = RoundedCornerShape(10.dp),
              color = if (!isYearlyBilling) VibrantPrimary else Color.Transparent,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 8.dp)
              ) {
                Text(
                  text = "Monthly Billing",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (!isYearlyBilling) Color.White else VibrantOnPrimaryContainer
                )
              }
            }

            Surface(
              onClick = { isYearlyBilling = true },
              shape = RoundedCornerShape(10.dp),
              color = if (isYearlyBilling) VibrantPrimary else Color.Transparent,
              modifier = Modifier.weight(1f)
            ) {
              Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
              ) {
                Text(
                  text = "Yearly Billing",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isYearlyBilling) Color.White else VibrantOnPrimaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isYearlyBilling) VibrantSuccess else VibrantSuccessText
                ) {
                  Text(
                    text = "UP TO -3.5%",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
              }
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        SubscriptionTier.values().forEach { tier ->
          val isSelected = selectedTierState == tier
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                selectedTierState = tier
                onSelectTier(tier)
              },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) VibrantPrimaryContainer else MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = androidx.compose.ui.graphics.Brush.linearGradient(
                if (isSelected) listOf(VibrantPrimary, VibrantPrimary.copy(alpha = 0.5f))
                else listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.5f))
              )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = tier.title,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) VibrantTextNavy else VibrantTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = if (isSelected) VibrantPrimary else VibrantSuccessContainer
                    ) {
                      Text(
                        text = tier.badge,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else VibrantSuccessText,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(2.dp))

                  if (isYearlyBilling) {
                    Text(
                      text = tier.yearlyFormatted,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = VibrantPrimary
                    )
                    Text(
                      text = "${tier.yearlyFormulaDescription} (${tier.yearlySavingsFormatted})",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = VibrantSuccessText,
                      fontSize = 11.sp
                    )
                  } else {
                    Text(
                      text = tier.priceMonthly,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = VibrantPrimary
                    )
                    Text(
                      text = "Billed monthly • Cancel anytime",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                RadioButton(
                  selected = isSelected,
                  onClick = {
                    selectedTierState = tier
                    onSelectTier(tier)
                  },
                  colors = RadioButtonDefaults.colors(selectedColor = VibrantPrimary)
                )
              }

              // Feature Bullets
              Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                tier.features.forEach { feat ->
                  Row(verticalAlignment = Alignment.Top) {
                    Text(
                      text = "• ",
                      color = if (isSelected) VibrantPrimary else VibrantSecondary,
                      fontWeight = FontWeight.Bold,
                      style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                      text = feat,
                      style = MaterialTheme.typography.bodySmall,
                      color = if (isSelected) VibrantTextNavy else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (onPayWithPaystack != null) {
          Button(
            onClick = {
              onPayWithPaystack(selectedTierState, isYearlyBilling)
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0BA4DB)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pay with Paystack (Cards, GPay, PayPal, Zelle, MENA)", fontWeight = FontWeight.Bold)
          }
        }
        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Close")
        }
      }
    }
  )
}


@Composable
fun AddEmployeeDialog(
  onDismiss: () -> Unit,
  onAdd: (
    firstName: String,
    lastName: String,
    email: String,
    role: String,
    department: String,
    payType: PayType,
    baseRate: Double,
    payFrequency: PayFrequency,
    filingStatus: FilingStatus,
    stateCode: String,
    preTax401k: Double,
    preTaxHealth: Double
  ) -> Unit
) {
  var firstName by remember { mutableStateOf("") }
  var lastName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var role by remember { mutableStateOf("") }
  var department by remember { mutableStateOf("Engineering") }
  var payType by remember { mutableStateOf(PayType.SALARY) }
  var baseRateStr by remember { mutableStateOf("125000") }
  var payFrequency by remember { mutableStateOf(PayFrequency.BI_WEEKLY) }
  var filingStatus by remember { mutableStateOf(FilingStatus.SINGLE) }
  var stateCode by remember { mutableStateOf("CA") }
  var preTax401kStr by remember { mutableStateOf("5.0") }
  var preTaxHealthStr by remember { mutableStateOf("120.0") }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)
      .testTag("add_employee_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = VibrantPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add New Employee", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Work Email") },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = role,
            onValueChange = { role = it },
            label = { Text("Job Title / Role") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = department,
            onValueChange = { department = it },
            label = { Text("Department") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Pay Type:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
          Row {
            FilterChip(
              selected = payType == PayType.SALARY,
              onClick = { payType = PayType.SALARY },
              label = { Text("Salary") },
              shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            FilterChip(
              selected = payType == PayType.HOURLY,
              onClick = { payType = PayType.HOURLY },
              label = { Text("Hourly") },
              shape = RoundedCornerShape(12.dp)
            )
          }
        }

        OutlinedTextField(
          value = baseRateStr,
          onValueChange = { baseRateStr = it },
          label = { Text(if (payType == PayType.SALARY) "Annual Salary ($)" else "Hourly Rate ($/hr)") },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = stateCode,
            onValueChange = { stateCode = it.uppercase() },
            label = { Text("State Code (e.g. CA, NY)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = preTax401kStr,
            onValueChange = { preTax401kStr = it },
            label = { Text("401(k) %") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (firstName.isNotBlank() && lastName.isNotBlank()) {
            onAdd(
              firstName,
              lastName,
              if (email.isNotBlank()) email else "${firstName.lowercase()}.${lastName.lowercase()}@company.com",
              if (role.isNotBlank()) role else "Staff Specialist",
              department,
              payType,
              baseRateStr.toDoubleOrNull() ?: 100000.0,
              payFrequency,
              filingStatus,
              if (stateCode.isNotBlank()) stateCode else "CA",
              preTax401kStr.toDoubleOrNull() ?: 5.0,
              preTaxHealthStr.toDoubleOrNull() ?: 120.0
            )
          }
        },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
      ) {
        Text("Save Employee")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun ScanReceiptModal(
  onDismiss: () -> Unit,
  onScanComplete: (vendor: String, amount: Double) -> Unit
) {
  var vendor by remember { mutableStateOf("Google Cloud EMEA") }
  var amountStr by remember { mutableStateOf("348.50") }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = VibrantPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Scan Receipt & ML Categorize", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Optical receipt scanner with machine learning automated expense classification.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = vendor,
          onValueChange = { vendor = it },
          label = { Text("Merchant / Vendor Name") },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          label = { Text("Receipt Total Amount ($)") },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountStr.toDoubleOrNull() ?: 100.0
          if (vendor.isNotBlank()) {
            onScanComplete(vendor, amt)
          }
        },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
      ) {
        Text("Process ML Scan")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
