package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.LaborMarketIndexItem
import com.example.data.model.MarketAnalysisSnippet
import com.example.data.model.MessageSender
import com.example.ui.theme.*
import com.example.ui.viewmodel.PayrollViewModel
import kotlinx.coroutines.launch

enum class CopilotTab(val title: String) {
  AI_CHAT("Live AI Tax Copilot"),
  MARKET_ANALYSIS("Global Labor & FX Index")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCopilotMarketAnalysisScreen(
  viewModel: PayrollViewModel,
  onNavigateToCalculator: () -> Unit = {}
) {
  var selectedTab by remember { mutableStateOf(CopilotTab.AI_CHAT) }
  val chatMessages by viewModel.chatMessages.collectAsState()
  val isAiThinking by viewModel.isAiThinking.collectAsState()
  var inputPrompt by remember { mutableStateOf("") }
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  // Auto-scroll chat to bottom on new messages
  LaunchedEffect(chatMessages.size, isAiThinking) {
    if (chatMessages.isNotEmpty()) {
      listState.animateScrollToItem(chatMessages.size - 1)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("ai_copilot_screen")
  ) {
    // Header & Tab Selector
    Surface(
      shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 3.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  Brush.linearGradient(
                    listOf(Color(0xFF6366F1), Color(0xFF9333EA))
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Enterprise Global AI Copilot",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = VibrantTextNavy
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Color(0xFFEEF2FF)
                ) {
                  Text(
                    text = "Gemini 3.5 Flash",
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4F46E5)
                  )
                }
              }
              Text(
                text = "Statutory Tax Laws • VAT/VAS • WPS • FX Hedging • Market Benchmark",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.5.sp
              )
            }
          }

          if (selectedTab == CopilotTab.AI_CHAT) {
            IconButton(
              onClick = { viewModel.clearChatHistory() },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Clear Chat",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        // Tab Selector Row
        TabRow(
          selectedTabIndex = selectedTab.ordinal,
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
        ) {
          CopilotTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            Tab(
              selected = isSelected,
              onClick = { selectedTab = tab },
              text = {
                Text(
                  text = tab.title,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 12.sp,
                  color = if (isSelected) VibrantPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            )
          }
        }
      }
    }

    // Body content based on tab
    when (selectedTab) {
      CopilotTab.AI_CHAT -> {
        AiChatContent(
          chatMessages = chatMessages,
          isAiThinking = isAiThinking,
          inputPrompt = inputPrompt,
          onInputChange = { inputPrompt = it },
          onSendMessage = { prompt ->
            viewModel.sendAiChatMessage(prompt)
            inputPrompt = ""
          },
          listState = listState
        )
      }
      CopilotTab.MARKET_ANALYSIS -> {
        LaborMarketAnalysisContent(
          markets = viewModel.getLaborMarketIndex(),
          onAskAiAboutCountry = { country ->
            selectedTab = CopilotTab.AI_CHAT
            viewModel.sendAiChatMessage("Provide a full statutory payroll, tax deduction, VAT/VAS, and employer cost breakdown for hiring in $country.")
          }
        )
      }
    }
  }
}

@Composable
fun AiChatContent(
  chatMessages: List<ChatMessage>,
  isAiThinking: Boolean,
  inputPrompt: String,
  onInputChange: (String) -> Unit,
  onSendMessage: (String) -> Unit,
  listState: androidx.compose.foundation.lazy.LazyListState
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    // Chat messages list
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(vertical = 8.dp)
    ) {
      items(chatMessages, key = { it.id }) { message ->
        ChatMessageBubble(
          message = message,
          onPromptClick = { prompt -> onSendMessage(prompt) }
        )
      }

      if (isAiThinking) {
        item {
          AiThinkingBubble()
        }
      }
    }

    // Suggested Quick Chips (if available from last message)
    val lastSuggestions = chatMessages.lastOrNull()?.suggestedPrompts
    if (!lastSuggestions.isNullOrEmpty() && !isAiThinking) {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(lastSuggestions) { suggestion ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF3E8FF),
            border = BorderStroke(1.dp, Color(0xFFD8B4FE)),
            modifier = Modifier.clickable { onSendMessage(suggestion) }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = suggestion,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7E22CE),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }
    }

    // Input Bar
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 4.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp, bottom = 6.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = inputPrompt,
          onValueChange = onInputChange,
          placeholder = {
            Text(
              "Ask about global tax laws, VAT, WPS, or salary benchmark...",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 12.sp
            )
          },
          modifier = Modifier
            .weight(1f)
            .testTag("ai_chat_input"),
          maxLines = 3,
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
          )
        )

        IconButton(
          onClick = {
            if (inputPrompt.isNotBlank() && !isAiThinking) {
              onSendMessage(inputPrompt)
            }
          },
          enabled = inputPrompt.isNotBlank() && !isAiThinking,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
              if (inputPrompt.isNotBlank() && !isAiThinking) VibrantPrimary else MaterialTheme.colorScheme.surfaceVariant
            )
            .testTag("ai_send_button")
        ) {
          Icon(
            imageVector = Icons.Default.Send,
            contentDescription = "Send",
            tint = if (inputPrompt.isNotBlank() && !isAiThinking) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ChatMessageBubble(
  message: ChatMessage,
  onPromptClick: (String) -> Unit
) {
  val isUser = message.sender == MessageSender.USER

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(0.92f),
      horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
      verticalAlignment = Alignment.Top
    ) {
      if (!isUser) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                listOf(Color(0xFF6366F1), Color(0xFF9333EA))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
      }

      Surface(
        shape = RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (isUser) 16.dp else 4.dp,
          bottomEnd = if (isUser) 4.dp else 16.dp
        ),
        color = if (isUser) VibrantPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = message.text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            lineHeight = 19.sp
          )

          // Market Analysis Snippet Card if attached
          message.marketSnippet?.let { snippet ->
            Spacer(modifier = Modifier.height(8.dp))
            MarketSnippetCard(snippet = snippet)
          }
        }
      }
    }
  }
}

