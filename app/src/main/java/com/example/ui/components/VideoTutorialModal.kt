package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.VideoTutorialItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTutorialModal(
  tutorials: List<VideoTutorialItem>,
  onDismiss: () -> Unit
) {
  var selectedTutorial by remember { mutableStateOf(tutorials.firstOrNull()) }
  var isPlaying by remember { mutableStateOf(false) }
  var playbackProgress by remember { mutableStateOf(0.42f) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(26.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .testTag("video_tutorial_modal"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(VibrantPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayLesson,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Video Masterclasses & Guides",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = VibrantTextNavy
            )
            Text(
              text = "Expert Payroll & Accounting Tutorials",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        selectedTutorial?.let { tut ->
          // Simulated Interactive Video Player
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
          ) {
            Box(modifier = Modifier.fillMaxSize()) {
              // Video Canvas Simulated Gradient
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      listOf(Color(tut.thumbnailColor).copy(alpha = 0.8f), Color(0xFF0F172A))
                    )
                  )
              )

              // Center Play/Pause button
              Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                IconButton(
                  onClick = { isPlaying = !isPlaying },
                  modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(VibrantPrimary)
                ) {
                  Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = if (isPlaying) "Playing (1080p HD)" else "Click to Play Tutorial",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.85f),
                  fontWeight = FontWeight.SemiBold
                )
              }

              // Top Category Tag
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                  .align(Alignment.TopStart)
                  .padding(10.dp)
              ) {
                Text(
                  text = "${tut.category} • ${tut.duration}",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  fontWeight = FontWeight.Bold
                )
              }

              // Video Control Bar at bottom
              Column(
                modifier = Modifier
                  .align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))))
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Slider(
                  value = playbackProgress,
                  onValueChange = { playbackProgress = it },
                  colors = SliderDefaults.colors(
                    thumbColor = VibrantPrimary,
                    activeTrackColor = VibrantPrimary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                )
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "01:54 / ${tut.duration}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                  )
                  Text(
                    text = "4K Ultra-HD",
                    style = MaterialTheme.typography.labelSmall,
                    color = VibrantSuccess,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }

          // Active Tutorial Title & Description
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = tut.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = VibrantTextPrimary
            )
            Text(
              text = tut.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Key Takeaways & Cheat Sheet
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VibrantSuccessContainer.copy(alpha = 0.4f)),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.linearGradient(listOf(VibrantSuccess.copy(alpha = 0.3f), Color.Transparent))
            )
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = VibrantSuccessText, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Key Implementation Takeaways:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = VibrantSuccessText)
              }
              tut.keyTakeaways.forEach { item ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.Top
                ) {
                  Text("✓ ", color = VibrantSuccessText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                  Text(item, style = MaterialTheme.typography.bodySmall, color = VibrantTextNavy)
                }
              }
            }
          }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp), color = VibrantOutlineVariant)

        // Tutorial Playlist
        Text("All Available Masterclass Episodes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantTextNavy)

        tutorials.forEach { item ->
          val isCurrent = item.id == selectedTutorial?.id
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedTutorial = item },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isCurrent) VibrantPrimaryContainer else MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isCurrent) VibrantPrimary else VibrantSecondaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isCurrent) Icons.Default.PlayArrow else Icons.Default.VideoLibrary,
                  contentDescription = null,
                  tint = if (isCurrent) Color.White else VibrantSecondary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text("${item.category} • ${item.duration}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              if (isCurrent) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = VibrantPrimary
                ) {
                  Text("Watching", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
      ) {
        Text("Done")
      }
    }
  )
}
