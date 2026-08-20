import express from "express";
import crypto from "node:crypto";
import { PROVIDERS, providerConfigured, createCheckout, verifyPayment, verifyPaystackSignature, verifyHmacSha256 } from "./providers.mjs";

const app = express();
const PORT = Number(process.env.PORT || 10000);
const FX_API_URL = process.env.FX_API_URL || "https://open.er-api.com/v6/latest/USD";
const DEFAULT_CURRENCY = process.env.PAYSTACK_CURRENCY || "NGN";
const CURRENCY_DECIMALS = { JPY: 0, KRW: 0, VND: 0, XOF: 0 };
const fxCache = new Map();
const quoteCacheMs = 5 * 60 * 1000;
const processedEvents = new Set();
const transactions = new Map();
const PLANS = {
  BASIC: { monthlyUsd: 99, discount: 1.5 }, PRO: { monthlyUsd: 299, discount: 2.5 }, BUSINESS: { monthlyUsd: 699, discount: 3 }, PREMIUM: { monthlyUsd: 1499, discount: 3.5 }, ENTERPRISE_PLUS: { monthlyUsd: 2999, discount: 3.5 }
};

app.disable("x-powered-by");
app.use(express.json({ verify: (req, _res, buf) => { req.rawBody = Buffer.from(buf); } }));
const minorUnits = (amount, currency) => Math.round(Number(amount) * (10 ** (CURRENCY_DECIMALS[currency] ?? 2)));
const safeReference = (prefix = "pp") => `${prefix}_${Date.now()}_${crypto.randomBytes(5).toString("hex")}`;

async function rates() {
  const cached = fxCache.get("USD"); if (cached && Date.now() - cached.at < quoteCacheMs) return cached.rates;
  const response = await fetch(FX_API_URL, { headers: { Accept: "application/json" } });
  if (!response.ok) throw new Error("FX provider unavailable"); const data = await response.json(); if (!data.rates) throw new Error("Invalid FX response");
  fxCache.set("USD", { at: Date.now(), rates: data.rates }); return data.rates;
}
async function convert(amount, from, to) {
  if (from === to) return Number(amount); const r = await rates(); const fromRate = from === "USD" ? 1 : r[from]; const toRate = to === "USD" ? 1 : r[to];
  if (!fromRate || !toRate) throw new Error(`FX rate unavailable for ${from}/${to}`); return Number(amount) * (toRate / fromRate);
}
function availableProviders({ country = "", currency = "USD", method = "" } = {}) {
  const c = String(country).toUpperCase(); const m = String(method).toLowerCase();
  return PROVIDERS.filter(providerConfigured).filter((p) => {
    if (p === "paystack") return c === "NG" || currency === "NGN" || ["GH", "KE", "ZA"].includes(c);
    if (p === "flutterwave" || p === "dodo") return true;
    if (p === "paddle") return currency !== "NGN";
    if (p === "paypal") return !m || m === "paypal" || m === "card";
    return false;
  }).sort((a, b) => ({ paystack: 1, flutterwave: 2, dodo: 3, paddle: 4, paypal: 5 }[a] - ({ paystack: 1, flutterwave: 2, dodo: 3, paddle: 4, paypal: 5 }[b])));
}

