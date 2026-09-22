package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun InternetScreen(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchSummary by viewModel.searchSummary.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Web Search, 1 = URL Inspector
    var urlToInspect by remember { mutableStateOf("https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary") }
    var fetchedContent by remember { mutableStateOf("") }
    var isFetchingUrl by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Tab Header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Slate900,
            contentColor = CyanBright
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🤖 Agent Prohlížeč", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("🌐 Dokumentace", fontSize = 11.sp) },
                icon = { Icon(Icons.Default.TravelExplore, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("🔗 URL Inspektor", fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Http, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        when (selectedTab) {
            0 -> {
                AgentBrowserView(viewModel = viewModel)
            }
            1 -> {
                // Web Search View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Hledat API dokumentaci, balíčky, specifikace...", color = Slate400) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("web_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900,
                            focusedBorderColor = CyanBright,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Slate400)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.performWebSearch() },
                        enabled = searchQuery.isNotBlank() && !isSearching,
                        modifier = Modifier.height(56.dp).testTag("web_search_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Slate950, strokeWidth = 2.dp)
                        } else {
                            Text("Hledat", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Query suggestions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 10.dp)
                ) {
                    val presets = listOf(
                        "Jetpack Compose WindowInsets",
                        "Room Database KSP",
                        "Model Context Protocol Spec",
                        "Kotlin Flow collectAsState",
                        "Android Material 3 Navigation"
                    )
                    presets.forEach { preset ->
                        SuggestionChip(
                            onClick = {
                                viewModel.updateSearchQuery(preset)
                                viewModel.performWebSearch(preset)
                            },
                            label = { Text(preset, fontSize = 11.sp) },
                            modifier = Modifier.padding(end = 6.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Slate900,
                                labelColor = Slate200
                            )
                        )
                    }
                }

                // Summary Card
                if (searchSummary.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanBright, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Shrnutí vyhledávání z webu", style = MaterialTheme.typography.labelMedium, color = CyanBright, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(searchSummary, style = MaterialTheme.typography.bodySmall, color = Slate200)
                        }
                    }
                }

                // Results list
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchResults) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate900)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanBright
                                )
                                if (item.url.isNotBlank()) {
                                    Text(
                                        text = item.url,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = item.snippet,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate200,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("URL", item.url))
                                            Toast.makeText(context, "Odkaz byl zkopírován", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Kopírovat odkaz", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.selectTab(0) // Switch to chat
                                            viewModel.sendMessage("Vysvětli mi podrobně toto téma s ukázkou kódu: ${item.title} (${item.url})")
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Slate800, contentColor = CyanBright)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Zeptat se Zen AI", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (searchResults.isEmpty() && !isSearching) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.TravelExplore, contentDescription = null, tint = Slate700, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Zadejte hledaný výraz pro vyhledání dokumentace a knihoven", color = Slate400, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        2 -> {
            // URL Inspector View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = urlToInspect,
                    onValueChange = { urlToInspect = it },
                    label = { Text("Cílová URL adresa") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900,
                        focusedBorderColor = CyanBright,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isFetchingUrl = true
                            fetchedContent = "Načítám webový obsah z $urlToInspect..."
                            coroutineScope.launch(Dispatchers.IO) {
                                val content = viewModel.repository.webSearchService.fetchUrlContent(urlToInspect)
                                fetchedContent = content
                                isFetchingUrl = false
                            }
                        },
                        enabled = urlToInspect.isNotBlank() && !isFetchingUrl,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                    ) {
                        if (isFetchingUrl) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Slate950)
                        } else {
                            Text("Načíst obsah URL")
                        }
                    }

                    if (fetchedContent.isNotBlank()) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.selectTab(0)
                                viewModel.sendMessage("Analyzuj tento webový obsah z $urlToInspect:\n```\n${fetchedContent.take(1500)}\n```")
                            }
                        ) {
                            Text("Odeslat do chatu", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate900,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (fetchedContent.isBlank()) "Zde se zobrazí náhled staženého obsahu webu..." else fetchedContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (fetchedContent.isBlank()) Slate600 else Slate200,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}
}
