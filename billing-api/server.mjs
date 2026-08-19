import express from "express";
import crypto from "node:crypto";

const app = express();
const PORT = Number(process.env.PORT || 10000);
const PAYSTACK_SECRET_KEY = process.env.PAYSTACK_SECRET_KEY;
const WEBHOOK_SECRET = process.env.PAYSTACK_WEBHOOK_SECRET || PAYSTACK_SECRET_KEY;
const FX_API_URL = process.env.FX_API_URL || "https://open.er-api.com/v6/latest/USD";
const DEFAULT_CURRENCY = process.env.PAYSTACK_CURRENCY || "NGN";
const CHECKOUT_CALLBACK_URL = process.env.PAYSTACK_CALLBACK_URL || "";

// Paystack only accepts currencies enabled for the merchant. For a Nigerian
// merchant, NGN is the default and USD requires international/USD activation.
const PAYSTACK_CURRENCIES = new Set(["NGN", "USD", "GHS", "KES", "ZAR", "XOF"]);
const CURRENCY_DECIMALS = { JPY: 0, KRW: 0, VND: 0, XOF: 0 };
const fxCache = new Map();
const quoteCacheMs = 5 * 60 * 1000;

app.disable("x-powered-by");
app.use(express.json({
  verify: (req, _res, buf) => { req.rawBody = Buffer.from(buf); }
}));

function requireSecret() {
  if (!PAYSTACK_SECRET_KEY) throw new Error("PAYSTACK_SECRET_KEY is not configured");
}

function minorUnits(amount, currency) {
  const decimals = CURRENCY_DECIMALS[currency] ?? 2;
  return Math.round(Number(amount) * (10 ** decimals));
}

async function paystack(path, options = {}) {
  requireSecret();
  const response = await fetch(`https://api.paystack.co${path}`, {
    ...options,
    headers: {
      Authorization: `Bearer ${PAYSTACK_SECRET_KEY}`,
      "Content-Type": "application/json",
      ...(options.headers || {})
    }
  });
  const data = await response.json();
  if (!response.ok || data.status === false) {
    throw new Error(data.message || `Paystack request failed: ${response.status}`);
  }
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
  const fromRate = from === "USD" ? 1 : rates[from];
  const toRate = to === "USD" ? 1 : rates[to];
  if (!fromRate || !toRate) throw new Error(`FX rate unavailable for ${from}/${to}`);
  return Number(amount) * (toRate / fromRate);
}

function safeReference(prefix = "pp") {
  return `${prefix}_${Date.now()}_${crypto.randomBytes(5).toString("hex")}`;
}

function verifyWebhook(req) {
  const signature = req.headers["x-paystack-signature"];
  if (!signature || !WEBHOOK_SECRET || !req.rawBody) return false;
  const digest = crypto.createHmac("sha512", WEBHOOK_SECRET).update(req.rawBody).digest("hex");
  return crypto.timingSafeEqual(Buffer.from(digest), Buffer.from(String(signature)));
}

app.get("/health", (_req, res) => res.json({ ok: true, service: "payroll-pro-billing" }));

app.get("/api/billing/currencies", (_req, res) => {
  res.json({
    displayCurrencies: ["NGN", "USD", "EUR", "GBP", "CAD", "AUD", "JPY", "CNY", "INR", "BRL", "ZAR", "KES", "GHS", "XOF"],
    paystackCurrencies: [...PAYSTACK_CURRENCIES],
    defaultCurrency: DEFAULT_CURRENCY
  });
});

app.get("/api/billing/fx-quote", async (req, res) => {
  try {
    const amount = Number(req.query.amount);
    const from = String(req.query.from || "USD").toUpperCase();
    const requestedTo = String(req.query.to || DEFAULT_CURRENCY).toUpperCase();
    if (!Number.isFinite(amount) || amount <= 0) return res.status(400).json({ error: "amount must be positive" });
    if (!PAYSTACK_CURRENCIES.has(requestedTo)) {
      return res.status(400).json({ error: `Paystack is not configured for ${requestedTo}; choose a supported merchant currency.` });
    }
    const converted = await convert(amount, from, requestedTo);
    res.json({
      from, to: requestedTo, inputAmount: amount,
      convertedAmount: Number(converted.toFixed(CURRENCY_DECIMALS[requestedTo] ?? 2)),
      paystackSubunitAmount: minorUnits(converted, requestedTo),
      quotedAt: new Date().toISOString(),
      expiresInSeconds: Math.floor(quoteCacheMs / 1000)
    });
  } catch (error) {
    res.status(503).json({ error: error.message });
  }
});

app.post("/api/billing/initialize", async (req, res) => {
  try {
    const { email, amount, displayCurrency = "USD", paystackCurrency = DEFAULT_CURRENCY, planCode, metadata = {} } = req.body || {};
    if (!email || !Number.isFinite(Number(amount)) || Number(amount) <= 0) return res.status(400).json({ error: "email and positive amount are required" });
    const currency = String(paystackCurrency).toUpperCase();
    if (!PAYSTACK_CURRENCIES.has(currency)) return res.status(400).json({ error: `Unsupported Paystack currency: ${currency}` });

    const converted = await convert(Number(amount), String(displayCurrency).toUpperCase(), currency);
    const reference = safeReference("payrollpro");
    const payload = {
      email,
      amount: String(minorUnits(converted, currency)),
      currency,
      reference,
      metadata: JSON.stringify({
        product: "Payroll Pro",
        displayCurrency: String(displayCurrency).toUpperCase(),
        displayAmount: Number(amount),
        paystackCurrency: currency,
        fxQuotedAt: new Date().toISOString(),
        ...metadata
      })
    };
    if (planCode) payload.plan = planCode;
    if (CHECKOUT_CALLBACK_URL) payload.callback_url = CHECKOUT_CALLBACK_URL;

    const result = await paystack("/transaction/initialize", { method: "POST", body: JSON.stringify(payload) });
    res.json({
      reference,
      authorizationUrl: result.data.authorization_url,
      accessCode: result.data.access_code,
      currency,
      amount: Number(converted.toFixed(CURRENCY_DECIMALS[currency] ?? 2)),
      subunitAmount: Number(payload.amount)
    });
  } catch (error) {
    res.status(502).json({ error: error.message });
  }
});

app.get("/api/billing/verify/:reference", async (req, res) => {
  try {
    const result = await paystack(`/transaction/verify/${encodeURIComponent(req.params.reference)}`);
    const tx = result.data;
    res.json({
      reference: tx.reference,
      status: tx.status,
      amount: tx.amount,
      currency: tx.currency,
      paidAt: tx.paid_at,
      channel: tx.channel,
      customer: tx.customer?.email || null,
      verified: tx.status === "success"
    });
  } catch (error) {
    res.status(502).json({ error: error.message, verified: false });
  }
});

app.post("/api/billing/webhook", (req, res) => {
  if (!verifyWebhook(req)) return res.status(401).json({ error: "Invalid Paystack signature" });

  // Acknowledge quickly. Production subscription state should be persisted
  // idempotently by reference/event ID before granting access.
  const event = req.body || {};
  if (event.event === "charge.success") {
    const reference = event.data?.reference;
    console.log(JSON.stringify({ type: "PAYMENT_CONFIRMED", reference, receivedAt: new Date().toISOString() }));
  }
  res.sendStatus(200);
});

app.use((_req, res) => res.status(404).json({ error: "Not found" }));

app.listen(PORT, "0.0.0.0", () => console.log(`Payroll Pro billing API listening on ${PORT}`));