@Composable
fun MarketSnippetCard(snippet: MarketAnalysisSnippet) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${snippet.country} (${snippet.continent})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = VibrantPrimary
          )
        }
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = VibrantSuccessContainer
        ) {
          Text(
            text = "Overhead: ${snippet.statutoryEmployerBurdenPercent}%",
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = VibrantSuccessText,
            fontSize = 9.sp
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Median Tech Salary: $${"%,.0f".format(snippet.medianSalaryUsd)} USD", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
        Text("VAT/VAS: ${snippet.vatVasRatePercent}%", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
      }

      Text(
        text = "Clearing: ${snippet.nationalClearingRail}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.5.sp
      )
    }
  }
}

@Composable
fun AiThinkingBubble() {
  Row(
    modifier = Modifier.padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(30.dp)
        .clip(CircleShape)
        .background(Color(0xFF8B5CF6)),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
    Spacer(modifier = Modifier.width(8.dp))
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(14.dp),
          strokeWidth = 2.dp,
          color = Color(0xFF8B5CF6)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Consulting Global Sovereign Tax Models & Gemini 3.5...",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }
  }
}

@Composable
fun LaborMarketAnalysisContent(
  markets: List<LaborMarketIndexItem>,
  onAskAiAboutCountry: (String) -> Unit
) {
  var selectedContinentFilter by remember { mutableStateOf("All") }
  val continents = remember {
    listOf("All", "North America", "Europe", "MENA", "Pacific", "Asia", "South America", "Africa")
  }

  val filteredMarkets = remember(selectedContinentFilter, markets) {
    if (selectedContinentFilter == "All") markets
    else markets.filter { it.continent.equals(selectedContinentFilter, ignoreCase = true) }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Continents filter carousel
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(continents) { continent ->
          val isSelected = selectedContinentFilter == continent
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) VibrantPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.clickable { selectedContinentFilter = continent }
          ) {
            Text(
              text = continent,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = VibrantPrimaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.TrendingUp, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Comparative 2026 Sovereign Tax, Social Charges & Mandatory Employer Burden Index",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = VibrantOnPrimaryContainer,
            fontSize = 11.sp
          )
        }
      }
    }

    items(filteredMarkets) { item ->
      MarketCountryCard(item = item, onAskAi = { onAskAiAboutCountry(item.country) })
    }
  }
}

@Composable
fun MarketCountryCard(
  item: LaborMarketIndexItem,
  onAskAi: () -> Unit
) {
  var isExpanded by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    shadowElevation = 1.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = item.flagEmoji, fontSize = 26.sp)
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(text = item.country, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
              ) {
                Text(
                  text = "${item.currencyCode} (${item.currencySymbol})",
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.5.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Text(
              text = "${item.continent} • Clearing: ${item.localClearingSystem}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.5.sp
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (item.employerOverheadPercent > 20.0) VibrantErrorContainer.copy(alpha = 0.6f) else VibrantSuccessContainer
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = "+${item.employerOverheadPercent}%",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (item.employerOverheadPercent > 20.0) VibrantError else VibrantSuccessText
            )
            Text(
              text = "Employer Burden",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 8.5.sp,
              color = if (item.employerOverheadPercent > 20.0) VibrantError else VibrantSuccessText
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

      // 3 Mini Metric Badges
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("Median Tech Salary", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.5.sp)
          Text("$${"%,.0f".format(item.medianDevSalaryAnnualUsd)} / yr", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = VibrantPrimary)
        }
        Column {
          Text("Personal Income Tax", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.5.sp)
          Text(item.personalIncomeTaxRange.take(18) + if (item.personalIncomeTaxRange.length > 18) "..." else "", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
        Column {
          Text("VAT / VAS Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.5.sp)
          Text("${item.vatGstVasPercent}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = VibrantTextNavy)
        }
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
              Text("• Pension Mandate: ${item.pensionMandate}", style = MaterialTheme.typography.labelSmall, fontSize = 10.5.sp)
              Text("• Health & Benefits: ${item.healthCareMandate}", style = MaterialTheme.typography.labelSmall, fontSize = 10.5.sp)
              Text("• Severance / Notice: ${item.severanceNoticeStandard}", style = MaterialTheme.typography.labelSmall, fontSize = 10.5.sp)
              Text("• Statutory Highlights: ${item.laborLawHighlights}", style = MaterialTheme.typography.labelSmall, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary)
            }
          }
        }
      }

      // Actions Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = { isExpanded = !isExpanded },
          contentPadding = PaddingValues(0.dp)
        ) {
          Text(
            text = if (isExpanded) "Hide Full Law Breakdown" else "View Full Statutory Details",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp
          )
        }

        Button(
          onClick = onAskAi,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = VibrantSecondary
          ),
          modifier = Modifier.height(30.dp)
        ) {
          Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Ask AI Copilot", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
