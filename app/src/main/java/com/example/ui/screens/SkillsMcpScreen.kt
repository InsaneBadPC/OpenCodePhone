package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.local.entities.McpServerEntity
import com.example.data.local.entities.SkillEntity
import com.example.ui.OpenCodeViewModel
import com.example.ui.components.AddMcpDialog
import com.example.ui.components.AddSkillDialog
import com.example.ui.theme.*

@Composable
fun SkillsMcpScreen(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Skills, 1 = MCP
    val skills by viewModel.skills.collectAsState()
    val mcpServers by viewModel.mcpServers.collectAsState()
    val context = LocalContext.current

    var showAddSkillDialog by remember { mutableStateOf(false) }
    var showAddMcpDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Slate950,
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 5) {
                FloatingActionButton(
                    onClick = {
                        if (selectedTab == 0) showAddSkillDialog = true else showAddMcpDialog = true
                    },
                    containerColor = CyanBright,
                    contentColor = Slate950,
                    modifier = Modifier.testTag("add_skill_or_mcp_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (selectedTab == 0) "Přidat dovednost" else "Přidat MCP Server"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header ScrollableTabRow
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Slate900,
                contentColor = CyanBright,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("🧠 Skilly (${skills.size})", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("⚡ Registr & Řetězení", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("✨ Autolearn", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("📜 .MD Archiv", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("🐙 GitHub", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    text = { Text("🔌 MCP (${mcpServers.size})", fontSize = 12.sp) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Skills List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanBright, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Aktivní dovednosti (Skills) vkládají pravidla přímo do systémového promptu Zen AI.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate200
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilledTonalButton(
                                        onClick = { viewModel.openMarketplace("Dovednosti") },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = CyanBright.copy(alpha = 0.2f),
                                            contentColor = CyanBright
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tržiště", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    items(skills, key = { it.id }) { skill ->
                        SkillItemCard(
                            skill = skill,
                            onToggle = { viewModel.toggleSkillEnabled(skill) },
                            onDelete = { viewModel.deleteSkill(skill.id) }
                        )
                    }
                }
            }
            1 -> {
                SkillRegistryView(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            2 -> {
                AutoLearnedSkillsView(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            3 -> {
                SessionMarkdownArchiveView(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            4 -> {
                GitHubIntegrationView(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            5 -> {
                // MCP Servers List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Lan, contentDescription = null, tint = EmeraldBright, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Model Context Protocol (MCP) propojuje AI asistenta s externími nástroji a databázemi.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate200
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilledTonalButton(
                                        onClick = { viewModel.openMarketplace("MCP Servery") },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = EmeraldBright.copy(alpha = 0.2f),
                                            contentColor = EmeraldBright
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tržiště", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    items(mcpServers, key = { it.id }) { server ->
                        McpServerCard(
                            server = server,
                            onToggleConnected = { viewModel.toggleMcpConnected(server) },
                            onDelete = { viewModel.deleteMcpServer(server.id) },
                            onTestTool = {
                                viewModel.runTerminalCommand("mcp call ${server.name} test")
                                Toast.makeText(context, "Provádím testování spojení MCP na ${server.name}...", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

    if (showAddSkillDialog) {
        AddSkillDialog(
            onDismiss = { showAddSkillDialog = false },
            onConfirm = { name, cat, desc, prompt, tags ->
                viewModel.installSkill(name, cat, desc, prompt, tags)
                showAddSkillDialog = false
            }
        )
    }

    if (showAddMcpDialog) {
        AddMcpDialog(
            onDismiss = { showAddMcpDialog = false },
            onConfirm = { name, desc, transport, endpoint, tools ->
                viewModel.addMcpServer(name, desc, transport, endpoint, tools)
                showAddMcpDialog = false
            }
        )
    }
}

@Composable
fun SkillItemCard(
    skill: SkillEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (skill.isEnabled) Slate900 else Slate950
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (skill.isEnabled) CyanBright.copy(alpha = 0.5f) else Slate800
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = { Text(skill.category, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Slate800, labelColor = CyanBright),
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }

                Switch(
                    checked = skill.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Slate950,
                        checkedTrackColor = CyanBright
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodySmall,
                color = Slate200
            )

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Slate950,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Systémové pravidlo (Prompt):",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                    Text(
                        text = skill.systemPrompt,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Slate200,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Klíčová slova: ${skill.tags}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Smazat dovednost",
                        tint = Slate600,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun McpServerCard(
    server: McpServerEntity,
    onToggleConnected: () -> Unit,
    onDelete: () -> Unit,
    onTestTool: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Slate900
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (server.isConnected) EmeraldBright.copy(alpha = 0.4f) else Slate800
        )
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
                            .background(
                                if (server.isConnected) EmeraldBright else AmberWarning,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text(server.transport, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Slate800, labelColor = Slate200),
                        modifier = Modifier.height(22.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = server.isConnected,
                        onCheckedChange = { onToggleConnected() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Slate950,
                            checkedTrackColor = EmeraldBright
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = server.description,
                style = MaterialTheme.typography.bodySmall,
                color = Slate200
            )

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Slate950,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = server.endpointOrCommand,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyanBright,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dostupné nástroje: ${server.toolsJson.replace("\"", "").replace("[", "").replace("]", "")}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
                fontFamily = FontFamily.Monospace
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onTestTool) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldBright)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Testovat spojení", fontSize = 11.sp, color = EmeraldBright)
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Smazat MCP", tint = Slate600, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
