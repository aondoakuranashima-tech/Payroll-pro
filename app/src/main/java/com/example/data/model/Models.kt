package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PayType(val displayName: String) {
  HOURLY("Hourly Rate"),
  SALARY("Annual Salary")
}

enum class PayFrequency(val displayName: String, val periodsPerYear: Int) {
  WEEKLY("Weekly (52/yr)", 52),
  BI_WEEKLY("Bi-Weekly (26/yr)", 26),
  SEMI_MONTHLY("Semi-Monthly (24/yr)", 24),
  MONTHLY("Monthly (12/yr)", 12)
}

enum class FilingStatus(val displayName: String, val standardDeductionAnnual: Double) {
  SINGLE("Single", 14600.0),
  MARRIED_FILING_JOINTLY("Married Filing Jointly", 29200.0),
  HEAD_OF_HOUSEHOLD("Head of Household", 21900.0)
}

enum class SubscriptionTier(
  val title: String,
  val monthlyPriceAmount: Double,
  val isPerUser: Boolean,
  val yearlyDiscountPercent: Double,
  val badge: String,
  val isPro: Boolean,
  val features: List<String>
) {
  BASIC(
    title = "Basic",
    monthlyPriceAmount = 49.0,
    isPerUser = false,
    yearlyDiscountPercent = 1.5,
    badge = "Starter",
    isPro = true,
    features = listOf(
      "Standard Gross-to-Net payroll calculations",
      "Up to 10 employees included",
      "Standard CSV & PDF payroll summary reports",
      "Direct email customer support"
    )
  ),
  PRO(
    title = "Pro",
    monthlyPriceAmount = 99.0,
    isPerUser = false,
    yearlyDiscountPercent = 2.0,
    badge = "Most Popular",
    isPro = true,
    features = listOf(
      "Unlimited staff & automated bulk payroll runs",
      "Live QuickBooks & Xero double-entry GL sync",
      "AI-driven budget forecasting (3-12 months)",
      "Multi-currency support & Ad-free experience"
    )
  ),
  PREMIUM(
    title = "Premium",
    monthlyPriceAmount = 139.0,
    isPerUser = false,
    yearlyDiscountPercent = 2.5,
    badge = "Growth",
    isPro = true,
    features = listOf(
      "Everything in Pro suite",
      "Self-learning ML receipt & expense categorization",
      "Plaid real-time bank feeds reconciliation",
      "IRS Form 941, FUTA & state tax schedule locks"
    )
  ),
  ENTERPRISE(
    title = "Enterprise",
    monthlyPriceAmount = 169.0,
    isPerUser = false,
    yearlyDiscountPercent = 3.0,
    badge = "Advanced",
    isPro = true,
    features = listOf(
      "Everything in Premium suite",
      "AI Anomaly & Fraud Prevention Audit engine",
      "Full Developer RESTful API & Webhooks access",
      "Custom role-based permissions & audit logging"
    )
  ),
  SOVEREIGN(
    title = "Sovereign",
    monthlyPriceAmount = 199.0,
    isPerUser = false,
    yearlyDiscountPercent = 3.5,
    badge = "Ultimate",
    isPro = true,
    features = listOf(
      "All Enterprise features + Private cloud enclave",
      "Dedicated CPA tax advisor & compliance review",
      "Real-time Monte Carlo cash burn simulations",
      "SLA 99.99% uptime & 24/7 VIP priority support"
    )
  ),
  USERBASE(
    title = "Userbase",
    monthlyPriceAmount = 29.0,
    isPerUser = true,
    yearlyDiscountPercent = 2.0,
    badge = "Per Seat",
    isPro = true,
    features = listOf(
      "Flexible pay-per-user seat model ($29/user/mo)",
      "All Pro payroll & GL double-entry sync tools",
      "Unlimited employee self-service access portals",
      "Dynamic team scaling with zero minimums"
    )
  );

  val priceMonthly: String
    get() = if (isPerUser) "$${monthlyPriceAmount.toInt()}/user/mo" else "$${monthlyPriceAmount.toInt()}/mo"

  val yearlyTotalPrice: Double
    get() = (monthlyPriceAmount * 12.0) * (1.0 - (yearlyDiscountPercent / 100.0))

  val yearlyFormulaDescription: String
    get() = if (isPerUser) {
      "User Base plan × 12 - ${yearlyDiscountPercent}%"
    } else {
      "${title.lowercase()} plan × 12 - ${yearlyDiscountPercent}%"
    }

  val yearlyFormatted: String
    get() = if (isPerUser) {
      "$${"%.2f".format(yearlyTotalPrice)}/user/yr"
    } else {
      "$${"%.2f".format(yearlyTotalPrice)}/yr"
    }

  val yearlySavingsAmount: Double
    get() = (monthlyPriceAmount * 12.0) * (yearlyDiscountPercent / 100.0)

  val yearlySavingsFormatted: String
    get() = if (isPerUser) {
      "Save $${"%.2f".format(yearlySavingsAmount)}/user/yr (-${yearlyDiscountPercent}%)"
    } else {
      "Save $${"%.2f".format(yearlySavingsAmount)}/yr (-${yearlyDiscountPercent}%)"
    }
}

enum class UserRole(val roleName: String, val description: String) {
  OWNER("Owner / CFO", "Full administrative access, tax filings, and API keys"),
  PAYROLL_ADMIN("Payroll Administrator", "Execute payroll runs, manage pay rates and benefits"),
  HR_MANAGER("HR Manager", "Manage employee records and onboarding"),
  AUDITOR("Auditor (Read-Only)", "View reports, tax compliance, and reconciliation logs")
}

enum class GlobalContinent(val displayName: String, val emoji: String) {
  NORTH_AMERICA("North America", "🌎"),
  SOUTH_AMERICA("South America", "🌎"),
  EUROPE("Europe", "🌍"),
  MENA("Middle East & North Africa (MENA)", "🌍"),
  ASIA_PACIFIC("Asia & Pacific", "🌏"),
  AFRICA("Sub-Saharan Africa", "🌍")
}

