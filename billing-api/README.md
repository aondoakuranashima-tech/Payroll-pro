# Payroll Pro Billing API

Payroll Pro now has a provider-agnostic billing router for **Paystack, Flutterwave, Dodo Payments, Paddle, and PayPal**. Provider credentials stay server-side; the Android/web clients receive hosted checkout URLs only.

## Architecture

```text
Payroll Pro client
      |
      v
Softwall-style Billing Router
      |
      +--> Paystack
      +--> Flutterwave
      +--> Dodo Payments
      +--> Paddle
      +--> PayPal
```

The router selects a configured provider from country, currency, and requested payment method. A client can also explicitly request a provider.

## Endpoints

- `GET /health` — service + provider configuration status
- `GET /api/billing/providers?country=NG&currency=NGN&method=card` — eligible configured providers
- `GET /api/billing/plans`
- `GET /api/billing/currencies`
- `GET /api/billing/fx-quote?amount=299&from=USD&to=NGN`
- `POST /api/billing/checkout` — unified checkout creation
- `POST /api/billing/initialize` — legacy Paystack-compatible entry point
- `GET /api/billing/verify/:provider/:id` — provider verification where supported
- `GET /api/billing/transaction/:reference` — transaction state

Webhooks:

- `POST /api/billing/webhooks/paystack`
- `POST /api/billing/webhooks/flutterwave`
- `POST /api/billing/webhooks/dodo`
- `POST /api/billing/webhooks/paddle`
- `POST /api/billing/webhooks/paypal`

## Provider behavior

- **Paystack:** hosted checkout + server verification + HMAC-SHA512 webhook verification.
- **Flutterwave:** hosted Standard checkout + reference verification + `verif-hash` webhook verification.
- **Dodo Payments:** checkout session creation + checkout-session retrieval. Product IDs are configured in environment variables.
- **Paddle:** transaction creation using a configured Paddle price ID and hosted checkout. Webhook signatures use Paddle's `ts:h1` signing scheme.
- **PayPal:** Orders v2 checkout creation + server-side order verification. Capture/approval is completed through PayPal's hosted approval flow.

Provider capabilities differ. The router therefore does not claim that every payment method works on every provider.

## Customer payment methods

The checkout can expose supported cards, bank payments, mobile money, USSD, wallets, Apple Pay/Google Pay, PayPal and other provider-supported local methods. **Gift cards are intentionally not supported.** The actual methods available are controlled by the selected provider, merchant account, country and currency.

## Security

- No secret key is sent to Android/web clients.
- Paystack webhook signatures are verified with HMAC-SHA512.
- Flutterwave webhook requests require the configured `verif-hash` secret.
- Dodo/Paddle webhook verification is supported when their webhook secrets are configured.
- Provider references are generated server-side.
- Webhook event IDs are deduplicated in the current process.
- Use HTTPS in production.

## Important production limitation

The current connector implementation keeps transaction/webhook state in memory so the billing API remains dependency-light. **Before production, move `transactions` and `processedEvents` into Payroll Pro's PostgreSQL database with unique constraints and an outbox/queue.** Do not grant subscription access solely from a browser redirect; grant access only after a verified provider event/transaction is persisted idempotently.

## Environment

Copy `.env.example` to the deployment secret manager. Never commit real credentials.

Paddle requires configured monthly/annual price IDs. Dodo requires a product ID. PayPal requires client credentials and production/sandbox base URL selection.

## Local check

```bash
npm install
npm run check
npm start
```
