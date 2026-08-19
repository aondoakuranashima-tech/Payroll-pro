package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun MetricStatCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.testTag("metric_${title.replace(" ", "_").lowercase()}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        listOf(
          MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
          MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
      )
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title.uppercase(),
          style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
          color = VibrantSecondary.copy(alpha = 0.85f),
          fontWeight = FontWeight.Bold
        )
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun MonetizationBanner(
  tier: SubscriptionTier,
  isDismissed: Boolean,
  onUpgradeClick: () -> Unit,
  onDismissClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (tier.isPro || isDismissed) return

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("monetization_ad_banner"),
    shape = RoundedCornerShape(24.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(VibrantGradientStart, VibrantGradientEnd)
          )
        )
        .padding(18.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.White.copy(alpha = 0.2f)
            ) {
              Text(
                text = "POWER USER FEATURES",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = VibrantPrimaryContainer,
                fontWeight = FontWeight.Bold
              )
            }
          }
          IconButton(
            onClick = onDismissClick,
            modifier = Modifier
              .size(28.dp)
              .testTag("dismiss_ad_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Dismiss",
              tint = Color.White.copy(alpha = 0.7f),
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Unlock Custom Financial Reports & AI Sync",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Automated double-entry reconciliation for QuickBooks and Xero with unlimited export capabilities.",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "$49",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "/mo",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.7f),
              modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
            )
          }

          Button(
            onClick = onUpgradeClick,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.White,
              contentColor = VibrantPrimary
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            modifier = Modifier.testTag("upgrade_banner_btn")
          ) {
            Text(
              text = "View Plans",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
fun PayStubSummaryCard(
  calculation: CalculationResult,
  currencySymbol: String = calculation.currencySymbol.ifEmpty { "$" },
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("paystub_summary_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        listOf(
          VibrantOutline,
          VibrantOutlineVariant
        )
      )
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "NET TAKE-HOME PAY",
              style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
              color = VibrantSecondary,
              fontWeight = FontWeight.Bold
            )
            if (calculation.jurisdictionCountryName.isNotBlank()) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = VibrantSecondaryContainer
              ) {
                Text(
                  text = calculation.jurisdictionCountryName,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = VibrantOnSecondaryContainer
                )
              }
            }
          }
          Text(
            text = "$currencySymbol${String.format("%,.2f", calculation.netTakeHomePay)}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = VibrantTertiary
          )
        }
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = VibrantPrimaryContainer
        ) {
          Text(
            text = "Effective: ${calculation.effectiveTaxRatePercent}%",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = VibrantOnPrimaryContainer
          )
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))

      // Breakdown Lines
      PayDetailRow(label = "Gross Earnings", amount = "$currencySymbol${String.format("%,.2f", calculation.grossPay)}", isBold = true)
      if (calculation.preTaxDeductions > 0) {
        PayDetailRow(label = "Pre-Tax Deductions", amount = "-$currencySymbol${String.format("%,.2f", calculation.preTaxDeductions)}", color = VibrantWarningTitle)
      }
      if (calculation.federalWithholding > 0) {
        PayDetailRow(label = "Income Tax / PAYE Withholding", amount = "-$currencySymbol${String.format("%,.2f", calculation.federalWithholding)}", color = VibrantError)
      }
      if (calculation.stateWithholding > 0) {
        PayDetailRow(label = "Regional / State Tax", amount = "-$currencySymbol${String.format("%,.2f", calculation.stateWithholding)}", color = VibrantError)
      }
      if (calculation.statutoryPensionEmployee > 0 || calculation.socialSecurityWithholding > 0) {
        val pensionAmount = if (calculation.statutoryPensionEmployee > 0) calculation.statutoryPensionEmployee else calculation.socialSecurityWithholding
        PayDetailRow(label = "${calculation.statutoryPensionLabel} (Employee)", amount = "-$currencySymbol${String.format("%,.2f", pensionAmount)}", color = VibrantError)
      }
      if (calculation.statutoryHealthEmployee > 0 || calculation.medicareWithholding > 0) {
        val healthAmount = if (calculation.statutoryHealthEmployee > 0) calculation.statutoryHealthEmployee else calculation.medicareWithholding
        PayDetailRow(label = "${calculation.statutoryHealthLabel} (Employee)", amount = "-$currencySymbol${String.format("%,.2f", healthAmount)}", color = VibrantError)
      }
      if (calculation.postTaxDeductions > 0) {
        PayDetailRow(label = "Post-Tax Deductions / Garnishments", amount = "-$currencySymbol${String.format("%,.2f", calculation.postTaxDeductions)}", color = VibrantWarningTitle)
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))

      // Employer Costs
      Text(
        text = "EMPLOYER TOTAL CONTRIBUTION & COST",
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
        fontWeight = FontWeight.Bold,
        color = VibrantPrimary
      )
      Spacer(modifier = Modifier.height(6.dp))
      val empyrPension = if (calculation.employerPensionMatch > 0) calculation.employerPensionMatch else calculation.employerSocialSecurity
      if (empyrPension > 0) {
        PayDetailRow(label = "Employer ${calculation.statutoryPensionLabel} Match", amount = "$currencySymbol${String.format("%,.2f", empyrPension)}")
      }
      val empyrHealth = if (calculation.employerHealthMatch > 0) calculation.employerHealthMatch else calculation.employerMedicare
      if (empyrHealth > 0) {
        PayDetailRow(label = "Employer ${calculation.statutoryHealthLabel} Match", amount = "$currencySymbol${String.format("%,.2f", empyrHealth)}")
      }
      if (calculation.vatGstVasAmount > 0) {
        PayDetailRow(label = "VAT / GST / VAS Rate Impact", amount = "$currencySymbol${String.format("%,.2f", calculation.vatGstVasAmount)}", color = VibrantSecondary)
      }
      if (calculation.thirteenthMonthAccrual > 0) {
        PayDetailRow(label = "Mandatory 13th-Month Bonus Accrual", amount = "$currencySymbol${String.format("%,.2f", calculation.thirteenthMonthAccrual)}", color = VibrantSecondary)
      }
      if (calculation.endOfServiceGratuityAccrual > 0) {
        PayDetailRow(label = "End of Service Gratuity (EOSG) Reserve", amount = "$currencySymbol${String.format("%,.2f", calculation.endOfServiceGratuityAccrual)}", color = VibrantSecondary)
      }
      if (calculation.employerFuta > 0 || calculation.employerSuta > 0) {
        PayDetailRow(label = "Federal & State Unemployment (FUTA/SUTA)", amount = "$currencySymbol${String.format("%,.2f", calculation.employerFuta + calculation.employerSuta)}")
      }
      PayDetailRow(
        label = "Total Statutory Cost to Employer",
        amount = "$currencySymbol${String.format("%,.2f", calculation.totalEmployerCost)}",
        isBold = true,
        color = VibrantPrimary
      )
    }
  }
}