enum class GlobalJurisdiction(
  val countryCode: String,
  val countryName: String,
  val continent: GlobalContinent,
  val flagEmoji: String,
  val currencyCode: String,
  val currencySymbol: String,
  val rateToUsd: Double,
  val standardVatGstPercent: Double,
  val pensionFundName: String,
  val employeePensionRate: Double, // %
  val employerPensionRate: Double, // %
  val healthInsuranceName: String,
  val employeeHealthRate: Double, // %
  val employerHealthRate: Double, // %
  val statutoryLaws: String,
  val clearingAndWpsFormat: String,
  val hasMandatoryWps: Boolean = false,
  val hasThirteenthMonthBonus: Boolean = false,
  val hasEndOfServiceGratuity: Boolean = false
) {
  // North America
  USA("USA", "United States", GlobalContinent.NORTH_AMERICA, "🇺🇸", "USD", "$", 1.00, 0.0, "FICA Social Security & 401(k)", 6.20, 6.20, "FICA Medicare", 1.45, 1.45, "IRC Sec 3402, FLSA & ERISA 2026", "ACH NACHA Direct Deposit / US Fedwire"),
  CANADA("CAN", "Canada", GlobalContinent.NORTH_AMERICA, "🇨🇦", "CAD", "C$", 1.36, 13.0, "Canada Pension Plan (CPP)", 5.95, 5.95, "Employment Insurance (EI)", 1.66, 2.32, "Income Tax Act (RSC 1985) & T4 Slip", "EFT CPA Standard 005 / Interac e-Transfer"),
  MEXICO("MEX", "Mexico", GlobalContinent.NORTH_AMERICA, "🇲🇽", "MXN", "Mex$", 18.20, 16.0, "IMSS Retiro & Cesantía (AFORE)", 2.375, 5.15, "IMSS Enfermedad y Maternidad", 0.625, 20.40, "Ley Federal del Trabajo (LFT) & Aguinaldo", "SPEI / SAT CFDI de Nómina XML 4.0", hasThirteenthMonthBonus = true),

  // South America
  BRAZIL("BRA", "Brazil", GlobalContinent.SOUTH_AMERICA, "🇧🇷", "BRL", "R$", 5.45, 17.0, "INSS Previdência Social", 9.00, 20.00, "FGTS Fundo de Garantia", 0.00, 8.00, "CLT Consolidação das Leis do Trabalho & 13º", "PIX / TED / eSocial Folha de Pagamento", hasThirteenthMonthBonus = true),
  ARGENTINA("ARG", "Argentina", GlobalContinent.SOUTH_AMERICA, "🇦🇷", "ARS", "$", 940.00, 21.0, "Jubilación (SIPA)", 11.00, 10.17, "Obra Social & PAMI Ley 19.032", 6.00, 6.00, "Ley de Contrato de Trabajo 20.744 & SAC", "CBU / Transferencia 3.0 / AFIP F.931", hasThirteenthMonthBonus = true),
  COLOMBIA("COL", "Colombia", GlobalContinent.SOUTH_AMERICA, "🇨🇴", "COP", "COL$", 4050.00, 19.0, "Pensión Obligatoria (Colpensiones/AFP)", 4.00, 12.00, "Salud (EPS / ADRES)", 4.00, 8.50, "Código Sustantivo del Trabajo & Prima Legal", "ACH Colombia / DIAN Nómina Electrónica", hasThirteenthMonthBonus = true),
  CHILE("CHL", "Chile", GlobalContinent.SOUTH_AMERICA, "🇨🇱", "CLP", "CLP$", 930.00, 19.0, "AFP Fondo de Pensiones", 10.00, 0.00, "Fonasa / Isapre Salud", 7.00, 0.00, "Código del Trabajo & Seguro Cesantía", "Previred / TEF BancoEstado"),

  // Europe
  UNITED_KINGDOM("GBR", "United Kingdom", GlobalContinent.EUROPE, "🇬🇧", "GBP", "£", 0.79, 20.0, "Auto-Enrolment Workplace Pension", 5.00, 3.00, "National Insurance (NIC Class 1)", 8.00, 13.80, "Finance Act 2026 & Employment Rights Act", "BACS Direct Credit / HMRC RTI FPS"),
  GERMANY("DEU", "Germany", GlobalContinent.EUROPE, "🇩🇪", "EUR", "€", 0.92, 19.0, "Gesetzliche Rentenversicherung (GRV)", 9.30, 9.30, "Krankenversicherung & Pflegeversicherung", 9.50, 9.50, "Einkommensteuergesetz (EStG) & ELStAM", "SEPA ISO 20022 XML Direct Debit/Credit"),
  FRANCE("FRA", "France", GlobalContinent.EUROPE, "🇫🇷", "EUR", "€", 0.92, 20.0, "Retraite Complémentaire Agirc-Arrco", 3.15, 4.72, "Sécurité Sociale (CSG / CRDS)", 9.70, 13.00, "Code du Travail & Prélèvement à la Source", "SEPA Credit Transfer / DSN Mensuelle"),
  NETHERLANDS("NLD", "Netherlands", GlobalContinent.EUROPE, "🇳🇱", "EUR", "€", 0.92, 21.0, "Pensioenfonds Premie", 4.00, 8.00, "Zorgverzekeringswet (Zvw)", 0.00, 6.57, "Wet op de Loonbelasting 1964 & 30% Ruling", "SEPA BACS / Belastingdienst Loonaangifte"),
  SPAIN("ESP", "Spain", GlobalContinent.EUROPE, "🇪🇸", "EUR", "€", 0.92, 21.0, "Seguridad Social (Contingencias Comunes)", 4.70, 23.60, "Desempleo y Formación Profesional", 1.65, 6.20, "Estatuto de los Trabajadores & Pagas Extras", "SEPA Transferencia / Sistema RED TGSS", hasThirteenthMonthBonus = true),

  // Middle East & North Africa (MENA)
  SAUDI_ARABIA("SAU", "Saudi Arabia", GlobalContinent.MENA, "🇸🇦", "SAR", "﷼", 3.75, 15.0, "GOSI Social Insurance & SANED", 9.75, 11.75, "Occupational Hazard (GOSI Expat)", 0.00, 2.00, "Saudi Labor Law Royal Decree M/51 & EOSG", "SAMA Wages Protection System (WPS SIF)", hasMandatoryWps = true, hasEndOfServiceGratuity = true),
  UAE("ARE", "United Arab Emirates", GlobalContinent.MENA, "🇦🇪", "AED", "د.إ", 3.67, 5.0, "GPSSA National Pension (UAE Citizens)", 5.00, 12.50, "Mandatory Medical Insurance (DHA/DoH)", 0.00, 3.00, "UAE Federal Decree Law No. 33/2021", "MOHRE Wages Protection System (WPS SIF)", hasMandatoryWps = true, hasEndOfServiceGratuity = true),
  EGYPT("EGY", "Egypt", GlobalContinent.MENA, "🇪🇬", "EGP", "ج.م", 48.50, 14.0, "Social Insurance Law 148", 11.00, 18.75, "Universal Health Insurance", 1.00, 4.00, "Unified Labor Law No. 12/2003", "ACH Egypt / Fawry Enterprise Direct Pay"),
  QATAR("QAT", "Qatar", GlobalContinent.MENA, "🇶🇦", "QAR", "ر.ق", 3.64, 0.0, "GRSIA National Retirement Fund", 5.00, 15.00, "National Health Hamad Coverage", 0.00, 0.00, "Qatar Labor Law No. 14 of 2004 & EOSG", "Qatar Central Bank WPS SIF Format", hasMandatoryWps = true, hasEndOfServiceGratuity = true),
  KUWAIT("KWT", "Kuwait", GlobalContinent.MENA, "🇰🇼", "KWD", "د.ك", 0.31, 0.0, "PIFSS Public Social Security", 10.50, 11.50, "Ministry of Health Assurance", 0.00, 0.00, "Private Sector Labor Law No. 6/2010", "Kuwait Central Bank WPS SIF Format", hasMandatoryWps = true, hasEndOfServiceGratuity = true),
  BAHRAIN("BHR", "Bahrain", GlobalContinent.MENA, "🇧🇭", "BHD", "BD", 0.38, 10.0, "SIO Social Insurance (Citizen 7% / Expat 1%)", 7.00, 12.00, "National Health Regulatory Coverage", 0.00, 3.00, "Labor Law No. 36 of 2012", "CBB EFTS / BenefitPay WPS Format", hasMandatoryWps = true, hasEndOfServiceGratuity = true),

  // Asia & Pacific
  AUSTRALIA("AUS", "Australia", GlobalContinent.ASIA_PACIFIC, "🇦🇺", "AUD", "A$", 1.52, 10.0, "Superannuation Guarantee (SG)", 0.00, 11.50, "Medicare Levy & Surcharge", 2.00, 0.00, "Fair Work Act 2009 & Super Guarantee Act", "ABA Direct Entry (BECS) / ATO STP Phase 2"),
  NEW_ZEALAND("NZL", "New Zealand", GlobalContinent.ASIA_PACIFIC, "🇳🇿", "NZD", "NZ$", 1.65, 15.0, "KiwiSaver Superannuation", 3.00, 3.00, "ACC Earner's Levy", 1.60, 0.00, "Employment Relations Act 2000 & PayDay", "Direct Credit BECS / IRD PayDay Filing"),
  JAPAN("JPN", "Japan", GlobalContinent.ASIA_PACIFIC, "🇯🇵", "JPY", "¥", 154.50, 10.0, "Kōsei Nenkin (Welfare Pension)", 9.15, 9.15, "Kenkō Hoken (Health Insurance)", 4.90, 4.90, "Labor Standards Act (Act No. 49 of 1947)", "Zengin Data Format (全銀フォーマット)"),
  SINGAPORE("SGP", "Singapore", GlobalContinent.ASIA_PACIFIC, "🇸🇬", "SGD", "S$", 1.34, 9.0, "Central Provident Fund (CPF)", 20.00, 17.00, "Skills Development Levy (SDL)", 0.00, 0.25, "Employment Act (Cap. 91) & CPF Act", "GIRO / FAST / IRAS AIS IR8A Format"),
  INDIA("IND", "India", GlobalContinent.ASIA_PACIFIC, "🇮🇳", "INR", "₹", 83.40, 18.0, "Employee Provident Fund (EPF)", 12.00, 12.00, "Employee State Insurance (ESI)", 0.75, 3.25, "Code on Wages 2019 & Income Tax Sec 115BAC", "NACH / NEFT / Form 16 / TDS 24Q"),
  PHILIPPINES("PHL", "Philippines", GlobalContinent.ASIA_PACIFIC, "🇵🇭", "PHP", "₱", 57.50, 12.0, "SSS Social Security Contribution", 4.50, 9.50, "PhilHealth & Pag-IBIG HDMF", 2.50, 2.50, "Labor Code of the Philippines & TRAIN Law", "InstaPay / PESONet / BIR Form 2316", hasThirteenthMonthBonus = true),

  // Sub-Saharan Africa
  NIGERIA("NGA", "Nigeria", GlobalContinent.AFRICA, "🇳🇬", "NGN", "₦", 1580.00, 7.5, "Pension Reform Act (PRA 2014)", 8.00, 10.00, "National Housing Fund (NHF) & NSITF", 2.50, 1.00, "Personal Income Tax Act (PITA) & Finance Act", "NIBSS EFT / Interswitch / Paystack Direct"),
  SOUTH_AFRICA("ZAF", "South Africa", GlobalContinent.AFRICA, "🇿🇦", "ZAR", "R", 18.10, 15.0, "Provident / Pension Fund & UIF", 1.00, 1.00, "Skills Development Levy (SDL)", 0.00, 1.00, "Basic Conditions of Employment Act & SARS", "ACB / BankservAfrica / SARS EMP201"),
  KENYA("KEN", "Kenya", GlobalContinent.AFRICA, "🇰🇪", "KES", "KSh", 129.00, 16.0, "NSSF Pension (Tier I & Tier II)", 6.00, 6.00, "SHIF Social Health & Housing Levy", 4.25, 1.50, "Employment Act 2007 & Finance Act 2024", "KRA iTax / M-Pesa B2C / EFT"),
  GHANA("GHA", "Ghana", GlobalContinent.AFRICA, "🇬🇭", "GHS", "GH₵", 15.20, 15.0, "SSNIT Pension (Tier 1 & Tier 2)", 5.50, 13.00, "NHIL & GETFund Statutory Levy", 2.50, 2.50, "Labour Act 2003 (Act 651) & GRA PITA", "GhIPSS ACH / Ghana Interbank Direct Credit");

  companion object {
    fun findByCode(code: String): GlobalJurisdiction {
      return values().find { it.countryCode.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) } ?: USA
    }
  }
}

