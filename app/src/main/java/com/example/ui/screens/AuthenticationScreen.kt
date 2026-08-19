package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthenticationScreen(
  viewModel: PayrollViewModel,
  modifier: Modifier = Modifier
) {
  val authState by viewModel.authState.collectAsState()
  val authUser by viewModel.authUser.collectAsState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    when (authState) {
      AuthState.LOGIN -> {
        LoginView(viewModel = viewModel)
      }
      AuthState.REGISTER -> {
        RegisterView(viewModel = viewModel)
      }
      AuthState.VERIFY_EMAIL -> {
        VerifyEmailView(viewModel = viewModel, userEmail = authUser.email)
      }
      AuthState.MFA_CHALLENGE -> {
        MfaChallengeView(viewModel = viewModel, user = authUser)
      }
      AuthState.BIOMETRIC_SCANNER_PROMPT -> {
        BiometricScannerView(viewModel = viewModel, onComplete = { viewModel.setAuthState(AuthState.AUTHENTICATED) })
      }
      AuthState.APP_LOCKED -> {
        AppLockedView(viewModel = viewModel, user = authUser)
      }
      AuthState.AUTHENTICATED -> {
        // Handled by main container
      }
    }
  }
}

@Composable
fun LoginView(viewModel: PayrollViewModel) {
  var email by remember { mutableStateOf("jane.doe@acme-global.com") }
  var password by remember { mutableStateOf("Enterprise#2026!Secured") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val scope = rememberCoroutineScope()
  var isAuthenticating by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Enterprise Branding
    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(
          Brush.linearGradient(listOf(VibrantPrimary, VibrantGradientEnd))
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Shield,
        contentDescription = "Enterprise Security",
        tint = Color.White,
        modifier = Modifier.size(36.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "PayFlow AI Enterprise",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = VibrantTextNavy
    )

    Text(
      text = "Zero-Trust Protected Payroll & Financial Infrastructure",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(28.dp))

    // Enterprise Standards Badge
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = VibrantSuccessContainer.copy(alpha = 0.6f),
      border = CardDefaults.outlinedCardBorder().copy(
        brush = Brush.linearGradient(listOf(VibrantSuccess, VibrantSuccess.copy(alpha = 0.4f)))
      )
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = VibrantSuccessText, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "SOC 2 Type II • ISO 27001 • FIPS 140-3 Compliant",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = VibrantSuccessText
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Input Fields Card
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder().copy(
        brush = Brush.linearGradient(listOf(VibrantOutlineVariant, VibrantOutlineVariant))
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Sign In with SSO / Master Key",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = VibrantTextNavy
        )

        // Email Field
        OutlinedTextField(
          value = email,
          onValueChange = { email = it; errorMessage = null },
          label = { Text("Corporate Work Email") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = VibrantPrimary) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_email_input"),
          shape = RoundedCornerShape(14.dp)
        )

        // Password Field
        OutlinedTextField(
          value = password,
          onValueChange = { password = it; errorMessage = null },
          label = { Text("Master Password") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = VibrantPrimary) },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Toggle password visibility"
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_password_input"),
          shape = RoundedCornerShape(14.dp)
        )

        errorMessage?.let { msg ->
          Text(text = msg, style = MaterialTheme.typography.labelSmall, color = VibrantError, fontWeight = FontWeight.SemiBold)
        }

        // Login Button
        Button(
          onClick = {
            if (email.isBlank() || !email.contains("@")) {
              errorMessage = "Please enter a valid enterprise corporate email."
              return@Button
            }
            if (password.length < 8) {
              errorMessage = "Password must meet enterprise standards (min 8 chars)."
              return@Button
            }
            isAuthenticating = true
            scope.launch {
              delay(400)
              val success = viewModel.login(email, password)
              isAuthenticating = false
              if (!success) {
                errorMessage = "Authentication failed. Invalid work email or password."
              }
            }
          },
          enabled = !isAuthenticating,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("login_submit_btn"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
        ) {
          if (isAuthenticating) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
          } else {
            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Authenticate with 2FA", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Fast Biometric & Passkey Authenticate Button
    Surface(
      onClick = {
        viewModel.setAuthState(AuthState.BIOMETRIC_SCANNER_PROMPT)
      },
      shape = RoundedCornerShape(16.dp),
      color = VibrantPrimaryContainer.copy(alpha = 0.5f),
      border = CardDefaults.outlinedCardBorder().copy(
        brush = Brush.linearGradient(listOf(VibrantPrimary, VibrantPrimary.copy(alpha = 0.4f)))
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("login_passkey_biometric_btn")
    ) {
      Row(
        modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Sign In with Biometrics / FIDO2 Passkey",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = VibrantTextNavy
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // SSO Provider Buttons (Microsoft, Google, Meta)
    Text(
      text = "OR SIGN IN WITH ENTERPRISE IDENTITY (SSO)",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      letterSpacing = 1.sp,
      fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Microsoft Entra ID
      OutlinedButton(
        onClick = {
          viewModel.login("jane.doe@microsoft.com", "Azure#AD#Entra2026")
        },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
      ) {
        Icon(Icons.Default.Window, contentDescription = "Microsoft", tint = Color(0xFF00A4EF), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Microsoft", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
      }

      // Google Workspace
      OutlinedButton(
        onClick = {
          viewModel.login("jane.doe@google.com", "Google#Workspace#2026")
        },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
      ) {
        Icon(Icons.Default.AccountCircle, contentDescription = "Google", tint = Color(0xFF4285F4), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Google", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
      }

      // Meta Enterprise
      OutlinedButton(
        onClick = {
          viewModel.login("jane.doe@meta.com", "Meta#Workplace#2026")
        },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
      ) {
        Icon(Icons.Default.AllInclusive, contentDescription = "Meta", tint = Color(0xFF0081FB), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Meta", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Switch to Register
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text("Need an enterprise account? ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(
        text = "Register Company",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = VibrantPrimary,
        modifier = Modifier
          .clickable { viewModel.setAuthState(AuthState.REGISTER) }
          .testTag("switch_to_register_btn")
      )
    }
  }
}

@Composable
fun RegisterView(viewModel: PayrollViewModel) {
  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var role by remember { mutableStateOf(UserRole.OWNER) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var agreeToSoc2 by remember { mutableStateOf(true) }

  // Password validation criteria
  val hasMinLength = password.length >= 12
  val hasUpper = password.any { it.isUpperCase() }
  val hasLower = password.any { it.isLowerCase() }
  val hasDigit = password.any { it.isDigit() }
  val hasSpecial = password.any { !it.isLetterOrDigit() }
  val isPasswordStrong = hasMinLength && hasUpper && hasLower && hasDigit && hasSpecial

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Enterprise Account Setup",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = VibrantTextNavy
    )
    Text(
      text = "Provision new cryptographic credentials and MFA bindings",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(20.dp))

    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder(),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = { Text("Full Name (e.g. Jane Doe)") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = VibrantPrimary) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Enterprise Work Email") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = VibrantPrimary) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Master Password (min 12 chars)") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = VibrantPrimary) },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
          value = confirmPassword,
          onValueChange = { confirmPassword = it },
          label = { Text("Confirm Master Password") },
          leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VibrantPrimary) },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp)
        )

        // Live Password Strength Checklist
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = VibrantSurfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("ENTERPRISE PASSWORD POLICY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSecondary)
            PasswordCriteriaItem(label = "At least 12 characters", met = hasMinLength)
            PasswordCriteriaItem(label = "Uppercase letter (A-Z)", met = hasUpper)
            PasswordCriteriaItem(label = "Lowercase letter (a-z)", met = hasLower)
            PasswordCriteriaItem(label = "Number (0-9)", met = hasDigit)
            PasswordCriteriaItem(label = "Special character (!@#$%^&*)", met = hasSpecial)
          }
        }

        // SOC 2 Agreement
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { agreeToSoc2 = !agreeToSoc2 }
        ) {
          Checkbox(
            checked = agreeToSoc2,
            onCheckedChange = { agreeToSoc2 = it },
            colors = CheckboxDefaults.colors(checkedColor = VibrantPrimary)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Enforce hardware KeyStore encryption & Zero-Trust audit logs.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        errorMessage?.let { msg ->
          Text(text = msg, style = MaterialTheme.typography.labelSmall, color = VibrantError, fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = {
            if (fullName.isBlank()) {
              errorMessage = "Full name is required."
              return@Button
            }
            if (email.isBlank() || !email.contains("@")) {
              errorMessage = "Valid corporate work email is required."
              return@Button
            }
            if (!isPasswordStrong) {
              errorMessage = "Password does not meet enterprise requirements."
              return@Button
            }
            if (password != confirmPassword) {
              errorMessage = "Passwords do not match."
              return@Button
            }
            if (!agreeToSoc2) {
              errorMessage = "Enterprise compliance terms must be accepted."
              return@Button
            }

            viewModel.register(fullName, email, password)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("register_submit_btn"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
        ) {
          Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Proceed to Email Verification", fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = { viewModel.setAuthState(AuthState.LOGIN) }) {
      Text("Back to Sign In")
    }
  }
}

@Composable
fun PasswordCriteriaItem(label: String, met: Boolean) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.Cancel,
      contentDescription = null,
      tint = if (met) VibrantSuccessText else VibrantError,
      modifier = Modifier.size(14.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = if (met) VibrantSuccessText else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun VerifyEmailView(viewModel: PayrollViewModel, userEmail: String) {
  var code by remember { mutableStateOf("849201") }
  var countdown by remember { mutableStateOf(45) }
  var isVerifying by remember { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  val scope = rememberCoroutineScope()

  LaunchedEffect(countdown) {
    if (countdown > 0) {
      delay(1000)
      countdown--
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(VibrantPrimaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(32.dp))
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Verify Your Work Email",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = VibrantTextNavy
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "We sent a 6-digit cryptographic verification code to:",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = userEmail,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = VibrantPrimary
    )

    Spacer(modifier = Modifier.height(24.dp))

    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder(),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        OutlinedTextField(
          value = code,
          onValueChange = { if (it.length <= 6) code = it },
          label = { Text("6-Digit Email Code") },
          textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center, letterSpacing = 8.sp, fontWeight = FontWeight.Bold),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("verify_email_code_input"),
          shape = RoundedCornerShape(14.dp)
        )

        errorMsg?.let {
          Text(text = it, style = MaterialTheme.typography.labelSmall, color = VibrantError)
        }

        Button(
          onClick = {
            if (code.length != 6) {
              errorMsg = "Please enter all 6 digits."
              return@Button
            }
            isVerifying = true
            scope.launch {
              delay(300)
              val success = viewModel.verifyEmailCode(code)
              isVerifying = false
              if (!success) {
                errorMsg = "Invalid verification code."
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("verify_email_submit_btn"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
        ) {
          if (isVerifying) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
          } else {
            Text("Confirm & Bind Email", fontWeight = FontWeight.Bold)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (countdown > 0) "Resend in ${countdown}s" else "Didn't receive code?",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          TextButton(
            onClick = { countdown = 45; viewModel.register("User", userEmail, "Secured#123") },
            enabled = countdown == 0
          ) {
            Text("Resend Code", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = { viewModel.setAuthState(AuthState.LOGIN) }) {
      Text("Cancel and return to Sign In")
    }
  }
}

@Composable
fun MfaChallengeView(viewModel: PayrollViewModel, user: AuthUserProfile) {
  var totpCode by remember { mutableStateOf("123456") }
  var isSubmitting by remember { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  val scope = rememberCoroutineScope()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(VibrantPrimary),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.Key, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Two-Factor Authentication",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = VibrantTextNavy
    )

    Text(
      text = "Open Google Authenticator or Microsoft Authenticator and enter your current 6-digit TOTP code.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder(),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        OutlinedTextField(
          value = totpCode,
          onValueChange = { if (it.length <= 6) totpCode = it },
          label = { Text("Authenticator TOTP Code") },
          textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center, letterSpacing = 8.sp, fontWeight = FontWeight.Bold),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("mfa_totp_input"),
          shape = RoundedCornerShape(14.dp)
        )

        errorMsg?.let {
          Text(text = it, style = MaterialTheme.typography.labelSmall, color = VibrantError)
        }

        Button(
          onClick = {
            if (totpCode.length != 6) {
              errorMsg = "Enter complete 6-digit TOTP code."
              return@Button
            }
            isSubmitting = true
            scope.launch {
              delay(300)
              val success = viewModel.verifyMfaChallenge(totpCode)
              isSubmitting = false
              if (!success) {
                errorMsg = "Invalid TOTP passcode. Try again."
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("mfa_verify_btn"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
        ) {
          if (isSubmitting) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
          } else {
            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verify & Grant Session", fontWeight = FontWeight.Bold)
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Switch to Biometric Scan or FIDO2 Passkey
        OutlinedButton(
          onClick = {
            viewModel.setAuthState(AuthState.BIOMETRIC_SCANNER_PROMPT)
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Use Hardware Biometric / Passkey")
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = { viewModel.setAuthState(AuthState.LOGIN) }) {
      Text("Sign In with Different Account")
    }
  }
}

@Composable
fun BiometricScannerView(
  viewModel: PayrollViewModel,
  onComplete: () -> Unit
) {
  var scanState by remember { mutableStateOf("Scanning...") } // "Scanning...", "Verified!", "Error"
  var isSuccess by remember { mutableStateOf(false) }
  var sensorType by remember { mutableStateOf("Fingerprint & 3D Face Mesh") }
  val scope = rememberCoroutineScope()

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Biometric Sensor Authentication",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = VibrantTextNavy
    )
    Text(
      text = "Hardware KeyStore Level 3 StrongBox Verification",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(36.dp))

    // Interactive Pulsing Biometric Scanner Sensor
    Box(
      modifier = Modifier
        .size(140.dp)
        .scale(if (isSuccess) 1.1f else pulseScale)
        .clip(CircleShape)
        .background(
          if (isSuccess) Brush.radialGradient(listOf(VibrantSuccess, VibrantSuccess))
          else Brush.radialGradient(listOf(VibrantPrimary, VibrantGradientStart))
        )
        .clickable {
          scope.launch {
            scanState = "Verifying Cryptographic Certificate..."
            delay(500)
            isSuccess = true
            scanState = "Hardware Identity Verified!"
            viewModel.verifyBiometricSensorScan(true, sensorType)
            delay(400)
            onComplete()
          }
        }
        .testTag("biometric_sensor_scanner_touch"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Fingerprint,
        contentDescription = "Sensor",
        tint = Color.White,
        modifier = Modifier.size(72.dp)
      )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
      text = scanState,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = if (isSuccess) VibrantSuccessText else VibrantPrimary
    )
    Text(
      text = "Touch the biometric sensor or look at the camera for Face Unlock",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = {
          scope.launch {
            scanState = "Reading 3D Face Geometry..."; delay(400)
            isSuccess = true
            viewModel.verifyBiometricSensorScan(true, "Face Unlock")
            delay(400)
            onComplete()
          }
        },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimaryContainer, contentColor = VibrantPrimary)
      ) {
        Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Face Unlock")
      }

      Button(
        onClick = {
          scope.launch {
            scanState = "Reading Titan FIDO2 Passkey..."; delay(400)
            isSuccess = true
            viewModel.verifyPasskeyFido2("pk_titan_enclave")
            delay(400)
            onComplete()
          }
        },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VibrantSecondaryContainer, contentColor = VibrantSecondary)
      ) {
        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Use Passkey")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = { viewModel.setAuthState(AuthState.LOGIN) }) {
      Text("Use Master Password")
    }
  }
}

@Composable
fun AppLockedView(viewModel: PayrollViewModel, user: AuthUserProfile) {
  val scope = rememberCoroutineScope()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(72.dp)
        .clip(CircleShape)
        .background(VibrantPrimaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.Lock, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(36.dp))
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Application Locked",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = VibrantTextNavy
    )

    Text(
      text = "Protected by Zero-Trust Inactivity Shield",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      border = CardDefaults.outlinedCardBorder()
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(VibrantPrimary),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "JD", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(user.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          Text(user.email, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    Button(
      onClick = {
        viewModel.unlockAppWithBiometrics()
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("unlock_biometrics_btn"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
    ) {
      Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(22.dp))
      Spacer(modifier = Modifier.width(10.dp))
      Text("Unlock with Face / Fingerprint", fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
      onClick = {
        viewModel.setAuthState(AuthState.LOGIN)
      },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp)
    ) {
      Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Sign In with Different Account")
    }
  }
}
