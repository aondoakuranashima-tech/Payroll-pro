import crypto from "node:crypto";

const json = async (url, options = {}) => {
  const response = await fetch(url, { ...options, headers: { "Content-Type": "application/json", ...(options.headers || {}) } });
  const text = await response.text();
  let data = {};
  try { data = text ? JSON.parse(text) : {}; } catch { data = { raw: text }; }
  if (!response.ok) throw new Error(data?.message || data?.error?.message || `Provider request failed (${response.status})`);
  return data;
};

const env = (key, required = false) => {
  const value = process.env[key];
  if (required && !value) throw new Error(`${key} is not configured`);
  return value;
};

export const PROVIDERS = ["paystack", "flutterwave", "dodo", "paddle", "paypal"];

export function providerConfigured(provider) {
  return {
    paystack: Boolean(process.env.PAYSTACK_SECRET_KEY),
    flutterwave: Boolean(process.env.FLW_SECRET_KEY),
    dodo: Boolean(process.env.DODO_PAYMENTS_API_KEY),
    paddle: Boolean(process.env.PADDLE_API_KEY),
    paypal: Boolean(process.env.PAYPAL_CLIENT_ID && process.env.PAYPAL_CLIENT_SECRET)
  }[provider];
}

async function paystackCreate({ email, amount, currency, reference, callbackUrl, metadata }) {
  const secret = env("PAYSTACK_SECRET_KEY", true);
  const body = { email, amount: String(amount), currency, reference, metadata: JSON.stringify(metadata || {}) };
  if (callbackUrl) body.callback_url = callbackUrl;
  const data = await json("https://api.paystack.co/transaction/initialize", { method: "POST", headers: { Authorization: `Bearer ${secret}` }, body: JSON.stringify(body) });
  return { provider: "paystack", reference, checkoutUrl: data.data.authorization_url, providerId: data.data.access_code, raw: data.data };
}

async function paystackVerify(reference) {
  const secret = env("PAYSTACK_SECRET_KEY", true);
  const data = await json(`https://api.paystack.co/transaction/verify/${encodeURIComponent(reference)}`, { headers: { Authorization: `Bearer ${secret}` } });
  return { provider: "paystack", reference: data.data.reference, status: data.data.status, amount: data.data.amount, currency: data.data.currency, channel: data.data.channel, raw: data.data };
}

async function flutterwaveCreate({ email, amount, currency, reference, callbackUrl, metadata }) {
  const secret = env("FLW_SECRET_KEY", true);
  const data = await json("https://api.flutterwave.com/v3/payments", { method: "POST", headers: { Authorization: `Bearer ${secret}` }, body: JSON.stringify({ tx_ref: reference, amount, currency, redirect_url: callbackUrl || env("FLW_CALLBACK_URL"), customer: { email }, meta: metadata || {}, customizations: { title: "Payroll Pro" } }) });
  return { provider: "flutterwave", reference, checkoutUrl: data.data.link, providerId: reference, raw: data.data };
}

async function flutterwaveVerify(reference) {
  const secret = env("FLW_SECRET_KEY", true);
  const data = await json(`https://api.flutterwave.com/v3/transactions/verify_by_reference?tx_ref=${encodeURIComponent(reference)}`, { headers: { Authorization: `Bearer ${secret}` } });
  const tx = data.data;
  return { provider: "flutterwave", reference, status: tx.status, amount: tx.amount, currency: tx.currency, channel: tx.payment_type, raw: tx };
}

async function dodoCreate({ email, amount, currency, reference, metadata }) {
  const key = env("DODO_PAYMENTS_API_KEY", true);
  const productId = env("DODO_DEFAULT_PRODUCT_ID", true);
  const base = env("DODO_API_BASE_URL") || "https://live.dodopayments.com";
  const data = await json(`${base}/checkouts`, { method: "POST", headers: { Authorization: `Bearer ${key}`, "Idempotency-Key": reference }, body: JSON.stringify({ product_cart: [{ product_id: productId, quantity: 1 }], customer: { email }, metadata: { payroll_pro_reference: reference, currency, amount: String(amount), ...(metadata || {}) } }) });
  return { provider: "dodo", reference, checkoutUrl: data.checkout_url, providerId: data.session_id, raw: data };
}

async function dodoVerify(id) {
  const key = env("DODO_PAYMENTS_API_KEY", true);
  const base = env("DODO_API_BASE_URL") || "https://live.dodopayments.com";
  const data = await json(`${base}/checkouts/${encodeURIComponent(id)}`, { headers: { Authorization: `Bearer ${key}` } });
  return { provider: "dodo", reference: data.metadata?.payroll_pro_reference || id, status: data.payment_status, amount: null, currency: null, raw: data };
}

