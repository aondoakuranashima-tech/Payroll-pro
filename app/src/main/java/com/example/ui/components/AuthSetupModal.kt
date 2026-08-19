package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSetupModal(
  viewModel: PayrollViewModel,
  onDismiss: () -> Unit
) {
  val authUser by viewModel.authUser.collectAsState()
  val securityPolicies by viewModel.securityPolicies.collectAsState()
  val passkeys by viewModel.registeredPasskeys.collectAsState()
  val clipboardManager = LocalClipboardManager.current

  var selectedTab by remember { mutableStateOf(0) } // 0: App Lock PIN, 1: 2FA TOTP, 2: Passkeys, 3: Enterprise SSO

  // PIN state
  var isPinEnabled by remember { mutableStateOf(true) }
  var pinValue by remember { mutableStateOf("1234") }
  var autoLockMinutes by remember { mutableStateOf(5) }
  var biometricEnabled by remember { mutableStateOf(true) }

  // 2FA state
  var is2faActive by remember { mutableStateOf(true) }
  var totpSecret by remember { mutableStateOf("JBSWY3DPEHPK3PXP") }
  var backupCodes by remember {
    mutableStateOf(
      listOf("8F92-4A1C", "99B2-E801", "7C33-D441", "12EA-90F3", "66D1-AA94", "4E81-19B0", "33F9-281C", "55A8-CC72")
    )
  }

  // Passkey enrollment state
  var newPasskeyName by remember { mutableStateOf("") }
  var isEnrollingPasskey by remember { mutableStateOf(false) }

  // SSO state
  var ssoDomain by remember { mutableStateOf("acme-global.com") }
  var ssoProvider by remember { mutableStateOf("Google Workspace") }
  var ssoEnforce2fa by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)
      .testTag("auth_setup_modal"),
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
              .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF0F766E)))),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Authentication Setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
            Text("Configure PIN, 2FA, Biometrics & Enterprise SSO", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
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
        // Tab Selector Row
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          shape = RoundedCornerShape(14.dp),
          indicator = {},
          divider = {}
        ) {
          listOf("PIN & Lock", "2FA TOTP", "Passkeys", "SSO / SAML").forEachIndexed { idx, title ->
            val selected = selectedTab == idx
            Tab(
              selected = selected,
              onClick = { selectedTab = idx },
              modifier = Modifier
                .padding(3.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) Color(0xFF0D9488) else Color.Transparent)
            ) {
              Text(
                text = title,
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
          }
        }

        when (selectedTab) {
          0 -> {
            // TAB 0: PIN & AUTO-LOCK CONFIGURATION
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text("Require PIN to Open App", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                      Text("Prompt for 4-digit security code on launch", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
                    }
                    Switch(
                      checked = isPinEnabled,
                      onCheckedChange = { isPinEnabled = it }
                    )
                  }

                  if (isPinEnabled) {
                    OutlinedTextField(
                      value = pinValue,
                      onValueChange = { if (it.length <= 6) pinValue = it },
                      label = { Text("App Lock PIN Code (4-6 digits)") },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                      visualTransformation = PasswordVisualTransformation(),
                      shape = RoundedCornerShape(12.dp),
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                }
              }

              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text("Biometric Authentication", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                      Text("Unlock via Fingerprint or Face Recognition", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
                    }
                    Switch(
                      checked = biometricEnabled,
                      onCheckedChange = { biometricEnabled = it }
                    )
                  }

                  HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                  Text("Inactivity Auto-Lock Timeout", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    listOf(1 to "1 Min", 5 to "5 Min", 15 to "15 Min", 30 to "30 Min", 0 to "Immediately").forEach { (mins, label) ->
                      val isSelected = autoLockMinutes == mins
                      Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF0D9488) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F766E) else Color.Transparent),
                        modifier = Modifier.clickable { autoLockMinutes = mins }
                      ) {
                        Text(
                          text = label,
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                          color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }
                  }
                }
              }
            }
          }

          1 -> {
            // TAB 1: TWO-FACTOR TOTP
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF0D9488))
                      Spacer(modifier = Modifier.width(8.dp))
                      Column {
                        Text("Authenticator App (TOTP)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                        Text("Google Authenticator, 1Password, Authy", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
                      }
                    }
                    Switch(
                      checked = is2faActive,
                      onCheckedChange = { is2faActive = it }
                    )
                  }

                  if (is2faActive) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Text("Secret Setup Key", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = Color(0xFF1E293B)
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = totpSecret,
                          fontFamily = FontFamily.Monospace,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFF38BDF8),
                          fontSize = 12.sp
                        )
                        IconButton(
                          onClick = {
                            clipboardManager.setText(AnnotatedString(totpSecret))
                            viewModel.showNotification("TOTP Secret copied to clipboard!")
                          },
                          modifier = Modifier.size(24.dp)
                        ) {
                          Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                      }
                    }

                    // Simulated Live Rolling TOTP Token
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text("Current Rolling Code", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary)
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFCCFBF1)
                      ) {
                        Text(
                          text = "849 201",
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.Bold,
                          fontFamily = FontFamily.Monospace,
                          color = Color(0xFF0F766E)
                        )
                      }
                    }
                  }
                }
              }

              // Recovery Backup Codes
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Emergency Backup Codes", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                    TextButton(
                      onClick = {
                        val codesText = backupCodes.joinToString("\n")
                        clipboardManager.setText(AnnotatedString(codesText))
                        viewModel.showNotification("Backup codes copied!")
                      },
                      contentPadding = PaddingValues(0.dp)
                    ) {
                      Text("Copy All", fontSize = 11.sp)
                    }
                  }

                  // 2-column grid of codes
                  backupCodes.chunked(2).forEach { row ->
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      row.forEach { code ->
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = MaterialTheme.colorScheme.surface,
                          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                          modifier = Modifier.weight(1f)
                        ) {
                          Text(
                            text = code,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextNavy
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          2 -> {
            // TAB 2: PASSKEYS (FIDO2 / WEBAUTHN)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              passkeys.forEach { pk ->
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(24.dp))
                      Spacer(modifier = Modifier.width(10.dp))
                      Column {
                        Text(pk.friendlyName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
                        Text("Enrolled: ${pk.createdDate} • Last used: ${pk.lastUsedDate}", style = MaterialTheme.typography.labelSmall, color = VibrantSecondary, fontSize = 10.sp)
                      }
                    }

                    IconButton(
                      onClick = {
                        viewModel.removePasskey(pk.id)
                        viewModel.showNotification("Removed passkey ${pk.friendlyName}")
                      },
                      modifier = Modifier.size(28.dp)
                    ) {
                      Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                  }
                }
              }

              // Enroll New Passkey Section
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  Text("Register Hardware Security Key", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                  OutlinedTextField(
                    value = newPasskeyName,
                    onValueChange = { newPasskeyName = it },
                    label = { Text("Passkey Nickname (e.g., YubiKey 5C NFC)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                  )
                  Button(
                    onClick = {
                      if (newPasskeyName.isNotBlank()) {
                        viewModel.registerNewPasskey(newPasskeyName)
                        newPasskeyName = ""
                        viewModel.showNotification("Hardware passkey registered & bound to account!")
                      }
                    },
                    enabled = newPasskeyName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Icon(Icons.Default.AddModerator, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Register FIDO2 Hardware Key", fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }

          3 -> {
            // TAB 3: ENTERPRISE SSO & SAML
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  Text("Single Sign-On (SSO) Connection", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)

                  OutlinedTextField(
                    value = ssoDomain,
                    onValueChange = { ssoDomain = it },
                    label = { Text("Corporate Domain") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                  )

                  Text("Identity Provider (IdP)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    listOf("Google Workspace", "Okta", "Microsoft Azure AD", "Ping Identity").forEach { idp ->
                      val isSelected = ssoProvider == idp
                      Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF0D9488) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F766E) else Color.Transparent),
                        modifier = Modifier.clickable { ssoProvider = idp }
                      ) {
                        Text(
                          text = idp,
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                          color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Enforce Hardware MFA for all domain users", style = MaterialTheme.typography.bodySmall, color = VibrantTextNavy)
                    Switch(
                      checked = ssoEnforce2fa,
                      onCheckedChange = { ssoEnforce2fa = it }
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
      Button(
        onClick = {
          viewModel.showNotification("Security & Auth preferences saved successfully!")
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.testTag("save_auth_setup_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save Security Configuration", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}