data class CurrencyInfo(
  val code: String,
  val symbol: String,
  val name: String,
  val flagEmoji: String = "🌐",
  val rateToUsd: Double = 1.00,
  val continent: String = "Global"
) {
  companion object {
    val SUPPORTED: List<CurrencyInfo> = listOf(
      CurrencyInfo("USD", "$", "United States Dollar", "🇺🇸", 1.00, "North America"),
      CurrencyInfo("EUR", "€", "Eurozone Euro", "🇪🇺", 0.92, "Europe"),
      CurrencyInfo("GBP", "£", "British Pound Sterling", "🇬🇧", 0.79, "Europe"),
      CurrencyInfo("CAD", "C$", "Canadian Dollar", "🇨🇦", 1.36, "North America"),
      CurrencyInfo("AUD", "A$", "Australian Dollar", "🇦🇺", 1.52, "Asia & Pacific"),
      CurrencyInfo("JPY", "¥", "Japanese Yen", "🇯🇵", 154.50, "Asia & Pacific"),
      CurrencyInfo("INR", "₹", "Indian Rupee", "🇮🇳", 83.40, "Asia & Pacific"),
      CurrencyInfo("BRL", "R$", "Brazilian Real", "🇧🇷", 5.45, "South America"),
      CurrencyInfo("MXN", "Mex$", "Mexican Peso", "🇲🇽", 18.20, "North America"),
      CurrencyInfo("SAR", "﷼", "Saudi Riyal", "🇸🇦", 3.75, "MENA"),
      CurrencyInfo("AED", "د.إ", "UAE Dirham", "🇦🇪", 3.67, "MENA"),
      CurrencyInfo("NGN", "₦", "Nigerian Naira", "🇳🇬", 1580.00, "Africa"),
      CurrencyInfo("ZAR", "R", "South African Rand", "🇿🇦", 18.10, "Africa"),
      CurrencyInfo("KES", "KSh", "Kenyan Shilling", "🇰🇪", 129.00, "Africa"),
      CurrencyInfo("SGD", "S$", "Singapore Dollar", "🇸🇬", 1.34, "Asia & Pacific"),
      CurrencyInfo("NZD", "NZ$", "New Zealand Dollar", "🇳🇿", 1.65, "Asia & Pacific"),
      CurrencyInfo("CHF", "CHF ", "Swiss Franc", "🇨🇭", 0.90, "Europe"),
      CurrencyInfo("QAR", "ر.ق", "Qatari Riyal", "🇶🇦", 3.64, "MENA"),
      CurrencyInfo("KWD", "د.ك", "Kuwaiti Dinar", "🇰🇼", 0.31, "MENA"),
      CurrencyInfo("BHD", "BD", "Bahraini Dinar", "🇧🇭", 0.38, "MENA"),
      CurrencyInfo("EGP", "ج.م", "Egyptian Pound", "🇪🇬", 48.50, "MENA"),
      CurrencyInfo("GHS", "GH₵", "Ghanaian Cedi", "🇬🇭", 15.20, "Africa"),
      CurrencyInfo("PHP", "₱", "Philippine Peso", "🇵🇭", 57.50, "Asia & Pacific"),
      CurrencyInfo("ARS", "$", "Argentine Peso", "🇦🇷", 940.00, "South America"),
      CurrencyInfo("COP", "COL$", "Colombian Peso", "🇨🇴", 4050.00, "South America"),
      CurrencyInfo("CLP", "CLP$", "Chilean Peso", "🇨🇱", 930.00, "South America")
    )
  }
}

