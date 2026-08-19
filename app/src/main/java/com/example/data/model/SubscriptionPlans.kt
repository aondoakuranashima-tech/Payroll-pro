package com.example.data.model

/** Canonical Payroll Pro commercial plans. Prices are USD and the billing API is authoritative. */
enum class SubscriptionPlan(
  val displayName: String,
  val monthlyUsd: Double,
  val annualDiscountPercent: Double,
  val features: List<String>
) {
  BASIC("Basic", 99.0, 1.5, listOf("Employee management", "Payroll processing", "Payslips", "Payroll calendar", "Basic deductions", "Bonuses and allowances", "Leave and attendance", "Payroll reports", "Payroll AI assistant", "Basic anomaly detection", "RBAC", "Audit logs", "Paystack monthly and annual billing", "Currency conversion")),
  PRO("Pro", 299.0, 2.5, listOf("Everything in Basic", "Advanced payroll automation", "Multiple payroll schedules", "Payroll approvals", "Multiple departments and locations", "Employee onboarding", "Advanced RBAC", "Approval workflows", "Advanced analytics", "AI forecasting", "AI-generated reports", "Accounting/HR integrations")),
  BUSINESS("Business", 699.0, 3.0, listOf("Everything in Pro", "Multi-company payroll", "Multi-currency payroll", "Multiple entities", "Advanced compliance tools", "Predictive payroll analytics", "Payroll anomaly prediction", "AI financial insights", "Automated workflows", "API access", "Webhooks", "Advanced audit logs", "Priority support")),
  PREMIUM("Premium", 1499.0, 3.5, listOf("Everything in Business", "Unlimited organizations/entities", "Large workforce support", "Dedicated environments", "Enterprise API limits", "Enterprise AI analytics", "Workforce forecasting", "Custom AI workflows", "SSO", "Fine-grained permissions", "Advanced security controls", "Dedicated account management", "SLA options")),
  ENTERPRISE_PLUS("Enterprise Plus", 2999.0, 3.5, listOf("Everything in Premium", "Unlimited employees", "Unlimited payroll entities", "Unlimited departments and locations", "Custom payroll rules", "Custom workflows", "Custom integrations", "Dedicated API infrastructure", "Custom AI assistants", "Data migration assistance", "Dedicated implementation team", "Dedicated success manager", "Premium support", "Custom SLA", "Enterprise architecture consultation"));

  val annualBeforeDiscount: Double get() = monthlyUsd * 12.0
  val annualPriceUsd: Double get() = annualBeforeDiscount * (1.0 - annualDiscountPercent / 100.0)
  val annualSavingsUsd: Double get() = annualBeforeDiscount - annualPriceUsd
}
