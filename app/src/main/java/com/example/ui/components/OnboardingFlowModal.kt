package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompanyProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFlowModal(
  currentProfile: CompanyProfile,
  onComplete: (companyName: String, ein: String, state: String, qb: Boolean, xero: Boolean) -> Unit,
  onDismiss: () -> Unit,
  onOpenVideoTutorials: () -> Unit
) {
  var currentStep by remember { mutableStateOf(1) }
  val totalSteps = 4

  var companyName by remember { mutableStateOf(currentProfile.companyName) }
  var ein by remember { mutableStateOf(currentProfile.taxIdEin) }
  var stateOfRegistration by remember { mutableStateOf(currentProfile.stateOfRegistration) }
  var paySchedule by remember { mutableStateOf("Bi-Weekly (26 Pay Periods)") }

  var connectQuickBooks by remember { mutableStateOf(currentProfile.quickBooksConnected) }
  var connectXero by remember { mutableStateOf(currentProfile.xeroConnected) }
  var connectPlaidBankFeed by remember { mutableStateOf(true) }

  var enableAiAnomalyAudit by remember { mutableStateOf(true) }
  var enableMlReceiptCategorization by remember { mutableStateOf(true) }
  var autoDepositTaxWithholding by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(26.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .testTag("onboarding_flow_modal"),
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
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Enterprise Setup Wizard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VibrantTextNavy
              )
              Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.labelSmall,
                color = VibrantPrimary,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        // Progress Bar
        LinearProgressIndicator(
          progress = { currentStep.toFloat() / totalSteps.toFloat() },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape),
          color = VibrantPrimary,
          trackColor = VibrantPrimaryContainer
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        when (currentStep) {
          1 -> {
            // Step 1: Legal Entity & EIN
            OnboardingStepHeader(
              icon = Icons.Default.Business,
              title = "Company Entity & Tax Details",
              subtitle = "Configure your corporate structure, Federal EIN, and primary tax nexus jurisdiction."
            )

            OutlinedTextField(
              value = companyName,
              onValueChange = { companyName = it },
              label = { Text("Legal Business Name") },
              leadingIcon = { Icon(Icons.Default.CorporateFare, contentDescription = null, tint = VibrantPrimary) },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = ein,
              onValueChange = { ein = it },
              label = { Text("Federal Tax ID / EIN") },
              leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = VibrantPrimary) },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = stateOfRegistration,
              onValueChange = { stateOfRegistration = it },
              label = { Text("Primary State Jurisdiction") },
              leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = VibrantPrimary) },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth()
            )

            Surface(
              shape = RoundedCornerShape(14.dp),
              color = VibrantPrimaryContainer.copy(alpha = 0.5f)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Federal 941 deposit schedule set to Semi-Weekly based on IRS publication 15 rules.",
                  style = MaterialTheme.typography.labelSmall,
                  color = VibrantOnPrimaryContainer
                )
              }
            }
          }

          2 -> {
            // Step 2: Employee Profiles & Direct Deposit
            OnboardingStepHeader(
              icon = Icons.Default.Groups,
              title = "Employee Profiles & Pay Cycle",
              subtitle = "Establish staff compensation parameters, payroll frequencies, and automated ACH direct deposits."
            )

            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Preloaded Seed Employee Roster", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("• 6 Active full-time employees initialized (Engineering, Design, Operations)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("• Automated 2026 W-4 progressive withholding tables enabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("• Pre-tax 401(k) retirement matching configured at standard 5%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }

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
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "ACH Direct Deposit NACHA file generation active with instant validation.",
                  style = MaterialTheme.typography.labelSmall,
                  color = VibrantSuccessText,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          3 -> {
            // Step 3: Accounting & Bank Integration
            OnboardingStepHeader(
              icon = Icons.Default.SyncAlt,
              title = "Connect Cloud Accounting & Bank Feeds",
              subtitle = "Synchronize payroll journals with QuickBooks Online, Xero, and live bank transaction feeds."
            )

            IntegrationToggleRow(
              title = "QuickBooks Online Sync",
              subtitle = "Automatic double-entry GL journal posting (#6000 & #2100)",
              icon = Icons.Default.CloudQueue,
              isChecked = connectQuickBooks,
              onCheckedChange = { connectQuickBooks = it }
            )

            IntegrationToggleRow(
              title = "Xero Accounting Integration",
              subtitle = "Direct reconciliation for wages, taxes, and bank transfers",
              icon = Icons.Default.CloudDone,
              isChecked = connectXero,
              onCheckedChange = { connectXero = it }
            )

            IntegrationToggleRow(
              title = "Plaid Real-Time Bank Feed",
              subtitle = "Stream live transactions for automated expense categorization",
              icon = Icons.Default.AccountBalance,
              isChecked = connectPlaidBankFeed,
              onCheckedChange = { connectPlaidBankFeed = it }
            )
          }

          4 -> {
            // Step 4: AI Safeguards & Tax Compliance
            OnboardingStepHeader(
              icon = Icons.Default.SmartToy,
              title = "AI Anomaly Detection & Safeguards",
              subtitle = "Configure automated machine learning audits to prevent payroll fraud and tax penalties."
            )

            IntegrationToggleRow(
              title = "AI Ghost Employee & Fraud Audits",
              subtitle = "Detect duplicate banking routing codes and abnormal overtime surges",
              icon = Icons.Default.Security,
              isChecked = enableAiAnomalyAudit,
              onCheckedChange = { enableAiAnomalyAudit = it }
            )

            IntegrationToggleRow(
              title = "Self-Learning ML Expense Categorization",
              subtitle = "AI learns custom GL classification rules from user corrections",
              icon = Icons.Default.AutoAwesome,
              isChecked = enableMlReceiptCategorization,
              onCheckedChange = { enableMlReceiptCategorization = it }
            )

            IntegrationToggleRow(
              title = "IRS Form 941 Quarterly Tax Lock",
              subtitle = "Guarantees exact FICA (12.4%) and Medicare (2.9%) reserves",
              icon = Icons.Default.VerifiedUser,
              isChecked = autoDepositTaxWithholding,
              onCheckedChange = { autoDepositTaxWithholding = it }
            )

            // Video Tutorial Callout Card
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenVideoTutorials() },
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = VibrantPrimaryContainer)
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text("Interactive Video Masterclass", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantOnPrimaryContainer)
                  Text("Watch 4 short step-by-step video tutorials on multi-state tax rules & GL sync.", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VibrantPrimary)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (currentStep > 1) {
          OutlinedButton(
            onClick = { currentStep-- },
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("Back")
          }
        }
        Button(
          onClick = {
            if (currentStep < totalSteps) {
              currentStep++
            } else {
              onComplete(companyName, ein, stateOfRegistration, connectQuickBooks, connectXero)
            }
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
          modifier = Modifier.testTag("onboarding_next_btn")
        ) {
          Text(if (currentStep < totalSteps) "Next Step →" else "Finish Setup")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Skip for Now")
      }
    }
  )
}

@Composable
fun OnboardingStepHeader(
  icon: ImageVector,
  title: String,
  subtitle: String
) {
  Row(
    verticalAlignment = Alignment.Top,
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(VibrantPrimaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(22.dp))
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextPrimary)
      Spacer(modifier = Modifier.height(2.dp))
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun IntegrationToggleRow(
  title: String,
  subtitle: String,
  icon: ImageVector,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isChecked) VibrantPrimaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
    ),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isChecked) VibrantPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = VibrantTextPrimary)
          Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      Switch(
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = VibrantPrimary
        )
      )
    }
  }
}