@Entity(tableName = "tax_settings")
data class TaxSettingEntity(
  @PrimaryKey val id: Int = 1,
  val activeJurisdictionCode: String = "USA",
  val activeContinent: String = "North America",
  val defaultCurrencyCode: String = "USD",
  val vatGstVasRatePercent: Double = 0.0,
  val employerPensionMatchPercent: Double = 6.20,
  val statutoryHealthPercent: Double = 1.45,
  val standardOvertimeMultiplier: Double = 1.5,
  val localClearingFormat: String = "ACH NACHA / US Fedwire",
  val enforceWpsPayroll: Boolean = false,
  val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "company_profile")
data class CompanyProfileEntity(
  @PrimaryKey val id: Int = 1,
  val companyName: String = "Acme Global Enterprise Inc.",
  val taxIdEin: String = "12-3456789",
  val stateOfRegistration: String = "California (CA)",
  val jurisdictionCountry: String = "United States",
  val selectedTierName: String = "PRO",
  val isYearlyBilling: Boolean = true,
  val quickBooksConnected: Boolean = true,
  val xeroConnected: Boolean = true,
  val isOnboardingCompleted: Boolean = true,
  val liveApiKey: String = "pk_live_pay_8f2930a918237b6c",
  val testApiKey: String = "pk_test_pay_sandbox_910283b749"
)

@Entity(tableName = "paystack_transactions")
data class PaystackTransactionEntity(
  @PrimaryKey val reference: String,
  val tierTitle: String,
  val billingCycle: String,
  val amount: Double,
  val currency: String,
  val channelName: String,
  val customerEmail: String,
  val status: String,
  val paidAt: String,
  val authorizationCode: String,
  val bankOrIssuer: String,
  val feesDeducted: Double,
  val paystackSettlementStatus: String,
  val regionalMetadata: String
)

@Entity(tableName = "employees")
data class EmployeeEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val firstName: String,
  val lastName: String,
  val email: String,
  val role: String,
  val department: String,
  val payType: PayType = PayType.SALARY,
  val baseRate: Double, // annual salary or hourly rate
  val payFrequency: PayFrequency = PayFrequency.BI_WEEKLY,
  val filingStatus: FilingStatus = FilingStatus.SINGLE,
  val w4Allowances: Int = 0,
  val preTax401kPercent: Double = 5.0, // e.g. 5%
  val preTaxHealthInsurance: Double = 120.0, // per pay period
  val postTaxDeductions: Double = 0.0,
  val stateCode: String = "CA", // CA, NY, TX, FL, WA, etc.
  val currencyCode: String = "USD",
  val jurisdictionCode: String = "USA",
  val statutoryIdNumber: String = "EMP-94021",
  val bankAccountLast4: String = "4821",
  val isActive: Boolean = true
) {
  val fullName: String get() = "$firstName $lastName"
}

@Entity(tableName = "payroll_runs")
data class PayrollRunEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val runDateTimestamp: Long = System.currentTimeMillis(),
  val periodStart: String,
  val periodEnd: String,
  val status: String = "Completed", // Completed, Draft, Processing
  val employeeCount: Int,
  val totalGross: Double,
  val totalNet: Double,
  val totalEmployeeTaxes: Double,
  val totalEmployerTaxes: Double,
  val totalPreTaxDeductions: Double,
  val currencyCode: String = "USD",
  val quickbooksSyncStatus: String = "Synced", // Synced, Pending, Not Linked
  val xeroSyncStatus: String = "Synced"
)

@Entity(tableName = "payroll_items")
data class PayrollItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val runId: Long,
  val employeeId: Long,
  val employeeName: String,
  val department: String,
  val regularHours: Double = 80.0,
  val overtimeHours: Double = 0.0,
  val grossPay: Double,
  val federalTax: Double,
  val stateTax: Double,
  val socialSecurityTax: Double,
  val medicareTax: Double,
  val preTaxDeductions: Double,
  val postTaxDeductions: Double,
  val netPay: Double,
  val employerFicaMatch: Double,
  val employerFuta: Double,
  val employerSuta: Double,
  val totalEmployerCost: Double
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val merchant: String,
  val category: String, // Software, Payroll Processing, Office, Travel, Contractor
  val amount: Double,
  val currencyCode: String = "USD",
  val dateString: String,
  val isMlAutoCategorized: Boolean = true,
  val mlConfidenceScore: Double = 0.96,
  val reconciliationStatus: String = "Reconciled" // Reconciled, Pending Review, Anomaly
)

data class CalculationResult(
  val grossPay: Double,
  val federalWithholding: Double,
  val stateWithholding: Double,
  val socialSecurityWithholding: Double, // 6.2% or statutory pension
  val medicareWithholding: Double,       // 1.45% or statutory health
  val additionalMedicare: Double = 0.0,  // 0.9% for high earners
  val statutoryPensionEmployee: Double = socialSecurityWithholding,
  val statutoryHealthEmployee: Double = medicareWithholding,
  val totalEmployeeTaxes: Double,
  val preTaxDeductions: Double,
  val postTaxDeductions: Double,
  val netTakeHomePay: Double,
  val employerSocialSecurity: Double,    // 6.2% or employer pension match
  val employerMedicare: Double,          // 1.45% or employer health match
  val employerFuta: Double = 0.0,        // 0.6% on first 7k
  val employerSuta: Double = 0.0,        // ~2.7%
  val employerPensionMatch: Double = employerSocialSecurity,
  val employerHealthMatch: Double = employerMedicare,
  val vatGstVasAmount: Double = 0.0,
  val thirteenthMonthAccrual: Double = 0.0,
  val endOfServiceGratuityAccrual: Double = 0.0,
  val totalEmployerTaxes: Double,
  val totalEmployerCost: Double,
  val effectiveTaxRatePercent: Double,
  val statutoryPensionLabel: String = "Pension / Social Insurance",
  val statutoryHealthLabel: String = "Health / Statutory Levy",
  val jurisdictionCountryName: String = "United States",
  val currencySymbol: String = "$"
)

data class ForecastPoint(
  val periodLabel: String,
  val projectedGrossPayroll: Double,
  val projectedEmployerTaxes: Double,
  val projectedOperatingExpenses: Double,
  val totalCashOutflow: Double,
  val confidenceIntervalHigh: Double,
  val confidenceIntervalLow: Double
)

data class AnomalyAlert(
  val id: String,
  val severity: AnomalySeverity,
  val title: String,
  val detail: String,
  val detectedAt: String,
  val category: String,
  val isResolved: Boolean = false,
  val resolutionAction: String? = null
)

enum class AnomalySeverity {
  CRITICAL,
  WARNING,
  INFO
}

data class JournalEntryLine(
  val accountCode: String,
  val accountName: String,
  val debit: Double,
  val credit: Double
)

enum class ReportType(val title: String, val description: String) {
  PAYROLL_SUMMARY("Payroll Liability Summary", "Executive summary of gross pay, net pay, taxes, and cost center allocations."),
  TAX_WITHHOLDING_941("Federal & State Tax Schedule", "Detailed quarterly IRS Form 941, FICA match, FUTA and multi-state SUTA liabilities."),
  DEPARTMENT_LABOR_BURDEN("Department Labor Burden", "Direct labor cost, employer taxes, benefit overhead, and overtime impact per team."),
  EMPLOYEE_EARNINGS_RECORD("Employee Earnings Register", "Individual year-to-date earnings, pre-tax deductions, and net distributions."),
  EXPENSE_AUDIT_GL("GL Reconciled Expenses", "Machine learning categorized expenses synced with QuickBooks and Xero chart of accounts.")
}

