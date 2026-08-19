package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CompanyProfileEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.FilingStatus
import com.example.data.model.PayFrequency
import com.example.data.model.PayType
import com.example.data.model.PayrollItemEntity
import com.example.data.model.PayrollRunEntity
import com.example.data.model.PaystackTransactionEntity
import com.example.data.model.TaxSettingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    EmployeeEntity::class,
    PayrollRunEntity::class,
    PayrollItemEntity::class,
    ExpenseEntity::class,
    TaxSettingEntity::class,
    CompanyProfileEntity::class,
    PaystackTransactionEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class PayrollDatabase : RoomDatabase() {
  abstract fun payrollDao(): PayrollDao

  companion object {
    @Volatile
    private var INSTANCE: PayrollDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): PayrollDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          PayrollDatabase::class.java,
          "payroll_pro_db"
        )
          .fallbackToDestructiveMigration()
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database.payrollDao())
          }
        }
      }
    }

    suspend fun populateInitialData(dao: PayrollDao) {
      val sampleEmployees = listOf(
        EmployeeEntity(
          firstName = "Alexander",
          lastName = "Wright",
          email = "a.wright@acme-enterprise.com",
          role = "Principal Software Architect",
          department = "Engineering",
          payType = PayType.SALARY,
          baseRate = 165000.0,
          payFrequency = PayFrequency.BI_WEEKLY,
          filingStatus = FilingStatus.MARRIED_FILING_JOINTLY,
          preTax401kPercent = 6.0,
          preTaxHealthInsurance = 180.0,
          postTaxDeductions = 50.0,
          stateCode = "CA",
          currencyCode = "USD"
        ),
        EmployeeEntity(
          firstName = "Elena",
          lastName = "Rostova",
          email = "e.rostova@acme-enterprise.com",
          role = "Head of Product Design",
          department = "Design",
          payType = PayType.SALARY,
          baseRate = 142000.0,
          payFrequency = PayFrequency.BI_WEEKLY,
          filingStatus = FilingStatus.SINGLE,
          preTax401kPercent = 5.0,
          preTaxHealthInsurance = 140.0,
          postTaxDeductions = 0.0,
          stateCode = "NY",
          currencyCode = "USD"
        ),
        EmployeeEntity(
          firstName = "Marcus",
          lastName = "Chen",
          email = "m.chen@acme-enterprise.com",
          role = "Senior Cloud Operations Lead",
          department = "Infrastructure",
          payType = PayType.HOURLY,
          baseRate = 68.0,
          payFrequency = PayFrequency.BI_WEEKLY,
          filingStatus = FilingStatus.SINGLE,
          preTax401kPercent = 4.0,
          preTaxHealthInsurance = 120.0,
          postTaxDeductions = 25.0,
          stateCode = "TX",
          currencyCode = "USD"
        ),
        EmployeeEntity(
          firstName = "Sophia",
          lastName = "Patel",
          email = "s.patel@acme-enterprise.com",
          role = "Finance & Tax Compliance Director",
          department = "Finance",
          payType = PayType.SALARY,
          baseRate = 135000.0,
          payFrequency = PayFrequency.BI_WEEKLY,
          filingStatus = FilingStatus.HEAD_OF_HOUSEHOLD,
          preTax401kPercent = 8.0,
          preTaxHealthInsurance = 160.0,
          postTaxDeductions = 0.0,
          stateCode = "CA",
          currencyCode = "USD"
        ),
        EmployeeEntity(
          firstName = "David",
          lastName = "Kim",
          email = "d.kim@acme-enterprise.com",
          role = "Lead Mobile Engineer",
          department = "Engineering",
          payType = PayType.SALARY,
          baseRate = 150000.0,
          payFrequency = PayFrequency.BI_WEEKLY,
          filingStatus = FilingStatus.SINGLE,
          preTax401kPercent = 5.0,
          preTaxHealthInsurance = 140.0,
          postTaxDeductions = 0.0,
          stateCode = "WA",
          currencyCode = "USD"
        ),
        EmployeeEntity(
          firstName = "Olivia",
          lastName = "Taylor",
          email = "o.taylor@acme-enterprise.com",
          role = "Technical Customer Support",
          department = "Operations",
          payType = PayType.HOURLY,
          baseRate = 32.50,
          payFrequency = PayFrequency.BI_WEEKLY,
          filingStatus = FilingStatus.SINGLE,
          preTax401kPercent = 3.0,
          preTaxHealthInsurance = 95.0,
          postTaxDeductions = 0.0,
          stateCode = "FL",
          currencyCode = "USD"
        )
      )
      dao.insertEmployees(sampleEmployees)

      val run1Id = dao.insertPayrollRun(
        PayrollRunEntity(
          title = "Bi-Weekly Regular Payroll - Cycle #16",
          runDateTimestamp = System.currentTimeMillis() - 86400000L * 14,
          periodStart = "Aug 01, 2026",
          periodEnd = "Aug 14, 2026",
          status = "Completed",
          employeeCount = 6,
          totalGross = 29780.0,
          totalNet = 21420.0,
          totalEmployeeTaxes = 6120.0,
          totalEmployerTaxes = 2480.0,
          totalPreTaxDeductions = 2240.0,
          currencyCode = "USD",
          quickbooksSyncStatus = "Synced",
          xeroSyncStatus = "Synced"
        )
      )

      val run1Items = listOf(
        PayrollItemEntity(
          runId = run1Id,
          employeeId = 1,
          employeeName = "Alexander Wright",
          department = "Engineering",
          regularHours = 80.0,
          overtimeHours = 0.0,
          grossPay = 6346.15,
          federalTax = 912.40,
          stateTax = 380.70,
          socialSecurityTax = 393.46,
          medicareTax = 92.02,
          preTaxDeductions = 560.77,
          postTaxDeductions = 50.0,
          netPay = 3956.80,
          employerFicaMatch = 485.48,
          employerFuta = 38.08,
          employerSuta = 171.35,
          totalEmployerCost = 7041.06
        ),
        PayrollItemEntity(
          runId = run1Id,
          employeeId = 2,
          employeeName = "Elena Rostova",
          department = "Design",
          regularHours = 80.0,
          overtimeHours = 0.0,
          grossPay = 5461.54,
          federalTax = 842.10,
          stateTax = 310.20,
          socialSecurityTax = 338.62,
          medicareTax = 79.19,
          preTaxDeductions = 413.08,
          postTaxDeductions = 0.0,
          netPay = 3478.35,
          employerFicaMatch = 417.81,
          employerFuta = 32.77,
          employerSuta = 147.46,
          totalEmployerCost = 6059.58
        ),
        PayrollItemEntity(
          runId = run1Id,
          employeeId = 3,
          employeeName = "Marcus Chen",
          department = "Infrastructure",
          regularHours = 80.0,
          overtimeHours = 6.0,
          grossPay = 6052.00,
          federalTax = 890.30,
          stateTax = 0.00,
          socialSecurityTax = 375.22,
          medicareTax = 87.75,
          preTaxDeductions = 362.08,
          postTaxDeductions = 25.0,
          netPay = 4311.65,
          employerFicaMatch = 462.98,
          employerFuta = 36.31,
          employerSuta = 163.40,
          totalEmployerCost = 6714.69
        )
      )
      dao.insertPayrollItems(run1Items)

      val sampleExpenses = listOf(
        ExpenseEntity(
          merchant = "AWS Cloud Infrastructure",
          category = "Cloud & IT Hosting",
          amount = 1420.50,
          dateString = "Aug 15, 2026",
          isMlAutoCategorized = true,
          mlConfidenceScore = 0.98,
          reconciliationStatus = "Reconciled"
        ),
        ExpenseEntity(
          merchant = "Gusto Payroll Tax Processing Fee",
          category = "Payroll Processing",
          amount = 89.00,
          dateString = "Aug 14, 2026",
          isMlAutoCategorized = true,
          mlConfidenceScore = 0.99,
          reconciliationStatus = "Reconciled"
        ),
        ExpenseEntity(
          merchant = "Fidelity 401(k) Employer Match Wire",
          category = "Employee Benefits",
          amount = 1840.20,
          dateString = "Aug 14, 2026",
          isMlAutoCategorized = true,
          mlConfidenceScore = 0.95,
          reconciliationStatus = "Reconciled"
        ),
        ExpenseEntity(
          merchant = "Kaiser Permanente Health Premiums",
          category = "Health Insurance",
          amount = 2650.00,
          dateString = "Aug 08, 2026",
          isMlAutoCategorized = true,
          mlConfidenceScore = 0.97,
          reconciliationStatus = "Reconciled"
        )
      )
      dao.insertExpenses(sampleExpenses)

      // Initial Global Tax Settings
      dao.insertTaxSettings(
        TaxSettingEntity(
          id = 1,
          activeJurisdictionCode = "USA",
          activeContinent = "North America",
          defaultCurrencyCode = "USD",
          vatGstVasRatePercent = 0.0,
          employerPensionMatchPercent = 6.20,
          statutoryHealthPercent = 1.45,
          standardOvertimeMultiplier = 1.5,
          localClearingFormat = "ACH NACHA / US Fedwire",
          enforceWpsPayroll = false
        )
      )

      // Initial Company Profile
      dao.insertCompanyProfile(
        CompanyProfileEntity(
          id = 1,
          companyName = "Acme Global Enterprise Inc.",
          taxIdEin = "12-3456789",
          stateOfRegistration = "California (CA)",
          jurisdictionCountry = "United States",
          selectedTierName = "PRO",
          isYearlyBilling = true,
          quickBooksConnected = true,
          xeroConnected = true,
          isOnboardingCompleted = true
        )
      )

      // Initial Paystack Global Transactions
      val samplePaystack = listOf(
        PaystackTransactionEntity(
          reference = "pstk_tx_99812401",
          tierTitle = "Enterprise Sovereign",
          billingCycle = "Yearly",
          amount = 2300.40,
          currency = "USD",
          channelName = "Card (Visa / Mastercard)",
          customerEmail = "cfo@acme-global.com",
          status = "Success",
          paidAt = "Today, 11:20 AM",
          authorizationCode = "AUTH_pstk_91823",
          bankOrIssuer = "JPMorgan Chase / Visa 3DS",
          feesDeducted = 34.50,
          paystackSettlementStatus = "Settled to Balance (Instant)",
          regionalMetadata = "US NACHA Direct Clearing"
        ),
        PaystackTransactionEntity(
          reference = "pstk_tx_99812402",
          tierTitle = "Pro Enterprise Multi-Rail",
          billingCycle = "Monthly",
          amount = 350.00,
          currency = "SAR",
          channelName = "Mada (Saudi Arabia)",
          customerEmail = "finance.mena@acme-global.com",
          status = "Success",
          paidAt = "Yesterday, 04:15 PM",
          authorizationCode = "AUTH_pstk_64210",
          bankOrIssuer = "Al Rajhi Bank / Mada Switch",
          feesDeducted = 5.25,
          paystackSettlementStatus = "Settled (SAMA Compliant)",
          regionalMetadata = "MENA Direct Debit Switch"
        )
      )
      dao.insertPaystackTransactions(samplePaystack)
    }
  }
}
