import express from "express";
import crypto from "node:crypto";

const app = express();
const PORT = Number(process.env.PORT || 10000);
const PAYSTACK_SECRET_KEY = process.env.PAYSTACK_SECRET_KEY;
const WEBHOOK_SECRET = process.env.PAYSTACK_WEBHOOK_SECRET || PAYSTACK_SECRET_KEY;
const FX_API_URL = process.env.FX_API_URL || "https://open.er-api.com/v6/latest/USD";
const DEFAULT_CURRENCY = process.env.PAYSTACK_CURRENCY || "NGN";
const CHECKOUT_CALLBACK_URL = process.env.PAYSTACK_CALLBACK_URL || "";

const PAYSTACK_CURRENCIES = new Set(["NGN", "USD", "GHS", "KES", "ZAR", "XOF"]);
const CURRENCY_DECIMALS = { JPY: 0, KRW: 0, VND: 0, XOF: 0 };
const fxCache = new Map();
const quoteCacheMs = 5 * 60 * 1000;
const PLANS = {
  BASIC: { monthlyUsd: 99, discount: 1.5 },
  PRO: { monthlyUsd: 299, discount: 2.5 },
  BUSINESS: { monthlyUsd: 699, discount: 3.0 },
  PREMIUM: { monthlyUsd: 1499, discount: 3.5 },
  ENTERPRISE_PLUS: { monthlyUsd: 2999, discount: 3.5 }
};

app.disable("x-powered-by");
app.use(express.json({ verify: (req, _res, buf) => { req.rawBody = Buffer.from(buf); } }));

function requireSecret() { if (!PAYSTACK_SECRET_KEY) throw new Error("PAYSTACK_SECRET_KEY is not configured"); }
function minorUnits(amount, currency) { return Math.round(Number(amount) * (10 ** (CURRENCY_DECIMALS[currency] ?? 2))); }
async function paystack(path, options = {}) {
  requireSecret();
  const response = await fetch(`https://api.paystack.co${path}`, { ...options, headers: { Authorization: `Bearer ${PAYSTACK_SECRET_KEY}`, "Content-Type": "application/json", ...(options.headers || {}) } });
  const data = await response.json();
  if (!response.ok || data.status === false) throw new Error(data.message || `Paystack request failed: ${response.status}`);
  return data;
}
async function getUsdRates() {
  const cached = fxCache.get("USD");
  if (cached && Date.now() - cached.at < quoteCacheMs) return cached.rates;
  const response = await fetch(FX_API_URL, { headers: { Accept: "application/json" } });
  if (!response.ok) throw new Error("FX provider unavailable");
  const data = await response.json();
  if (!data.rates || typeof data.rates !== "object") throw new Error("Invalid FX provider response");
  fxCache.set("USD", { at: Date.now(), rates: data.rates });
  return data.rates;
}
async function convert(amount, from, to) {
  if (from === to) return Number(amount);
  const rates = await getUsdRates();
  const fromRate = from === "USD" ? 1 : rates[from]; const toRate = to === "USD" ? 1 : rates[to];
  if (!fromRate || !toRate) throw new Error(`FX rate unavailable for ${from}/${to}`);
  return Number(amount) * (toRate / fromRate);
}
function safeReference(prefix = "pp") { return `${prefix}_${Date.now()}_${crypto.randomBytes(5).toString("hex")}`; }
function verifyWebhook(req) {
  const signature = req.headers["x-paystack-signature"];
  if (!signature || !WEBHOOK_SECRET || !req.rawBody) return false;
  const digest = crypto.createHmac("sha512", WEBHOOK_SECRET).update(req.rawBody).digest("hex");
  const a = Buffer.from(digest, "utf8"); const b = Buffer.from(String(signature), "utf8");
  return a.length === b.length && crypto.timingSafeEqual(a, b);
}

