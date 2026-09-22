package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*

// =======================================================
// 1. GIT STUDIO VIEW
// =======================================================
@Composable
fun GitStudioView(viewModel: OpenCodeViewModel, modifier: Modifier = Modifier) {
    val changedFiles by viewModel.gitChangedFiles.collectAsState()
    val stagedFiles by viewModel.stagedFilePaths.collectAsState()
    val commits by viewModel.gitCommits.collectAsState()
    val selectedDiff by viewModel.selectedDiffFile.collectAsState()
    var commitMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (selectedDiff != null) {
        // Visual Diff Dialog
        AlertDialog(
            onDismissRequest = { viewModel.selectDiffFile(null) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Difference, contentDescription = null, tint = CyanBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vizuální Diff: ${selectedDiff!!.path.substringAfterLast("/")}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = selectedDiff!!.path,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Slate950,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            selectedDiff!!.diff.forEach { line ->
                                val (bg, prefixColor, prefix) = when (line.type) {
                                    DiffLineType.ADDED -> Triple(EmeraldDark.copy(alpha = 0.35f), EmeraldBright, "+ ")
                                    DiffLineType.REMOVED -> Triple(CrimsonError.copy(alpha = 0.25f), CrimsonError, "- ")
                                    DiffLineType.UNCHANGED -> Triple(Color.Transparent, Slate400, "  ")
                                }
                                Surface(
                                    color = bg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = prefix,
                                            color = prefixColor,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = line.text,
                                            color = if (line.type == DiffLineType.REMOVED) Slate400 else Slate100,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.selectDiffFile(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                ) {
                    Text("Zavřít")
                }
            },
            containerColor = Slate900
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Commit Box
        item {
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
                            Icon(Icons.Default.Commit, contentDescription = null, tint = CyanBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nový Commit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                viewModel.generateAiCommitMessage { msg ->
                                    commitMessage = msg
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CyanBright.copy(alpha = 0.2f),
                                contentColor = CyanBright
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Zen AI zpráva", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = commitMessage,
                        onValueChange = { commitMessage = it },
                        placeholder = { Text("Zpráva commitu (např. feat: přídavný modul)", color = Slate500) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanBright,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate200,
                            focusedContainerColor = Slate950,
                            unfocusedContainerColor = Slate950
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Připraveno ke commitu: ${stagedFiles.size} souborů",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                        Button(
                            onClick = {
                                if (commitMessage.isBlank()) {
                                    Toast.makeText(context, "Zadejte zprávu commitu!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.commitStagedChanges(commitMessage)
                                    commitMessage = ""
                                    Toast.makeText(context, "Commit byl úspěšně vytvořen!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = commitMessage.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Commitnout", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Changed Files List
        item {
            Text(
                text = "Změněné soubory (${changedFiles.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate200
            )
        }

        items(changedFiles, key = { it.path }) { file ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Checkbox(
                            checked = file.isStaged,
                            onCheckedChange = { viewModel.toggleStageFile(file.path) },
                            colors = CheckboxDefaults.colors(checkedColor = CyanBright, checkmarkColor = Slate950)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = file.path.substringAfterLast("/"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Text(
                                text = file.path,
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Slate800
                        ) {
                            Text(
                                text = "+${file.additions} -${file.deletions}",
                                fontSize = 11.sp,
                                color = EmeraldBright,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { viewModel.selectDiffFile(file) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = "Diff", tint = CyanBright, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Commit History
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Historie commitů (${commits.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate200
            )
        }

        items(commits, key = { it.id }) { commit ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = commit.message,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${commit.author} • ${commit.date} • ${commit.filesChangedCount} souborů",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                    }
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = commit.id,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyanBright,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// =======================================================
// 2. SQLITE & DATABASE STUDIO VIEW
// =======================================================
@Composable
fun SqlStudioView(viewModel: OpenCodeViewModel, modifier: Modifier = Modifier) {
    val query by viewModel.currentSqlQuery.collectAsState()
    val result by viewModel.sqlQueryResult.collectAsState()
    val quickQueries = listOf(
        "SELECT * FROM plugins WHERE isInstalled = 1;",
        "SELECT id, name, category, rating FROM plugins ORDER BY rating DESC;",
        "SELECT * FROM skills WHERE isInstalled = 1;",
        "SELECT name, path, language FROM workspace_files;",
        "SELECT sender, content, timestamp FROM chat_messages LIMIT 10;"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Query Box
        item {
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
                            Icon(Icons.Default.Storage, contentDescription = null, tint = CyanBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SQLite Studio: opencode.db",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                        }

                        Button(
                            onClick = { viewModel.executeSqlQuery(query) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Spustit SQL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.currentSqlQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanBright,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate200,
                            focusedContainerColor = Slate950,
                            unfocusedContainerColor = Slate950
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick query chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickQueries.forEach { q ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.currentSqlQuery.value = q
                                    viewModel.executeSqlQuery(q)
                                },
                                label = { Text(q.take(28) + "...", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Slate800,
                                    labelColor = CyanBright
                                )
                            )
                        }
                    }
                }
            }
        }

        // Result Grid
        item {
            result?.let { res ->
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
                            Text(
                                text = if (res.error != null) "Chyba SQL dotazu" else "Výsledek: ${res.rowCount} řádků (${res.executionTimeMs} ms)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (res.error != null) CrimsonError else EmeraldBright
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (res.error != null) {
                            Surface(
                                color = CrimsonError.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = res.error,
                                    color = CrimsonError,
                                    modifier = Modifier.padding(10.dp),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                        } else if (res.columns.isEmpty()) {
                            Text("Žádná data k zobrazení", color = Slate400, fontSize = 13.sp)
                        } else {
                            // Table Grid with horizontal scroll
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Column {
                                    // Column Headers
                                    Row(
                                        modifier = Modifier
                                            .background(Slate800)
                                            .padding(vertical = 6.dp, horizontal = 4.dp)
                                    ) {
                                        res.columns.forEach { col ->
                                            Text(
                                                text = col,
                                                fontWeight = FontWeight.Bold,
                                                color = CyanBright,
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .width(130.dp)
                                                    .padding(horizontal = 6.dp)
                                            )
                                        }
                                    }

                                    // Data Rows
                                    res.rows.forEachIndexed { idx, row ->
                                        Row(
                                            modifier = Modifier
                                                .background(if (idx % 2 == 0) Slate950 else Slate900)
                                                .padding(vertical = 6.dp, horizontal = 4.dp)
                                        ) {
                                            row.forEach { cell ->
                                                Text(
                                                    text = cell,
                                                    color = Slate200,
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier
                                                        .width(130.dp)
                                                        .padding(horizontal = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =======================================================
// 3. DEV CONTAINERS & DOCKER VIEW
// =======================================================
@Composable
fun DevContainersView(viewModel: OpenCodeViewModel, modifier: Modifier = Modifier) {
    val containers by viewModel.devContainers.collectAsState()
    var selectedForLogs by remember { mutableStateOf<DevContainer?>(null) }

    if (selectedForLogs != null) {
        AlertDialog(
            onDismissRequest = { selectedForLogs = null },
            title = {
                Text(
                    text = "Konzolové logy: ${selectedForLogs!!.name}",
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Surface(
                        color = Slate950,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            selectedForLogs!!.logs.forEach { log ->
                                Text(
                                    text = log,
                                    color = EmeraldBright,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedForLogs = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                ) {
                    Text("Zavřít")
                }
            },
            containerColor = Slate900
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = CyanBright, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Správce vývojových kontejnerů",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(
                            text = "Spravujte lokální služby pro databáze, mezipaměti a backendy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }
        }

        items(containers, key = { it.id }) { c ->
            val isRunning = c.status == "RUNNING"
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(10.dp),
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
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isRunning) EmeraldBright else CrimsonError)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = c.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalButton(
                                onClick = { viewModel.toggleContainer(c.id) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isRunning) CrimsonError.copy(alpha = 0.2f) else EmeraldBright.copy(alpha = 0.2f),
                                    contentColor = if (isRunning) CrimsonError else EmeraldBright
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isRunning) "Zastavit" else "Spustit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { viewModel.restartContainer(c.id) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Slate300, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Obraz: ${c.image} • Porty: ${c.ports}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RAM: ${c.memoryUsage}  |  CPU: ${c.cpuUsage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate300
                        )

                        TextButton(
                            onClick = { selectedForLogs = c },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Logy kontejneru", color = CyanBright, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// =======================================================
// 4. LOGCAT & PROFILER VIEW
// =======================================================
@Composable
fun LogcatProfilerView(viewModel: OpenCodeViewModel, modifier: Modifier = Modifier) {
    val entries by viewModel.logcatEntries.collectAsState()
    val levelFilter by viewModel.logcatLevelFilter.collectAsState()
    val searchQuery by viewModel.logcatSearchQuery.collectAsState()
    val cpu by viewModel.cpuUsagePercent.collectAsState()
    val ram by viewModel.memoryUsageMb.collectAsState()
    var aiDiagnosisText by remember { mutableStateOf<String?>(null) }

    if (aiDiagnosisText != null) {
        AlertDialog(
            onDismissRequest = { aiDiagnosisText = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zen AI Diagnostika chyby", fontWeight = FontWeight.Bold, color = Slate100)
                }
            },
            text = {
                Text(
                    text = aiDiagnosisText!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate200
                )
            },
            confirmButton = {
                Button(
                    onClick = { aiDiagnosisText = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                ) {
                    Text("Rozumím")
                }
            },
            containerColor = Slate900
        )
    }

    val filteredEntries = entries.filter { entry ->
        (levelFilter == null || entry.level == levelFilter) &&
        (searchQuery.isBlank() || entry.message.contains(searchQuery, ignoreCase = true) || entry.tag.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // System Profiler
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = CyanBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Systémový Profiler & Prostředky",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Využití procesoru (CPU): $cpu %", fontSize = 12.sp, color = Slate300)
                        Text("Využití paměti (RAM): $ram MB", fontSize = 12.sp, color = Slate300)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { cpu / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyanBright,
                        trackColor = Slate800,
                    )
                }
            }
        }

        // Logcat Filter Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = levelFilter == null,
                    onClick = { viewModel.logcatLevelFilter.value = null },
                    label = { Text("VŠECHNY") }
                )
                LogLevel.values().forEach { lvl ->
                    FilterChip(
                        selected = levelFilter == lvl,
                        onClick = { viewModel.logcatLevelFilter.value = lvl },
                        label = { Text(lvl.name) }
                    )
                }
            }
        }

        // Log entries
        items(filteredEntries, key = { it.id }) { entry ->
            val color = when (entry.level) {
                LogLevel.ERROR -> CrimsonError
                LogLevel.WARN -> AmberWarning
                LogLevel.INFO -> CyanBright
                LogLevel.DEBUG -> EmeraldBright
                LogLevel.VERBOSE -> Slate400
            }
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "[${entry.level.name}]",
                                color = color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${entry.tag} • ${entry.timestamp}",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.message,
                            color = Slate100,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }

                    if (entry.level == LogLevel.ERROR || entry.level == LogLevel.WARN) {
                        IconButton(
                            onClick = {
                                viewModel.analyzeLogException(entry) { diagnosis ->
                                    aiDiagnosisText = diagnosis
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Diagnostika", tint = CyanBright, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// =======================================================
// 5. TEST RUNNER VIEW
// =======================================================
@Composable
fun TestRunnerView(viewModel: OpenCodeViewModel, modifier: Modifier = Modifier) {
    val tests by viewModel.testSuites.collectAsState()
    val isRunning by viewModel.isTestsRunning.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
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
                            Icon(Icons.Default.Science, contentDescription = null, tint = CyanBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Spouštěč automatických testů",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                        }

                        Button(
                            onClick = { viewModel.runAllTests() },
                            enabled = !isRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isRunning) "Probíhá..." else "Spustit testy", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isRunning) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = CyanBright,
                            trackColor = Slate800
                        )
                    }
                }
            }
        }

        items(tests, key = { it.id }) { test ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = when (test.status) {
                                "PASSED" -> Icons.Default.CheckCircle
                                "FAILED" -> Icons.Default.Cancel
                                "RUNNING" -> Icons.Default.HourglassTop
                                else -> Icons.Default.Circle
                            },
                            contentDescription = null,
                            tint = when (test.status) {
                                "PASSED" -> EmeraldBright
                                "FAILED" -> CrimsonError
                                "RUNNING" -> AmberWarning
                                else -> Slate500
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = test.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Text(
                                text = "${test.category} • ${test.message}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                    }

                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${test.durationMs} ms",
                            fontSize = 11.sp,
                            color = Slate300,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// =======================================================
// 6. WORKSPACE EXPORT & BACKUP VIEW
// =======================================================
@Composable
fun WorkspaceBackupView(viewModel: OpenCodeViewModel, modifier: Modifier = Modifier) {
    val stats = viewModel.getWorkspaceStats()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Archive, contentDescription = null, tint = CyanBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zálohování a export projektu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Vytvořte kompletní záložní archiv projektu včetně zdrojových kódů, SQLite databáze, nastavení pluginů a dovedností.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate300
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.exportWorkspaceZip { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportovat celý Workspace do ZIP", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Metriky a statistiky projektu",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate200
            )
        }

        items(stats.entries.toList(), key = { it.key }) { (key, value) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = key, style = MaterialTheme.typography.bodyMedium, color = Slate300)
                    Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CyanBright)
                }
            }
        }
    }
}
