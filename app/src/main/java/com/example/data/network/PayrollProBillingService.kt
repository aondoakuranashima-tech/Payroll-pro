package com.example.data.network

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Real Payroll Pro billing client. Secrets never enter the Android app. */
class PayrollProBillingService {
  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  suspend fun initializePayment(
    email: String,
    amount: Double,
    displayCurrency: String,
    paystackCurrency: String,
    planCode: String? = null
  ): PaymentSession = withContext(Dispatchers.IO) {
    val body = JSONObject().apply {
      put("email", email)
      put("amount", amount)
      put("displayCurrency", displayCurrency.uppercase())
      put("paystackCurrency", paystackCurrency.uppercase())
      if (planCode != null) put("planCode", planCode)
    }

    val request = Request.Builder()
      .url(BuildConfig.PAYROLL_PRO_BILLING_BASE_URL.trimEnd('/') + "/api/billing/initialize")
      .post(body.toString().toRequestBody("application/json".toMediaType()))
      .header("Accept", "application/json")
      .build()

    client.newCall(request).execute().use { response ->
      val json = JSONObject(response.body?.string().orEmpty())
      if (!response.isSuccessful) {
        throw IllegalStateException(json.optString("error", "Unable to initialize payment"))
      }
      PaymentSession(
        reference = json.getString("reference"),
        authorizationUrl = json.getString("authorizationUrl"),
        accessCode = json.optString("accessCode"),
        currency = json.getString("currency"),
        amount = json.getDouble("amount")
      )
    }
  }

  suspend fun verifyPayment(reference: String): PaymentVerification = withContext(Dispatchers.IO) {
    val request = Request.Builder()
      .url(BuildConfig.PAYROLL_PRO_BILLING_BASE_URL.trimEnd('/') + "/api/billing/verify/$reference")
      .get()
      .header("Accept", "application/json")
      .build()

    client.newCall(request).execute().use { response ->
      val json = JSONObject(response.body?.string().orEmpty())
      if (!response.isSuccessful) throw IllegalStateException(json.optString("error", "Unable to verify payment"))
      PaymentVerification(
        reference = json.getString("reference"),
        status = json.getString("status"),
        verified = json.optBoolean("verified", false),
        amount = json.optLong("amount", 0L),
        currency = json.optString("currency")
      )
    }
  }
}

data class PaymentSession(
  val reference: String,
  val authorizationUrl: String,
  val accessCode: String,
  val currency: String,
  val amount: Double
)

data class PaymentVerification(
  val reference: String,
  val status: String,
  val verified: Boolean,
  val amount: Long,
  val currency: String
)
