# Payroll Pro Billing API

Standalone payment service for Payroll Pro. It does not depend on Softwall Payroll AI.

## Endpoints

- `GET /health`
- `GET /api/billing/currencies`
- `GET /api/billing/fx-quote?amount=49&from=USD&to=NGN`
- `POST /api/billing/initialize`
- `GET /api/billing/verify/:reference`
- `POST /api/billing/webhook`

## Security

Paystack's secret key is server-side only. Never put it in the Android app. Paystack webhook requests are verified with HMAC-SHA512 before processing.

## Global currency behavior

Payroll Pro can display prices in many customer currencies, then convert the price to a currency enabled on the merchant's Paystack account. The converted amount is calculated server-side and the quote is timestamped.

Paystack's supported currencies depend on the merchant's country and account configuration. This service therefore rejects currencies that are not in its configured Paystack set instead of pretending Paystack supports every world currency.

## Production requirements

1. Deploy `billing-api` as its own backend service.
2. Set `PAYSTACK_SECRET_KEY` and `PAYSTACK_WEBHOOK_SECRET` in the deployment secret manager.
3. Enable international payments/currencies in Paystack where eligible.
4. Configure the Paystack dashboard webhook URL to `/api/billing/webhook`.
5. Persist subscription and webhook state in Payroll Pro's database with idempotency before granting access.
6. Replace the example FX provider with a production FX provider under a commercial/appropriate license if required.
