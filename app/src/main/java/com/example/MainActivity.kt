package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.MultiplatformCompilationHubModal
import com.example.ui.components.OnboardingFlowModal
import com.example.ui.components.VideoTutorialModal
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

enum class NavigationDestination(
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
) {
  DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
  AI_COPILOT("AI Tax & Market", Icons.Filled.Psychology, Icons.Outlined.Psychology, "nav_ai_copilot"),
  CALCULATOR("Calc", Icons.Filled.Calculate, Icons.Outlined.Calculate, "nav_calculator"),
  PAYROLL_RUNS("Runs", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_runs"),
  EXPENSES("Expenses", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_expenses"),
  EMPLOYEES("Team", Icons.Filled.Group, Icons.Outlined.Group, "nav_employees"),
  SECURITY("Security", Icons.Filled.Security, Icons.Outlined.Security, "nav_security"),
  ANALYTICS("Reports", Icons.Filled.Summarize, Icons.Outlined.Summarize, "nav_analytics"),
  SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

class MainActivity : ComponentActivity() {
  private val viewModel: PayrollViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val globalConfig by viewModel.globalConfig.collectAsState()

      MyApplicationTheme(
        themeMode = globalConfig.activeThemeMode,
        colorTheme = globalConfig.activeColorTheme,
        dynamicColor = globalConfig.dynamicMonetColors,
        highContrast = globalConfig.highContrastMode
      ) {
        val authState by viewModel.authState.collectAsState()
        val authUser by viewModel.authUser.collectAsState()
        var currentDestination by remember { mutableStateOf(NavigationDestination.DASHBOARD) }
        val notification by viewModel.notification.collectAsState()
        val companyProfile by viewModel.companyProfile.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        var showOnboardingModal by remember { mutableStateOf(false) }
        var showVideoTutorialsModal by remember { mutableStateOf(false) }
        var showMultiplatformModal by remember { mutableStateOf(false) }
        var isDesktopLayout by remember { mutableStateOf(false) }

        LaunchedEffect(notification) {
          notification?.let {
            snackbarHostState.showSnackbar(it.message)
            viewModel.dismissNotification()
          }
        }

        if (authState != AuthState.AUTHENTICATED) {
          AuthenticationScreen(viewModel = viewModel)
        } else {
          Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
              TopAppBar(
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
                          .clip(RoundedCornerShape(12.dp))
                          .background(VibrantPrimary),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.Payments,
                          contentDescription = null,
                          tint = Color.White,
                          modifier = Modifier.size(22.dp)
                        )
                      }
                      Spacer(modifier = Modifier.width(10.dp))
                      Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text(
                            text = "PayFlow AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextNavy
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isDesktopLayout) VibrantTertiaryContainer else VibrantPrimaryContainer
                          ) {
                            Text(
                              text = if (isDesktopLayout) "DESKTOP" else "MOBILE",
                              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                              style = MaterialTheme.typography.labelSmall,
                              fontSize = 8.5.sp,
                              fontWeight = FontWeight.Bold,
                              color = if (isDesktopLayout) VibrantTertiary else VibrantPrimary
                            )
                          }
                        }
                        Text(
                          text = when (currentDestination) {
                            NavigationDestination.DASHBOARD -> "Company Overview"
                            NavigationDestination.AI_COPILOT -> "Gemini 3.5 Tax Copilot & Market Index"
                            NavigationDestination.CALCULATOR -> "Gross-to-Net Simulator"
                            NavigationDestination.PAYROLL_RUNS -> "Batch Cycles & GL"
                            NavigationDestination.EXPENSES -> "AI Bank & Receipt Sync"
                            NavigationDestination.EMPLOYEES -> "Staff Directory"
                            NavigationDestination.SECURITY -> "Enterprise Security Center"
                            NavigationDestination.ANALYTICS -> "Custom Reports & AI Forecast"
                            NavigationDestination.SETTINGS -> "Enterprise & API Hub"
                          },
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }

                    // Top Action Buttons: AI Copilot, Multiplatform Hub, Desktop Toggle, Security Shield, Video Masterclass & Setup Wizard
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                      modifier = Modifier.padding(end = 8.dp)
                    ) {
                      // Live AI Copilot Quick Launch
                      IconButton(
                        onClick = { currentDestination = NavigationDestination.AI_COPILOT },
                        modifier = Modifier.size(34.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Psychology,
                          contentDescription = "AI Tax & Market Copilot",
                          tint = Color(0xFF8B5CF6),
                          modifier = Modifier.size(20.dp)
                        )
                      }

                      // Multiplatform Compilation Hub (iOS, macOS, Windows)
                      IconButton(
                        onClick = { showMultiplatformModal = true },
                        modifier = Modifier.size(34.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Devices,
                          contentDescription = "Multiplatform Hub (iOS, macOS, Windows)",
                          tint = Color(0xFF6366F1),
                          modifier = Modifier.size(20.dp)
                        )
                      }

                      // Layout Toggle: Mobile Handheld vs macOS/Windows Desktop Rail
                      IconButton(
                        onClick = {
                          isDesktopLayout = !isDesktopLayout
                          viewModel.showNotification(if (isDesktopLayout) "Switched to macOS / Windows Desktop Sidebar Layout" else "Switched to Mobile Handheld Layout")
                        },
                        modifier = Modifier.size(34.dp)
                      ) {
                        Icon(
                          imageVector = if (isDesktopLayout) Icons.Default.PhoneAndroid else Icons.Default.LaptopMac,
                          contentDescription = "Toggle Desktop Layout",
                          tint = VibrantPrimary,
                          modifier = Modifier.size(20.dp)
                        )
                      }

                      IconButton(
                        onClick = { currentDestination = NavigationDestination.SECURITY },
                        modifier = Modifier.size(34.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Shield,
                          contentDescription = "Enterprise Security Center",
                          tint = VibrantSuccessText,
                          modifier = Modifier.size(20.dp)
                        )
                      }

                      IconButton(
                        onClick = { showVideoTutorialsModal = true },
                        modifier = Modifier.size(34.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.PlayLesson,
                          contentDescription = "Video Tutorials",
                          tint = VibrantPrimary,
                          modifier = Modifier.size(20.dp)
                        )
                      }

                      IconButton(
                        onClick = { showOnboardingModal = true },
                        modifier = Modifier.size(34.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.RocketLaunch,
                          contentDescription = "Setup Wizard",
                          tint = VibrantWarningAction,
                          modifier = Modifier.size(20.dp)
                        )
                      }

                      Box(
                        modifier = Modifier
                          .size(32.dp)
                          .clip(CircleShape)
                          .background(
                            Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF6366F1)))
                          )
                          .clickable { currentDestination = NavigationDestination.SECURITY },
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = "JD",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = Color.White
                        )
                      }
                    }
                  }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.background
                )
              )
            },
            bottomBar = {
              if (!isDesktopLayout) {
                Surface(
                  shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                  color = MaterialTheme.colorScheme.surface,
                  shadowElevation = 8.dp,
                  border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                      listOf(
                        VibrantOutlineVariant,
                        VibrantOutlineVariant.copy(alpha = 0.5f)
                      )
                    )
                  ),
                  modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
                ) {
                  NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                  ) {
                    NavigationDestination.values().forEach { destination ->
                      val isSelected = currentDestination == destination
                      NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                          Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.title,
                            modifier = Modifier.size(20.dp)
                          )
                        },
                        label = {
                          Text(
                            text = destination.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                          )
                        },
                        colors = NavigationBarItemDefaults.colors(
                          indicatorColor = VibrantPrimaryContainer,
                          selectedIconColor = VibrantOnPrimaryContainer,
                          selectedTextColor = VibrantOnPrimaryContainer,
                          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                      )
                    }
                  }
                }
              }
            },
            snackbarHost = {
              SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = if (isDesktopLayout) 16.dp else 80.dp)
              )
            }
          ) { innerPadding ->
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            ) {
              // Desktop Sidebar Navigation Rail (for macOS & Windows Desktop Mode)
              if (isDesktopLayout) {
                Surface(
                  shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                  color = MaterialTheme.colorScheme.surface,
                  shadowElevation = 4.dp,
                  border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                      listOf(
                        VibrantOutlineVariant,
                        VibrantOutlineVariant.copy(alpha = 0.4f)
                      )
                    )
                  ),
                  modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxHeight()
                      .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                      Text(
                        text = "ENTERPRISE DESKTOP",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                      )

                      NavigationDestination.values().forEach { destination ->
                        val isSelected = currentDestination == destination
                        Surface(
                          shape = RoundedCornerShape(12.dp),
                          color = if (isSelected) VibrantPrimaryContainer else Color.Transparent,
                          modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentDestination = destination }
                        ) {
                          Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Icon(
                              imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                              contentDescription = destination.title,
                              tint = if (isSelected) VibrantPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                              modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                              text = destination.title,
                              style = MaterialTheme.typography.labelMedium,
                              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                              color = if (isSelected) VibrantOnPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                          }
                        }
                      }
                    }

                    // Desktop Footer Status
                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = VibrantSuccessContainer.copy(alpha = 0.5f),
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMultiplatformModal = true }
                    ) {
                      Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Icon(Icons.Default.Devices, contentDescription = null, tint = VibrantSuccessText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                          Text("Cross-Platform", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VibrantSuccessText, fontSize = 10.sp)
                          Text("macOS • Win • iOS", style = MaterialTheme.typography.labelSmall, color = VibrantSuccessText.copy(alpha = 0.8f), fontSize = 8.5.sp)
                        }
                      }
                    }
                  }
                }
              }

              // Main Screen Canvas
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
              ) {
                when (currentDestination) {
                  NavigationDestination.DASHBOARD -> {
                    DashboardScreen(
                      viewModel = viewModel,
                      onNavigateToCalculator = { currentDestination = NavigationDestination.CALCULATOR },
                      onNavigateToPayrollRuns = { currentDestination = NavigationDestination.PAYROLL_RUNS },
                      onNavigateToEmployees = { currentDestination = NavigationDestination.EMPLOYEES },
                      onNavigateToExpenses = { currentDestination = NavigationDestination.EXPENSES },
                      onNavigateToAnalytics = { currentDestination = NavigationDestination.ANALYTICS },
                      onNavigateToSettings = { currentDestination = NavigationDestination.SETTINGS },
                      onNavigateToAiCopilot = { currentDestination = NavigationDestination.AI_COPILOT }
                    )
                  }
                  NavigationDestination.AI_COPILOT -> {
                    AiCopilotMarketAnalysisScreen(
                      viewModel = viewModel,
                      onNavigateToCalculator = { currentDestination = NavigationDestination.CALCULATOR }
                    )
                  }
                  NavigationDestination.CALCULATOR -> {
                    CalculatorScreen(viewModel = viewModel)
                  }
                  NavigationDestination.PAYROLL_RUNS -> {
                    PayrollRunsScreen(viewModel = viewModel)
                  }
                  NavigationDestination.EXPENSES -> {
                    ExpensesReconciliationScreen(
                      viewModel = viewModel,
                      onOpenUpgrade = { currentDestination = NavigationDestination.SETTINGS }
                    )
                  }
                  NavigationDestination.EMPLOYEES -> {
                    EmployeesScreen(
                      viewModel = viewModel,
                      onNavigateToCalculator = { currentDestination = NavigationDestination.CALCULATOR }
                    )
                  }
                  NavigationDestination.SECURITY -> {
                    EnterpriseSecurityCenterScreen(viewModel = viewModel)
                  }
                  NavigationDestination.ANALYTICS -> {
                    AnalyticsForecastScreen(
                      viewModel = viewModel,
                      onOpenUpgrade = { currentDestination = NavigationDestination.SETTINGS }
                    )
                  }
                  NavigationDestination.SETTINGS -> {
                    EnterpriseSettingsScreen(viewModel = viewModel)
                  }
                }
              }
            }
          }
        }

        // Global Modals
        if (showMultiplatformModal) {
          MultiplatformCompilationHubModal(
            onDismiss = { showMultiplatformModal = false },
            onShowNotification = { viewModel.showNotification(it) }
          )
        }

        // Global Modals
        if (showOnboardingModal) {
          OnboardingFlowModal(
            currentProfile = companyProfile,
            onComplete = { name, ein, state, qb, xero ->
              viewModel.completeOnboarding(name, ein, state, qb, xero)
              showOnboardingModal = false
            },
            onDismiss = { showOnboardingModal = false },
            onOpenVideoTutorials = {
              showOnboardingModal = false
              showVideoTutorialsModal = true
            }
          )
        }

        if (showVideoTutorialsModal) {
          VideoTutorialModal(
            tutorials = viewModel.getVideoTutorials(),
            onDismiss = { showVideoTutorialsModal = false }
          )
        }
      }
    }
  }

  override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)
    // Handle memory trimming gracefully on Android Q+ to replace deprecated ashmem pinning
  }
}
