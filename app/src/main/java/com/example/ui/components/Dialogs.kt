package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.PluginEntity
import com.example.data.model.MarketItemType
import com.example.data.model.MarketSource
import com.example.data.model.MarketplaceCatalog
import com.example.data.model.MarketplaceItem
import com.example.ui.MarketplaceItemUi
import com.example.ui.theme.*

@Composable
fun NewFileDialog(
    onDismiss: () -> Unit,
    onConfirm: (path: String, content: String) -> Unit
) {
    var path by remember { mutableStateOf("src/NovySoubor.kt") }
    var initialContent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vytvořit nový soubor v projektu") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("Cesta k souboru (např. src/Nastroj.kt)") },
                    modifier = Modifier.fillMaxWidth().testTag("new_file_path_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = initialContent,
                    onValueChange = { initialContent = it },
                    label = { Text("Výchozí obsah (volitelné)") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (path.isNotBlank()) {
                        onConfirm(path.trim(), initialContent)
                    }
                },
                modifier = Modifier.testTag("confirm_create_file_btn")
            ) {
                Text("Vytvořit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )
}

@Composable
fun AddSkillDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, desc: String, prompt: String, tags: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Android UI") }
    var desc by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("custom,ai") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nainstalovat vývojářskou dovednost (Skill)") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Název dovednosti") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategorie (Android, Bezpečnost, AI, Web)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Stručný popis") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Systémová pravidla pro model (Prompt)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Klíčová slova (oddělená čárkami)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), category.trim(), desc.trim(), prompt.trim(), tags.trim())
                    }
                }
            ) {
                Text("Uložit dovednost")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )
}

