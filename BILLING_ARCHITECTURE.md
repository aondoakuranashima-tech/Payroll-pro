# Payroll Pro Billing Architecture v2

**Stripe is not part of this architecture.** Gift cards are not supported.

## Runtime flow

Products -> App Checkout -> Payment Router -> provider adapter -> provider checkout/authorization -> verified webhook -> Billing Engine -> Ledger -> Invoice -> Subscription -> Entitlement.

### App Checkout

Collects:
- country
- currency
- amount
- requested payment method
- product/plan
- organization
- billing cycle
- seat/usage quantity

### Payment Router

Selects the best configured provider using:
- customer country
- settlement currency
- requested payment method
- provider capability
- provider availability
- provider health
- transaction cost
- provider priority/fallback policy

The router must never promise a payment method unless the selected provider reports that capability for the transaction.

## Providers

1. **Paystack** — Nigeria/Africa flows and NGN settlement.
2. **Flutterwave** — Africa/global payment coverage and configured settlement routes.
3. **Dodo Payments** — global SaaS checkout/subscription processing where supported.
4. **Paddle** — global SaaS merchant-of-record checkout where supported; settlement destination is provider/account configuration, not hard-coded.
5. **PayPal** — PayPal checkout and supported card/payment flows where the account and customer country permit them.

Settlement destinations are configuration, not application assumptions. The application records provider settlement status and destination metadata without pretending that every provider pays directly to a Nigerian bank account.

## Customer payment methods

The checkout can request:
- credit card
- debit card
- prepaid card
- Apple Pay
- Google Pay
- PayPal
- bank transfer
- direct bank payment
- USSD
- mobile money
- local wallets such as OPay where the selected provider supports them
- QR payments
- other local methods exposed by the selected provider

## Provider contract

Every adapter implements the same contract:

```text
PaymentProvider
├── createCheckout()
├── authorizePayment()
├── capturePayment()
├── verifyPayment()
├── refundPayment()
├── createSubscription()
├── cancelSubscription()
├── getPaymentMethods()
├── getTransaction()
└── handleWebhook()
```

Providers may return `unsupported` for operations they do not expose. The core must not emulate provider features by guessing.

## Webhook pipeline

Provider webhook -> webhook verification -> event idempotency -> normalized billing event -> ledger -> invoice -> subscription -> entitlement.

Browser redirects are never proof of payment.

## Services/packages

```text
apps/
├── billing-api/
├── checkout/
└── billing-worker/

packages/
├── billing-core/
├── payment-router/
├── payment-providers/
│   ├── paystack/
│   ├── flutterwave/
│   ├── dodo/
│   ├── paddle/
│   └── paypal/
├── webhook-engine/
├── subscription-engine/
├── invoice-engine/
├── entitlement-engine/
├── usage-engine/
├── ledger/
├── reconciliation/
└── shared/
```

## Ledger requirements

The ledger is append-only. Payment state changes are recorded as immutable events. Reconciliation compares internal transactions with provider transactions and settlement reports. Refunds, chargebacks, failed payments and subscription state transitions are separate ledger events.

## Routing example

Nigeria + NGN + Card -> router -> Paystack or Flutterwave -> checkout -> authorization -> verified webhook -> ledger -> invoice PAID -> subscription ACTIVE -> entitlement ENABLED.

The router may fall back to another healthy provider only when the original transaction has not been created/authorized and the retry policy allows it. It must never double-charge a customer.