@Composable
fun PayDetailRow(
  label: String,
  amount: String,
  isBold: Boolean = false,
  color: Color = Color.Unspecified
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = amount,
      style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
      color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
    )
  }
}

@Composable
fun AnomalyAlertCard(
  alert: AnomalyAlert,
  modifier: Modifier = Modifier
) {
  val (containerColor, borderColor, titleColor, bodyColor) = when (alert.severity) {
    AnomalySeverity.CRITICAL -> Tuple4(VibrantErrorContainer, VibrantError.copy(alpha = 0.3f), VibrantError, Color(0xFF410002))
    AnomalySeverity.WARNING -> Tuple4(VibrantWarningContainer, VibrantWarningBorder, VibrantWarningTitle, VibrantWarningBody)
    AnomalySeverity.INFO -> Tuple4(VibrantPrimaryContainer.copy(alpha = 0.6f), VibrantOutline, VibrantPrimary, VibrantOnPrimaryContainer)
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = containerColor
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(listOf(borderColor, borderColor.copy(alpha = 0.6f)))
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = alert.severity.name,
          tint = titleColor,
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = "AI ANOMALY ALERT",
          style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
          fontWeight = FontWeight.Bold,
          color = titleColor
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = alert.detail,
        style = MaterialTheme.typography.bodySmall,
        color = bodyColor,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${alert.category} • ${alert.detectedAt}",
          style = MaterialTheme.typography.labelSmall,
          color = bodyColor.copy(alpha = 0.75f)
        )
        Text(
          text = "Review Entry →",
          style = MaterialTheme.typography.labelSmall,
          color = if (alert.severity == AnomalySeverity.WARNING) VibrantWarningAction else titleColor,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun ActionChipButton(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  containerColor: Color = VibrantPrimaryContainer,
  contentColor: Color = VibrantPrimary,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    color = containerColor,
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(
        listOf(contentColor.copy(alpha = 0.25f), contentColor.copy(alpha = 0.1f))
      )
    ),
    modifier = modifier.testTag("action_chip_${label.replace(" ", "_").lowercase()}")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = contentColor,
        modifier = Modifier.size(18.dp)
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = contentColor
      )
    }
  }
}

