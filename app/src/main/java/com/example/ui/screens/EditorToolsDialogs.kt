package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CodeSnippet
import com.example.data.models.GlobalSearchResult
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*

// =======================================================
// AI CODE ACTIONS DIALOG
// =======================================================
@Composable
fun AiCodeActionDialog(
    title: String,
    explanation: String,
    proposedCode: String,
    onApply: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanBright)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, color = Slate100, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    color = Slate950,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate200,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                if (proposedCode.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Navržený kód:",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyanBright,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Slate950,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = proposedCode,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Slate100,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (proposedCode.isNotBlank()) {
                    Button(
                        onClick = {
                            onApply(proposedCode)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Použít do editoru", fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Zavřít")
                }
            }
        },
        containerColor = Slate900
    )
}

// =======================================================
// CODE SNIPPETS DIALOG
// =======================================================
@Composable
fun SnippetsDialog(
    viewModel: OpenCodeViewModel,
    onDismiss: () -> Unit
) {
    val snippets by viewModel.codeSnippets.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = CyanBright)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Knihovna šablon a snippetů", fontWeight = FontWeight.Bold, color = Slate100)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(snippets, key = { it.id }) { snippet ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate950),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Slate800),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(snippet.title, fontWeight = FontWeight.Bold, color = Slate100, fontSize = 13.sp)
                                        Text("${snippet.category} • ${snippet.language}", color = CyanBright, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.insertSnippetIntoCurrentEditor(snippet)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Vložit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(snippet.description, color = Slate400, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Slate200)
            ) {
                Text("Zavřít")
            }
        },
        containerColor = Slate900
    )
}

// =======================================================
// GLOBAL SEARCH & GREP DIALOG
// =======================================================
@Composable
fun GlobalSearchDialog(
    viewModel: OpenCodeViewModel,
    onDismiss: () -> Unit
) {
    val query by viewModel.globalSearchQuery.collectAsState()
    val results by viewModel.globalSearchResults.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = CyanBright)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Globální vyhledávání v projektu", fontWeight = FontWeight.Bold, color = Slate100)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.searchGlobally(it) },
                    placeholder = { Text("Hledat text v kódu (např. fun, import, Room)...", color = Slate500, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanBright,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate200,
                        focusedContainerColor = Slate950,
                        unfocusedContainerColor = Slate950
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Nalezeno výsledků: ${results.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(results) { res ->
                        Surface(
                            color = Slate950,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Slate800),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.openSearchResult(res)
                                    onDismiss()
                                }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = res.fileName,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanBright,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Řádek ${res.lineNumber}",
                                        color = Slate400,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = res.lineContent,
                                    color = Slate200,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Slate200)
            ) {
                Text("Zavřít")
            }
        },
        containerColor = Slate900
    )
}

// =======================================================
// MARKDOWN / HTML LIVE PREVIEW COMPONENT
// =======================================================
@Composable
fun MarkdownLivePreview(
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val lines = content.lines()
        lines.forEach { line ->
            when {
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# "),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate200,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Row(modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)) {
                        Text("•", color = CyanBright, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = line.substring(2),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate200
                        )
                    }
                }
                line.startsWith("> ") -> {
                    Surface(
                        color = Slate900,
                        border = BorderStroke(1.dp, Slate700),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = line.removePrefix("> "),
                            color = Slate300,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                line.startsWith("```") -> {
                    // Code block fence
                    Divider(color = Slate800, modifier = Modifier.padding(vertical = 4.dp))
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                else -> {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate300,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}
