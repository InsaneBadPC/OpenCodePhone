package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.WorkspaceFileEntity
import com.example.ui.OpenCodeViewModel
import com.example.ui.components.NewFileDialog
import com.example.ui.theme.*

@Composable
fun FilesScreen(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val files by viewModel.workspaceFiles.collectAsState()
    val openedFile by viewModel.openedFile.collectAsState()
    val editorContent by viewModel.editorContent.collectAsState()
    val isFileSaved by viewModel.isFileSaved.collectAsState()
    val context = LocalContext.current

    var showNewFileDialog by remember { mutableStateOf(false) }
    var isFileListExpanded by remember { mutableStateOf(true) }
    var showSnippetsDialog by remember { mutableStateOf(false) }
    var showGlobalSearchDialog by remember { mutableStateOf(false) }
    var showAiActionDialog by remember { mutableStateOf(false) }
    var aiActionTitle by remember { mutableStateOf("") }
    var aiActionExplanation by remember { mutableStateOf("") }
    var aiActionCode by remember { mutableStateOf("") }
    var showActionMenu by remember { mutableStateOf(false) }

    val isLivePreview by viewModel.isLivePreviewActive.collectAsState()

    if (showSnippetsDialog) {
        SnippetsDialog(viewModel = viewModel, onDismiss = { showSnippetsDialog = false })
    }

    if (showGlobalSearchDialog) {
        GlobalSearchDialog(viewModel = viewModel, onDismiss = { showGlobalSearchDialog = false })
    }

    if (showAiActionDialog) {
        AiCodeActionDialog(
            title = aiActionTitle,
            explanation = aiActionExplanation,
            proposedCode = aiActionCode,
            onApply = { newCode ->
                viewModel.updateEditorContent(newCode)
                Toast.makeText(context, "Kód byl aktualizován v editoru", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAiActionDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Workspace Header
        Surface(
            color = Slate900,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = null,
                        tint = CyanBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Workspace (${files.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showGlobalSearchDialog = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Hledat v projektu",
                            tint = CyanBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showSnippetsDialog = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Šablony",
                            tint = Slate300,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showNewFileDialog = true },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("create_new_file_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "Nový soubor",
                            tint = CyanBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isFileListExpanded = !isFileListExpanded },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isFileListExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                            contentDescription = "Přepnout zobrazení souborů",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Expandable File List Horizontal Strip
        if (isFileListExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                files.forEach { file ->
                    val isSelected = file.path == openedFile?.path
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Slate800 else Slate950,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanBright) else null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { viewModel.openFile(file) }
                            .testTag("file_chip_${file.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (file.language) {
                                "kotlin" -> Icons.Default.Code
                                "python" -> Icons.Default.Terminal
                                "json" -> Icons.Default.DataObject
                                "sql" -> Icons.Default.Storage
                                else -> Icons.Default.Description
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) CyanBright else Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Slate100 else Slate400,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (file.gitStatus != "unmodified") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (file.gitStatus == "new") EmeraldBright else AmberWarning, androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider(color = Slate800, thickness = 1.dp)

        // Editor View
        if (openedFile != null) {
            val file = openedFile!!
            // File Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = file.path,
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanBright,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text(file.language.uppercase(), fontSize = 10.sp) },
                        modifier = Modifier.height(24.dp),
                        colors = AssistChipDefaults.assistChipColors(containerColor = Slate800, labelColor = Slate200)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // AI Actions Dropdown
                    Box {
                        IconButton(
                            onClick = { showActionMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Zen AI Akce",
                                tint = CyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showActionMenu,
                            onDismissRequest = { showActionMenu = false },
                            modifier = Modifier.background(Slate900)
                        ) {
                            DropdownMenuItem(
                                text = { Text("⚡ Refaktorovat a zjednodušit", color = Slate100, fontSize = 13.sp) },
                                onClick = {
                                    showActionMenu = false
                                    viewModel.executeCodeAction("REFACTOR", editorContent, file.language) { t, exp, code ->
                                        aiActionTitle = t
                                        aiActionExplanation = exp
                                        aiActionCode = code
                                        showAiActionDialog = true
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = CyanBright, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("💡 Vysvětlit tento kód", color = Slate100, fontSize = 13.sp) },
                                onClick = {
                                    showActionMenu = false
                                    viewModel.executeCodeAction("EXPLAIN", editorContent, file.language) { t, exp, code ->
                                        aiActionTitle = t
                                        aiActionExplanation = exp
                                        aiActionCode = ""
                                        showAiActionDialog = true
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("🛡️ Bezpečnostní audit kódu", color = Slate100, fontSize = 13.sp) },
                                onClick = {
                                    showActionMenu = false
                                    viewModel.executeCodeAction("SECURITY_AUDIT", editorContent, file.language) { t, exp, code ->
                                        aiActionTitle = t
                                        aiActionExplanation = exp
                                        aiActionCode = ""
                                        showAiActionDialog = true
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldBright, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("🧪 Vygenerovat Unit testy", color = Slate100, fontSize = 13.sp) },
                                onClick = {
                                    showActionMenu = false
                                    viewModel.executeCodeAction("GENERATE_TESTS", editorContent, file.language) { t, exp, code ->
                                        aiActionTitle = t
                                        aiActionExplanation = exp
                                        aiActionCode = code
                                        showAiActionDialog = true
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Science, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }

                    // Live Preview Toggle
                    IconButton(
                        onClick = { viewModel.toggleLivePreview() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isLivePreview) Icons.Default.Edit else Icons.Default.Visibility,
                            contentDescription = "Přepnout náhled",
                            tint = if (isLivePreview) CyanBright else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Run script in terminal if runnable
                    if (file.language == "python" || file.path.endsWith(".sh")) {
                        IconButton(
                            onClick = {
                                viewModel.runTerminalCommand("python ${file.path}")
                                Toast.makeText(context, "Spouštím v terminálu...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Spustit",
                                tint = EmeraldBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Save Button
                    FilledTonalButton(
                        onClick = {
                            viewModel.saveCurrentFile()
                            Toast.makeText(context, "Soubor ${file.name} byl uložen", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("save_file_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isFileSaved) Slate800 else CyanBright,
                            contentColor = if (isFileSaved) Slate400 else Slate950
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFileSaved) "Uloženo" else "Uložit", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { viewModel.deleteFile(file.path) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Smazat soubor",
                            tint = RoseError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (isLivePreview) {
                // Live Markdown / Document Preview
                MarkdownLivePreview(
                    content = editorContent,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            } else {
                // Monospace Editor with Line Numbers
                val lineCount = remember(editorContent) { editorContent.lines().size.coerceAtLeast(1) }
                val lineNumbersString = remember(lineCount) {
                    (1..lineCount).joinToString("\n") { it.toString() }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Slate950)
                ) {
                    // Line numbers gutter
                    Column(
                        modifier = Modifier
                            .width(40.dp)
                            .background(Slate900)
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = lineNumbersString,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            color = Slate600
                        )
                    }

                    // Editor Content Text Field
                    OutlinedTextField(
                        value = editorContent,
                        onValueChange = { viewModel.updateEditorContent(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("code_editor_text_field"),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            color = Slate100
                        ),
                        shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate950,
                            unfocusedContainerColor = Slate950,
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                }
            }
        } else {
            // Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Není vybrán žádný soubor",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showNewFileDialog = true }) {
                        Text("Vytvořit soubor")
                    }
                }
            }
        }
    }

    if (showNewFileDialog) {
        NewFileDialog(
            onDismiss = { showNewFileDialog = false },
            onConfirm = { path, content ->
                viewModel.createNewFile(path, content)
                showNewFileDialog = false
            }
        )
    }
}