enum class ReportFormat(val extension: String, val displayName: String, val mimeType: String) {
  PDF("PDF", "Executive PDF Report (.pdf)", "application/pdf"),
  CSV("CSV", "Standard Spreadsheets (.csv)", "text/csv"),
  JSON("JSON", "Developer REST API Dump (.json)", "application/json"),
  QUICKBOOKS_IIF("IIF", "QuickBooks Desktop (.iif)", "application/qbooks"),
  XERO_CSV("CSV", "Xero Journal Import (.csv)", "text/csv")
}

data class CustomReportFilter(
  val reportType: ReportType = ReportType.PAYROLL_SUMMARY,
  val dateRange: String = "Q3 2026 (Jul 1 - Sep 30)",
  val selectedDepartment: String = "All Departments",
  val selectedPayType: String = "All Types",
  val includeEmployerTaxes: Boolean = true,
  val includeBenefitsBreakdown: Boolean = true,
  val includeAnomaliesAudit: Boolean = true
)

data class GeneratedReportData(
  val title: String,
  val generatedDate: String,
  val filter: CustomReportFilter,
  val totalGross: Double,
  val totalNet: Double,
  val totalEmployeeTaxes: Double,
  val totalEmployerTaxes: Double,
  val totalBenefits: Double,
  val totalEmployerCost: Double,
  val employeeCount: Int,
  val departmentBreakdowns: Map<String, Double>,
  val format: ReportFormat
)

data class BankTransaction(
  val id: String,
  val bankName: String, // e.g. "Silicon Valley Bank Checking (*8821)"
  val date: String,
  val description: String,
  val amount: Double,
  val originalCategory: String,
  val suggestedCategory: String,
  val mlConfidence: Double,
  val isLearnedUserRule: Boolean = false,
  val status: String = "Pending Review" // "Auto-Categorized", "User Confirmed", "Pending Review"
)

data class MlLearnedRule(
  val id: String,
  val keywordOrMerchant: String,
  val mappedCategory: String,
  val glAccountCode: String,
  val timesApplied: Int = 1,
  val createdAt: String = "Aug 2026",
  val userPreferenceNote: String = "Auto-override enabled"
)

data class MlModelStats(
  val accuracyScore: Double = 99.4,
  val totalTransactionsProcessed: Int = 248,
  val userTrainedRulesCount: Int = 18,
  val autoCategorizationRate: Double = 96.8,
  val timeSavedHoursMonthly: Double = 14.5
)

data class VideoTutorialItem(
  val id: String,
  val title: String,
  val duration: String,
  val description: String,
  val category: String,
  val keyTakeaways: List<String>,
  val thumbnailColor: Long = 0xFF005FB0
)

data class CompanyProfile(
  val companyName: String = "Acme Global Technologies Inc.",
  val taxIdEin: String = "XX-XXXX8920",
  val stateOfRegistration: String = "California",
  val selectedTier: SubscriptionTier = SubscriptionTier.PRO,
  val currentUserRole: UserRole = UserRole.OWNER,
  val liveApiKey: String = "pk_live_pay_94f8a291bc7702ee",
  val testApiKey: String = "pk_test_pay_sandbox_11893aa",
  val isBiometricSecured: Boolean = true,
  val quickBooksConnected: Boolean = true,
  val xeroConnected: Boolean = true,
  val isAdBannerDismissed: Boolean = false,
  val isOnboardingCompleted: Boolean = false
)

enum class AuthState {
  AUTHENTICATED,
  LOGIN,
  REGISTER,
  VERIFY_EMAIL,
  MFA_CHALLENGE,
  BIOMETRIC_SCANNER_PROMPT,
  APP_LOCKED
}

enum class MfaType(val displayName: String, val provider: String, val iconName: String) {
  AUTHENTICATOR_APP("Google / MS Authenticator", "TOTP RFC 6238 (Time-based One Time Password)", "Smartphone"),
  PASSKEY_FIDO2("FIDO2 / WebAuthn Hardware Passkey", "Hardware Security Key (YubiKey, Titan, Touch ID)", "Key"),
  BIOMETRIC_SENSORS("Biometric Sensor Scanner", "Android BiometricPrompt Face & Fingerprint Level 3", "Fingerprint"),
  SMS_BACKUP("SMS Secure Code", "Encrypted SMS Carrier Fallback (+1 ***-***-8821)", "Message"),
  EMAIL_OTP("Email Verification Code", "Zero-Knowledge One-Time Code", "Mail")
}

data class AuthUserProfile(
  val id: String = "usr_enterprise_8829",
  val fullName: String = "Jane Doe",
  val email: String = "jane.doe@acme-global.com",
  val role: UserRole = UserRole.OWNER,
  val isEmailVerified: Boolean = true,
  val is2faEnabled: Boolean = true,
  val activeMfaType: MfaType = MfaType.BIOMETRIC_SENSORS,
  val isFingerprintEnabled: Boolean = true,
  val isFaceUnlockEnabled: Boolean = true,
  val isPasskeyEnrolled: Boolean = true,
  val registeredPasskeysCount: Int = 3,
  val lastLoginTime: String = "Today, 10:42 AM",
  val lastLoginIp: String = "192.175.44.12",
  val lastLoginLocation: String = "San Francisco, CA (USA)",
  val securityHealthScore: Int = 98 // out of 100
)

data class RegisteredPasskey(
  val id: String,
  val name: String,
  val aaguid: String,
  val enrolledDate: String,
  val lastUsed: String,
  val transportType: String // "Internal Platform (Touch ID/Face)", "USB/NFC Security Key", "Bluetooth Low Energy"
)

data class ActiveDeviceSession(
  val id: String,
  val deviceName: String,
  val platform: String,
  val ipAddress: String,
  val location: String,
  val lastActive: String,
  val isCurrent: Boolean,
  val securityGrade: String = "FIPS 140-3 Hardware Enclave"
)

enum class AuditSeverity {
  LOW,
  MEDIUM,
  HIGH,
  CRITICAL
}

data class EnterpriseSecurityAuditLog(
  val id: String,
  val action: String,
  val actor: String,
  val timestamp: String,
  val ipAddress: String,
  val device: String,
  val status: String, // "Verified", "Blocked Anomaly", "Granted", "Flagged"
  val severity: AuditSeverity
)

data class EnterpriseSecurityPolicies(
  val zeroTrustNetworkAccess: Boolean = true,
  val hardwareStrongBoxEnclave: Boolean = true, // Android KeyStore StrongBox
  val requireBiometricsForPayrollRun: Boolean = true,
  val preventScreenCapture: Boolean = true,
  val tlsCertificatePinning: Boolean = true,
  val enforceSessionTimeoutMinutes: Int = 15,
  val requireComplexPasswords: Boolean = true,
  val minPasswordLength: Int = 12,
  val soc2ComplianceReporting: Boolean = true,
  val iso27001FrameworkEnforced: Boolean = true
)

enum class ThemeMode(val title: String, val subtitle: String) {
  SYSTEM("Device Theme", "Match Android system theme (auto day/night)"),
  LIGHT("Light Theme", "Crisp high-contrast daylight financial canvas"),
  DARK("Dark Theme", "OLED optimized enterprise midnight canvas"),
  CUSTOM("Custom Theme", "Select tailored enterprise color palettes & accents")
}

