package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AuthSetupModal
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseSecurityCenterScreen(
  viewModel: PayrollViewModel,
  modifier: Modifier = Modifier
) {
  val authUser by viewModel.authUser.collectAsState()
  val registeredPasskeys by viewModel.registeredPasskeys.collectAsState()
  val activeSessions by viewModel.activeSessions.collectAsState()
  val auditLogs by viewModel.securityAuditLogs.collectAsState()
  val policies by viewModel.securityPolicies.collectAsState()
  val clipboardManager = LocalClipboardManager.current

  var showAddPasskeyDialog by remember { mutableStateOf(false) }
  var showMfaSetupDialog by remember { mutableStateOf(false) }
  var showAuthSetupModal by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("enterprise_security_center_screen"),
    contentPadding = PaddingValues(bottom = 96.dp, top = 12.dp, start = 16.dp, end = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Enterprise Security Health Score Hero
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VibrantPrimaryContainer),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(listOf(VibrantOutline, VibrantPrimary))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(VibrantPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text("Enterprise Security Posture", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                Text("SOC 2 Type II • ISO 27001 • FIPS 140-3", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = VibrantSuccessContainer
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "${authUser.securityHealthScore}/100",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = VibrantSuccessText
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Quick Security Check Stats
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            SecurityBadgeItem(
              icon = Icons.Default.VpnKey,
              title = "MFA Active",
              subtitle = authUser.activeMfaType.displayName.take(15) + "...",
              modifier = Modifier.weight(1f)
            )
            SecurityBadgeItem(
              icon = Icons.Default.Key,
              title = "Passkeys",
              subtitle = "${registeredPasskeys.size} Enrolled Keys",
              modifier = Modifier.weight(1f)
            )
            SecurityBadgeItem(
              icon = Icons.Default.Devices,
              title = "Sessions",
              subtitle = "${activeSessions.size} Active Devices",
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { showAuthSetupModal = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
            modifier = Modifier.fillMaxWidth().testTag("open_auth_setup_btn")
          ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Auth Setup & Zero-Trust Policies", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { viewModel.lockApp() },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantPrimary),
              modifier = Modifier.weight(1f).testTag("quick_lock_app_btn")
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Lock App", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { viewModel.logout() },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantError),
              modifier = Modifier.weight(1f).testTag("quick_logout_btn")
            ) {
              Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Sign Out", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 2. Multi-Factor Authentication (MFA / 2FA) Section
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Password, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Two-Factor Authentication (2FA)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Requires second factor on untrusted devices", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Switch(
              checked = authUser.is2faEnabled,
              onCheckedChange = { viewModel.toggle2fa(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = VibrantPrimary)
            )
          }

          if (authUser.is2faEnabled) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text("SELECT ACTIVE PRIMARY MFA METHOD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
            Spacer(modifier = Modifier.height(8.dp))

            MfaType.values().forEach { type ->
              val isSelected = authUser.activeMfaType == type
              Surface(
                onClick = { viewModel.toggle2fa(true, type) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) VibrantPrimaryContainer else MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder().copy(
                  brush = Brush.linearGradient(
                    if (isSelected) listOf(VibrantPrimary, VibrantPrimary)
                    else listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant)
                  )
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 3.dp)
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                      imageVector = when (type) {
                        MfaType.AUTHENTICATOR_APP -> Icons.Default.Smartphone
                        MfaType.PASSKEY_FIDO2 -> Icons.Default.Key
                        MfaType.BIOMETRIC_SENSORS -> Icons.Default.Fingerprint
                        MfaType.SMS_BACKUP -> Icons.Default.Message
                        MfaType.EMAIL_OTP -> Icons.Default.Mail
                      },
                      contentDescription = null,
                      tint = if (isSelected) VibrantPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(type.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isSelected) VibrantTextNavy else MaterialTheme.colorScheme.onSurface)
                      Text(type.provider, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
                  RadioButton(selected = isSelected, onClick = { viewModel.toggle2fa(true, type) })
                }
              }
            }
          }
        }
      }
    }

    // 3. Biometric Sensors (Face Unlock & Fingerprint)
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Biometric Hardware Sensors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text("Android BiometricPrompt Level 3 (StrongBox Bound)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          // Fingerprint
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Fingerprint Sensor Scanner", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
              Text("Ultra-fast sub-second payroll authorization", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = authUser.isFingerprintEnabled,
              onCheckedChange = { viewModel.toggleBiometrics(it) }
            )
          }

          HorizontalDivider()

          // Face Unlock
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("3D Face Recognition / Unlock", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
              Text("Cryptographic neural depth mesh match", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = authUser.isFaceUnlockEnabled,
              onCheckedChange = { viewModel.toggleFaceUnlock(it) }
            )
          }
        }
      }
    }

    // 4. FIDO2 / WebAuthn Hardware Passkeys Vault
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Key, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Hardware Passkeys Vault (FIDO2)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Phishing-resistant cryptographic public keys", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            FilledTonalButton(
              onClick = { showAddPasskeyDialog = true },
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Enroll Key", style = MaterialTheme.typography.labelSmall)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          registeredPasskeys.forEach { passkey ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = VibrantSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(VibrantPrimaryContainer),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(18.dp))
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(passkey.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text("${passkey.transportType} • Last used: ${passkey.lastUsed}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }

                IconButton(
                  onClick = { viewModel.removePasskey(passkey.id) },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(Icons.Default.DeleteOutline, contentDescription = "Delete key", tint = VibrantError, modifier = Modifier.size(18.dp))
                }
              }
            }
          }
        }
      }
    }

    // 5. Active Sessions & Remote Device Management
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Devices, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Active Sessions & Devices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${activeSessions.size} devices currently authenticated", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            TextButton(
              onClick = { viewModel.terminateAllOtherSessions() },
              colors = ButtonDefaults.textButtonColors(contentColor = VibrantError)
            ) {
              Text("Revoke Others", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          activeSessions.forEach { session ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (session.isCurrent) VibrantPrimaryContainer.copy(alpha = 0.5f) else VibrantSurfaceVariant.copy(alpha = 0.5f),
              border = if (session.isCurrent) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(VibrantPrimary, VibrantPrimary.copy(alpha = 0.5f)))) else null,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(
                    imageVector = if (session.platform.contains("Android")) Icons.Default.PhoneAndroid else Icons.Default.Laptop,
                    contentDescription = null,
                    tint = if (session.isCurrent) VibrantPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(session.deviceName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                      if (session.isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = VibrantSuccessContainer) {
                          Text("THIS DEVICE", modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = VibrantSuccessText, fontWeight = FontWeight.Bold)
                        }
                      }
                    }
                    Text("${session.platform} • ${session.ipAddress}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${session.location} • ${session.lastActive}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }

                if (!session.isCurrent) {
                  IconButton(
                    onClick = { viewModel.terminateSession(session.id) },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(Icons.Default.Close, contentDescription = "Terminate Session", tint = VibrantError, modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
        }
      }
    }

    // 6. Enterprise Zero-Trust Policies
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Zero Trust & Cryptographic Policies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text("Automated runtime integrity evaluation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          PolicyToggleRow(
            title = "Hardware StrongBox KeyStore",
            subtitle = "Keys never leave hardware security module",
            checked = policies.hardwareStrongBoxEnclave,
            onChecked = { viewModel.updateSecurityPolicies(policies.copy(hardwareStrongBoxEnclave = it)) }
          )

          PolicyToggleRow(
            title = "Require Biometrics for Batch Payroll",
            subtitle = "Step-up prompt before disbursing ACH bank batches",
            checked = policies.requireBiometricsForPayrollRun,
            onChecked = { viewModel.updateSecurityPolicies(policies.copy(requireBiometricsForPayrollRun = it)) }
          )

          PolicyToggleRow(
            title = "TLS 1.3 Certificate Pinning",
            subtitle = "Mitigates man-in-the-middle network interception",
            checked = policies.tlsCertificatePinning,
            onChecked = { viewModel.updateSecurityPolicies(policies.copy(tlsCertificatePinning = it)) }
          )

          PolicyToggleRow(
            title = "Screen Capture & Spyware Shield",
            subtitle = "Enforce FLAG_SECURE against malicious overlays",
            checked = policies.preventScreenCapture,
            onChecked = { viewModel.updateSecurityPolicies(policies.copy(preventScreenCapture = it)) }
          )
        }
      }
    }

    // 7. Security Audit Log
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Enterprise Security Audit Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text("Immutable cryptographic event stream", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          auditLogs.take(6).forEach { log ->
            val severityColor = when (log.severity) {
              AuditSeverity.CRITICAL -> VibrantError
              AuditSeverity.HIGH -> VibrantWarningTitle
              AuditSeverity.MEDIUM -> VibrantSecondary
              AuditSeverity.LOW -> VibrantSuccessText
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = VibrantSurfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(log.action, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = severityColor.copy(alpha = 0.15f)
                  ) {
                    Text(
                      text = log.status,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      style = MaterialTheme.typography.labelSmall,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = severityColor
                    )
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "${log.timestamp} • ${log.actor} • IP: ${log.ipAddress}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }

  // Dialog: Add Hardware Passkey
  if (showAddPasskeyDialog) {
    var passkeyName by remember { mutableStateOf("YubiKey 5C NFC (Backup)") }
    var transportType by remember { mutableStateOf("USB/NFC Hardware Security Key") }

    AlertDialog(
      onDismissRequest = { showAddPasskeyDialog = false },
      shape = RoundedCornerShape(24.dp),
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Key, contentDescription = null, tint = VibrantPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Enroll New FIDO2 Passkey", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Connect your hardware security key (YubiKey, Titan Key, or platform biometric) to bind a new FIDO2 public key credential.")
          OutlinedTextField(
            value = passkeyName,
            onValueChange = { passkeyName = it },
            label = { Text("Passkey Name") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.enrollNewPasskey(passkeyName, transportType)
            showAddPasskeyDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
        ) {
          Text("Enroll in StrongBox")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddPasskeyDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun SecurityBadgeItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    border = CardDefaults.outlinedCardBorder(),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Icon(icon, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
      Text(subtitle, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun PolicyToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onChecked: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
      Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Switch(
      checked = checked,
      onCheckedChange = onChecked,
      colors = SwitchDefaults.colors(checkedThumbColor = VibrantPrimary)
    )
  }
}
