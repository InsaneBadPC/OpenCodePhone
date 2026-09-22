package com.example.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.ZenModel
import com.example.data.local.entities.PluginEntity
import com.example.ui.OpenCodeViewModel
import com.example.ui.components.AddCustomPluginDialog
import com.example.ui.components.PluginDetailDialog
import com.example.ui.theme.*

@Composable
fun PluginsSettingsScreen(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(0) } // 0 = Pluginy, 1 = Terminál, 2 = REST, 3 = Zen Nastavení
    val filteredPlugins by viewModel.filteredPlugins.collectAsState()
    val availableUpdatesCount by viewModel.availableUpdatesCount.collectAsState()
    val installedPluginsCount by viewModel.installedPluginsCount.collectAsState()
    val pluginSearchQuery by viewModel.pluginSearchQuery.collectAsState()
    val pluginCategoryFilter by viewModel.pluginCategoryFilter.collectAsState()

    val terminalHistory by viewModel.terminalHistory.collectAsState()
    val terminalInput by viewModel.terminalInput.collectAsState()
    val restState by viewModel.restState.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val context = LocalContext.current

    var selectedPluginForDetail by remember { mutableStateOf<PluginEntity?>(null) }
    var showAddCustomPluginDialog by remember { mutableStateOf(false) }

    if (showAddCustomPluginDialog) {
        AddCustomPluginDialog(
            onDismiss = { showAddCustomPluginDialog = false },
            onConfirm = { name, version, desc, category, author, perms ->
                viewModel.installCustomPlugin(name, version, desc, category, author, perms)
                showAddCustomPluginDialog = false
                Toast.makeText(context, "Plugin \"$name\" byl úspěšně nainstalován!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    selectedPluginForDetail?.let { plugin ->
        PluginDetailDialog(
            plugin = plugin,
            onDismiss = { selectedPluginForDetail = null },
            onInstall = {
                viewModel.installPlugin(plugin)
                Toast.makeText(context, "Plugin \"${plugin.name}\" byl nainstalován.", Toast.LENGTH_SHORT).show()
            },
            onUpdate = {
                viewModel.updatePlugin(plugin)
                Toast.makeText(context, "Plugin \"${plugin.name}\" byl aktualizován na v${plugin.latestVersion}.", Toast.LENGTH_SHORT).show()
            },
            onUninstall = {
                viewModel.uninstallPlugin(plugin)
                Toast.makeText(context, "Plugin \"${plugin.name}\" byl odinstalován.", Toast.LENGTH_SHORT).show()
            },
            onToggleEnable = {
                viewModel.togglePluginEnabled(plugin)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Podzáložky sekce (Scrollable pro plnou škálu nástrojů)
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            containerColor = Slate900,
            contentColor = CyanBright,
            edgePadding = 12.dp
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧩 Pluginy", fontSize = 12.sp)
                        if (availableUpdatesCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AmberWarning)
                            )
                        }
                    }
                }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("🔀 Git VCS", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = { Text("🗄️ SQL Studio", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 3,
                onClick = { selectedSection = 3 },
                text = { Text("🐳 Kontejnery", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 4,
                onClick = { selectedSection = 4 },
                text = { Text("📊 Logcat", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 5,
                onClick = { selectedSection = 5 },
                text = { Text("🧪 Testy", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 6,
                onClick = { selectedSection = 6 },
                text = { Text("💻 Terminál", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 7,
                onClick = { selectedSection = 7 },
                text = { Text("📡 REST", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 8,
                onClick = { selectedSection = 8 },
                text = { Text("📦 Zálohování", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 9,
                onClick = { selectedSection = 9 },
                text = { Text("⚙️ Nastavení", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 10,
                onClick = { selectedSection = 10 },
                text = { Text("☁️ Cloud Služby", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 11,
                onClick = { selectedSection = 11 },
                text = { Text("🔐 Trezor & Google", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 12,
                onClick = { selectedSection = 12 },
                text = { Text("🐧 Termux & OCI", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 13,
                onClick = { selectedSection = 13 },
                text = { Text("🗺️ AST Graf", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 14,
                onClick = { selectedSection = 14 },
                text = { Text("📴 Offline LLM", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 15,
                onClick = { selectedSection = 15 },
                text = { Text("🐛 Debugger", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 16,
                onClick = { selectedSection = 16 },
                text = { Text("🚀 CI/CD", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 17,
                onClick = { selectedSection = 17 },
                text = { Text("🔣 Regex & Sockety", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 18,
                onClick = { selectedSection = 18 },
                text = { Text("🎙️ Hlasový asistent", fontSize = 12.sp) }
            )
        }

        when (selectedSection) {
            0 -> {
                // SYSTÉM PRO SPRÁVU PLUGINŮ
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Hlavní karta správce pluginů
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
                                        Icon(
                                            Icons.Default.Extension,
                                            contentDescription = null,
                                            tint = CyanBright,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Správce doplňků a pluginů",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate100
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilledTonalButton(
                                            onClick = { viewModel.openMarketplace("Pluginy") },
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = CyanBright.copy(alpha = 0.2f),
                                                contentColor = CyanBright
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Tržiště", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { showAddCustomPluginDialog = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = CyanBright,
                                                contentColor = Slate950
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Vlastní", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Rozšiřte vývojové prostředí OpenCode o analyzátory kódu, git nástroje, databázová studia a UI témata.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate300
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Informační statistika a hromadná aktualizace
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Slate800
                                        ) {
                                            Text(
                                                text = "Nainstalováno: $installedPluginsCount",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EmeraldSuccess,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        if (availableUpdatesCount > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = AmberWarning.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "⚡ Aktualizace: $availableUpdatesCount",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = AmberWarning,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    if (availableUpdatesCount > 0) {
                                        FilledTonalButton(
                                            onClick = {
                                                viewModel.updateAllPlugins()
                                                Toast.makeText(context, "Všechny dostupné pluginy byly aktualizovány!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = AmberWarning,
                                                contentColor = Slate950
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Aktualizovat vše", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Vyhledávací pole pluginů
                    item {
                        OutlinedTextField(
                            value = pluginSearchQuery,
                            onValueChange = { viewModel.setPluginSearchQuery(it) },
                            placeholder = { Text("Hledat plugin podle názvu, popisu nebo autora...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = CyanBright)
                            },
                            trailingIcon = {
                                if (pluginSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.setPluginSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Vymazat", tint = Slate400)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("plugin_search_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanBright,
                                unfocusedBorderColor = Slate800,
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate200
                            ),
                            singleLine = true
                        )
                    }

                    // Filtrační kategorie (čipy)
                    item {
                        val categories = listOf(
                            "Všechny",
                            "Nainstalované",
                            "Aktualizace",
                            "Nástroje",
                            "Jazyky",
                            "AI & Asistenti",
                            "Databáze",
                            "Verzování",
                            "Vzhled"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = pluginCategoryFilter == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setPluginCategoryFilter(cat) },
                                    label = {
                                        Text(
                                            text = if (cat == "Aktualizace" && availableUpdatesCount > 0) "Aktualizace ($availableUpdatesCount)" else cat,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (cat == "Aktualizace") AmberWarning else CyanBright,
                                        selectedLabelColor = Slate950,
                                        containerColor = Slate900,
                                        labelColor = Slate300
                                    )
                                )
                            }
                        }
                    }

                    // Seznam pluginů
                    if (filteredPlugins.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = Slate500,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Žádné pluginy nebyly nalezeny",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Slate300
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Zkuste změnit hledaný výraz nebo zvolit jinou kategorii filtrů.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredPlugins, key = { it.id }) { plugin ->
                            PluginItemCard(
                                plugin = plugin,
                                onClick = { selectedPluginForDetail = plugin },
                                onInstall = {
                                    viewModel.installPlugin(plugin)
                                    Toast.makeText(context, "Plugin \"${plugin.name}\" byl nainstalován.", Toast.LENGTH_SHORT).show()
                                },
                                onUpdate = {
                                    viewModel.updatePlugin(plugin)
                                    Toast.makeText(context, "Plugin \"${plugin.name}\" byl aktualizován na v${plugin.latestVersion}.", Toast.LENGTH_SHORT).show()
                                },
                                onUninstall = {
                                    viewModel.uninstallPlugin(plugin)
                                    Toast.makeText(context, "Plugin \"${plugin.name}\" byl odinstalován.", Toast.LENGTH_SHORT).show()
                                },
                                onToggleEnabled = {
                                    viewModel.togglePluginEnabled(plugin)
                                }
                            )
                        }
                    }
                }
            }

            1 -> {
                // GIT & VERSION CONTROL STUDIO
                GitStudioView(viewModel = viewModel)
            }

            2 -> {
                // SQL STUDIO
                SqlStudioView(viewModel = viewModel)
            }

            3 -> {
                // DEV CONTAINERS & DOCKER
                DevContainersView(viewModel = viewModel)
            }

            4 -> {
                // LOGCAT & PROFILER
                LogcatProfilerView(viewModel = viewModel)
            }

            5 -> {
                // AUTOMATICKÉ TESTY
                TestRunnerView(viewModel = viewModel)
            }

            6 -> {
                // INTERAKTIVNÍ TERMINÁL
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = CyanBright, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Interaktivní vývojářská konzole (příkazy: ls, cat, git, python, zen, mcp, clear)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate200
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF090D16))
                            .padding(12.dp)
                    ) {
                        items(terminalHistory) { line ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "$ opencode > ${line.command}",
                                    color = CyanBright,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = line.output,
                                    color = if (line.exitCode == 0) Slate200 else MaterialTheme.colorScheme.error,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = terminalInput,
                            onValueChange = { viewModel.updateTerminalInput(it) },
                            placeholder = { Text("Zadejte příkaz (např. git status, ls, python scripts/script.py)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("terminal_input_field"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900,
                                focusedBorderColor = CyanBright,
                                unfocusedBorderColor = Slate800,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate200
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.runTerminalCommand() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                        ) {
                            Text("Spustit")
                        }
                    }
                }
            }

            7 -> {
                // REST API KLIENT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Tester HTTP & REST API požadavků",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanBright
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Otestujte libovolné vývojářské API přímo z OpenCode a odešlete odpověď do chatu.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate300
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate800
                        ) {
                            Text(
                                text = restState.method,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                            )
                        }

                        OutlinedTextField(
                            value = restState.url,
                            onValueChange = { viewModel.updateRestUrl(it) },
                            label = { Text("URL adresa endpointu") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900,
                                focusedBorderColor = CyanBright,
                                unfocusedBorderColor = Slate800,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate200
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.executeRestRequest() },
                        enabled = !restState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                    ) {
                        if (restState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Slate950)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Načítám požadavek...")
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Odeslat HTTP požadavek", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (restState.responseStatus.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stav odpovědi: ${restState.responseStatus}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (restState.responseStatus.startsWith("200")) EmeraldSuccess else AmberWarning
                            )

                            FilledTonalButton(
                                onClick = {
                                    viewModel.selectTab(0)
                                    viewModel.sendMessage("Zanalyzuj tuto JSON odpověď z ${restState.url}:\n```json\n${restState.responseBody.take(1200)}\n```")
                                }
                            ) {
                                Text("Odeslat do Zen Chatu", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0B101D),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = restState.responseBody,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Slate200,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            8 -> {
                // WORKSPACE EXPORT & ZÁLOHOVÁNÍ
                WorkspaceBackupView(viewModel = viewModel)
            }

            9 -> {
                // NASTAVENÍ ZEN AI & APLIKACE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Konfigurace modelů Zen AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanBright
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Vyberte výchozí model rodiny Zen pro generování kódu a systémovou architekturu.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate300
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            ZenModel.values().forEach { model ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectModel(model) }
                                        .background(if (selectedModel == model) Slate800 else Slate900)
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedModel == model,
                                        onClick = { viewModel.selectModel(model) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = model.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate100
                                        )
                                        Text(
                                            text = "${model.apiModelId} · ${model.description}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate400
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Vlastní Gemini API Klíč",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pokud chcete využít vlastní kvóty nebo firemní Gemini API klíč, zadejte jej níže.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            var apiKeyInput by remember { mutableStateOf(customApiKey) }
                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                placeholder = { Text("AIzaSy...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate950,
                                    unfocusedContainerColor = Slate950,
                                    focusedBorderColor = CyanBright,
                                    unfocusedBorderColor = Slate800,
                                    focusedTextColor = Slate100,
                                    unfocusedTextColor = Slate200
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    viewModel.updateCustomApiKey(apiKeyInput.trim())
                                    Toast.makeText(context, "API klíč byl bezpečně uložen!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                            ) {
                                Text("Uložit API klíč")
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "O vývojovém prostředí OpenCode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Verze: 2.4.0 (Nativní Android prostředí)\nAutor: OpenCode Core Team\nLokální databáze: Room v2 (opencode.db)\nPodpora protokolů: Model Context Protocol (MCP) 2024\nJazyk: Kompletní česká lokalizace",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate300
                            )
                        }
                    }
                }
            }

            10 -> {
                // CLOUD INTEGRATIONS (Google Disk, Supabase, Firefly, NotebookLM, GCP, OCI)
                CloudIntegrationsView(viewModel = viewModel)
            }

            11 -> {
                // SECRETS VAULT & GOOGLE DOCS / SHEETS IMPORT
                VaultAndGoogleImportView(viewModel = viewModel)
            }

            12 -> {
                // TERMUX & ORACLE CLOUD TERMINAL
                TermuxAndOracleView(viewModel = viewModel)
            }

            13 -> {
                // ARCHITECTURE AST DIAGRAMS
                ArchitectureDiagramView(viewModel = viewModel)
            }

            14 -> {
                // OFFLINE LOCAL LLM RUNTIME
                OfflineLlmView(viewModel = viewModel)
            }

            15 -> {
                // INTERACTIVE DEBUGGER
                DebuggerView(viewModel = viewModel)
            }

            16 -> {
                // CI/CD PIPELINE & WEBHOOKS
                CiPipelineView(viewModel = viewModel)
            }

            17 -> {
                // REGEX PLAYGROUND & WEBSOCKET STUDIO
                RegexAndSocketsView(viewModel = viewModel)
            }

            18 -> {
                // AI VOICE CODING ASSISTANT
                VoiceCodingView(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun PluginItemCard(
    plugin: PluginEntity,
    onClick: () -> Unit,
    onInstall: () -> Unit,
    onUpdate: () -> Unit,
    onUninstall: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Ikona pluginu
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (plugin.category) {
                                "Nástroje" -> CyanBright.copy(alpha = 0.15f)
                                "AI & Asistenti" -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                                "Databáze" -> EmeraldSuccess.copy(alpha = 0.15f)
                                "Verzování" -> AmberWarning.copy(alpha = 0.15f)
                                else -> Slate800
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (plugin.iconName) {
                            "brush" -> Icons.Default.Brush
                            "terminal" -> Icons.Default.Terminal
                            "http" -> Icons.Default.Http
                            "history" -> Icons.Default.History
                            "auto_awesome" -> Icons.Default.AutoAwesome
                            "storage" -> Icons.Default.Storage
                            "palette" -> Icons.Default.Palette
                            "account_tree" -> Icons.Default.AccountTree
                            "developer_board" -> Icons.Default.DeveloperBoard
                            else -> Icons.Default.Extension
                        },
                        contentDescription = null,
                        tint = when (plugin.category) {
                            "Nástroje" -> CyanBright
                            "AI & Asistenti" -> Color(0xFFA78BFA)
                            "Databáze" -> EmeraldSuccess
                            "Verzování" -> AmberWarning
                            else -> Slate200
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plugin.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )

                        // Verze odznak
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (plugin.hasUpdate) {
                                AmberWarning.copy(alpha = 0.2f)
                            } else if (plugin.isInstalled) {
                                EmeraldSuccess.copy(alpha = 0.2f)
                            } else {
                                Slate800
                            }
                        ) {
                            Text(
                                text = if (plugin.hasUpdate) {
                                    "v${plugin.installedVersion} ➔ v${plugin.latestVersion}"
                                } else if (plugin.isInstalled) {
                                    "v${plugin.installedVersion}"
                                } else {
                                    "v${plugin.latestVersion}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (plugin.hasUpdate) AmberWarning else if (plugin.isInstalled) EmeraldSuccess else Slate300,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${plugin.category} · ${plugin.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = plugin.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate300,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metriky: Hodnocení, stažení, velikost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${plugin.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate200
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${plugin.downloadsCount} stažení",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${plugin.sizeKb} KB",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }

                        // Akční tlačítko
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (plugin.hasUpdate) {
                                Button(
                                    onClick = onUpdate,
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning, contentColor = Slate950),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Aktualizovat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (!plugin.isInstalled) {
                                Button(
                                    onClick = onInstall,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Instalovat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = plugin.isEnabled,
                                        onCheckedChange = { onToggleEnabled() },
                                        modifier = Modifier.scale(0.8f)
                                    )
                                    IconButton(
                                        onClick = onClick,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Podrobnosti", tint = Slate400)
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

