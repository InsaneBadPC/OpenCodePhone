package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.SessionMarkdownArchive
import com.example.data.models.SessionSearchResult
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*

@Composable
fun SessionMarkdownArchiveView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val archives by viewModel.sessionArchives.collectAsState()
    val searchQuery by viewModel.sessionSearchQuery.collectAsState()
    val searchResults by viewModel.sessionSearchResults.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedArchiveForReader by remember { mutableStateOf<SessionMarkdownArchive?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
    ) {
        // Hero Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanBright.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = CyanBright)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Session .MD Archiv & Paměť",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Text(
                                text = "Kompletní historie relací s fulltextovým prohledáváním",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.saveCurrentSessionToMarkdown { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Uložit do .MD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Všechny zprávy, myšlenky modelu, volání nástrojů a systémové výstupy jsou ukládány do strukturovaných Markdown souborů. Agent i vývojář v nich mohou kdykoliv vyhledat ztracené detaily či architekturu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate300,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search in Markdown Sessions Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchSessionHistory(it) },
            placeholder = { Text("Vyhledat v .MD historii (např. oracle, supabase, opencode.db, error)...", fontSize = 12.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = CyanBright)
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.searchSessionHistory("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Vymazat", tint = Slate400)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate900,
                unfocusedContainerColor = Slate900,
                focusedBorderColor = CyanBright,
                unfocusedBorderColor = Slate800,
                focusedTextColor = Slate100,
                unfocusedTextColor = Slate200
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // If search query is active, show search results
        if (searchQuery.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Slate900,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nalezeno ${searchResults.size} shod v .MD archivech relací",
                        fontSize = 12.sp,
                        color = AmberWarning,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Žádné výsledky odpovídající zadanému dotazu.", color = Slate400, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(searchResults) { result ->
                        SearchResultItemCard(
                            result = result,
                            onInject = {
                                val archive = archives.find { it.id == result.sessionId }
                                if (archive != null) {
                                    viewModel.injectSessionContextIntoChat(archive) {
                                        Toast.makeText(context, "Kontext z řádku ${result.matchedLine} byl předán do aktivního chatu!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        } else {
            // Show all session archives
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Uložené Markdown relace (${archives.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate300
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(archives, key = { it.id }) { archive ->
                    SessionArchiveCard(
                        archive = archive,
                        onRead = { selectedArchiveForReader = archive },
                        onInject = {
                            viewModel.injectSessionContextIntoChat(archive) {
                                Toast.makeText(context, "🧠 Paměť z relace '${archive.title}' načtena do chatu!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(archive.markdownContent))
                            Toast.makeText(context, "Markdown zkopírován do schránky", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Markdown Reader Dialog
    selectedArchiveForReader?.let { archive ->
        AlertDialog(
            onDismissRequest = { selectedArchiveForReader = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Article, contentDescription = null, tint = CyanBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(archive.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate100)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Soubor: ${archive.filePath} (${archive.wordCount} slov)",
                        fontSize = 11.sp,
                        color = Slate400,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF090D16),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                    ) {
                        Text(
                            text = archive.markdownContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Slate200,
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(archive.markdownContent))
                        Toast.makeText(context, "Markdown export zkopírován", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kopírovat .MD")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedArchiveForReader = null }) {
                    Text("Zavřít", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}

@Composable
private fun SearchResultItemCard(
    result: SessionSearchResult,
    onInject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.sessionTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanBright
                )
                Text(
                    text = "Řádek ${result.matchedLine}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AmberWarning
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Slate950,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = result.context,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Slate200,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FilledTonalButton(
                    onClick = onInject,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Slate800, contentColor = CyanBright),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Předat kontext agentovi", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun SessionArchiveCard(
    archive: SessionMarkdownArchive,
    onRead: () -> Unit,
    onInject: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = archive.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = archive.filePath,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Slate800
                ) {
                    Text(
                        text = "${archive.wordCount} slov",
                        fontSize = 10.sp,
                        color = Slate300,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Slate950,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = archive.markdownContent.take(160) + "...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Slate300,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vytvořeno: ${archive.timestamp}",
                    fontSize = 11.sp,
                    color = Slate500
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Kopírovat", tint = Slate400, modifier = Modifier.size(16.dp))
                    }

                    OutlinedButton(
                        onClick = onRead,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Slate300, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Číst", fontSize = 11.sp, color = Slate300)
                    }

                    Button(
                        onClick = onInject,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Načíst do chatu", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
