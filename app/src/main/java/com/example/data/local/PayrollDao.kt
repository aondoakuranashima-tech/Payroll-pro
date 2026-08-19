package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CompanyProfileEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.PayrollItemEntity
import com.example.data.model.PayrollRunEntity
import com.example.data.model.PaystackTransactionEntity
import com.example.data.model.TaxSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayrollDao {
  // Employees
  @Query("SELECT * FROM employees ORDER BY firstName ASC")
  fun getAllEmployees(): Flow<List<EmployeeEntity>>

  @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
  suspend fun getEmployeeById(id: Long): EmployeeEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEmployee(employee: EmployeeEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEmployees(employees: List<EmployeeEntity>)

  @Update
  suspend fun updateEmployee(employee: EmployeeEntity)

  @Delete
  suspend fun deleteEmployee(employee: EmployeeEntity)

  // Payroll Runs
  @Query("SELECT * FROM payroll_runs ORDER BY runDateTimestamp DESC")
  fun getAllPayrollRuns(): Flow<List<PayrollRunEntity>>

  @Query("SELECT * FROM payroll_runs WHERE id = :id LIMIT 1")
  suspend fun getPayrollRunById(id: Long): PayrollRunEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayrollRun(run: PayrollRunEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayrollRuns(runs: List<PayrollRunEntity>)

  // Payroll Items
  @Query("SELECT * FROM payroll_items WHERE runId = :runId")
  fun getItemsForRun(runId: Long): Flow<List<PayrollItemEntity>>

  @Query("SELECT * FROM payroll_items WHERE runId = :runId")
  suspend fun getItemsForRunSync(runId: Long): List<PayrollItemEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayrollItems(items: List<PayrollItemEntity>)

  // Expenses
  @Query("SELECT * FROM expenses ORDER BY id DESC")
  fun getAllExpenses(): Flow<List<ExpenseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpenses(expenses: List<ExpenseEntity>)

  // Tax Settings (Local Persistence)
  @Query("SELECT * FROM tax_settings WHERE id = 1 LIMIT 1")
  fun getTaxSettings(): Flow<TaxSettingEntity?>

  @Query("SELECT * FROM tax_settings WHERE id = 1 LIMIT 1")
  suspend fun getTaxSettingsSync(): TaxSettingEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTaxSettings(settings: TaxSettingEntity)

  // Company Profile (Local Persistence)
  @Query("SELECT * FROM company_profile WHERE id = 1 LIMIT 1")
  fun getCompanyProfile(): Flow<CompanyProfileEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCompanyProfile(profile: CompanyProfileEntity)

  // Paystack Transactions (Local Persistence)
  @Query("SELECT * FROM paystack_transactions ORDER BY paidAt DESC")
  fun getAllPaystackTransactions(): Flow<List<PaystackTransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPaystackTransaction(transaction: PaystackTransactionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPaystackTransactions(transactions: List<PaystackTransactionEntity>)
}
