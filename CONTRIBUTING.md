# Contributing to Payroll Pro

## Before committing

1. Never add `.env`, credentials, API keys, signing keys, or service-account files.
2. Keep production secrets in the appropriate secret manager.
3. Run the Android unit tests and lint checks.
4. Verify that debug/sample data is not presented as real production data.
5. Do not add direct privileged calls from the Android client to payment or AI provider APIs.

## Pull requests

PRs should explain security-sensitive changes, data-flow changes, authentication changes, and any changes affecting payroll calculations.
