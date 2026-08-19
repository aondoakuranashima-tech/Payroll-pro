package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.EmployeeEntity
import com.example.data.model.ExportCategory
import com.example.data.model.ImportTargetType
import com.example.data.model.PayType
import com.example.ui.components.AddEmployeeDialog
import com.example.ui.components.EnterpriseExportModal
import com.example.ui.components.EnterpriseImportModal
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
  viewModel: PayrollViewModel,
  onNavigateToCalculator: () -> Unit,
  modifier: Modifier = Modifier
) {
  val employees by viewModel.employees.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedDepartment by remember { mutableStateOf("All") }
  var showAddDialog by remember { mutableStateOf(false) }
  var showExportModal by remember { mutableStateOf(false) }
  var showImportModal by remember { mutableStateOf(false) }
  var employeeToDelete by remember { mutableStateOf<EmployeeEntity?>(null) }

  val departments = listOf("All", "Engineering", "Design", "Finance", "Infrastructure", "Operations")

  val filteredEmployees = employees.filter { emp ->
    val matchesSearch = emp.fullName.contains(searchQuery, ignoreCase = true) ||
      emp.role.contains(searchQuery, ignoreCase = true) ||
      emp.email.contains(searchQuery, ignoreCase = true)
    val matchesDept = selectedDepartment == "All" || emp.department.equals(selectedDepartment, ignoreCase = true)
    matchesSearch && matchesDept
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showAddDialog = true },
        icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add Employee") },
        text = { Text("Add Employee", fontWeight = FontWeight.Bold) },
        containerColor = VibrantPrimary,
        contentColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
          .padding(bottom = 60.dp)
          .testTag("add_employee_fab")
      )
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("employees_screen"),
      contentPadding = PaddingValues(bottom = 96.dp, top = 12.dp, start = 16.dp, end = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Search Bar & Quick Actions Row
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search employees by name, role or email...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VibrantSecondary) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          shape = RoundedCornerShape(20.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = VibrantPrimary,
            unfocusedBorderColor = VibrantOutlineVariant
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("employee_search_bar")
        )
      }

      // Quick Import/Export action pills
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = { showImportModal = true },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Import CSV", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = { showExportModal = true },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantPrimary),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Export Roster", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }
        }
      }

      // 2. Department Filters
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(departments) { dept ->
            FilterChip(
              selected = selectedDepartment == dept,
              onClick = { selectedDepartment = dept },
              label = { Text(dept, fontWeight = if (selectedDepartment == dept) FontWeight.Bold else FontWeight.Medium) },
              shape = RoundedCornerShape(16.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = VibrantPrimaryContainer,
                selectedLabelColor = VibrantOnPrimaryContainer
              )
            )
          }
        }
      }

      // 3. Employee Count Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "STAFF DIRECTORY (${filteredEmployees.size})",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
            fontWeight = FontWeight.Bold,
            color = VibrantSecondary
          )
          Text(
            text = "Total Annual: ${viewModel.formatCurrency(employees.sumOf { if (it.payType == PayType.SALARY) it.baseRate else it.baseRate * 2080.0 })}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // 4. Employee List
      if (filteredEmployees.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.5f)))
            )
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PeopleOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = VibrantSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("No staff members found matching search", style = MaterialTheme.typography.bodyMedium, color = VibrantTextPrimary)
              }
            }
          }
        }
      } else {
        items(filteredEmployees, key = { it.id }) { emp ->
          EmployeeCard(
            employee = emp,
            onCalculatePaycheck = {
              viewModel.updateCalculator(
                payType = emp.payType,
                baseRate = if (emp.payType == PayType.SALARY) emp.baseRate.toInt().toString() else emp.baseRate.toString(),
                payFrequency = emp.payFrequency,
                filingStatus = emp.filingStatus,
                stateCode = emp.stateCode,
                preTax401kPercent = emp.preTax401kPercent.toString(),
                preTaxHealth = emp.preTaxHealthInsurance.toString(),
                postTaxDeductions = emp.postTaxDeductions.toString()
              )
              onNavigateToCalculator()
            },
            onDelete = { employeeToDelete = emp },
            formattedSalary = if (emp.payType == PayType.SALARY) {
              "${viewModel.formatCurrency(emp.baseRate)}/yr"
            } else {
              "${viewModel.formatCurrency(emp.baseRate)}/hr"
            }
          )
        }
      }
    }
  }

  if (showAddDialog) {
    AddEmployeeDialog(
      onDismiss = { showAddDialog = false },
      onAdd = { firstName, lastName, email, role, dept, payType, baseRate, freq, status, state, k401, health ->
        viewModel.addEmployee(
          firstName = firstName,
          lastName = lastName,
          email = email,
          role = role,
          department = dept,
          payType = payType,
          baseRate = baseRate,
          payFrequency = freq,
          filingStatus = status,
          stateCode = state,
          preTax401k = k401,
          preTaxHealth = health
        )
        showAddDialog = false
      }
    )
  }

  employeeToDelete?.let { emp ->
    AlertDialog(
      onDismissRequest = { employeeToDelete = null },
      title = { Text("Remove Employee") },
      text = { Text("Are you sure you want to remove ${emp.fullName} from payroll calculations?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteEmployee(emp)
            employeeToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = VibrantError)
        ) {
          Text("Remove")
        }
      },
      dismissButton = {
        TextButton(onClick = { employeeToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  if (showExportModal) {
    EnterpriseExportModal(
      viewModel = viewModel,
      initialCategory = ExportCategory.EMPLOYEE_ROSTER,
      onDismiss = { showExportModal = false }
    )
  }

  if (showImportModal) {
    EnterpriseImportModal(
      viewModel = viewModel,
      initialTargetType = ImportTargetType.EMPLOYEES,
      onDismiss = { showImportModal = false }
    )
  }
}

@Composable
fun EmployeeCard(
  employee: EmployeeEntity,
  onCalculatePaycheck: () -> Unit,
  onDelete: () -> Unit,
  formattedSalary: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("employee_card_${employee.id}"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.linearGradient(listOf(VibrantOutlineVariant, VibrantOutlineVariant.copy(alpha = 0.5f)))
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(VibrantPrimaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${employee.firstName.take(1)}${employee.lastName.take(1)}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = VibrantOnPrimaryContainer
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(employee.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
            Text(employee.role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(formattedSalary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTertiary)
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = VibrantSecondaryContainer.copy(alpha = 0.5f)
          ) {
            Text(
              text = "${employee.department} • ${employee.stateCode}",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              style = MaterialTheme.typography.labelSmall,
              color = VibrantSecondary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp), tint = VibrantSecondary)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Direct Deposit (****${employee.bankAccountLast4})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.width(10.dp))
          Text("401k: ${employee.preTax401kPercent}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row {
          IconButton(onClick = onCalculatePaycheck, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Calculate, contentDescription = "Simulate Pay", tint = VibrantPrimary, modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = VibrantError, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}
