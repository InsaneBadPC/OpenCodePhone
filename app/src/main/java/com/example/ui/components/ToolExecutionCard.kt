package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ToolExecutionCard(
    toolName: String,
    toolArgs: String?,
    toolResult: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tool_card_${toolName}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Slate900
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = if (toolName == "webSearch") Icons.Default.TravelExplore else Icons.Default.Terminal
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CyanBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nástroj: $toolName",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanBright
                    )
                    if (!toolArgs.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(\"${toolArgs.take(24)}${if (toolArgs.length > 24) "..." else ""}\")",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950)
                        .padding(10.dp)
                ) {
                    if (!toolArgs.isNullOrBlank()) {
                        Text(
                            text = "Parametry:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                        Text(
                            text = toolArgs,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate200,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (!toolResult.isNullOrBlank()) {
                        Text(
                            text = "Výsledek / Výstup:",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldBright
                        )
                        Text(
                            text = toolResult,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate200,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