app.get("/health", (_req, res) => res.json({ ok: true, service: "payroll-pro-billing" }));
app.get("/api/billing/plans", (_req, res) => {
  const plans = Object.entries(PLANS).map(([code, p]) => ({ code, monthlyUsd: p.monthlyUsd, annualBeforeDiscountUsd: p.monthlyUsd * 12, annualDiscountPercent: p.discount, annualPriceUsd: Number((p.monthlyUsd * 12 * (1 - p.discount / 100)).toFixed(2)), annualSavingsUsd: Number((p.monthlyUsd * 12 * p.discount / 100).toFixed(2)) }));
  res.json({ currency: "USD", plans });
});
app.get("/api/billing/currencies", (_req, res) => res.json({ displayCurrencies: ["NGN", "USD", "EUR", "GBP", "CAD", "AUD", "JPY", "CNY", "INR", "BRL", "ZAR", "KES", "GHS", "XOF"], paystackCurrencies: [...PAYSTACK_CURRENCIES], defaultCurrency: DEFAULT_CURRENCY }));
app.get("/api/billing/fx-quote", async (req, res) => {
  try {
    const amount = Number(req.query.amount); const from = String(req.query.from || "USD").toUpperCase(); const to = String(req.query.to || DEFAULT_CURRENCY).toUpperCase();
    if (!Number.isFinite(amount) || amount <= 0) return res.status(400).json({ error: "amount must be positive" });
    if (!PAYSTACK_CURRENCIES.has(to)) return res.status(400).json({ error: `Paystack is not configured for ${to}` });
    const converted = await convert(amount, from, to);
    res.json({ from, to, inputAmount: amount, convertedAmount: Number(converted.toFixed(CURRENCY_DECIMALS[to] ?? 2)), paystackSubunitAmount: minorUnits(converted, to), quotedAt: new Date().toISOString(), expiresInSeconds: 300 });
  } catch (error) { res.status(503).json({ error: error.message }); }
});
app.post("/api/billing/initialize", async (req, res) => {
  try {
    const { email, planCode, billingCycle = "monthly", displayCurrency = "USD", paystackCurrency = DEFAULT_CURRENCY, metadata = {} } = req.body || {};
    const plan = PLANS[String(planCode || "").toUpperCase()];
    if (!email || !plan) return res.status(400).json({ error: "email and a valid planCode are required" });
    const cycle = String(billingCycle).toLowerCase();
    if (!["monthly", "annual"].includes(cycle)) return res.status(400).json({ error: "billingCycle must be monthly or annual" });
    const currency = String(paystackCurrency).toUpperCase();
    if (!PAYSTACK_CURRENCIES.has(currency)) return res.status(400).json({ error: `Unsupported Paystack currency: ${currency}` });
    const usdAmount = cycle === "annual" ? plan.monthlyUsd * 12 * (1 - plan.discount / 100) : plan.monthlyUsd;
    const converted = await convert(usdAmount, "USD", currency);
    const reference = safeReference("payrollpro");
    const payload = { email, amount: String(minorUnits(converted, currency)), currency, reference, metadata: JSON.stringify({ product: "Payroll Pro", planCode: String(planCode).toUpperCase(), billingCycle: cycle, displayCurrency: String(displayCurrency).toUpperCase(), priceUsd: usdAmount, paystackCurrency: currency, fxQuotedAt: new Date().toISOString(), ...metadata }) };
    if (CHECKOUT_CALLBACK_URL) payload.callback_url = CHECKOUT_CALLBACK_URL;
    const result = await paystack("/transaction/initialize", { method: "POST", body: JSON.stringify(payload) });
    res.json({ reference, authorizationUrl: result.data.authorization_url, accessCode: result.data.access_code, planCode: String(planCode).toUpperCase(), billingCycle: cycle, currency, usdAmount, amount: Number(converted.toFixed(CURRENCY_DECIMALS[currency] ?? 2)), subunitAmount: Number(payload.amount) });
  } catch (error) { res.status(502).json({ error: error.message }); }
});
app.get("/api/billing/verify/:reference", async (req, res) => {
  try { const result = await paystack(`/transaction/verify/${encodeURIComponent(req.params.reference)}`); const tx = result.data; res.json({ reference: tx.reference, status: tx.status, amount: tx.amount, currency: tx.currency, paidAt: tx.paid_at, channel: tx.channel, customer: tx.customer?.email || null, verified: tx.status === "success" }); }
  catch (error) { res.status(502).json({ error: error.message, verified: false }); }
});
app.post("/api/billing/webhook", (req, res) => {
  if (!verifyWebhook(req)) return res.status(401).json({ error: "Invalid Paystack signature" });
  if (req.body?.event === "charge.success") console.log(JSON.stringify({ type: "PAYMENT_CONFIRMED", reference: req.body.data?.reference, receivedAt: new Date().toISOString() }));
  res.sendStatus(200);
});
app.use((_req, res) => res.status(404).json({ error: "Not found" }));
app.listen(PORT, "0.0.0.0", () => console.log(`Payroll Pro billing API listening on ${PORT}`));
