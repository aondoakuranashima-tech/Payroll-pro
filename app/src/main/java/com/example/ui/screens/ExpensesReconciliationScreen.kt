package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.BankTransaction
import com.example.data.model.ExpenseEntity
import com.example.data.model.MlLearnedRule
import com.example.ui.components.MlTrainingRuleModal
import com.example.ui.components.ScanReceiptModal
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@Composable
fun ExpensesReconciliationScreen(
  viewModel: PayrollViewModel,
  onOpenUpgrade: () -> Unit
) {
  val expenses by viewModel.expenses.collectAsState()
  val bankTransactions by viewModel.bankTransactions.collectAsState()
  val mlRules by viewModel.mlLearnedRules.collectAsState()
  val mlStats = remember(bankTransactions, mlRules) { viewModel.getMlStats() }

  var selectedTab by remember { mutableStateOf(0) }
  var showReceiptScanner by remember { mutableStateOf(false) }
  var showMlTrainer by remember { mutableStateOf(false) }
  var trainingMerchant by remember { mutableStateOf("") }
  var trainingCategory by remember { mutableStateOf("Cloud & IT Hosting") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("expenses_reconciliation_screen"),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header Hero Card: AI Expense & ML Learning Engine
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
                  .size(42.dp)
                  .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(VibrantPrimary, VibrantSecondary))),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text("AI Expense & Bank Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                Text("Continuous ML preference learning engine", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }

            Button(
              onClick = { showReceiptScanner = true },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Scan Receipt", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }

          // ML Stats Metric Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            MlStatBadge(
              title = "Model Accuracy",
              value = "${mlStats.accuracyScore}%",
              icon = Icons.Default.Verified,
              color = VibrantSuccessText,
              modifier = Modifier.weight(1f)
            )
            MlStatBadge(
              title = "Learned Rules",
              value = "${mlStats.userTrainedRulesCount} Active",
              icon = Icons.Default.Psychology,
              color = VibrantPrimary,
              modifier = Modifier.weight(1f)
            )
            MlStatBadge(
              title = "Auto-Classified",
              value = "${mlStats.autoCategorizationRate}%",
              icon = Icons.Default.Bolt,
              color = VibrantWarningAction,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // 2. Navigation TabRow
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
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
            text = { Text("Bank Feeds", style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium) },
            icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Scanned Receipts", style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = { Text("ML Rules", style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium) },
            icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
        }
      }
    }

    // 3. Tab Contents
    when (selectedTab) {
      0 -> {
        // Tab 0: Bank Live Feeds (Plaid synced)
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Live Bank Stream (${bankTransactions.size} Transacted)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
            TextButton(onClick = {
              trainingMerchant = ""
              showMlTrainer = true
            }) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Train Custom Rule", style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        items(bankTransactions) { tx ->
          BankTransactionCard(
            tx = tx,
            onConfirm = {
              viewModel.confirmBankTransaction(tx.id, tx.suggestedCategory)
            },
            onRetrain = {
              trainingMerchant = tx.description.split(" ").take(2).joinToString(" ")
              showMlTrainer = true
            }
          )
        }
      }

      1 -> {
        // Tab 1: Scanned Receipts
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("OCR Processed Receipts (${expenses.size} Items)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
            Button(
              onClick = { showReceiptScanner = true },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
            ) {
              Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Scan New Receipt")
            }
          }
        }

        items(expenses) { exp ->
          ScannedExpenseCard(expense = exp)
        }
      }

      2 -> {
        // Tab 2: Active ML Learned Preference Rules
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Self-Trained Preference Rules (${mlRules.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
            Button(
              onClick = {
                trainingMerchant = ""
                showMlTrainer = true
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("New Rule")
            }
          }
        }

        items(mlRules) { rule ->
          MlLearnedRuleCard(rule = rule)
        }
      }
    }
  }

  // Modals
  if (showReceiptScanner) {
    ScanReceiptModal(
      onDismiss = { showReceiptScanner = false },
      onScanComplete = { vendor, amount ->
        viewModel.autoScanAndAddReceipt(vendor, amount)
        showReceiptScanner = false
      }
    )
  }

  if (showMlTrainer) {
    MlTrainingRuleModal(
      initialMerchant = trainingMerchant,
      initialCategory = trainingCategory,
      onDismiss = { showMlTrainer = false },
      onSaveRule = { merchant, cat, gl ->
        viewModel.learnUserRule(merchant, cat, gl)
        showMlTrainer = false
      }
    )
  }
}

@Composable
fun MlStatBadge(
  title: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = color.copy(alpha = 0.1f),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
      Text(title, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun BankTransactionCard(
  tx: BankTransaction,
  onConfirm: () -> Unit,
  onRetrain: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        if (tx.status == "User Confirmed") listOf(VibrantSuccess.copy(alpha = 0.3f), Color.Transparent)
        else listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.5f))
      )
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
          Text(tx.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = VibrantTextPrimary)
          Text("${tx.bankName} • ${tx.date}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
          text = "-$${"%.2f".format(tx.amount)}",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = VibrantTextNavy
        )
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = VibrantPrimaryContainer.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = tx.suggestedCategory,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = VibrantOnPrimaryContainer
            )
          }
          Text(
            text = "${(tx.mlConfidence * 100).toInt()}% match",
            style = MaterialTheme.typography.labelSmall,
            color = VibrantPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = onRetrain,
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Retrain Rule", style = MaterialTheme.typography.labelSmall)
        }

        if (tx.status != "User Confirmed") {
          Button(
            onClick = onConfirm,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VibrantSuccess),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reconcile GL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }
        } else {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = VibrantSuccessContainer
          ) {
            Text(
              "Reconciled ✓",
              style = MaterialTheme.typography.labelSmall,
              color = VibrantSuccessText,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun ScannedExpenseCard(expense: ExpenseEntity) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(VibrantPrimaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Receipt, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(expense.merchant, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = VibrantTextPrimary)
          Text("${expense.category} • ${(expense.mlConfidenceScore * 100).toInt()}% ML Confidence", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      Column(horizontalAlignment = Alignment.End) {
        Text("$${"%.2f".format(expense.amount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
        Text(expense.reconciliationStatus, style = MaterialTheme.typography.labelSmall, color = VibrantSuccessText, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
fun MlLearnedRuleCard(rule: MlLearnedRule) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Psychology, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(rule.keywordOrMerchant, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = VibrantSuccessContainer
        ) {
          Text("Applied ${rule.timesApplied}x", color = VibrantSuccessText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Maps to: ${rule.mappedCategory}", style = MaterialTheme.typography.bodySmall, color = VibrantTextPrimary, fontWeight = FontWeight.SemiBold)
        Text(rule.glAccountCode, style = MaterialTheme.typography.labelSmall, color = VibrantPrimary)
      }

      Text(
        text = "${rule.userPreferenceNote} • Trained ${rule.createdAt}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