enum class EnterpriseColorTheme(
  val displayName: String,
  val description: String,
  val primaryHex: Long,
  val primaryContainerHex: Long,
  val onPrimaryContainerHex: Long,
  val secondaryHex: Long,
  val backgroundDarkHex: Long,
  val surfaceDarkHex: Long
) {
  SAPPHIRE_BLUE(
    "Sapphire Enterprise",
    "Standard Wall Street corporate executive blue",
    0xFF005FB0,
    0xFFD3E3FD,
    0xFF001D35,
    0xFF004A77,
    0xFF001426,
    0xFF001D35
  ),
  EMERALD_FINANCIAL(
    "Emerald Wealth",
    "Asset management & venture capital deep green",
    0xFF006C4C,
    0xFF8BF7C6,
    0xFF002114,
    0xFF005138,
    0xFF00170C,
    0xFF002114
  ),
  AMETHYST_TECH(
    "Amethyst Sovereign",
    "High-tech enterprise innovation violet",
    0xFF6B4FA8,
    0xFFEBDDFF,
    0xFF23005C,
    0xFF533B88,
    0xFF150A24,
    0xFF211138
  ),
  MIDNIGHT_ONYX(
    "Cyber Onyx Gold",
    "Ultra-luxury dark fintech with gold accents",
    0xFFB58B00,
    0xFFFFE082,
    0xFF241A00,
    0xFF785900,
    0xFF0D0F12,
    0xFF161920
  ),
  CRIMSON_EXECUTIVE(
    "Crimson Dynasty",
    "Executive board & global equity burgundy",
    0xFF9C1D38,
    0xFFFFD9DD,
    0xFF3E0010,
    0xFF7D0824,
    0xFF200007,
    0xFF32000D
  ),
  SUNSET_COPPER(
    "Copper Apex",
    "Warm high-growth startup energetic amber",
    0xFFB85D19,
    0xFFFFDBC8,
    0xFF381A00,
    0xFF8F4308,
    0xFF1F0C00,
    0xFF2E1500
  ),
  TEAL_GLACIER(
    "Nordic Glacier",
    "Clean Scandinavian cyan and cool slate",
    0xFF006877,
    0xFFA1EFFF,
    0xFF001F25,
    0xFF004F5B,
    0xFF001519,
    0xFF002127
  )
}

data class EnterpriseGlobalConfiguration(
  val fiscalYearStartMonth: String = "January",
  val defaultPayrollCycle: String = "Bi-Weekly (Every other Friday)",
  val standardOvertimeMultiplier: Double = 1.5,
  val standard401kMatchLimitPercent: Double = 4.0,
  val primaryAccountingNexusState: String = "California (CA)",
  val autoBackupFrequency: String = "Continuous Real-Time Cloud Stream",
  val auditLogRetentionYears: Int = 7,
  val highInformationDensity: Boolean = false,
  val highContrastMode: Boolean = false,
  val dynamicMonetColors: Boolean = false,
  val activeThemeMode: ThemeMode = ThemeMode.SYSTEM,
  val activeColorTheme: EnterpriseColorTheme = EnterpriseColorTheme.SAPPHIRE_BLUE
)

enum class PaymentChannelCategory(
  val title: String,
  val iconName: String,
  val subtitle: String
) {
  CARDS_WALLETS("Cards & Wallets", "credit_card", "Cards, Google Pay, Apple Pay & Stripe"),
  US_GLOBAL("US & Global", "account_balance_wallet", "PayPal, Venmo & Zelle Direct"),
  MENA_REGIONAL("MENA & Arab Gulf", "public", "Mada, Fawry, BenefitPay, KNET, STC Pay & Tamara"),
  GIFT_VOUCHERS("Gift Cards", "card_giftcard", "Enterprise Vouchers & Prepaid Cards"),
  DIRECT_RAILS("Direct Rails", "account_balance", "Virtual Accounts, USSD & Mobile Money")
}

enum class PaystackPaymentChannel(
  val title: String,
  val iconName: String,
  val description: String,
  val category: PaymentChannelCategory,
  val badge: String = ""
) {
  // Cards & Wallets
  CARD("Debit / Credit Card", "card", "Visa, Mastercard, Amex, Discover, Verve", PaymentChannelCategory.CARDS_WALLETS, "Instant"),
  GOOGLE_PAY("Google Pay", "gpay", "1-Tap Instant Biometric GPay Wallet", PaymentChannelCategory.CARDS_WALLETS, "Fastest"),
  APPLE_PAY("Apple Pay", "apple", "Encrypted Face ID / Touch ID Wallet", PaymentChannelCategory.CARDS_WALLETS, "Fast"),
  STRIPE("Stripe & Link", "stripe", "Stripe 1-Click Vaulted Checkout", PaymentChannelCategory.CARDS_WALLETS, "Secure"),

  // US & Global Wallets
  PAYPAL("PayPal Express", "paypal", "Instant Balance & Linked Bank Settlement", PaymentChannelCategory.US_GLOBAL, "Global"),
  VENMO("Venmo Pay", "venmo", "@username & Mobile Instant Transfer", PaymentChannelCategory.US_GLOBAL, "US Popular"),
  ZELLE("Zelle Direct", "zelle", "Direct Zero-Fee Interbank Transfer", PaymentChannelCategory.US_GLOBAL, "No Fee"),

  // MENA Regional Rails
  MADA("Mada (Saudi Arabia)", "mada", "Saudi National Debit Card Switch", PaymentChannelCategory.MENA_REGIONAL, "KSA #1"),
  FAWRY("Fawry Pay (Egypt)", "fawry", "160,000+ POS Kiosks & Fawry Wallet", PaymentChannelCategory.MENA_REGIONAL, "Egypt #1"),
  BENEFIT_PAY("BenefitPay (Bahrain)", "benefit", "Bahrain National QR & EFTS Transfer", PaymentChannelCategory.MENA_REGIONAL, "Bahrain"),
  KNET("KNET (Kuwait)", "knet", "Kuwait National Interbank Gateway", PaymentChannelCategory.MENA_REGIONAL, "Kuwait"),
  QPAY_NAPS("QPay / NAPS (Qatar)", "qpay", "Qatar National Debit Switch", PaymentChannelCategory.MENA_REGIONAL, "Qatar"),
  STC_PAY("STC Pay / SADAD", "stc", "Saudi Digital Wallet & SADAD Bill", PaymentChannelCategory.MENA_REGIONAL, "MENA"),
  TABBY_TAMARA("Tamara & Tabby (BNPL)", "tabby", "Split in 4 Interest-Free Monthly Payments", PaymentChannelCategory.MENA_REGIONAL, "Buy Now Pay Later"),

  // Gift Cards & Prepaid
  GIFT_CARD("Gift Card & Voucher", "gift", "Enterprise Code, Amazon or Visa Prepaid", PaymentChannelCategory.GIFT_VOUCHERS, "Prepaid"),

  // Direct Paystack African & Global Rails
  BANK_TRANSFER("Dedicated Bank Transfer", "transfer", "Dynamic Virtual Account Direct Routing", PaymentChannelCategory.DIRECT_RAILS, "Auto-Settle"),
  USSD("USSD Mobile Banking", "phone", "Direct dial shortcode (*737#, *919#)", PaymentChannelCategory.DIRECT_RAILS, "Offline Ready"),
  MOBILE_MONEY("Mobile Money / MoMo", "momo", "M-Pesa, MTN MoMo, Airtel Money Push", PaymentChannelCategory.DIRECT_RAILS, "Africa"),
  QR_CODE("Scan to Pay (EMVCo QR)", "qr", "Scan with banking app or Visa QR", PaymentChannelCategory.DIRECT_RAILS, "Camera")
}