async function checkout(req, res) {
  try {
    const { email, planCode, billingCycle = "monthly", displayCurrency = "USD", currency = DEFAULT_CURRENCY, provider, country = "NG", paymentMethod = "", metadata = {} } = req.body || {};
    const plan = PLANS[String(planCode || "").toUpperCase()]; if (!email || !plan) return res.status(400).json({ error: "email and a valid planCode are required" });
    const cycle = String(billingCycle).toLowerCase(); if (!["monthly", "annual"].includes(cycle)) return res.status(400).json({ error: "billingCycle must be monthly or annual" });
    const selected = String(provider || "").toLowerCase(); const candidates = selected ? [selected] : availableProviders({ country, currency, method: paymentMethod }); const chosen = candidates[0];
    if (!chosen || !PROVIDERS.includes(chosen) || !providerConfigured(chosen)) return res.status(400).json({ error: "No configured payment provider is available" });
    const usdAmount = cycle === "annual" ? plan.monthlyUsd * 12 * (1 - plan.discount / 100) : plan.monthlyUsd;
    const targetCurrency = String(currency).toUpperCase(); const amount = await convert(usdAmount, "USD", targetCurrency); const reference = safeReference(`payrollpro_${chosen}`);
    const common = { email, amount: chosen === "paypal" ? Number(amount.toFixed(2)) : minorUnits(amount, targetCurrency), currency: targetCurrency, reference, callbackUrl: process.env.PAYSTACK_CALLBACK_URL || process.env.FLW_CALLBACK_URL, metadata: { product: "Payroll Pro", planCode: String(planCode).toUpperCase(), billingCycle: cycle, displayCurrency: String(displayCurrency).toUpperCase(), paymentMethod, country, ...metadata } };
    if (chosen === "paddle") common.priceId = cycle === "annual" ? process.env.PADDLE_ANNUAL_PRICE_ID : process.env.PADDLE_MONTHLY_PRICE_ID;
    if (chosen === "paddle" && !common.priceId) return res.status(400).json({ error: "PADDLE_MONTHLY_PRICE_ID/PADDLE_ANNUAL_PRICE_ID is required" });
    const result = await createCheckout(chosen, common); transactions.set(reference, { reference, provider: chosen, providerId: result.providerId, email, planCode: common.metadata.planCode, billingCycle: cycle, amount, currency: targetCurrency, status: "pending", createdAt: new Date().toISOString() });
    res.json({ ...result, reference, planCode: common.metadata.planCode, billingCycle: cycle, amount, currency: targetCurrency, provider: chosen });
  } catch (error) { res.status(502).json({ error: error.message }); }
}

app.get("/health", (_req, res) => res.json({ ok: true, service: "payroll-pro-billing", providers: Object.fromEntries(PROVIDERS.map((p) => [p, providerConfigured(p)])) }));
app.get("/api/billing/providers", (req, res) => res.json({ providers: availableProviders(req.query).map((provider) => ({ provider, configured: true })), requested: { country: req.query.country || null, currency: req.query.currency || null, method: req.query.method || null } }));
app.get("/api/billing/plans", (_req, res) => res.json({ currency: "USD", plans: Object.entries(PLANS).map(([code, p]) => ({ code, monthlyUsd: p.monthlyUsd, annualBeforeDiscountUsd: p.monthlyUsd * 12, annualDiscountPercent: p.discount, annualPriceUsd: Number((p.monthlyUsd * 12 * (1 - p.discount / 100)).toFixed(2)), annualSavingsUsd: Number((p.monthlyUsd * 12 * p.discount / 100).toFixed(2)) })) }));
app.get("/api/billing/currencies", (_req, res) => res.json({ displayCurrencies: ["NGN", "USD", "EUR", "GBP", "CAD", "AUD", "JPY", "CNY", "INR", "BRL", "ZAR", "KES", "GHS", "XOF"], defaultCurrency: DEFAULT_CURRENCY }));
app.get("/api/billing/fx-quote", async (req, res) => { try { const amount = Number(req.query.amount); const from = String(req.query.from || "USD").toUpperCase(); const to = String(req.query.to || DEFAULT_CURRENCY).toUpperCase(); if (!Number.isFinite(amount) || amount <= 0) return res.status(400).json({ error: "amount must be positive" }); const converted = await convert(amount, from, to); res.json({ from, to, inputAmount: amount, convertedAmount: Number(converted.toFixed(CURRENCY_DECIMALS[to] ?? 2)), quotedAt: new Date().toISOString(), expiresInSeconds: 300 }); } catch (error) { res.status(503).json({ error: error.message }); } });
app.post("/api/billing/checkout", checkout);
app.post("/api/billing/initialize", (req, res) => { req.body = { ...(req.body || {}), provider: req.body?.provider || "paystack" }; return checkout(req, res); });
app.get("/api/billing/verify/:provider/:id", async (req, res) => { try { const result = await verifyPayment(req.params.provider.toLowerCase(), req.params.id); res.json({ verified: ["success", "succeeded", "completed"].includes(String(result.status).toLowerCase()), ...result }); } catch (error) { res.status(502).json({ verified: false, error: error.message }); } });