async function paddleCreate({ email, priceId, reference }) {
  const key = env("PADDLE_API_KEY", true);
  const data = await json("https://api.paddle.com/transactions", { method: "POST", headers: { Authorization: `Bearer ${key}`, "Idempotency-Key": reference }, body: JSON.stringify({ items: [{ price_id: priceId, quantity: 1 }], custom_data: { payroll_pro_reference: reference, email }, collection_mode: "automatic", checkout: { url: env("PADDLE_CHECKOUT_URL", true) } }) });
  return { provider: "paddle", reference, checkoutUrl: data.data?.checkout?.url, providerId: data.data?.id, raw: data.data };
}

async function paypalToken() {
  const id = env("PAYPAL_CLIENT_ID", true); const secret = env("PAYPAL_CLIENT_SECRET", true);
  const base = env("PAYPAL_API_BASE_URL") || "https://api-m.paypal.com";
  const auth = Buffer.from(`${id}:${secret}`).toString("base64");
  const data = await json(`${base}/v1/oauth2/token`, { method: "POST", headers: { Authorization: `Basic ${auth}`, "Content-Type": "application/x-www-form-urlencoded" }, body: "grant_type=client_credentials" });
  return { token: data.access_token, base };
}

async function paypalCreate({ email, amount, currency, reference, callbackUrl }) {
  const { token, base } = await paypalToken();
  const data = await json(`${base}/v2/checkout/orders`, { method: "POST", headers: { Authorization: `Bearer ${token}`, "PayPal-Request-Id": reference }, body: JSON.stringify({ intent: "CAPTURE", purchase_units: [{ reference_id: reference, amount: { currency_code: currency, value: Number(amount).toFixed(2) }, custom_id: reference }], payment_source: { paypal: { experience_context: { user_action: "PAY_NOW", return_url: callbackUrl || env("PAYPAL_RETURN_URL", true), cancel_url: env("PAYPAL_CANCEL_URL", true) } } } }) });
  const approve = (data.links || []).find((link) => link.rel === "payer-action" || link.rel === "approve");
  return { provider: "paypal", reference, checkoutUrl: approve?.href, providerId: data.id, raw: data };
}

async function paypalVerify(orderId) {
  const { token, base } = await paypalToken();
  const data = await json(`${base}/v2/checkout/orders/${encodeURIComponent(orderId)}`, { headers: { Authorization: `Bearer ${token}` } });
  return { provider: "paypal", reference: data.purchase_units?.[0]?.reference_id || orderId, status: data.status, amount: data.purchase_units?.[0]?.amount?.value, currency: data.purchase_units?.[0]?.amount?.currency_code, raw: data };
}

export async function createCheckout(provider, input) {
  if (!providerConfigured(provider)) throw new Error(`${provider} is not configured`);
  if (provider === "paystack") return paystackCreate(input);
  if (provider === "flutterwave") return flutterwaveCreate(input);
  if (provider === "dodo") return dodoCreate(input);
  if (provider === "paddle") return paddleCreate(input);
  if (provider === "paypal") return paypalCreate(input);
  throw new Error(`Unsupported provider: ${provider}`);
}

export async function verifyPayment(provider, id) {
  if (provider === "paystack") return paystackVerify(id);
  if (provider === "flutterwave") return flutterwaveVerify(id);
  if (provider === "dodo") return dodoVerify(id);
  if (provider === "paypal") return paypalVerify(id);
  throw new Error(`${provider} verification is webhook/checkout based; use its provider ID and webhook for final settlement`);
}

export function verifyPaystackSignature(rawBody, signature) {
  const secret = env("PAYSTACK_WEBHOOK_SECRET") || env("PAYSTACK_SECRET_KEY");
  if (!secret || !signature) return false;
  const digest = crypto.createHmac("sha512", secret).update(rawBody).digest("hex");
  return crypto.timingSafeEqual(Buffer.from(digest), Buffer.from(String(signature)));
}

export function verifyHmacSha256(rawBody, signature, secret) {
  if (!secret || !signature) return false;
  const digest = crypto.createHmac("sha256", secret).update(rawBody).digest("hex");
  const expected = String(signature).replace(/^sha256=/, "");
  return crypto.timingSafeEqual(Buffer.from(digest), Buffer.from(expected));
}