data class PaystackConfig(
  val publicKey: String = "pk_live_payflow_9a823f401b28e90c7",
  val testPublicKey: String = "pk_test_payflow_demo_3840281928a",
  val businessName: String = "PayFlow Enterprise Technologies Inc.",
  val settlementCurrency: String = "USD", // USD, NGN, GHS, ZAR, KES, AED, SAR
  val isLiveMode: Boolean = true,
  val webhookEndpoint: String = "https://api.payflow.ai/v1/webhooks/paystack",
  val subaccountCode: String = "ACCT_pf981023910",
  val autoRenewSubscription: Boolean = true,
  val splitSettlementPercentage: Double = 100.0,
  val routingMode: String = "Unified Global-to-Paystack Multi-Rail Bridge",
  val totalSettledRevenueUsd: Double = 34850.00
)

data class PaystackTransaction(
  val reference: String,
  val tierTitle: String,
  val billingCycle: String, // "Monthly", "Yearly"
  val amount: Double,
  val currency: String,
  val channel: PaystackPaymentChannel,
  val customerEmail: String,
  val status: String, // "Success", "Processing", "Requires OTP", "Failed"
  val paidAt: String,
  val authorizationCode: String = "AUTH_pstk_${System.currentTimeMillis() % 100000}",
  val bankOrIssuer: String = "Access Bank / Visa Secure",
  val feesDeducted: Double = 0.0,
  val paystackSettlementStatus: String = "Settled to Paystack Balance (Instant)",
  val regionalMetadata: String = "Cross-border Swift / Direct Clearing"
)

enum class MessageSender {
  USER,
  AI_COPILOT,
  SYSTEM
}

data class MarketAnalysisSnippet(
  val country: String,
  val continent: String,
  val currencyCode: String,
  val medianSalaryUsd: Double,
  val statutoryEmployerBurdenPercent: Double,
  val vatVasRatePercent: Double,
  val fxVolatilityRisk: String, // "Low", "Moderate", "High"
  val keyStatutoryRule: String,
  val nationalClearingRail: String
)

data class ChatMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val sender: MessageSender,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val suggestedPrompts: List<String> = emptyList(),
  val marketSnippet: MarketAnalysisSnippet? = null,
  val isStreaming: Boolean = false
)