function eventId(provider, body) { return `${provider}:${body?.id || body?.event_id || body?.data?.id || body?.data?.reference || crypto.createHash("sha256").update(JSON.stringify(body)).digest("hex")}`; }
function markEvent(provider, body) { const id = eventId(provider, body); if (processedEvents.has(id)) return false; processedEvents.add(id); return true; }
function updateFromWebhook(provider, body) { const data = body?.data || body; const reference = data?.reference || data?.tx_ref || data?.custom_data?.payroll_pro_reference || data?.metadata?.payroll_pro_reference || data?.purchase_units?.[0]?.reference_id; if (!reference) return; const tx = transactions.get(reference); if (tx) tx.status = String(data?.status || body?.event || "received").toLowerCase(); }

app.post("/api/billing/webhooks/paystack", (req, res) => { if (!verifyPaystackSignature(req.rawBody, req.headers["x-paystack-signature"])) return res.status(401).json({ error: "Invalid Paystack signature" }); if (!markEvent("paystack", req.body)) return res.sendStatus(200); updateFromWebhook("paystack", req.body); res.sendStatus(200); });
app.post("/api/billing/webhooks/flutterwave", (req, res) => { if (!process.env.FLW_WEBHOOK_SECRET || req.headers["verif-hash"] !== process.env.FLW_WEBHOOK_SECRET) return res.status(401).json({ error: "Invalid Flutterwave signature" }); if (!markEvent("flutterwave", req.body)) return res.sendStatus(200); updateFromWebhook("flutterwave", req.body); res.sendStatus(200); });
app.post("/api/billing/webhooks/dodo", (req, res) => { const signature = req.headers["webhook-signature"] || req.headers["x-dodo-signature"]; if (process.env.DODO_WEBHOOK_SECRET && !verifyHmacSha256(req.rawBody, signature, process.env.DODO_WEBHOOK_SECRET)) return res.status(401).json({ error: "Invalid Dodo signature" }); if (!markEvent("dodo", req.body)) return res.sendStatus(200); updateFromWebhook("dodo", req.body); res.sendStatus(200); });
app.post("/api/billing/webhooks/paddle", (req, res) => { const signature = String(req.headers["paddle-signature"] || ""); if (process.env.PADDLE_WEBHOOK_SECRET) { const ts = signature.match(/(?:^|;)ts=([^;]+)/)?.[1]; const h1 = signature.match(/(?:^|;)h1=([^;]+)/)?.[1]; const expected = ts && h1 ? crypto.createHmac("sha256", process.env.PADDLE_WEBHOOK_SECRET).update(`${ts}:${req.rawBody.toString()}`).digest("hex") : ""; if (!expected || !crypto.timingSafeEqual(Buffer.from(expected), Buffer.from(h1))) return res.status(401).json({ error: "Invalid Paddle signature" }); } if (!markEvent("paddle", req.body)) return res.sendStatus(200); updateFromWebhook("paddle", req.body); res.sendStatus(200); });
app.post("/api/billing/webhooks/paypal", (req, res) => { if (process.env.PAYPAL_WEBHOOK_ID && !req.headers["paypal-transmission-id"]) return res.status(401).json({ error: "Missing PayPal transmission headers" }); if (!markEvent("paypal", req.body)) return res.sendStatus(200); updateFromWebhook("paypal", req.body); res.sendStatus(200); });
app.get("/api/billing/transaction/:reference", (req, res) => { const tx = transactions.get(req.params.reference); if (!tx) return res.status(404).json({ error: "Transaction not found" }); res.json(tx); });
app.use((_req, res) => res.status(404).json({ error: "Not found" }));
app.listen(PORT, "0.0.0.0", () => console.log(`Payroll Pro multi-provider billing API listening on ${PORT}`));
