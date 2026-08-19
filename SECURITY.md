# Security Policy

## Security rules

- Never commit API keys, passwords, signing keys, keystores, Firebase service-account files, or production credentials.
- Production secrets must be stored in GitHub Actions Secrets, Google AI Studio Secrets, or the deployment platform's secret manager.
- The Android application must not contain privileged server credentials.
- Gemini/OpenAI/Paystack/Stripe secrets belong on the Softwall backend, not inside the APK.
- Use HTTPS for all production API communication.
- Do not log authentication tokens, API keys, passwords, payroll records, or personal financial data.
- Treat employee payroll and identity data as confidential.
- Report suspected vulnerabilities privately to the repository owner rather than opening a public issue with sensitive details.

## Architecture requirement

Payroll Pro should communicate with the Softwall API for authenticated production operations. AI provider credentials should remain server-side.

## Secret rotation

If a secret is ever committed accidentally, rotate/revoke it immediately. Removing it from a later commit does not make the exposed credential safe.
