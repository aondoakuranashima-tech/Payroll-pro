package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MlLearnedRule
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MlTrainingRuleModal(
  initialMerchant: String = "",
  initialCategory: String = "Cloud & IT Hosting",
  onDismiss: () -> Unit,
  onSaveRule: (merchant: String, category: String, glCode: String) -> Unit
) {
  var merchantKeyword by remember { mutableStateOf(initialMerchant) }
  var selectedCategory by remember { mutableStateOf(initialCategory) }
  var glAccountCode by remember { mutableStateOf("GL #6200 - Cloud Services") }

  val categories = listOf(
    "Cloud & IT Hosting" to "GL #6200 - Cloud Services",
    "SaaS Subscriptions" to "GL #6210 - Software & Tools",
    "Payroll Processing" to "GL #6020 - Payroll Processing Fees",
    "Executive Travel & Lodging" to "GL #6400 - Travel & Lodging",
    "Hardware & IT Equipment" to "GL #1500 - Fixed Assets / Equipment",
    "Facilities & Office Space" to "GL #6100 - Office Lease & Rent",
    "Meals & Team Morale" to "GL #6500 - Meals & Entertainment",
    "Professional Legal & CPA" to "GL #6300 - Professional Fees"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(26.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .testTag("ml_training_rule_modal"),
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
              imageVector = Icons.Default.Psychology,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Train AI Categorization Model", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
            Text("Continuous Learning Feedback Loop", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Text(
          text = "Teach the machine learning classifier how to categorize recurring vendor expenses automatically across bank feeds and scanned receipts.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = merchantKeyword,
          onValueChange = { merchantKeyword = it },
          label = { Text("Merchant / Vendor Name Keyword") },
          placeholder = { Text("e.g. OpenAI, Notion, Delta") },
          leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = VibrantPrimary) },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Text("Target GL Classification Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)

        categories.forEach { (catName, glCode) ->
          val isSelected = selectedCategory == catName
          Surface(
            onClick = {
              selectedCategory = catName
              glAccountCode = glCode
            },
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) VibrantPrimaryContainer else MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(
                if (isSelected) listOf(VibrantPrimary, VibrantPrimary.copy(alpha = 0.5f))
                else listOf(VibrantOutlineVariant, VibrantOutlineVariant)
              )
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(catName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = VibrantTextPrimary)
                Text(glCode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(20.dp))
              }
            }
          }
        }

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = VibrantSuccessContainer.copy(alpha = 0.5f)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VibrantSuccessText, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Applying this rule increases the AI model confidence score to 99.8% for future matches.",
              style = MaterialTheme.typography.labelSmall,
              color = VibrantSuccessText,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (merchantKeyword.isNotBlank()) {
            onSaveRule(merchantKeyword, selectedCategory, glAccountCode)
          }
        },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
      ) {
        Text("Save & Train Rule")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
