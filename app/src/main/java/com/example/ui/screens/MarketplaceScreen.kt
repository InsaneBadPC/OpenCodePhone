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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketItemType
import com.example.data.model.MarketSource
import com.example.data.model.MarketplaceItem
import com.example.ui.MarketplaceItemUi
import com.example.ui.OpenCodeViewModel
import com.example.ui.components.GithubImportDialog
import com.example.ui.components.MarketplaceDetailDialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val items by viewModel.filteredMarketItems.collectAsState()
    val searchQuery by viewModel.marketSearchQuery.collectAsState()
    val typeFilter by viewModel.marketTypeFilter.collectAsState()
    val sourceFilter by viewModel.marketSourceFilter.collectAsState()

    var showGithubImportDialog by remember { mutableStateOf(false) }
    var selectedItemForDetail by remember { mutableStateOf<MarketplaceItemUi?>(null) }

    val installedCount = items.count { it.isInstalled }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanBright.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = CyanBright, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Tržiště rozšíření",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = Slate800) {
                                    Text(
                                        text = "${items.size}",
                                        color = CyanBright,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Oficiální & komunitní Pluginy, Dovednosti a MCP",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showGithubImportDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Slate800,
                            contentColor = CyanBright
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_github_import")
                    ) {
                        Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Z GitHubu", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate950,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setMarketSearchQuery(it) },
                placeholder = { Text("Hledat rozšíření, nástroje, klíčová slova...", color = Slate400, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Hledat", tint = CyanBright, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setMarketSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Vymazat", tint = Slate400)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900,
                    focusedBorderColor = CyanBright,
                    unfocusedBorderColor = Slate800,
                    focusedTextColor = Slate100,
                    unfocusedTextColor = Slate200
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("input_market_search")
            )

            // Type Filter Chips (All, Plugins, Skills, MCP)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Vše" to Icons.Default.AllInclusive,
                    "Pluginy" to Icons.Default.Extension,
                    "Dovednosti" to Icons.Default.Psychology,
                    "MCP Servery" to Icons.Default.Hub
                ).forEach { (typeLabel, icon) ->
                    val isSelected = typeFilter == typeLabel
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setMarketTypeFilter(typeLabel) },
                        label = { Text(typeLabel, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) Slate950 else Slate400
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanBright,
                            selectedLabelColor = Slate950,
                            containerColor = Slate900,
                            labelColor = Slate300
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CyanBright else Slate800
                        )
                    )
                }
            }

            // Source Filter Chips (All sources, Official, Community, GitHub)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Všechny zdroje" to null,
                    "Oficiální" to Icons.Default.Verified,
                    "Komunitní" to Icons.Default.Groups,
                    "Z GitHubu" to Icons.Default.Code
                ).forEach { (sourceLabel, icon) ->
                    val isSelected = sourceFilter == sourceLabel
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setMarketSourceFilter(sourceLabel) },
                        label = { Text(sourceLabel, fontSize = 11.sp) },
                        leadingIcon = icon?.let {
                            {
                                Icon(
                                    it,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSelected) Slate950 else Slate400
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (sourceLabel) {
                                "Oficiální" -> EmeraldBright
                                "Komunitní" -> VioletPurple
                                "Z GitHubu" -> AmberWarning
                                else -> Slate300
                            },
                            selectedLabelColor = Slate950,
                            containerColor = Slate900,
                            labelColor = Slate400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color.Transparent else Slate800
                        )
                    )
                }
            }

            // Stat bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zobrazeno: ${items.size} položek",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Text(
                    text = "Nainstalováno: $installedCount",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldBright
                )
            }

            // Items List
            if (items.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
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
                            text = "Nebyly nalezeny žádné položky",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate300
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zkuste změnit hledaný výraz nebo importovat z GitHubu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showGithubImportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                        ) {
                            Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Přidat z GitHub URL")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(items, key = { it.item.id }) { itemUi ->
                        MarketplaceCard(
                            itemUi = itemUi,
                            onClick = { selectedItemForDetail = itemUi },
                            onInstall = {
                                viewModel.installMarketItem(itemUi.item)
                                Toast.makeText(context, "${itemUi.item.name} byl nainstalován!", Toast.LENGTH_SHORT).show()
                            },
                            onUninstall = {
                                viewModel.uninstallMarketItem(itemUi.item)
                                Toast.makeText(context, "${itemUi.item.name} byl odebrán.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail dialog
    selectedItemForDetail?.let { currentDetail ->
        // Keep updated state if installed
        val latestUi = items.firstOrNull { it.item.id == currentDetail.item.id } ?: currentDetail
        MarketplaceDetailDialog(
            itemUi = latestUi,
            onDismiss = { selectedItemForDetail = null },
            onInstall = {
                viewModel.installMarketItem(latestUi.item)
                Toast.makeText(context, "${latestUi.item.name} byl nainstalován!", Toast.LENGTH_SHORT).show()
            },
            onUninstall = {
                viewModel.uninstallMarketItem(latestUi.item)
                Toast.makeText(context, "${latestUi.item.name} byl odebrán.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // GitHub import dialog
    if (showGithubImportDialog) {
        GithubImportDialog(
            onDismiss = { showGithubImportDialog = false },
            onImport = { url, name, type, desc, details ->
                viewModel.importGithubRepository(
                    githubUrl = url,
                    customName = name,
                    targetType = type,
                    customDescription = desc,
                    customDetails = details
                )
                showGithubImportDialog = false
                Toast.makeText(context, "Rozšíření z GitHubu bylo úspěšně importováno a nainstalováno!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun MarketplaceCard(
    itemUi: MarketplaceItemUi,
    onClick: () -> Unit,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val item = itemUi.item
    val isInstalled = itemUi.isInstalled

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isInstalled) Slate900.copy(alpha = 0.95f) else Slate900
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isInstalled) {
            androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("market_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                    val iconTint = when (item.type) {
                        MarketItemType.PLUGIN -> CyanBright
                        MarketItemType.SKILL -> VioletPurple
                        MarketItemType.MCP -> EmeraldBright
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100,
                                maxLines = 1
                            )
                            if (item.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Ověřeno",
                                    tint = CyanBright,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Text(
                            text = "${item.author} · v${item.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }
                }

                // Type tag
                AssistChip(
                    onClick = onClick,
                    label = { Text(item.type.titleCzech, fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Slate800,
                        labelColor = when (item.type) {
                            MarketItemType.PLUGIN -> CyanBright
                            MarketItemType.SKILL -> VioletPurple
                            MarketItemType.MCP -> EmeraldBright
                        }
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = Slate300,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer row with stats and Action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Source badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (item.source) {
                                    MarketSource.OFFICIAL -> EmeraldSuccess.copy(alpha = 0.2f)
                                    MarketSource.COMMUNITY -> VioletPurple.copy(alpha = 0.2f)
                                    MarketSource.GITHUB -> AmberWarning.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.source.titleCzech,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (item.source) {
                                MarketSource.OFFICIAL -> EmeraldBright
                                MarketSource.COMMUNITY -> VioletPurple
                                MarketSource.GITHUB -> AmberWarning
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "${item.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate300)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "· ${item.downloadsCount}", fontSize = 11.sp, color = Slate500)
                }

                // Action button
                if (isInstalled) {
                    FilledTonalButton(
                        onClick = onClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = EmeraldSuccess.copy(alpha = 0.2f),
                            contentColor = EmeraldBright
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nainstalováno", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onInstall,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Instalovat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
