# Payroll Pro Production Security Checklist

## Secrets
- [x] `.env` ignored
- [x] Keystores and signing files ignored
- [x] Service-account JSON ignored
- [x] Production API credentials excluded from source control
- [ ] Configure CI/CD secrets in GitHub Actions
- [ ] Rotate any credential that was previously exposed

## Network
- [x] Android INTERNET permission is present
- [ ] Production API URL must use HTTPS
- [ ] Reject cleartext HTTP in production
- [ ] Add certificate pinning only after the API certificate lifecycle is defined

## Authentication
- [ ] Use Softwall API authentication
- [ ] Store short-lived access tokens securely (Android Keystore-backed storage)
- [ ] Implement refresh-token rotation server-side
- [ ] Never log bearer tokens
- [ ] Enforce organization/tenant authorization on the API

## AI
- [ ] Gemini/OpenAI keys remain server-side
- [ ] Android sends AI requests through Softwall API
- [ ] Apply API rate limits and abuse controls
- [ ] Avoid sending unnecessary employee PII to AI providers
- [ ] Log AI audit metadata without storing secrets

## Payroll data
- [ ] Encrypt sensitive data at rest on the backend
- [ ] Use tenant-scoped queries for every payroll resource
- [ ] Record payroll approvals and changes in an immutable audit trail
- [ ] Apply least-privilege RBAC
- [ ] Validate all monetary values server-side

## Release
- [ ] Run unit tests
- [ ] Run Android lint/static analysis
- [ ] Run dependency/security scanning
- [ ] Build signed release artifact in CI
- [ ] Verify no secrets are present in APK/AAB before release
