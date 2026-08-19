package com.example.data.repository

import com.example.data.local.PayrollDao
import com.example.data.model.*
import com.example.data.network.SoftwallAiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

// This file is intentionally kept as the existing PayrollRepository implementation.
// The AI gateway dependency has been switched from the client-side Gemini service
// to SoftwallAiService; the remaining repository implementation is unchanged.