data class LaborMarketIndexItem(
  val country: String,
  val flagEmoji: String,
  val continent: String,
  val currencyCode: String,
  val currencySymbol: String,
  val medianDevSalaryAnnualUsd: Double,
  val employerOverheadPercent: Double,
  val personalIncomeTaxRange: String,
  val pensionMandate: String,
  val healthCareMandate: String,
  val severanceNoticeStandard: String,
  val localClearingSystem: String,
  val vatGstVasPercent: Double,
  val fxStabilityIndex: String,
  val laborLawHighlights: String
) {
  companion object {
    val GLOBAL_MARKETS = listOf(
      LaborMarketIndexItem(
        country = "United States",
        flagEmoji = "🇺🇸",
        continent = "North America",
        currencyCode = "USD",
        currencySymbol = "$",
        medianDevSalaryAnnualUsd = 145000.0,
        employerOverheadPercent = 9.65,
        personalIncomeTaxRange = "10% - 37% (Federal) + 0%-13.3% (State)",
        pensionMandate = "FICA (6.2%) + 401(k) voluntary match",
        healthCareMandate = "ACA employer mandate for 50+ FTE",
        severanceNoticeStandard = "At-will employment; WARN Act 60-day notice",
        localClearingSystem = "ACH NACHA / Fedwire / RTP",
        vatGstVasPercent = 0.0,
        fxStabilityIndex = "Global Base Anchor (USD)",
        laborLawHighlights = "FLSA overtime 1.5x after 40 hrs/wk; W-4 progressive withholding; 50 state tax rules."
      ),
      LaborMarketIndexItem(
        country = "United Kingdom",
        flagEmoji = "🇬🇧",
        continent = "Europe",
        currencyCode = "GBP",
        currencySymbol = "£",
        medianDevSalaryAnnualUsd = 92000.0,
        employerOverheadPercent = 15.05,
        personalIncomeTaxRange = "20% (Basic), 40% (Higher), 45% (Additional)",
        pensionMandate = "Workplace Auto-Enrolment (min 3% employer)",
        healthCareMandate = "National Health Service via Class 1 NICs (13.8% employer)",
        severanceNoticeStandard = "Statutory redundancy pay + 1 to 12 weeks notice",
        localClearingSystem = "BACS / Faster Payments / CHAPS",
        vatGstVasPercent = 20.0,
        fxStabilityIndex = "High (G7 Reserve)",
        laborLawHighlights = "HMRC Real-Time Information (RTI) PAYE filing; Student Loan Plan 1/2/4 deduction."
      ),
      LaborMarketIndexItem(
        country = "Germany",
        flagEmoji = "🇩🇪",
        continent = "Europe",
        currencyCode = "EUR",
        currencySymbol = "€",
        medianDevSalaryAnnualUsd = 88000.0,
        employerOverheadPercent = 20.75,
        personalIncomeTaxRange = "14% - 45% (Lohnsteuer + Solidaritätszuschlag)",
        pensionMandate = "Gesetzliche Rentenversicherung (9.3% employer match)",
        healthCareMandate = "Gesetzliche Krankenversicherung (7.3% + care insurance)",
        severanceNoticeStandard = "Kündigungsschutzgesetz statutory dismissal notice",
        localClearingSystem = "SEPA Direct Debit & Credit Transfer (ISO 20022)",
        vatGstVasPercent = 19.0,
        fxStabilityIndex = "High (ECB Eurozone Anchor)",
        laborLawHighlights = "German tax classes I-VI; Social security parity split 50/50 employer/employee."
      ),
      LaborMarketIndexItem(
        country = "Saudi Arabia",
        flagEmoji = "🇸🇦",
        continent = "MENA",
        currencyCode = "SAR",
        currencySymbol = "ر.س",
        medianDevSalaryAnnualUsd = 72000.0,
        employerOverheadPercent = 11.75,
        personalIncomeTaxRange = "0.0% Personal Income Tax",
        pensionMandate = "GOSI (General Org for Social Insurance) 9% pension match",
        healthCareMandate = "Private Health Insurance compulsory for all staff",
        severanceNoticeStandard = "Saudi Labor Law End of Service Award (EOSB)",
        localClearingSystem = "SARIE / SAMA WPS (Wage Protection System)",
        vatGstVasPercent = 15.0,
        fxStabilityIndex = "Fixed Peg (3.75 SAR/USD)",
        laborLawHighlights = "Mandatory Ministry of Human Resources WPS electronic payroll file validation."
      ),
      LaborMarketIndexItem(
        country = "United Arab Emirates",
        flagEmoji = "🇦🇪",
        continent = "MENA",
        currencyCode = "AED",
        currencySymbol = "د.إ",
        medianDevSalaryAnnualUsd = 76000.0,
        employerOverheadPercent = 12.50,
        personalIncomeTaxRange = "0.0% Personal Income Tax",
        pensionMandate = "GPSSA (UAE nationals 12.5% employer); Expat gratuity",
        healthCareMandate = "DHA / DoH mandatory employer health coverage",
        severanceNoticeStandard = "MoHRE End of Service Gratuity (21 days/yr up to 5 yrs)",
        localClearingSystem = "Central Bank WPS SIF (Salary Information File)",
        vatGstVasPercent = 5.0,
        fxStabilityIndex = "Fixed Peg (3.6725 AED/USD)",
        laborLawHighlights = "Federal Decree-Law No. 33 of 2021; MoHRE compliant SIF bank disbursement."
      ),
      LaborMarketIndexItem(
        country = "Australia",
        flagEmoji = "🇦🇺",
        continent = "Pacific",
        currencyCode = "AUD",
        currencySymbol = "A$",
        medianDevSalaryAnnualUsd = 98000.0,
        employerOverheadPercent = 12.00,
        personalIncomeTaxRange = "16% - 45% + 2% Medicare Levy",
        pensionMandate = "Superannuation Guarantee (11.5% statutory employer)",
        healthCareMandate = "Medicare Levy (2.0%) + Surcharge for high earners",
        severanceNoticeStandard = "National Employment Standards (NES) 1-5 wks notice",
        localClearingSystem = "NPP (New Payments Platform) / Direct Entry / PayTo",
        vatGstVasPercent = 10.0,
        fxStabilityIndex = "High (AAA Sovereign)",
        laborLawHighlights = "ATO Single Touch Payroll (STP Phase 2) pay-event reporting on each disbursement."
      ),
      LaborMarketIndexItem(
        country = "Singapore",
        flagEmoji = "🇸🇬",
        continent = "Asia",
        currencyCode = "SGD",
        currencySymbol = "S$",
        medianDevSalaryAnnualUsd = 85000.0,
        employerOverheadPercent = 17.00,
        personalIncomeTaxRange = "0% - 24% (Progressive IRAS brackets)",
        pensionMandate = "Central Provident Fund (CPF) 17% employer match",
        healthCareMandate = "MediSave component of CPF",
        severanceNoticeStandard = "Employment Act 1 day to 4 weeks based on service",
        localClearingSystem = "FAST / PayNow / GIRO Interbank",
        vatGstVasPercent = 9.0,
        fxStabilityIndex = "Very High (MAS Managed Float)",
        laborLawHighlights = "IRAS Form IR8A annual tax return; CDAC, MBMF, SINDA self-help community funds."
      ),
      LaborMarketIndexItem(
        country = "Japan",
        flagEmoji = "🇯🇵",
        continent = "Asia",
        currencyCode = "JPY",
        currencySymbol = "¥",
        medianDevSalaryAnnualUsd = 62000.0,
        employerOverheadPercent = 16.20,
        personalIncomeTaxRange = "5% - 45% + 10% Local Inhabitant Resident Tax",
        pensionMandate = "Kōsei Nenkin (Employee Pension) 9.15% employer match",
        healthCareMandate = "Kenko Hoken (Health Insurance) 4.9% employer match",
        severanceNoticeStandard = "Labor Standards Act 30 days notice or pay in lieu",
        localClearingSystem = "Zengin System (All Banks Data Telecommunication)",
        vatGstVasPercent = 10.0,
        fxStabilityIndex = "Moderate / High Liquidity",
        laborLawHighlights = "Year-end tax adjustment (Nenmatsu Chosei); Strict labor contract protection."
      ),
      LaborMarketIndexItem(
        country = "Brazil",
        flagEmoji = "🇧🇷",
        continent = "South America",
        currencyCode = "BRL",
        currencySymbol = "R$",
        medianDevSalaryAnnualUsd = 42000.0,
        employerOverheadPercent = 32.50,
        personalIncomeTaxRange = "7.5% - 27.5% (IRPF progressive table)",
        pensionMandate = "INSS (Social Security) up to 14% + FGTS (8% monthly deposit)",
        healthCareMandate = "SUS public universal + Private collective plan subsidy",
        severanceNoticeStandard = "Aviso Prévio (30-90 days) + 40% FGTS penalty on dismissal",
        localClearingSystem = "PIX Instant / TED / DOC / CNAB 240",
        vatGstVasPercent = 18.0,
        fxStabilityIndex = "Moderate (Emerging Market FX)",
        laborLawHighlights = "CLT Consolidação das Leis do Trabalho; 13th Salary (Décimo Terceiro) & 1/3 vacation bonus."
      ),
      LaborMarketIndexItem(
        country = "Nigeria",
        flagEmoji = "🇳🇬",
        continent = "Africa",
        currencyCode = "NGN",
        currencySymbol = "₦",
        medianDevSalaryAnnualUsd = 26000.0,
        employerOverheadPercent = 13.00,
        personalIncomeTaxRange = "7% - 24% (PAYE State Internal Revenue Service)",
        pensionMandate = "Pension Reform Act (10% employer + 8% employee match)",
        healthCareMandate = "NHIS (National Health Insurance Scheme)",
        severanceNoticeStandard = "Labor Act 1-4 weeks statutory notice",
        localClearingSystem = "NIBSS Instant Payments (NIP) / Paystack Direct Rail",
        vatGstVasPercent = 7.5,
        fxStabilityIndex = "Moderate / High Yield (CBN Floating)",
        laborLawHighlights = "Consolidated Relief Allowance (CRA); ITF (1%) & NSITF (1%) employer levies."
      ),
      LaborMarketIndexItem(
        country = "South Africa",
        flagEmoji = "🇿🇦",
        continent = "Africa",
        currencyCode = "ZAR",
        currencySymbol = "R",
        medianDevSalaryAnnualUsd = 38000.0,
        employerOverheadPercent = 8.50,
        personalIncomeTaxRange = "18% - 45% (SARS PAYE tax tables)",
        pensionMandate = "Provident / Pension fund (typically 5-10% employer)",
        healthCareMandate = "Medical Scheme Fees Tax Credit system",
        severanceNoticeStandard = "BCEA 1-4 weeks notice + 1 week severance per year",
        localClearingSystem = "BankservAfrica RTC / PayShap / EFT",
        vatGstVasPercent = 15.0,
        fxStabilityIndex = "Moderate (Commodity Linked)",
        laborLawHighlights = "SARS Monthly EMP201 (PAYE, UIF 1% employer, SDL 1% Skills Development Levy)."
      ),
      LaborMarketIndexItem(
        country = "Kenya",
        flagEmoji = "🇰🇪",
        continent = "Africa",
        currencyCode = "KES",
        currencySymbol = "KSh",
        medianDevSalaryAnnualUsd = 28000.0,
        employerOverheadPercent = 9.00,
        personalIncomeTaxRange = "10% - 35% (KRA PAYE graduated scale)",
        pensionMandate = "NSSF (Tier I & Tier II) 6% employer match",
        healthCareMandate = "SHIF (Social Health Insurance Fund 2.75%)",
        severanceNoticeStandard = "Employment Act 2007 28 days notice",
        localClearingSystem = "M-Pesa B2C / KEPSS Real-Time Gross Settlement / EFT",
        vatGstVasPercent = 16.0,
        fxStabilityIndex = "Moderate (CBK Managed)",
        laborLawHighlights = "Affordable Housing Levy (1.5% employer + 1.5% employee); KRA iTax integration."
      )
    )
  }
}




