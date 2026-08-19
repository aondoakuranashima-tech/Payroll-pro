package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PayrollDatabase
import com.example.data.model.*
import com.example.data.repository.PayrollRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CalculatorUiState(
  val payType: PayType = PayType.SALARY,
  val baseRateInput: String = "120000",
  val payFrequency: PayFrequency = PayFrequency.BI_WEEKLY,
  val filingStatus: FilingStatus = FilingStatus.SINGLE,
  val hoursWorkedInput: String = "80",
  val overtimeHoursInput: String = "0",
  val overtimeMultiplier: Double = 1.5,
  val preTax401kPercentInput: String = "5.0",
  val preTaxHealthInput: String = "120.00",
  val postTaxDeductionsInput: String = "0.00",
  val stateCode: String = "CA",
  val currencyCode: String = "USD",
  val allowances: Int = 0,
  val selectedJurisdiction: GlobalJurisdiction = GlobalJurisdiction.USA,
  val customVatPercentInput: String = "",
  val result: CalculationResult? = null
)

data class UiNotification(
  val message: String,
  val isError: Boolean = false
)

class PayrollViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: PayrollRepository

  val employees: StateFlow<List<EmployeeEntity>>
  val payrollRuns: StateFlow<List<PayrollRunEntity>>
  val expenses: StateFlow<List<ExpenseEntity>>
  val taxSettings: StateFlow<TaxSettingEntity?>
  val persistedCompanyProfile: StateFlow<CompanyProfileEntity?>
  val persistedPaystackTransactions: StateFlow<List<PaystackTransactionEntity>>
  val companyProfile: StateFlow<CompanyProfile>
  val bankTransactions: StateFlow<List<BankTransaction>>
  val mlLearnedRules: StateFlow<List<MlLearnedRule>>
  val resolvedAnomalyIds: StateFlow<Map<String, String>>

  private val _reportFilter = MutableStateFlow(CustomReportFilter())
  val reportFilter: StateFlow<CustomReportFilter> = _reportFilter.asStateFlow()

  // Scenario modeling state
  private val _forecastGrowthRate = MutableStateFlow(5.0)
  val forecastGrowthRate: StateFlow<Double> = _forecastGrowthRate.asStateFlow()

  private val _forecastWageInflation = MutableStateFlow(2.5)
  val forecastWageInflation: StateFlow<Double> = _forecastWageInflation.asStateFlow()

  private val _forecastHorizonMonths = MutableStateFlow(6)
  val forecastHorizonMonths: StateFlow<Int> = _forecastHorizonMonths.asStateFlow()

  private val _calculatorState = MutableStateFlow(CalculatorUiState())
  val calculatorState: StateFlow<CalculatorUiState> = _calculatorState.asStateFlow()

  private val _selectedPayrollRun = MutableStateFlow<PayrollRunEntity?>(null)
  val selectedPayrollRun: StateFlow<PayrollRunEntity?> = _selectedPayrollRun.asStateFlow()

  private val _selectedRunItems = MutableStateFlow<List<PayrollItemEntity>>(emptyList())
  val selectedRunItems: StateFlow<List<PayrollItemEntity>> = _selectedRunItems.asStateFlow()

  private val _notification = MutableStateFlow<UiNotification?>(null)
  val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

  private val _activeCurrency = MutableStateFlow("USD")
  val activeCurrency: StateFlow<String> = _activeCurrency.asStateFlow()

  init {
    val db = PayrollDatabase.getDatabase(application, viewModelScope)
    repository = PayrollRepository(db.payrollDao())

    employees = repository.employees.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    payrollRuns = repository.payrollRuns.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    expenses = repository.expenses.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    taxSettings = repository.taxSettings.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      null
    )

    persistedCompanyProfile = repository.companyProfileEntity.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      null
    )

    persistedPaystackTransactions = repository.persistedPaystackTransactions.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    companyProfile = repository.companyProfile
    bankTransactions = repository.bankTransactions
    mlLearnedRules = repository.mlLearnedRules
    resolvedAnomalyIds = repository.resolvedAnomalyIds

    // Listen to tax settings to keep default jurisdiction in sync
    viewModelScope.launch {
      repository.taxSettings.collectLatest { setting ->
        if (setting != null) {
          val matched = GlobalJurisdiction.entries.find { it.countryCode == setting.activeJurisdictionCode }
          if (matched != null && _calculatorState.value.selectedJurisdiction != matched) {
            _calculatorState.value = _calculatorState.value.copy(
              selectedJurisdiction = matched,
              currencyCode = matched.currencyCode
            )
            recalculate()
          }
        }
      }
    }

    // Initial calculation for calculator screen
    recalculate()
  }

  val authUser: StateFlow<AuthUserProfile> get() = repository.authUser
  val authState: StateFlow<AuthState> get() = repository.authState
  val registeredPasskeys: StateFlow<List<RegisteredPasskey>> get() = repository.registeredPasskeys
  val activeSessions: StateFlow<List<ActiveDeviceSession>> get() = repository.activeSessions
  val securityAuditLogs: StateFlow<List<EnterpriseSecurityAuditLog>> get() = repository.securityAuditLogs
  val securityPolicies: StateFlow<EnterpriseSecurityPolicies> get() = repository.securityPolicies

  fun setAuthState(state: AuthState) {
    repository.setAuthState(state)
  }

  fun login(email: String, pass: String): Boolean {
    val success = repository.login(email, pass)
    if (success) {
      showNotification("Identity verified with Enterprise Credentials.")
    } else {
      showNotification("Authentication failed. Please check your credentials.", isError = true)
    }
    return success
  }

  fun register(fullName: String, email: String, pass: String) {
    repository.register(fullName, email, pass)
    showNotification("Security verification code dispatched to $email")
  }

  fun verifyEmailCode(code: String): Boolean {
    val ok = repository.verifyEmailCode(code)
    if (ok) {
      showNotification("Email address successfully cryptographically verified!")
    } else {
      showNotification("Invalid 6-digit verification code.", isError = true)
    }
    return ok
  }

  fun verifyMfaChallenge(code: String): Boolean {
    val ok = repository.verifyMfaChallenge(code)
    if (ok) {
      showNotification("MFA 2FA Multi-Factor Authentication Approved.")
    } else {
      showNotification("Invalid TOTP verification code.", isError = true)
    }
    return ok
  }

  fun verifyBiometricSensorScan(success: Boolean, sensorType: String): Boolean {
    val ok = repository.verifyBiometricSensorScan(success, sensorType)
    if (ok) {
      showNotification("Biometric $sensorType Hardware Match Confirmed.")
    } else {
      showNotification("Biometric scan not recognized. Try again or use PIN.", isError = true)
    }
    return ok
  }

  fun verifyPasskeyFido2(passkeyId: String): Boolean {
    val ok = repository.verifyPasskeyFido2(passkeyId)
    showNotification("FIDO2 Hardware Passkey Cryptographic Signature Verified.")
    return ok
  }

  fun logout() {
    repository.logout()
    showNotification("Securely signed out of Enterprise Session.")
  }

  fun lockApp() {
    repository.lockApp()
    showNotification("Application secured with biometric lock.")
  }

  fun unlockAppWithBiometrics() {
    repository.unlockAppWithBiometrics()
    showNotification("Welcome back! Biometric identity confirmed.")
  }

  fun toggle2fa(enabled: Boolean, mfaType: MfaType = MfaType.AUTHENTICATOR_APP) {
    repository.toggle2fa(enabled, mfaType)
    showNotification(if (enabled) "MFA enabled with ${mfaType.displayName}" else "MFA disabled")
  }

  fun toggleBiometrics(enabled: Boolean) {
    repository.toggleBiometrics(enabled)
    showNotification(if (enabled) "Fingerprint Sensor Security enabled" else "Fingerprint Sensor Security disabled")
  }

  fun toggleFaceUnlock(enabled: Boolean) {
    repository.toggleFaceUnlock(enabled)
    showNotification(if (enabled) "Face Unlock Biometric Sensor enabled" else "Face Unlock Biometric Sensor disabled")
  }

  fun enrollNewPasskey(name: String, transport: String) {
    repository.enrollNewPasskey(name, transport)
    showNotification("New FIDO2 Hardware Passkey enrolled in StrongBox!")
  }

  fun removePasskey(passkeyId: String) {
    repository.removePasskey(passkeyId)
    showNotification("Hardware Passkey revoked from account.")
  }

  fun terminateSession(sessionId: String) {
    repository.terminateSession(sessionId)
    showNotification("Active remote session terminated.")
  }

  fun terminateAllOtherSessions() {
    repository.terminateAllOtherSessions()
    showNotification("All other enterprise active sessions revoked.")
  }

  fun updateSecurityPolicies(policies: EnterpriseSecurityPolicies) {
    repository.updateSecurityPolicies(policies)
    showNotification("Zero Trust & ISO 27001 Security Policies Applied.")
  }

  val globalConfig: StateFlow<EnterpriseGlobalConfiguration> get() = repository.globalConfig

  fun setThemeMode(mode: ThemeMode) {
    repository.setThemeMode(mode)
    showNotification("Theme set to ${mode.title}")
  }

  fun setColorTheme(theme: EnterpriseColorTheme) {
    repository.setColorTheme(theme)
    showNotification("Enterprise Color Theme set to ${theme.displayName}")
  }

  fun setHighContrast(enabled: Boolean) {
    repository.setHighContrast(enabled)
    showNotification(if (enabled) "High contrast accessibility enabled" else "Standard contrast restored")
  }

  fun setDynamicMonetColors(enabled: Boolean) {
    repository.setDynamicMonetColors(enabled)
    showNotification(if (enabled) "Dynamic Android 12+ Monet colors active" else "Enterprise brand colors active")
  }

  fun setHighInformationDensity(enabled: Boolean) {
    repository.setHighInformationDensity(enabled)
    showNotification(if (enabled) "High information density active" else "Standard spacing active")
  }

  fun updateGlobalConfiguration(config: EnterpriseGlobalConfiguration) {
    repository.updateGlobalConfiguration(config)
    showNotification("Enterprise global configuration updated successfully!")
  }

  fun resetConfigurationToDefaults() {
    repository.resetConfigurationToDefaults()
    showNotification("Enterprise configuration reset to standard defaults.")
  }

  fun setCurrency(currencyCode: String) {
    _activeCurrency.value = currencyCode
  }

  fun dismissNotification() {
    _notification.value = null
  }

  fun showNotification(msg: String, isError: Boolean = false) {
    _notification.value = UiNotification(msg, isError)
  }

  // Calculator Screen handlers
  fun updateCalculator(
    payType: PayType? = null,
    baseRate: String? = null,
    payFrequency: PayFrequency? = null,
    filingStatus: FilingStatus? = null,
    hoursWorked: String? = null,
    overtimeHours: String? = null,
    overtimeMultiplier: Double? = null,
    preTax401kPercent: String? = null,
    preTaxHealth: String? = null,
    postTaxDeductions: String? = null,
    stateCode: String? = null,
    allowances: Int? = null,
    jurisdiction: GlobalJurisdiction? = null,
    customVatPercent: String? = null
  ) {
    val current = _calculatorState.value
    _calculatorState.value = current.copy(
      payType = payType ?: current.payType,
      baseRateInput = baseRate ?: current.baseRateInput,
      payFrequency = payFrequency ?: current.payFrequency,
      filingStatus = filingStatus ?: current.filingStatus,
      hoursWorkedInput = hoursWorked ?: current.hoursWorkedInput,
      overtimeHoursInput = overtimeHours ?: current.overtimeHoursInput,
      overtimeMultiplier = overtimeMultiplier ?: current.overtimeMultiplier,
      preTax401kPercentInput = preTax401kPercent ?: current.preTax401kPercentInput,
      preTaxHealthInput = preTaxHealth ?: current.preTaxHealthInput,
      postTaxDeductionsInput = postTaxDeductions ?: current.postTaxDeductionsInput,
      stateCode = stateCode ?: current.stateCode,
      allowances = allowances ?: current.allowances,
      selectedJurisdiction = jurisdiction ?: current.selectedJurisdiction,
      currencyCode = jurisdiction?.currencyCode ?: current.currencyCode,
      customVatPercentInput = customVatPercent ?: current.customVatPercentInput
    )
    recalculate()
  }

  fun setGlobalJurisdiction(jurisdiction: GlobalJurisdiction) {
    _calculatorState.value = _calculatorState.value.copy(
      selectedJurisdiction = jurisdiction,
      currencyCode = jurisdiction.currencyCode
    )
    _activeCurrency.value = jurisdiction.currencyCode
    recalculate()
    viewModelScope.launch {
      repository.updateTaxSettings(
        TaxSettingEntity(
          id = 1,
          activeJurisdictionCode = jurisdiction.countryCode,
          activeContinent = jurisdiction.continent.displayName,
          defaultCurrencyCode = jurisdiction.currencyCode,
          vatGstVasRatePercent = jurisdiction.standardVatGstPercent,
          employerPensionMatchPercent = jurisdiction.employerPensionRate,
          statutoryHealthPercent = jurisdiction.employerHealthRate,
          standardOvertimeMultiplier = 1.5,
          localClearingFormat = jurisdiction.clearingAndWpsFormat,
          enforceWpsPayroll = jurisdiction.hasMandatoryWps
        )
      )
    }
    showNotification("Switched jurisdiction to ${jurisdiction.countryName} (${jurisdiction.currencyCode})")
  }

  fun saveTaxSettings(settings: TaxSettingEntity) {
    viewModelScope.launch {
      repository.updateTaxSettings(settings)
      val matched = GlobalJurisdiction.entries.find { it.countryCode == settings.activeJurisdictionCode }
      if (matched != null) {
        _calculatorState.value = _calculatorState.value.copy(
          selectedJurisdiction = matched,
          currencyCode = matched.currencyCode
        )
        _activeCurrency.value = matched.currencyCode
        recalculate()
      }
      showNotification("Global Tax & Statutory settings saved to Room database!")
    }
  }

  fun saveCompanyProfile(profile: CompanyProfileEntity) {
    viewModelScope.launch {
      repository.updateCompanyProfile(profile)
      showNotification("Enterprise Company Profile saved locally!")
    }
  }

  fun recordPaystackTransactionEntity(tx: PaystackTransactionEntity) {
    viewModelScope.launch {
      repository.recordPaystackTransaction(tx)
      showNotification("Transaction logged to local Room database: ${tx.reference}")
    }
  }

  private fun recalculate() {
    val state = _calculatorState.value
    val baseRate = state.baseRateInput.toDoubleOrNull() ?: 0.0
    val hoursWorked = state.hoursWorkedInput.toDoubleOrNull() ?: 80.0
    val overtimeHours = state.overtimeHoursInput.toDoubleOrNull() ?: 0.0
    val preTax401k = state.preTax401kPercentInput.toDoubleOrNull() ?: 0.0
    val preTaxHealth = state.preTaxHealthInput.toDoubleOrNull() ?: 0.0
    val postTax = state.postTaxDeductionsInput.toDoubleOrNull() ?: 0.0
    val customVat = state.customVatPercentInput.toDoubleOrNull()

    val res = repository.calculateGlobalPaycheck(
      jurisdiction = state.selectedJurisdiction,
      payType = state.payType,
      baseRate = baseRate,
      payFrequency = state.payFrequency,
      filingStatus = state.filingStatus,
      hoursWorked = hoursWorked,
      overtimeHours = overtimeHours,
      overtimeMultiplier = state.overtimeMultiplier,
      voluntaryPensionPercent = preTax401k,
      voluntaryHealthInsurance = preTaxHealth,
      postTaxDeductions = postTax,
      subRegionCode = state.stateCode,
      allowances = state.allowances,
      customVatGstPercent = customVat
    )
    _calculatorState.value = _calculatorState.value.copy(result = res)
  }

  // Employee management
  fun addEmployee(
    firstName: String,
    lastName: String,
    email: String,
    role: String,
    department: String,
    payType: PayType,
    baseRate: Double,
    payFrequency: PayFrequency,
    filingStatus: FilingStatus,
    stateCode: String,
    preTax401k: Double,
    preTaxHealth: Double
  ) {
    viewModelScope.launch {
      val emp = EmployeeEntity(
        firstName = firstName,
        lastName = lastName,
        email = email,
        role = role,
        department = department,
        payType = payType,
        baseRate = baseRate,
        payFrequency = payFrequency,
        filingStatus = filingStatus,
        stateCode = stateCode,
        preTax401kPercent = preTax401k,
        preTaxHealthInsurance = preTaxHealth
      )
      repository.addEmployee(emp)
      showNotification("Employee ${emp.fullName} successfully added!")
    }
  }

  fun deleteEmployee(employee: EmployeeEntity) {
    viewModelScope.launch {
      repository.deleteEmployee(employee)
      showNotification("Employee ${employee.fullName} removed")
    }
  }

  // Bulk Import Operations
  fun importEmployeesBulk(list: List<EmployeeEntity>) {
    viewModelScope.launch {
      repository.importEmployees(list)
      showNotification("Imported ${list.size} employee records into database")
    }
  }

  fun importPayrollRunsBulk(runs: List<PayrollRunEntity>) {
    viewModelScope.launch {
      repository.importPayrollRuns(runs)
      showNotification("Imported ${runs.size} historical payroll runs into database")
    }
  }

  fun importExpensesBulk(expensesList: List<ExpenseEntity>) {
    viewModelScope.launch {
      repository.importExpenses(expensesList)
      showNotification("Imported ${expensesList.size} expense entries into database")
    }
  }

  fun calculatePaycheck(
    jurisdiction: GlobalJurisdiction,
    payType: PayType,
    baseRate: Double,
    payFrequency: PayFrequency,
    filingStatus: FilingStatus = FilingStatus.SINGLE,
    hoursWorked: Double = 80.0
  ): CalculationResult {
    return repository.calculateGlobalPaycheck(
      jurisdiction = jurisdiction,
      payType = payType,
      baseRate = baseRate,
      payFrequency = payFrequency,
      filingStatus = filingStatus,
      hoursWorked = hoursWorked
    )
  }

  fun getJournalEntriesForRun(run: PayrollRunEntity): List<JournalEntryLine> {
    return repository.generateJournalEntries(run)
  }

  fun registerNewPasskey(name: String) {
    repository.enrollNewPasskey(name, "Internal Authenticator")
  }

  fun removePasskey(id: String) {
    repository.removePasskey(id)
  }

  // Payroll execution
  fun processBulkPayrollRun(periodStart: String, periodEnd: String) {
    viewModelScope.launch {
      val currentEmployees = employees.value
      if (currentEmployees.isEmpty()) {
        showNotification("No employees to process in payroll run!", true)
        return@launch
      }
      val runTitle = "Bi-Weekly Payroll - ${periodStart} to ${periodEnd}"
      val runId = repository.executePayrollRun(runTitle, periodStart, periodEnd, currentEmployees)
      showNotification("Payroll Run #$runId successfully executed & reconciled!")
    }
  }

  fun selectPayrollRun(run: PayrollRunEntity) {
    _selectedPayrollRun.value = run
    viewModelScope.launch {
      repository.getItemsForRun(run.id).collect { items ->
        _selectedRunItems.value = items
      }
    }
  }

  fun clearSelectedRun() {
    _selectedPayrollRun.value = null
    _selectedRunItems.value = emptyList()
  }

  // Subscription / Monetization
  fun upgradeSubscription(tier: SubscriptionTier) {
    repository.updateSubscriptionTier(tier)
    showNotification("Plan updated to ${tier.title}! All enterprise features unlocked.")
  }

  fun updateUserRole(role: UserRole) {
    repository.updateUserRole(role)
    showNotification("Switched view role to ${role.roleName}")
  }

  fun dismissAdBanner() {
    repository.toggleAdBanner(true)
    showNotification("Sponsored banner minimized")
  }

  fun restoreAdBanner() {
    repository.toggleAdBanner(false)
  }

  fun regenerateApiKey(isLive: Boolean) {
    val key = repository.generateNewApiKey(isLive)
    showNotification("New ${if (isLive) "Production" else "Sandbox"} API key generated!")
  }

  fun autoScanAndAddReceipt(vendor: String, amount: Double) {
    viewModelScope.launch {
      val (category, confidence) = repository.autoCategorizeReceipt(vendor, amount)
      val expense = ExpenseEntity(
        merchant = vendor,
        category = category,
        amount = amount,
        dateString = "Today",
        isMlAutoCategorized = true,
        mlConfidenceScore = confidence,
        reconciliationStatus = "Reconciled"
      )
      repository.addExpense(expense)
      showNotification("ML classified \"$vendor\" under $category (${(confidence * 100).toInt()}% confidence)")
    }
  }

  fun getAnomalies(): List<AnomalyAlert> {
    val raw = repository.detectAnomalies(employees.value, payrollRuns.value)
    val resolvedMap: Map<String, String> = resolvedAnomalyIds.value
    return raw.map { alert ->
      if (resolvedMap.containsKey(alert.id)) {
        alert.copy(isResolved = true, resolutionAction = resolvedMap[alert.id])
      } else alert
    }
  }

  fun resolveAnomaly(anomalyId: String, action: String) {
    repository.resolveAnomaly(anomalyId, action)
    showNotification("Audit item resolved: $action")
  }

  fun updateForecastScenario(growthRate: Double? = null, wageInflation: Double? = null, horizon: Int? = null) {
    growthRate?.let { _forecastGrowthRate.value = it }
    wageInflation?.let { _forecastWageInflation.value = it }
    horizon?.let { _forecastHorizonMonths.value = it }
  }

  fun getForecastPoints(): List<ForecastPoint> {
    val lastRunGross = payrollRuns.value.firstOrNull()?.totalGross ?: 28000.0
    val monthlyExp = expenses.value.sumOf { it.amount }
    return repository.generateForecast(
      currentRunCost = lastRunGross,
      monthlyExpenses = monthlyExp,
      growthRatePercent = _forecastGrowthRate.value,
      wageInflationPercent = _forecastWageInflation.value,
      horizonMonths = _forecastHorizonMonths.value
    )
  }

  fun updateReportFilter(
    reportType: ReportType? = null,
    dateRange: String? = null,
    selectedDepartment: String? = null,
    selectedPayType: String? = null,
    includeEmployerTaxes: Boolean? = null,
    includeBenefitsBreakdown: Boolean? = null,
    includeAnomaliesAudit: Boolean? = null
  ) {
    val current = _reportFilter.value
    _reportFilter.value = current.copy(
      reportType = reportType ?: current.reportType,
      dateRange = dateRange ?: current.dateRange,
      selectedDepartment = selectedDepartment ?: current.selectedDepartment,
      selectedPayType = selectedPayType ?: current.selectedPayType,
      includeEmployerTaxes = includeEmployerTaxes ?: current.includeEmployerTaxes,
      includeBenefitsBreakdown = includeBenefitsBreakdown ?: current.includeBenefitsBreakdown,
      includeAnomaliesAudit = includeAnomaliesAudit ?: current.includeAnomaliesAudit
    )
  }

  fun generateReport(format: ReportFormat): GeneratedReportData {
    return repository.generateCustomReport(
      filter = _reportFilter.value,
      employeesList = employees.value,
      payrollRunsList = payrollRuns.value,
      format = format
    )
  }

  fun exportReport(report: GeneratedReportData): String {
    return repository.exportReportContent(report)
  }

  fun learnUserRule(merchant: String, newCategory: String, glCode: String = "GL #6290") {
    repository.learnUserCategorizationRule(merchant, newCategory, glCode)
    showNotification("ML Model retrained! All future \"$merchant\" items will map to $newCategory.")
  }

  fun confirmBankTransaction(txId: String, category: String) {
    repository.confirmBankTransaction(txId, category)
    showNotification("Transaction reconciled & posted to General Ledger!")
  }

  fun getMlStats(): MlModelStats {
    return repository.getMlStats()
  }

  fun getVideoTutorials(): List<VideoTutorialItem> {
    return repository.getVideoTutorials()
  }

  fun completeOnboarding(
    companyName: String,
    ein: String,
    state: String,
    quickBooks: Boolean,
    xero: Boolean
  ) {
    repository.completeOnboarding(companyName, ein, state, quickBooks, xero)
    showNotification("Welcome to PayFlow AI! Your enterprise configuration is active.")
  }

  fun resetOnboarding() {
    repository.resetOnboarding()
  }

  fun getJournalEntries(run: PayrollRunEntity): List<JournalEntryLine> {
    return repository.generateJournalEntries(run)
  }

  val availableCurrencies: List<CurrencyInfo> = CurrencyInfo.SUPPORTED

  fun setActiveCurrency(currencyCode: String) {
    _activeCurrency.value = currencyCode
    val info = CurrencyInfo.SUPPORTED.find { it.code.equals(currencyCode, ignoreCase = true) }
    val name = info?.name ?: currencyCode
    val rate = repository.getExchangeRateToUsd(currencyCode)
    if (currencyCode.equals("USD", ignoreCase = true)) {
      showNotification("Dashboard currency switched to $name ($currencyCode) [Base 1.00]")
    } else {
      showNotification("Dashboard currency switched to $name ($currencyCode) • 1 USD = $rate ${info?.symbol ?: ""}")
    }
  }

  fun getExchangeRate(currencyCode: String = _activeCurrency.value): Double {
    return repository.getExchangeRateToUsd(currencyCode)
  }

  fun getCurrencySymbol(currencyCode: String = _activeCurrency.value): String {
    return repository.getCurrencySymbol(currencyCode)
  }

  fun convertFromUsd(amountUsd: Double, targetCurrency: String = _activeCurrency.value): Double {
    return repository.convertFromUsd(amountUsd, targetCurrency)
  }

  fun formatCurrency(amount: Double): String {
    return repository.formatAmount(amount, _activeCurrency.value)
  }

  fun formatConvertedCurrency(amountUsd: Double, targetCurrency: String = _activeCurrency.value): String {
    return repository.formatConvertedAmount(amountUsd, targetCurrency)
  }

  // Paystack Integration Methods
  val paystackConfig: StateFlow<PaystackConfig> get() = repository.paystackConfig
  val paystackTransactions: StateFlow<List<PaystackTransaction>> get() = repository.paystackTransactions

  fun updatePaystackConfig(config: PaystackConfig) {
    repository.updatePaystackConfig(config)
    showNotification("Paystack configuration updated successfully!")
  }

  fun recordPaystackPayment(
    tier: SubscriptionTier,
    isYearly: Boolean,
    channel: PaystackPaymentChannel,
    customerEmail: String
  ): PaystackTransaction {
    val tx = repository.recordPaystackPayment(tier, isYearly, channel, customerEmail)
    showNotification("Payment verified via Paystack! Upgraded to ${tier.title}.")
    return tx
  }

  // Live AI Copilot & Market Analysis Integration
  val chatMessages: StateFlow<List<ChatMessage>> get() = repository.chatMessages
  val isAiThinking: StateFlow<Boolean> get() = repository.isAiThinking

  fun sendAiChatMessage(prompt: String) {
    viewModelScope.launch {
      repository.sendAiChatMessage(prompt)
    }
  }

  fun clearChatHistory() {
    repository.clearChatHistory()
    showNotification("AI Chat history reset.")
  }

  fun getLaborMarketIndex(): List<LaborMarketIndexItem> {
    return repository.getLaborMarketIndex()
  }
}

