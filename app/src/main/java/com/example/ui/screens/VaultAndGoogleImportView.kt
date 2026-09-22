package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.GoogleImportItem
import com.example.data.models.GoogleImportType
import com.example.data.models.VaultSecret
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*

@Composable
fun VaultAndGoogleImportView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val secrets by viewModel.vaultSecrets.collectAsState()
    val googleImports by viewModel.googleImports.collectAsState()
    val context = LocalContext.current

    var selectedSection by remember { mutableStateOf(0) } // 0 = Google Docs & Sheets Import, 1 = Secrets Vault (.env)

    var showGoogleImportDialog by remember { mutableStateOf(false) }
    var showAddSecretDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Switcher tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = Slate900,
            contentColor = CyanBright
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("📄 Google Docs & Sheets Import", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("🔐 Secrets Vault (.env)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedSection == 0) {
            // GOOGLE DOCS & SHEETS IMPORT
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
                        Column {
                            Text("Přímý import z Google Workspace", style = MaterialTheme.typography.titleMedium, color = Slate100)
                            Text("Importujte proměnné z Google Tabulek a specifikace z Google Docs", style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }

                        Button(
                            onClick = { showGoogleImportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nový import", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    googleImports.forEach { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Badge(
                                            containerColor = if (item.type == GoogleImportType.GOOGLE_SHEETS) EmeraldSuccess else CyanBright
                                        ) {
                                            Text(
                                                text = if (item.type == GoogleImportType.GOOGLE_SHEETS) "Google Tabulky" else "Google Dokument",
                                                color = Slate950,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(item.title, style = MaterialTheme.typography.titleSmall, color = Slate100, fontSize = 13.sp)
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.createNewFile(item.targetFileName, item.parsedContent)
                                            Toast.makeText(context, "Synchronizováno do ${item.targetFileName}", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Slate700, contentColor = Slate100),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Synchronizovat", fontSize = 10.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Cílový soubor: ${item.targetFileName} • Naposledy: ${item.lastSyncTime}", fontSize = 10.sp, color = Slate400)

                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF090D16),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.parsedContent.take(200) + if (item.parsedContent.length > 200) "..." else "",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Slate300,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // SECRETS VAULT (.env)
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
                        Column {
                            Text("Šifrovaný trezor klíčů (.env Vault)", style = MaterialTheme.typography.titleMedium, color = Slate100)
                            Text("Bezpečně spravujte API tokeny a hesla pro Dev, Staging a Prod", style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }

                        Button(
                            onClick = { showAddSecretDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPurple, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Přidat klíč", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    secrets.forEach { secret ->
                        var isRevealed by remember { mutableStateOf(false) }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Badge(containerColor = if (secret.environment == "Production") RoseError else IndigoAccent) {
                                            Text(secret.environment, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(secret.key, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = CyanBright, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isRevealed) secret.value else "••••••••••••••••••••",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Slate300
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { isRevealed = !isRevealed }, modifier = Modifier.size(32.dp)) {
                                        Icon(
                                            imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = Slate400,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.removeSecret(secret.key)
                                            Toast.makeText(context, "Klíč odebrán", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = RoseError, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGoogleImportDialog) {
        var importUrl by remember { mutableStateOf("") }
        var importTitle by remember { mutableStateOf("") }
        var importType by remember { mutableStateOf(GoogleImportType.GOOGLE_SHEETS) }

        AlertDialog(
            onDismissRequest = { showGoogleImportDialog = false },
            title = { Text("Import z Google Dokumentů / Tabulek", color = Slate100, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Zadejte sdílený odkaz na Google Tabulku (např. konfigurace .env proměnných) nebo Google Dokument (architektura, specifikace).", fontSize = 12.sp, color = Slate300)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = importType == GoogleImportType.GOOGLE_SHEETS,
                            onClick = { importType = GoogleImportType.GOOGLE_SHEETS },
                            label = { Text("Google Tabulky (.env / csv)") }
                        )
                        FilterChip(
                            selected = importType == GoogleImportType.GOOGLE_DOCS,
                            onClick = { importType = GoogleImportType.GOOGLE_DOCS },
                            label = { Text("Google Dokumenty (md)") }
                        )
                    }

                    OutlinedTextField(
                        value = importTitle,
                        onValueChange = { importTitle = it },
                        label = { Text("Název položky") },
                        placeholder = { Text("např. Produkční proměnné") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = importUrl,
                        onValueChange = { importUrl = it },
                        label = { Text("URL odkaz Google dokumentu/tabulky") },
                        placeholder = { Text("https://docs.google.com/spreadsheets/d/...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validUrl = if (importUrl.isBlank()) "https://docs.google.com/spreadsheets/d/demo_sheet_77" else importUrl
                        viewModel.importFromGoogleUrl(validUrl, importType, importTitle) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                        showGoogleImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                ) {
                    Text("Importovat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleImportDialog = false }) {
                    Text("Zrušit", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }

    if (showAddSecretDialog) {
        var newKey by remember { mutableStateOf("") }
        var newValue by remember { mutableStateOf("") }
        var newEnv by remember { mutableStateOf("Production") }

        AlertDialog(
            onDismissRequest = { showAddSecretDialog = false },
            title = { Text("Přidat tajný klíč do Trezoru", color = Slate100, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("Klíč (např. API_KEY)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        label = { Text("Hodnota") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Development", "Staging", "Production").forEach { env ->
                            FilterChip(
                                selected = newEnv == env,
                                onClick = { newEnv = env },
                                label = { Text(env, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addSecret(newKey, newValue, newEnv)
                        showAddSecretDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPurple, contentColor = Color.White)
                ) {
                    Text("Uložit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSecretDialog = false }) {
                    Text("Zrušit", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}
