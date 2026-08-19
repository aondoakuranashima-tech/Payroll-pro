package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.CalculationResult
import com.example.ui.components.PayStubSummaryCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleCalc = CalculationResult(
      grossPay = 5000.00,
      federalWithholding = 650.00,
      stateWithholding = 280.00,
      socialSecurityWithholding = 310.00,
      medicareWithholding = 72.50,
      additionalMedicare = 0.0,
      totalEmployeeTaxes = 1312.50,
      preTaxDeductions = 370.00,
      postTaxDeductions = 0.00,
      netTakeHomePay = 3317.50,
      employerSocialSecurity = 310.00,
      employerMedicare = 72.50,
      employerFuta = 30.00,
      employerSuta = 135.00,
      totalEmployerTaxes = 547.50,
      totalEmployerCost = 5547.50,
      effectiveTaxRatePercent = 26.25
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        PayStubSummaryCard(calculation = sampleCalc)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
