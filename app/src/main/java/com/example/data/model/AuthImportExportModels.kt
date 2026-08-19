package com.example.data.model

enum class ExportCategory(val title: String, val subtitle: String, val iconName: String) {
  EMPLOYEES("Staff Roster", "Employee records, salaries, tax IDs & banking", "Group"),
  PAYROLL_RUNS("Payroll Ledger", "Completed batch runs, gross-to-net summaries", "ReceiptLong"),
  BANK_CLEARING("Bank Clearing Batches", "NACHA ACH, SEPA ISO20022 XML, SARIE SIF, BACS, PIX", "AccountBalance"),
  TAX_FILINGS("Statutory Tax Returns", "IRS 941, HMRC FPS, STP Phase 2, eSocial, GOSI", "Summarize"),
  GL_JOURNALS("GL Double-Entry Sync", "QuickBooks, Xero & NetSuite journal entries", "AccountBalanceWallet"),
  EXPENSES("Expenses & Receipts", "Categorized bank transactions & OCR receipts", "Receipt"),
  FULL_BACKUP("Full Database JSON", "Complete Room DB schema archive & snapshot", "CloudDownload")
}

enum class ExportFileFormat(val displayName: String, val extension: String, val mimeType: String) {
  CSV("CSV (Excel / Sheets)", ".csv", "text/csv"),
  JSON("JSON (REST / API)", ".json", "application/json"),
  NACHA_ACH("NACHA ACH 94-Column (US)", ".ach", "text/plain"),
  SEPA_XML("SEPA ISO20022 XML (EU)", ".xml", "application/xml"),
  SARIE_WPS_SIF("SARIE / WPS SIF (MENA)", ".sif", "text/plain"),
  BACS_18("BACS Standard 18 (UK)", ".bacs", "text/plain"),
  PIX_CNAB("PIX CNAB 240 (Brazil)", ".rem", "text/plain"),
  NIBSS_CSV("NIBSS NIP CSV (Africa)", ".csv", "text/csv"),
  SUMMARY_REPORT("Executive Report Text", ".txt", "text/plain")
}

enum class ImportTargetType(val title: String, val description: String, val sampleFilename: String) {
  EMPLOYEES("Staff Roster (Employees)", "Bulk onboard employees with salary, role, department and bank data", "employees_template.csv"),
  PAYROLL_RUNS("Historical Payroll Runs", "Import past period payroll summaries and tax deductions", "payroll_runs_template.csv"),
  EXPENSES("Expenses & Receipts", "Import categorized bank line items and vendor expenses", "expenses_template.csv")
}

data class ParsedImportValidationResult(
  val totalRows: Int,
  val validRowsCount: Int,
  val invalidRowsCount: Int,
  val errorMessages: List<String>,
  val parsedEmployees: List<EmployeeEntity> = emptyList(),
  val parsedPayrollRuns: List<PayrollRunEntity> = emptyList(),
  val parsedExpenses: List<ExpenseEntity> = emptyList()
)

data class TaxBracketBand(
  val tierLabel: String,
  val lowerLimit: Double,
  val upperLimit: Double,
  val marginalRatePercent: Double,
  val taxableInThisTier: Double,
  val taxOwedInThisTier: Double,
  val colorHex: Long
)

data class TaxComparisonJurisdiction(
  val countryName: String,
  val flagEmoji: String,
  val currencyCode: String,
  val currencySymbol: String,
  val employeeEffectiveTaxPercent: Double,
  val employeeSocialSecPercent: Double,
  val employeePensionPercent: Double,
  val employeeNetTakeHomePercent: Double,
  val employerOverheadPercent: Double,
  val sampleGrossAnnual: Double,
  val sampleNetAnnual: Double,
  val sampleEmployerTotalCost: Double
)

data class AuthPinLockConfig(
  val isPinLockEnabled: Boolean = false,
  val pinCodeHash: String = "1234",
  val autoLockTimeoutMinutes: Int = 5,
  val isBiometricAllowed: Boolean = true,
  val maxFailedAttempts: Int = 5,
  val currentFailedAttempts: Int = 0,
  val isAppLockedNow: Boolean = false
)

data class TwoFactorAuthSetupData(
  val isEnabled: Boolean = true,
  val secretKeyBase32: String = "JBSWY3DPEHPK3PXP",
  val issuer: String = "PayFlow Enterprise (Global)",
  val accountName: String = "jane.doe@acme-global.com",
  val currentTotpToken: String = "849 201",
  val secondsRemainingInWindow: Int = 24,
  val recoveryBackupCodes: List<String> = listOf(
    "8F92-4A1C", "99B2-E801", "7C33-D441", "12EA-90F3",
    "66D1-AA94", "4E81-19B0", "33F9-281C", "55A8-CC72"
  )
)