@Composable
fun AddMcpDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, transport: String, endpoint: String, tools: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var transport by remember { mutableStateOf("STDIO") }
    var endpoint by remember { mutableStateOf("") }
    var tools by remember { mutableStateOf("tool1, tool2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zaregistrovat MCP Server") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Název serveru (např. SQLite MCP)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Popis serveru") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = transport,
                    onValueChange = { transport = it },
                    label = { Text("Transport (STDIO, SSE, HTTP)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("Příkaz nebo URL adresa endpointu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tools,
                    onValueChange = { tools = it },
                    label = { Text("Poskytované nástroje (oddělené čárkou)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && endpoint.isNotBlank()) {
                        onConfirm(name.trim(), desc.trim(), transport.trim(), endpoint.trim(), tools.trim())
                    }
                }
            ) {
                Text("Registrovat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )
}

@Composable
fun PluginDetailDialog(
    plugin: PluginEntity,
    onDismiss: () -> Unit,
    onInstall: () -> Unit,
    onUpdate: () -> Unit,
    onUninstall: () -> Unit,
    onToggleEnable: () -> Unit
) {
    var showUninstallConfirm by remember { mutableStateOf(false) }

    if (showUninstallConfirm) {
        AlertDialog(
            onDismissRequest = { showUninstallConfirm = false },
            title = { Text("Odinstalovat plugin?") },
            text = { Text("Opravdu si přejete odebrat doplněk \"${plugin.name}\" z vašeho prostředí OpenCode?") },
            confirmButton = {
                Button(
                    onClick = {
                        showUninstallConfirm = false
                        onUninstall()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Odinstalovat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUninstallConfirm = false }) {
                    Text("Zrušit")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Autor: ${plugin.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Metrics row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${plugin.rating}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${plugin.downloadsCount} stažení",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${plugin.sizeKb} KB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = plugin.category,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Versions box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Nainstalovaná verze:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = plugin.installedVersion?.let { "v$it" } ?: "Nenainstalováno",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (plugin.isInstalled) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Dostupná verze:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "v${plugin.latestVersion}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanBright
                            )
                        }

                        if (plugin.hasUpdate) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmberWarning.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Upgrade,
                                        contentDescription = null,
                                        tint = AmberWarning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "K dispozici je novější verze v${plugin.latestVersion}!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AmberWarning,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Popis
                Text(
                    text = "O doplňku",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plugin.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (plugin.detailedDescription.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plugin.detailedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Changelog
                Text(
                    text = "Novinky ve verzi (Changelog)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = plugin.changelog,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Oprávnění
                Text(
                    text = "Požadovaná oprávnění",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = plugin.permissions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Enabled switch if installed
                if (plugin.isInstalled) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Aktivní v prostředí",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (plugin.isEnabled) "Doplněk je zapnutý a aktivní" else "Doplněk je dočasně pozastaven",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = plugin.isEnabled,
                            onCheckedChange = { onToggleEnable() }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (plugin.hasUpdate) {
                    Button(
                        onClick = {
                            onUpdate()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning, contentColor = Slate950)
                    ) {
                        Icon(imageVector = Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aktualizovat")
                    }
                } else if (!plugin.isInstalled) {
                    Button(
                        onClick = {
                            onInstall()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nainstalovat")
                    }
                }

                if (plugin.isInstalled) {
                    OutlinedButton(
                        onClick = { showUninstallConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Odinstalovat")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zavřít")
            }
        }
    )
}

@Composable
fun AddCustomPluginDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, version: String, desc: String, category: String, author: String, permissions: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var version by remember { mutableStateOf("1.0.0") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Nástroje") }
    var author by remember { mutableStateOf("Vlastní autor") }
    var permissions by remember { mutableStateOf("Čtení souborů, Terminál") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nainstalovat vlastní plugin do OpenCode") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Název pluginu (např. JSON Schema Validator)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = version,
                        onValueChange = { version = it },
                        label = { Text("Verze") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategorie") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Autor / Organizace") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Popis funkcionality") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = permissions,
                    onValueChange = { permissions = it },
                    label = { Text("Požadovaná oprávnění") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), version.trim(), desc.trim(), category.trim(), author.trim(), permissions.trim())
                    }
                }
            ) {
                Text("Instalovat plugin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )
}

@Composable
fun MarketplaceDetailDialog(
    itemUi: MarketplaceItemUi,
    onDismiss: () -> Unit,
    onInstall: () -> Unit,
    onUninstall: () -> Unit
) {
    val item = itemUi.item
    var showUninstallConfirm by remember { mutableStateOf(false) }

    if (showUninstallConfirm) {
        AlertDialog(
            onDismissRequest = { showUninstallConfirm = false },
            title = { Text("Odinstalovat položku?") },
            text = { Text("Opravdu chcete odebrat ${item.name} z vašeho prostředí OpenCode?") },
            confirmButton = {
                Button(
                    onClick = {
                        showUninstallConfirm = false
                        onUninstall()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Odinstalovat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUninstallConfirm = false }) {
                    Text("Zrušit")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val icon = when (item.type) {
                        MarketItemType.PLUGIN -> Icons.Default.Extension
                        MarketItemType.SKILL -> Icons.Default.Psychology
                        MarketItemType.MCP -> Icons.Default.Hub
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (item.type) {
                                    MarketItemType.PLUGIN -> CyanBright.copy(alpha = 0.2f)
                                    MarketItemType.SKILL -> VioletPurple.copy(alpha = 0.2f)
                                    MarketItemType.MCP -> EmeraldBright.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = when (item.type) {
                                MarketItemType.PLUGIN -> CyanBright
                                MarketItemType.SKILL -> VioletPurple
                                MarketItemType.MCP -> EmeraldBright
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            if (item.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Ověřeno",
                                    tint = CyanBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "${item.author} · v${item.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Badges row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(item.type.titleCzech, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Slate800, labelColor = CyanBright)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(item.source.titleCzech, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (item.source) {
                                MarketSource.OFFICIAL -> EmeraldSuccess.copy(alpha = 0.2f)
                                MarketSource.COMMUNITY -> VioletPurple.copy(alpha = 0.2f)
                                MarketSource.GITHUB -> AmberWarning.copy(alpha = 0.2f)
                            },
                            labelColor = when (item.source) {
                                MarketSource.OFFICIAL -> EmeraldBright
                                MarketSource.COMMUNITY -> VioletPurple
                                MarketSource.GITHUB -> AmberWarning
                            }
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "${item.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate200)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "(${item.downloadsCount} stažení)", fontSize = 11.sp, color = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Popis",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate300
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate200
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type-specific content
                when (item.type) {
                    MarketItemType.PLUGIN -> {
                        Text(
                            text = "Požadovaná oprávnění",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate300
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.permissionsJson,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = Slate300,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    MarketItemType.SKILL -> {
                        Text(
                            text = "Systémová pravidla (Prompt)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate300
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.systemPrompt.ifBlank { "Výchozí systémový kontext pro model" },
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate200,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    MarketItemType.MCP -> {
                        Text(
                            text = "MCP Konfigurace",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate300
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Transport: ${item.transport}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldBright
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Příkaz: ${item.endpointOrCommand}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate300
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "GitHub repozitář",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate300
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.githubUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanBright,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        confirmButton = {
            if (itemUi.isInstalled) {
                Button(
                    onClick = { showUninstallConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Odinstalovat")
                }
            } else {
                Button(
                    onClick = {
                        onInstall()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Instalovat do OpenCode")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zavřít")
            }
        }
    )
}

@Composable
fun GithubImportDialog(
    onDismiss: () -> Unit,
    onImport: (url: String, name: String?, type: MarketItemType, desc: String?, details: String?) -> Unit
) {
    var githubUrl by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MarketItemType.PLUGIN) }
    var customName by remember { mutableStateOf("") }
    var customDesc by remember { mutableStateOf("") }
    var customDetails by remember { mutableStateOf("") }

    val parsedInfo = remember(githubUrl) {
        if (githubUrl.contains("github.com") || githubUrl.contains("/")) {
            MarketplaceCatalog.parseGithubUrl(githubUrl)
        } else null
    }

    LaunchedEffect(parsedInfo) {
        parsedInfo?.let {
            selectedType = it.detectedType
            if (customName.isBlank()) customName = it.suggestedName
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddLink, contentDescription = null, tint = CyanBright)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importovat z GitHub repozitáře")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Zadejte libovolný GitHub repozitář obsahující Plugin, Dovednost (Skill) nebo MCP Server. OpenCode jej automaticky analyzuje a nainstaluje do vašeho prostředí.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = githubUrl,
                    onValueChange = { githubUrl = it },
                    label = { Text("GitHub URL repozitáře") },
                    placeholder = { Text("https://github.com/owner/repository") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick preset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SuggestionChip(
                        onClick = { githubUrl = "https://github.com/modelcontextprotocol/servers/tree/main/src/sqlite" },
                        label = { Text("SQLite MCP", fontSize = 10.sp) }
                    )
                    SuggestionChip(
                        onClick = { githubUrl = "https://github.com/opencode-ide/skill-compose-master" },
                        label = { Text("Compose Skill", fontSize = 10.sp) }
                    )
                    SuggestionChip(
                        onClick = { githubUrl = "https://github.com/community-opencode/vim-opencode" },
                        label = { Text("Vim Plugin", fontSize = 10.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Typ rozšíření",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate300
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        MarketItemType.PLUGIN to "Plugin",
                        MarketItemType.SKILL to "Dovednost",
                        MarketItemType.MCP to "MCP"
                    ).forEach { (type, label) ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanBright,
                                selectedLabelColor = Slate950
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Název rozšíření") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customDesc,
                    onValueChange = { customDesc = it },
                    label = { Text("Vlastní popis (volitelné)") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedType) {
                    MarketItemType.SKILL -> {
                        OutlinedTextField(
                            value = customDetails,
                            onValueChange = { customDetails = it },
                            label = { Text("Systémová pravidla (Prompt)") },
                            placeholder = { Text("Jsi specialista na...") },
                            modifier = Modifier.fillMaxWidth().height(90.dp),
                            maxLines = 4
                        )
                    }
                    MarketItemType.MCP -> {
                        OutlinedTextField(
                            value = customDetails,
                            onValueChange = { customDetails = it },
                            label = { Text("Příkaz nebo URL serveru") },
                            placeholder = { Text("npx -y @modelcontextprotocol/server") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    MarketItemType.PLUGIN -> {
                        OutlinedTextField(
                            value = customDetails,
                            onValueChange = { customDetails = it },
                            label = { Text("Požadovaná oprávnění (oddělená čárkou)") },
                            placeholder = { Text("Čtení souborů, Síť") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (githubUrl.isNotBlank()) {
                        onImport(
                            githubUrl.trim(),
                            customName.trim().ifBlank { null },
                            selectedType,
                            customDesc.trim().ifBlank { null },
                            customDetails.trim().ifBlank { null }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Importovat a instalovat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )
}
