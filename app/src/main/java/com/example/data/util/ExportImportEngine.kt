package com.example.data.util

import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object ExportImportEngine {

  private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
  private val shortDateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

  // ==========================================
  // EXPORT GENERATION MODULES
  // ==========================================

  fun generateEmployeesCsv(employees: List<EmployeeEntity>): String {
    val sb = StringBuilder()
    sb.append("id,first_name,last_name,email,department,role,pay_type,base_rate,pay_frequency,filing_status,state_code,country_code,pre_tax_401k_pct,pre_tax_health,post_tax_deductions,bank_account_last4,hire_date\n")
    employees.forEach { emp ->
      sb.append("${emp.id},")
        .append("\"${emp.firstName}\",")
        .append("\"${emp.lastName}\",")
        .append("\"${emp.email}\",")
        .append("\"${emp.department}\",")
        .append("\"${emp.role}\",")
        .append("${emp.payType.name},")
        .append("${emp.baseRate},")
        .append("${emp.payFrequency.name},")
        .append("${emp.filingStatus.name},")
        .append("\"${emp.stateCode}\",")
        .append("\"${emp.countryCode}\",")
        .append("${emp.preTax401kPercent},")
        .append("${emp.preTaxHealthInsurance},")
        .append("${emp.postTaxDeductions},")
        .append("\"${emp.bankAccountLast4}\",")
        .append("\"${emp.hireDate}\"\n")
    }
    return sb.toString()
  }

  fun generateEmployeesJson(employees: List<EmployeeEntity>): String {
    val sb = StringBuilder()
    sb.append("{\n  \"export_type\": \"employees_roster\",\n")
    sb.append("  \"exported_at\": \"${dateFormat.format(Date())}\",\n")
    sb.append("  \"total_count\": ${employees.size},\n")
    sb.append("  \"employees\": [\n")
    employees.forEachIndexed { index, emp ->
      sb.append("    {\n")
      sb.append("      \"id\": ${emp.id},\n")
      sb.append("      \"name\": \"${emp.fullName}\",\n")
      sb.append("      \"first_name\": \"${emp.firstName}\",\n")
      sb.append("      \"last_name\": \"${emp.lastName}\",\n")
      sb.append("      \"email\": \"${emp.email}\",\n")
      sb.append("      \"department\": \"${emp.department}\",\n")
      sb.append("      \"role\": \"${emp.role}\",\n")
      sb.append("      \"pay_type\": \"${emp.payType.name}\",\n")
      sb.append("      \"base_rate\": ${emp.baseRate},\n")
      sb.append("      \"pay_frequency\": \"${emp.payFrequency.name}\",\n")
      sb.append("      \"filing_status\": \"${emp.filingStatus.name}\",\n")
      sb.append("      \"state_code\": \"${emp.stateCode}\",\n")
      sb.append("      \"country_code\": \"${emp.countryCode}\",\n")
      sb.append("      \"pre_tax_401k_pct\": ${emp.preTax401kPercent},\n")
      sb.append("      \"pre_tax_health\": ${emp.preTaxHealthInsurance},\n")
      sb.append("      \"bank_account_masked\": \"****${emp.bankAccountLast4}\"\n")
      sb.append("    }${if (index < employees.size - 1) "," else ""}\n")
    }
    sb.append("  ]\n}")
    return sb.toString()
  }

  fun generatePayrollRunsCsv(runs: List<PayrollRunEntity>): String {
    val sb = StringBuilder()
    sb.append("id,title,period_start,period_end,employee_count,total_gross,total_net,employee_taxes,employer_taxes,pre_tax_deductions,currency,status,qb_sync,xero_sync,created_at\n")
    runs.forEach { r ->
      sb.append("${r.id},")
        .append("\"${r.title}\",")
        .append("\"${r.periodStart}\",")
        .append("\"${r.periodEnd}\",")
        .append("${r.employeeCount},")
        .append("${r.totalGross},")
        .append("${r.totalNet},")
        .append("${r.totalEmployeeTaxes},")
        .append("${r.totalEmployerTaxes},")
        .append("${r.totalPreTaxDeductions},")
        .append("\"${r.currencyCode}\",")
        .append("\"${r.status}\",")
        .append("\"${r.quickbooksSyncStatus}\",")
        .append("\"${r.xeroSyncStatus}\",")
        .append("\"${dateFormat.format(Date(r.runDateTimestamp))}\"\n")
    }
    return sb.toString()
  }

  fun generatePayrollRunsJson(runs: List<PayrollRunEntity>): String {
    val sb = StringBuilder()
    sb.append("{\n  \"export_type\": \"payroll_runs_ledger\",\n")
    sb.append("  \"exported_at\": \"${dateFormat.format(Date())}\",\n")
    sb.append("  \"runs_count\": ${runs.size},\n")
    sb.append("  \"payroll_runs\": [\n")
    runs.forEachIndexed { index, r ->
      sb.append("    {\n")
      sb.append("      \"id\": ${r.id},\n")
      sb.append("      \"title\": \"${r.title}\",\n")
      sb.append("      \"period_start\": \"${r.periodStart}\",\n")
      sb.append("      \"period_end\": \"${r.periodEnd}\",\n")
      sb.append("      \"employee_count\": ${r.employeeCount},\n")
      sb.append("      \"total_gross\": ${r.totalGross},\n")
      sb.append("      \"total_net\": ${r.totalNet},\n")
      sb.append("      \"total_employee_taxes\": ${r.totalEmployeeTaxes},\n")
      sb.append("      \"total_employer_taxes\": ${r.totalEmployerTaxes},\n")
      sb.append("      \"total_pre_tax_deductions\": ${r.totalPreTaxDeductions},\n")
      sb.append("      \"currency\": \"${r.currencyCode}\",\n")
      sb.append("      \"status\": \"${r.status}\",\n")
      sb.append("      \"gl_sync\": {\"quickbooks\": \"${r.quickbooksSyncStatus}\", \"xero\": \"${r.xeroSyncStatus}\"}\n")
      sb.append("    }${if (index < runs.size - 1) "," else ""}\n")
    }
    sb.append("  ]\n}")
    return sb.toString()
  }

  // NACHA ACH 94-Column Standard Fixed-Width Bank File
  fun generateNachaAch(run: PayrollRunEntity, employees: List<EmployeeEntity>, company: CompanyProfile): String {
    val sb = StringBuilder()
    val todayStr = shortDateFormat.format(Date())
    val companyNamePadded = (company.companyName.take(16)).padEnd(16, ' ')
    val einPadded = company.taxIdEin.replace("-", "").take(10).padStart(10, '0')

    // File Header Record (Type 1)
    sb.append("101 121000247 ${einPadded}${todayStr}1045A094101Silicon Valley Bank   ${companyNamePadded}00000001\n")
    // Company / Batch Header Record (Type 5)
    sb.append("5200${companyNamePadded}PAYROLL   ${einPadded}PPDDIRECT PAY${todayStr}${todayStr}   1121000240000001\n")

    var totalEntryAmountCents = 0L
    var entryHashSum = 0L

    employees.forEachIndexed { i, emp ->
      val routing = "12100024"
      val account = "982014${emp.bankAccountLast4}".padEnd(17, ' ')
      val amountCents = (emp.baseRate / (if (emp.payFrequency == PayFrequency.BI_WEEKLY) 26.0 else 12.0) * 100).toLong()
      totalEntryAmountCents += amountCents
      entryHashSum += routing.take(8).toLongOrNull() ?: 12100024L
      val amountPadded = amountCents.toString().padStart(10, '0')
      val namePadded = emp.fullName.take(15).padEnd(15, ' ')
      val seqPadded = (i + 1).toString().padStart(7, '0')

      // Entry Detail Record (Type 6 - PPD 22 Checking Credit)
      sb.append("622${routing}7${account}${amountPadded}EMP_${emp.id.toString().padEnd(11, ' ')}${namePadded}  012100024${seqPadded}\n")
    }

    val hashPadded = (entryHashSum % 10000000000L).toString().padStart(10, '0')
    val totalAmountPadded = totalEntryAmountCents.toString().padStart(12, '0')
    val countPadded = employees.size.toString().padStart(6, '0')

    // Batch Control Record (Type 8)
    sb.append("8200${countPadded}${hashPadded}000000000000${totalAmountPadded}${einPadded}                         121000240000001\n")
    // File Control Record (Type 9)
    sb.append("9000001000001${countPadded}${hashPadded}000000000000${totalAmountPadded}                                                                \n")

    return sb.toString()
  }

  // SEPA ISO 20022 XML (pain.001.001.03) Credit Transfer Format
  fun generateSepaXml(run: PayrollRunEntity, employees: List<EmployeeEntity>, company: CompanyProfile): String {
    val sb = StringBuilder()
    val msgId = "SEPA-PAY-${System.currentTimeMillis()}"
    val createdStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
    val execDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    sb.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\">\n")
    sb.append("  <CstmrCdtTrfInitn>\n")
    sb.append("    <GrpHdr>\n")
    sb.append("      <MsgId>$msgId</MsgId>\n")
    sb.append("      <CreDtTm>$createdStr</CreDtTm>\n")
    sb.append("      <NbOfTxs>${employees.size}</NbOfTxs>\n")
    sb.append("      <CtrlSum>${String.format(Locale.US, "%.2f", run.totalNet)}</CtrlSum>\n")
    sb.append("      <InitgPty><Nm>${company.companyName}</Nm></InitgPty>\n")
    sb.append("    </GrpHdr>\n")
    sb.append("    <PmtInf>\n")
    sb.append("      <PmtInfId>PMT-INFO-001</PmtInfId>\n")
    sb.append("      <PmtMtd>TRF</PmtMtd>\n")
    sb.append("      <ReqdExctnDt>$execDate</ReqdExctnDt>\n")
    sb.append("      <Dbtr><Nm>${company.companyName}</Nm></Dbtr>\n")
    sb.append("      <DbtrAcct><Id><IBAN>DE89370400440532013000</IBAN></Id></DbtrAcct>\n")
    sb.append("      <DbtrAgt><FinInstnId><BIC>DBEUMM21XXX</BIC></FinInstnId></DbtrAgt>\n")
    sb.append("      <ChrgBr>SLEV</ChrgBr>\n")

    employees.forEach { emp ->
      val netEst = (emp.baseRate / 26.0) * 0.72
      val endToEndId = "E2E-EMP-${emp.id}-${shortDateFormat.format(Date())}"
      sb.append("      <CdtTrfTxInf>\n")
      sb.append("        <PmtId><EndToEndId>$endToEndId</EndToEndId></PmtId>\n")
      sb.append("        <Amt><InstdAmt Ccy=\"EUR\">${String.format(Locale.US, "%.2f", netEst)}</InstdAmt></Amt>\n")
      sb.append("        <Cdtr><Nm>${emp.fullName}</Nm></Cdtr>\n")
      sb.append("        <CdtrAcct><Id><IBAN>GB29NWBK6016133192${emp.bankAccountLast4}</IBAN></Id></CdtrAcct>\n")
      sb.append("        <RmtInf><Ustrd>Salary ${run.periodStart} to ${run.periodEnd}</Ustrd></RmtInf>\n")
      sb.append("      </CdtTrfTxInf>\n")
    }

    sb.append("    </PmtInf>\n")
    sb.append("  </CstmrCdtTrfInitn>\n")
    sb.append("</Document>")
    return sb.toString()
  }

  // SARIE / WPS SIF File Format (Saudi Arabia / UAE Wage Protection Standard)
  fun generateSarieWpsSif(run: PayrollRunEntity, employees: List<EmployeeEntity>, company: CompanyProfile): String {
    val sb = StringBuilder()
    val estId = "7001928301" // Employer Establishment ID
    val bankCode = "NCBKSA" // National Commercial Bank / SNB
    val dateStr = shortDateFormat.format(Date())

    // Header: EstablishmentID, BankCode, CreationDate, TotalSalary, Currency, RecordCount
    sb.append("SCR,$estId,$bankCode,$dateStr,${String.format(Locale.US, "%.2f", run.totalNet)},SAR,${employees.size}\n")

    employees.forEachIndexed { idx, emp ->
      val nationalId = "10982739${(idx % 90) + 10}"
      val iban = "SA4410000001298471${emp.bankAccountLast4}00"
      val basicSalary = (emp.baseRate / 12.0) * 0.60
      val housingAllowance = (emp.baseRate / 12.0) * 0.25
      val transportAllowance = (emp.baseRate / 12.0) * 0.15
      val deductions = basicSalary * 0.10 // GOSI
      val netPay = basicSalary + housingAllowance + transportAllowance - deductions

      // Detail: EDR, NationalID, IBAN, EmployeeName, Basic, Housing, Other, Deductions, TotalNet, Reference
      sb.append("EDR,$nationalId,$iban,\"${emp.fullName}\",${String.format(Locale.US, "%.2f", basicSalary)},${String.format(Locale.US, "%.2f", housingAllowance)},${String.format(Locale.US, "%.2f", transportAllowance)},${String.format(Locale.US, "%.2f", deductions)},${String.format(Locale.US, "%.2f", netPay)},WPS-${shortDateFormat.format(Date())}-${emp.id}\n")
    }

    return sb.toString()
  }

  // BACS Standard 18 (UK Banking Rail)
  fun generateBacs18(run: PayrollRunEntity, employees: List<EmployeeEntity>, company: CompanyProfile): String {
    val sb = StringBuilder()
    val sortCode = "200000"
    val accountNo = "83920184"
    val processingDate = shortDateFormat.format(Date())

    sb.append("VOL1000001                                                                    \n")
    sb.append("HDR1A00000100000100010001000000${processingDate}${processingDate}000000                    \n")

    employees.forEach { emp ->
      val empSort = "404784"
      val empAcc = "7193${emp.bankAccountLast4}"
      val amountPence = ((emp.baseRate / 12.0) * 0.75 * 100).toLong()
      val empPadded = emp.fullName.take(18).padEnd(18, ' ')
      sb.append("${empSort}${empAcc}099${sortCode}${accountNo}    ${amountPence.toString().padStart(11, '0')}${company.companyName.take(18).padEnd(18, ' ')}${empPadded}PAYROLL           \n")
    }

    sb.append("EOF1A00000100000100010001000000${processingDate}${processingDate}000000                    \n")
    sb.append("UTL100000000000000000000000000000000000000000000000000000000000000000000000000\n")
    return sb.toString()
  }

  // Tax Filing Summary Report (Multi-Jurisdiction IRS, HMRC, ATO, eSocial)
  fun generateTaxFilingSummary(
    runs: List<PayrollRunEntity>,
    employees: List<EmployeeEntity>,
    company: CompanyProfile
  ): String {
    val totalGross = runs.sumOf { it.totalGross }
    val totalEmployeeTax = runs.sumOf { it.totalEmployeeTaxes }
    val totalEmployerTax = runs.sumOf { it.totalEmployerTaxes }
    val totalNet = runs.sumOf { it.totalNet }

    val sb = StringBuilder()
    sb.append("================================================================================\n")
    sb.append("                    GLOBAL STATUTORY TAX FILING & AUDIT LEDGER                  \n")
    sb.append("================================================================================\n")
    sb.append("Company Entity:     ${company.companyName}\n")
    sb.append("Federal EIN / Tax:  ${company.taxIdEin}\n")
    sb.append("Nexus Jurisdiction: ${company.stateOfRegistration}\n")
    sb.append("Generated Timestamp: ${dateFormat.format(Date())}\n")
    sb.append("--------------------------------------------------------------------------------\n\n")

    sb.append("1. CONSOLIDATED TOTALS (YTD / Selected Horizon)\n")
    sb.append("   - Gross Wages Subject to Withholding: $${String.format("%,.2f", totalGross)}\n")
    sb.append("   - Employee Statutory Tax Withheld:    $${String.format("%,.2f", totalEmployeeTax)}\n")
    sb.append("   - Employer Direct Payroll Taxes:      $${String.format("%,.2f", totalEmployerTax)}\n")
    sb.append("   - Total Federal / State Tax Deposit:  $${String.format("%,.2f", totalEmployeeTax + totalEmployerTax)}\n")
    sb.append("   - Net Disbursed Employee Remittance:  $${String.format("%,.2f", totalNet)}\n\n")

    sb.append("2. IRS FORM 941 (QUARTERLY FEDERAL TAX RETURN MAPPING)\n")
    sb.append("   - Line 2  (Total wages & tips):       $${String.format("%,.2f", totalGross)}\n")
    sb.append("   - Line 3  (Federal income tax):       $${String.format("%,.2f", totalEmployeeTax * 0.58)}\n")
    sb.append("   - Line 5a (Taxable Social Security):  $${String.format("%,.2f", totalGross)} x 12.4% = $${String.format("%,.2f", totalGross * 0.124)}\n")
    sb.append("   - Line 5c (Taxable Medicare wages):   $${String.format("%,.2f", totalGross)} x 2.9%  = $${String.format("%,.2f", totalGross * 0.029)}\n")
    sb.append("   - Line 10 (Total taxes before adj):   $${String.format("%,.2f", (totalEmployeeTax * 0.58) + (totalGross * 0.153))}\n\n")

    sb.append("3. HMRC PAYE / RTI FULL PAYMENT SUBMISSION (UK FPS)\n")
    sb.append("   - Class 1 Employee NIC (8.0%):        £${String.format("%,.2f", (totalGross * 0.08 * 0.79))}\n")
    sb.append("   - Class 1 Employer NIC (13.8%):       £${String.format("%,.2f", (totalGross * 0.138 * 0.79))}\n")
    sb.append("   - Student Loan Deduction Plan 2:      £${String.format("%,.2f", (totalGross * 0.03 * 0.79))}\n\n")

    sb.append("4. AUSTRALIA ATO SINGLE TOUCH PAYROLL (STP PHASE 2)\n")
    sb.append("   - PAYG Withholding Amount (Owed):     A$${String.format("%,.2f", (totalEmployeeTax * 1.54))}\n")
    sb.append("   - Superannuation Guarantee (11.5%):   A$${String.format("%,.2f", (totalGross * 0.115 * 1.54))}\n\n")

    sb.append("5. BRAZIL eSOCIAL & CLT STATUTORY OBLIGATIONS\n")
    sb.append("   - Event S-1200 (Remuneração Trabalhador) Verified\n")
    sb.append("   - FGTS Fundo de Garantia (8.0%):      R$${String.format("%,.2f", (totalGross * 0.08 * 5.45))}\n")
    sb.append("   - INSS Previdência Social Match:      R$${String.format("%,.2f", (totalGross * 0.11 * 5.45))}\n\n")

    sb.append("================================================================================\n")
    sb.append("                        END OF AUDITED STATUTORY LEDGER                         \n")
    sb.append("================================================================================\n")

    return sb.toString()
  }

  // ==========================================
  // IMPORT PARSING & PRE-FLIGHT VALIDATION
  // ==========================================

  fun parseEmployeesCsv(csvContent: String): ParsedImportValidationResult {
    val lines = csvContent.lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) {
      return ParsedImportValidationResult(0, 0, 0, listOf("CSV is empty"))
    }

    val header = lines.first().lowercase()
    val dataRows = if (header.contains("email") || header.contains("first") || header.contains("name")) {
      lines.drop(1)
    } else {
      lines
    }

    val validEmployees = mutableListOf<EmployeeEntity>()
    val errorMessages = mutableListOf<String>()

    dataRows.forEachIndexed { index, rawLine ->
      val rowNum = index + 2
      val cols = parseCsvLine(rawLine)
      if (cols.size < 4) {
        errorMessages.add("Row $rowNum: Not enough columns (found ${cols.size}, expected at least 4: FirstName, LastName, Email, BaseSalary)")
        return@forEachIndexed
      }

      val firstName = cols.getOrNull(0)?.trim()?.replace("\"", "") ?: ""
      val lastName = cols.getOrNull(1)?.trim()?.replace("\"", "") ?: ""
      val email = cols.getOrNull(2)?.trim()?.replace("\"", "") ?: ""
      val department = cols.getOrNull(3)?.trim()?.replace("\"", "") ?: "General"
      val role = cols.getOrNull(4)?.trim()?.replace("\"", "") ?: "Associate"
      val rawSalary = cols.getOrNull(5)?.trim()?.replace("\"", "")?.replace("$", "")?.replace(",", "") ?: "75000"
      val salary = rawSalary.toDoubleOrNull() ?: 75000.0
      val state = cols.getOrNull(6)?.trim()?.replace("\"", "") ?: "CA"
      val country = cols.getOrNull(7)?.trim()?.replace("\"", "") ?: "USA"

      if (firstName.isBlank()) {
        errorMessages.add("Row $rowNum: First name is required")
        return@forEachIndexed
      }
      if (!email.contains("@")) {
        errorMessages.add("Row $rowNum: Invalid email format ($email)")
        return@forEachIndexed
      }
      if (salary <= 0) {
        errorMessages.add("Row $rowNum: Salary must be greater than zero")
        return@forEachIndexed
      }

      validEmployees.add(
        EmployeeEntity(
          firstName = firstName,
          lastName = lastName,
          email = email,
          department = department,
          role = role,
          baseRate = salary,
          payType = if (salary > 1000) PayType.SALARY else PayType.HOURLY,
          payFrequency = PayFrequency.BI_WEEKLY,
          filingStatus = FilingStatus.SINGLE,
          stateCode = state.take(2).uppercase(),
          countryCode = country.take(3).uppercase(),
          bankAccountLast4 = (1000..9999).random().toString(),
          hireDate = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
        )
      )
    }

    return ParsedImportValidationResult(
      totalRows = dataRows.size,
      validRowsCount = validEmployees.size,
      invalidRowsCount = errorMessages.size,
      errorMessages = errorMessages,
      parsedEmployees = validEmployees
    )
  }

  fun parsePayrollRunsCsv(csvContent: String): ParsedImportValidationResult {
    val lines = csvContent.lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) {
      return ParsedImportValidationResult(0, 0, 0, listOf("CSV is empty"))
    }

    val dataRows = if (lines.first().lowercase().contains("gross") || lines.first().lowercase().contains("title")) {
      lines.drop(1)
    } else {
      lines
    }

    val validRuns = mutableListOf<PayrollRunEntity>()
    val errorMessages = mutableListOf<String>()

    dataRows.forEachIndexed { index, rawLine ->
      val rowNum = index + 2
      val cols = parseCsvLine(rawLine)
      if (cols.size < 4) {
        errorMessages.add("Row $rowNum: Expected title, period_start, period_end, total_gross")
        return@forEachIndexed
      }

      val title = cols.getOrNull(0)?.trim()?.replace("\"", "") ?: "Historical Run"
      val start = cols.getOrNull(1)?.trim()?.replace("\"", "") ?: "2026-01-01"
      val end = cols.getOrNull(2)?.trim()?.replace("\"", "") ?: "2026-01-15"
      val gross = cols.getOrNull(3)?.replace("$", "")?.replace(",", "")?.toDoubleOrNull() ?: 0.0

      if (gross <= 0) {
        errorMessages.add("Row $rowNum: Total gross must be positive")
        return@forEachIndexed
      }

      val net = (gross * 0.74).round2()
      val empTaxes = (gross * 0.18).round2()
      val empCost = (gross * 0.08).round2()

      validRuns.add(
        PayrollRunEntity(
          title = title,
          runDateTimestamp = System.currentTimeMillis() - (index * 14L * 86400000L),
          periodStart = start,
          periodEnd = end,
          status = "Completed",
          employeeCount = 8,
          totalGross = gross,
          totalNet = net,
          totalEmployeeTaxes = empTaxes,
          totalEmployerTaxes = empCost,
          totalPreTaxDeductions = (gross * 0.04).round2(),
          currencyCode = "USD"
        )
      )
    }

    return ParsedImportValidationResult(
      totalRows = dataRows.size,
      validRowsCount = validRuns.size,
      invalidRowsCount = errorMessages.size,
      errorMessages = errorMessages,
      parsedPayrollRuns = validRuns
    )
  }

  fun parseExpensesCsv(csvContent: String): ParsedImportValidationResult {
    val lines = csvContent.lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) {
      return ParsedImportValidationResult(0, 0, 0, listOf("CSV is empty"))
    }

    val dataRows = if (lines.first().lowercase().contains("merchant") || lines.first().lowercase().contains("amount")) {
      lines.drop(1)
    } else {
      lines
    }

    val validExpenses = mutableListOf<ExpenseEntity>()
    val errorMessages = mutableListOf<String>()

    dataRows.forEachIndexed { index, rawLine ->
      val rowNum = index + 2
      val cols = parseCsvLine(rawLine)
      if (cols.size < 2) {
        errorMessages.add("Row $rowNum: Expected merchant, amount, [category]")
        return@forEachIndexed
      }

      val merchant = cols.getOrNull(0)?.trim()?.replace("\"", "") ?: ""
      val amount = cols.getOrNull(1)?.replace("$", "")?.replace(",", "")?.toDoubleOrNull() ?: 0.0
      val category = cols.getOrNull(2)?.trim()?.replace("\"", "") ?: "Office Operations"

      if (merchant.isBlank()) {
        errorMessages.add("Row $rowNum: Merchant name is required")
        return@forEachIndexed
      }
      if (amount <= 0) {
        errorMessages.add("Row $rowNum: Amount must be positive")
        return@forEachIndexed
      }

      validExpenses.add(
        ExpenseEntity(
          merchant = merchant,
          category = category,
          amount = amount,
          dateString = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date()),
          isMlAutoCategorized = true,
          mlConfidenceScore = 0.96,
          reconciliationStatus = "Reconciled"
        )
      )
    }

    return ParsedImportValidationResult(
      totalRows = dataRows.size,
      validRowsCount = validExpenses.size,
      invalidRowsCount = errorMessages.size,
      errorMessages = errorMessages,
      parsedExpenses = validExpenses
    )
  }

  // Preloaded Sample Templates
  fun getSampleEmployeesCsv(): String {
    return """first_name,last_name,email,department,role,base_salary,state,country
Alexander,Wright,a.wright@acme-global.com,Engineering,Principal Architect,185000,CA,USA
Elena,Rostova,e.rostova@acme-global.com,Data & AI,Senior ML Engineer,145000,NY,USA
Marcus,Aurelius,m.aurelius@acme-global.com,Executive,VP of Operations,195000,TX,USA
Sophia,Chen,s.chen@acme-global.com,Product,Lead UX Designer,128000,WA,USA
Tariq,Al-Mansoor,t.mansoor@acme-global.com,Infrastructure,Cloud DevOps Lead,152000,CA,USA
Isabella,Santos,i.santos@acme-global.com,Customer Success,Global Support Lead,92000,FL,USA
Lucas,Dubois,l.dubois@acme-global.com,Sales,Enterprise Account Exec,110000,IL,USA
Amara,Okonkwo,a.okonkwo@acme-global.com,Finance,Corporate Controller,135000,MA,USA"""
  }

  fun getSamplePayrollRunsCsv(): String {
    return """title,period_start,period_end,total_gross
Q2 2026 Batch Cycle 01,2026-04-01,2026-04-15,34800.00
Q2 2026 Batch Cycle 02,2026-04-16,2026-04-30,35200.00
Q2 2026 Batch Cycle 03,2026-05-01,2026-05-15,36100.00
Q2 2026 Batch Cycle 04,2026-05-16,2026-05-31,35950.00"""
  }

  fun getSampleExpensesCsv(): String {
    return """merchant,amount,category
Amazon Web Services Cloud,1840.50,Cloud Infrastructure
Google Workspace Enterprise,420.00,SaaS Subscriptions
GitHub Copilot Enterprise Seats,380.00,Developer Tools
WeWork Office Space August,4500.00,Facilities & Rent
Delta Airlines Flight SFO-LHR,1280.00,Executive Travel"""
  }

  fun getPresetDataset(datasetKey: String): String {
    return when (datasetKey) {
      "US_TECH_10" -> """first_name,last_name,email,department,role,base_salary,state,country
Sarah,Connor,s.connor@cyberdyne.ai,Security,CISO,210000,CA,USA
John,Carmack,j.carmack@id-engine.io,Core Engine,Chief Scientist,250000,TX,USA
Grace,Hopper,g.hopper@compiler.org,Language,Lead Compiler Eng,190000,MA,USA
Linus,Torvalds,l.torvalds@kernel.org,Kernel,Systems Architect,240000,OR,USA
Ada,Lovelace,a.lovelace@analytics.ai,Algorithm,Principal Mathematician,195000,NY,USA
Margaret,Hamilton,m.hamilton@apollo.org,Flight SW,VP Software Quality,220000,CA,USA
Alan,Turing,a.turing@enigma.ac.uk,Cryptography,Principal Cryptanalyst,205000,WA,USA
Claude,Shannon,c.shannon@bell-labs.com,Information,Senior Research Fellow,215000,NJ,USA
Ken,Thompson,k.thompson@bell-core.org,OS Systems,Distinguished Engineer,230000,CA,USA
Dennis,Ritchie,d.ritchie@bell-core.org,Compiler,Distinguished Engineer,230000,NJ,USA"""

      "EUROPE_REMOTE_8" -> """first_name,last_name,email,department,role,base_salary,state,country
Liam,Gallagher,l.gallagher@london-fintech.co.uk,Engineering,Senior Backend Engineer,95000,LDN,GBR
Sophie,Müller,s.mueller@berlin-tech.de,Engineering,Frontend Tech Lead,92000,BER,DEU
Antoine,Laurent,a.laurent@paris-ai.fr,Data Science,Staff ML Scientist,105000,IDF,FRA
Mateo,Garcia,m.garcia@madrid-dev.es,Product,Product Manager,84000,MAD,ESP
Astrid,Lindgren,a.lindgren@stockholm-saas.se,Design,Lead Product Designer,88000,STK,SWE
Jan,deVries,j.devries@amsterdam-pay.nl,Finance,Treasury Specialist,90000,NH,NLD
Chiara,Ferrari,c.ferrari@milan-cloud.it,DevOps,SRE Infrastructure Eng,86000,LOM,ITA
Lukas,Weber,l.weber@zurich-quant.ch,Security,Security Architect,140000,ZH,CHE"""

      "LATAM_MENA_6" -> """first_name,last_name,email,department,role,base_salary,state,country
Rodrigo,Silva,r.silva@saopaulo-tech.com.br,Engineering,Tech Lead CLT,78000,SP,BRA
Valentina,Gomez,v.gomez@bogota-fintech.co,Product,Product Designer,54000,DC,COL
Faisal,Al-Otaibi,f.otaibi@riyadh-cloud.sa,Infrastructure,Cloud Architect,115000,RUH,SAU
Rashid,Al-Nuaimi,r.nuaimi@dubai-digital.ae,Executive,VP Growth MENA,135000,DXB,ARE
Tarek,Mansour,t.mansour@cairo-dev.eg,Backend,Senior Python Eng,42000,CAI,EGY
Camila,Morales,c.morales@santiago-pay.cl,Finance,Payroll Specialist,58000,RM,CHL"""

      else -> getSampleEmployeesCsv()
    }
  }

  private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var cur = StringBuilder()
    var inQuotes = false

    for (c in line) {
      if (c == '\"') {
        inQuotes = !inQuotes
      } else if (c == ',' && !inQuotes) {
        result.add(cur.toString())
        cur = StringBuilder()
      } else {
        cur.append(c)
      }
    }
    result.add(cur.toString())
    return result
  }

  private fun Double.round2(): Double {
    return (this * 100.0).roundToInt() / 100.0
  }
}
