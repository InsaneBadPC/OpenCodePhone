package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.SkillRegistryEntity
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*

@Composable
fun SkillRegistryView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val registeredSkills by viewModel.registeredSkills.collectAsState()
    val chainedPipeline by viewModel.chainedPipeline.collectAsState()
    val isChainRunning by viewModel.isChainRunning.collectAsState()
    val activeChainStepIndex by viewModel.activeChainStepIndex.collectAsState()
    val chainExecutionLogs by viewModel.chainExecutionLogs.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Vše") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val categories = listOf("Vše", "DevOps", "Refactoring", "Bezpečnost", "Dokumentace", "Databáze", "Kvalita kódu", "Automatizace")

    val filteredSkills = registeredSkills.filter { skill ->
        val matchesCategory = if (selectedCategory == "Vše") true else skill.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                skill.name.contains(searchQuery, ignoreCase = true) ||
                skill.description.contains(searchQuery, ignoreCase = true) ||
                skill.requiredTools.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner & Quick Actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanBright.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(CyanBright.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = CyanBright, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "⚡ Registr Dovedností (Skill Registry)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ukládá bezchybné sekvence do lokální Room DB pro řetězení agentem",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.detectExecutionSequenceFromSession { detectedSkill, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanBright,
                                contentColor = Slate950
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("detect_sequence_button")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Detekovat ze session", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberBright),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberBright.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_skill_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Přidat novou", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Metrics summary row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricSmallCard(
                    title = "V registrech",
                    value = "${registeredSkills.size}",
                    color = CyanBright,
                    modifier = Modifier.weight(1f)
                )
                MetricSmallCard(
                    title = "Řetězitelných",
                    value = "${registeredSkills.count { it.isChainable }}",
                    color = EmeraldBright,
                    modifier = Modifier.weight(1f)
                )
                MetricSmallCard(
                    title = "Úspěšnost",
                    value = "99.8%",
                    color = AmberBright,
                    modifier = Modifier.weight(1f)
                )
                MetricSmallCard(
                    title = "V řetězci",
                    value = "${chainedPipeline.size}",
                    color = if (chainedPipeline.isNotEmpty()) MagentaBright else Slate400,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Chained Execution Pipeline Card
        item {
            ChainedPipelineCard(
                pipeline = chainedPipeline,
                isRunning = isChainRunning,
                activeStep = activeChainStepIndex,
                logs = chainExecutionLogs,
                onExecute = {
                    viewModel.executeChainedPipeline(
                        onStepProgress = { stepLog ->
                            Toast.makeText(context, stepLog, Toast.LENGTH_SHORT).show()
                        },
                        onFinished = { finMsg ->
                            Toast.makeText(context, finMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                onClear = { viewModel.clearPipeline() },
                onRemove = { id -> viewModel.removeSkillFromPipeline(id) },
                onMoveUp = { idx -> viewModel.moveSkillInPipeline(idx, idx - 1) },
                onMoveDown = { idx -> viewModel.moveSkillInPipeline(idx, idx + 1) }
            )
        }

        // 4. Search and Filter row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Hledat dovednost, nástroj, krok...", color = Slate500, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Vymazat", tint = Slate400, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900,
                        focusedBorderColor = CyanBright,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanBright.copy(alpha = 0.2f),
                                selectedLabelColor = CyanBright,
                                containerColor = Slate900,
                                labelColor = Slate300
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) CyanBright else Slate800
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // 5. Registered Skills List
        if (filteredSkills.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = Slate500, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Žádné dovednosti neodpovídají filtru", color = Slate400, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredSkills, key = { it.id }) { skill ->
                RegisteredSkillItemCard(
                    skill = skill,
                    isInPipeline = chainedPipeline.any { it.id == skill.id },
                    onAddToPipeline = { viewModel.addSkillToPipeline(skill) },
                    onExecute = {
                        viewModel.executeSingleRegisteredSkill(skill) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDelete = { viewModel.deleteRegisteredSkill(skill.id) }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateSkillRegistryDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc, cat, steps, tools, chainable, input, output ->
                viewModel.registerNewSkillInRegistry(
                    name = name,
                    description = desc,
                    category = cat,
                    steps = steps,
                    tools = tools,
                    isChainable = chainable,
                    inputTemplate = input,
                    outputArtifact = output
                ) {
                    Toast.makeText(context, "Dovednost '$name' byla uložena do Room databáze!", Toast.LENGTH_SHORT).show()
                    showCreateDialog = false
                }
            }
        )
    }
}

@Composable
fun MetricSmallCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(title, color = Slate400, fontSize = 10.sp)
        }
    }
}

@Composable
fun ChainedPipelineCard(
    pipeline: List<SkillRegistryEntity>,
    isRunning: Boolean,
    activeStep: Int,
    logs: List<String>,
    onExecute: () -> Unit,
    onClear: () -> Unit,
    onRemove: (String) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (pipeline.isNotEmpty()) Slate900 else Slate950
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRunning) MagentaBright else if (pipeline.isNotEmpty()) CyanBright.copy(alpha = 0.5f) else Slate800
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = if (pipeline.isNotEmpty()) MagentaBright else Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Řetězení Dovedností (Skill Chaining Pipeline)",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (pipeline.isNotEmpty() && !isRunning) {
                    TextButton(
                        onClick = onClear,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Vyčistit", color = Slate400, fontSize = 11.sp)
                    }
                }
            }

            if (pipeline.isEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "V řetězci nejsou zařazeny žádné dovednosti. Klepněte na '🔗 Do řetězce' u libovolné dovednosti pro sestavení autonomní pipeline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    fontSize = 11.sp
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))

                if (isRunning) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MagentaBright,
                        trackColor = Slate800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Chained Nodes Sequence
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    pipeline.forEachIndexed { index, skill ->
                        val isNodeActive = isRunning && activeStep == index
                        val isNodeDone = isRunning && activeStep > index

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isNodeActive) MagentaBright.copy(alpha = 0.15f) else Slate950
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isNodeActive) MagentaBright else if (isNodeDone) EmeraldBright else Slate800
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(
                                                if (isNodeDone) EmeraldBright else if (isNodeActive) MagentaBright else CyanBright.copy(alpha = 0.2f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isNodeDone) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Slate950, modifier = Modifier.size(14.dp))
                                        } else {
                                            Text(
                                                "${index + 1}",
                                                color = if (isNodeActive) Slate950 else CyanBright,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = skill.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Výstup ➔ ${skill.outputArtifact.ifBlank { "Validovaný kontext" }}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate400,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                if (!isRunning) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (index > 0) {
                                            IconButton(onClick = { onMoveUp(index) }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.ArrowUpward, contentDescription = "Nahoru", tint = Slate400, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        if (index < pipeline.size - 1) {
                                            IconButton(onClick = { onMoveDown(index) }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.ArrowDownward, contentDescription = "Dolů", tint = Slate400, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        IconButton(onClick = { onRemove(skill.id) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Odebrat", tint = Slate500, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (logs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate950),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            logs.takeLast(3).forEach { log ->
                                Text(
                                    text = log,
                                    color = EmeraldBright,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onExecute,
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MagentaBright,
                        contentColor = Slate950
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("execute_chain_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunning) "Probíhá spouštění řetězce..." else "🚀 Spustit řetězec agentem (${pipeline.size} kroků)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RegisteredSkillItemCard(
    skill: SkillRegistryEntity,
    isInPipeline: Boolean,
    onAddToPipeline: () -> Unit,
    onExecute: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedSteps by remember { mutableStateOf(false) }

    val steps = remember(skill.executionStepsJson) {
        skill.executionStepsJson
            .replace("[", "")
            .replace("]", "")
            .replace("\"", "")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isInPipeline) MagentaBright.copy(alpha = 0.5f) else Slate800
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(skill.category, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Slate800,
                            labelColor = CyanBright
                        ),
                        border = null,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(24.dp)
                    )

                    if (skill.autoDetected) {
                        AssistChip(
                            onClick = {},
                            leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null, tint = AmberBright, modifier = Modifier.size(12.dp)) },
                            label = { Text("Auto-detekováno", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = AmberBright.copy(alpha = 0.12f),
                                labelColor = AmberBright
                            ),
                            border = null,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    AssistChip(
                        onClick = {},
                        label = { Text("${(skill.successScore * 100).toInt()}% spolehlivost", fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = EmeraldBright.copy(alpha = 0.1f),
                            labelColor = EmeraldBright
                        ),
                        border = null,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(24.dp)
                    )
                }

                Text(
                    text = "Spuštěno ${skill.executionCount}×",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title and Description
            Text(
                text = skill.name,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodySmall,
                color = Slate300,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Required tools tags
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Build, contentDescription = null, tint = Slate500, modifier = Modifier.size(12.dp))
                Text("Nástroje: ", color = Slate500, fontSize = 10.sp)
                skill.requiredTools.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tool ->
                    Box(
                        modifier = Modifier
                            .background(Slate950, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(tool, color = Slate300, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Steps section toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedSteps = !expandedSteps }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sekvence exekučních kroků (${steps.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanBright,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (expandedSteps) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CyanBright,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expandedSteps) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .background(Slate950, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    steps.forEachIndexed { i, step ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "${i + 1}.",
                                color = EmeraldBright,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(18.dp)
                            )
                            Text(
                                text = step,
                                color = Slate200,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    if (skill.outputArtifact.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = AmberBright, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Artefakt: ${skill.outputArtifact}", color = AmberBright, fontSize = 9.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onExecute,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CyanBright.copy(alpha = 0.15f),
                            contentColor = CyanBright
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Spustit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (skill.isChainable) {
                        FilledTonalButton(
                            onClick = onAddToPipeline,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isInPipeline) MagentaBright.copy(alpha = 0.25f) else Slate800,
                                contentColor = if (isInPipeline) MagentaBright else Slate200
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                if (isInPipeline) Icons.Default.Check else Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isInPipeline) "V řetězci" else "Do řetězce", fontSize = 11.sp)
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Smazat", tint = Slate500, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun CreateSkillRegistryDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        desc: String,
        category: String,
        steps: List<String>,
        tools: List<String>,
        isChainable: Boolean,
        input: String,
        output: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("DevOps") }
    var stepsText by remember { mutableStateOf("1. Zvalidovat kód\n2. Provést potřebné změny\n3. Otestovat funkcionalitu") }
    var toolsText by remember { mutableStateOf("terminal, editor, git") }
    var isChainable by remember { mutableStateOf(true) }
    var inputTemplate by remember { mutableStateOf("context_arg") }
    var outputArtifact by remember { mutableStateOf("Verified Output") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanBright.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Registrovat novou dovednost",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Název dovednosti") },
                    placeholder = { Text("např. Automatický Git & Release Tag") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyanBright,
                        unfocusedBorderColor = Slate700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Popis") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyanBright,
                        unfocusedBorderColor = Slate700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stepsText,
                    onValueChange = { stepsText = it },
                    label = { Text("Exekuční kroky (po řádcích)") },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyanBright,
                        unfocusedBorderColor = Slate700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = toolsText,
                    onValueChange = { toolsText = it },
                    label = { Text("Vyžadované nástroje (oddělené čárkou)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyanBright,
                        unfocusedBorderColor = Slate700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Řetězitelná v pipeline (Chainable)", color = Slate300, fontSize = 12.sp)
                    Switch(
                        checked = isChainable,
                        onCheckedChange = { isChainable = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanBright,
                            checkedTrackColor = CyanBright.copy(alpha = 0.3f)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Zrušit", color = Slate400)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedSteps = stepsText.lines().map { it.trim() }.filter { it.isNotBlank() }
                            val parsedTools = toolsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onConfirm(name, desc, category, parsedSteps, parsedTools, isChainable, inputTemplate, outputArtifact)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                    ) {
                        Text("Uložit do DB", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
