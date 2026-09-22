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
import com.example.data.models.OracleCloudHost
import com.example.data.models.TerminalExecutionTarget
import com.example.data.models.TermuxInstalledPackage
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*

@Composable
fun TermuxAndOracleView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val executionTarget by viewModel.terminalTarget.collectAsState()
    val packages by viewModel.termuxPackages.collectAsState()
    val oracleHost by viewModel.oracleCloudHost.collectAsState()
    val terminalHistory by viewModel.terminalHistory.collectAsState()
    val terminalCmd by viewModel.terminalInput.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0 = Terminál, 1 = Správce balíčků (pkg/apt/pip), 2 = Oracle Cloud SSH

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
    ) {
        // Mode & Environment Switcher
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Slate900,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cílové prostředí vykonávání:",
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate300,
                        fontSize = 11.sp
                    )
                    Badge(
                        containerColor = when (executionTarget) {
                            TerminalExecutionTarget.LOCAL_TERMUX -> EmeraldSuccess
                            TerminalExecutionTarget.ORACLE_CLOUD_OCI -> AmberWarning
                            TerminalExecutionTarget.DOCKER_CONTAINER -> CyanBright
                        }
                    ) {
                        Text(
                            text = when (executionTarget) {
                                TerminalExecutionTarget.LOCAL_TERMUX -> "LOKÁLNÍ TERMUX"
                                TerminalExecutionTarget.ORACLE_CLOUD_OCI -> "ORACLE CLOUD VM"
                                TerminalExecutionTarget.DOCKER_CONTAINER -> "DEV DOCKER"
                            },
                            color = Slate950,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = executionTarget == TerminalExecutionTarget.LOCAL_TERMUX,
                        onClick = { viewModel.setTerminalExecutionTarget(TerminalExecutionTarget.LOCAL_TERMUX) },
                        label = { Text("📱 Lokální Termux", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = executionTarget == TerminalExecutionTarget.ORACLE_CLOUD_OCI,
                        onClick = { viewModel.setTerminalExecutionTarget(TerminalExecutionTarget.ORACLE_CLOUD_OCI) },
                        label = { Text("☁️ Oracle Cloud", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = executionTarget == TerminalExecutionTarget.DOCKER_CONTAINER,
                        onClick = { viewModel.setTerminalExecutionTarget(TerminalExecutionTarget.DOCKER_CONTAINER) },
                        label = { Text("🐳 Docker", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Subtabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Slate900,
            contentColor = CyanBright
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("💻 Konzole", fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("📦 Balíčky (pkg/apt)", fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("⚡ OCI Instance", fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> {
                // Interactive Shell
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(terminalHistory) { line ->
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = when (executionTarget) {
                                                TerminalExecutionTarget.LOCAL_TERMUX -> "termux@localhost:~$ "
                                                TerminalExecutionTarget.ORACLE_CLOUD_OCI -> "opc@oracle-vm-frankfurt:~$ "
                                                TerminalExecutionTarget.DOCKER_CONTAINER -> "root@docker-container:/# "
                                            },
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldBright
                                        )
                                        Text(
                                            text = line.command,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = Slate100
                                        )
                                    }
                                    if (line.output.isNotBlank()) {
                                        Text(
                                            text = line.output,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = if (line.exitCode == 0) Slate300 else RoseError,
                                            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = terminalCmd,
                                onValueChange = { viewModel.updateTerminalInput(it) },
                                placeholder = { Text("Zadejte příkaz (např. pkg install htop, oci compute...)", color = Slate600, fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Slate100),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate900,
                                    unfocusedContainerColor = Slate900,
                                    focusedBorderColor = CyanBright,
                                    unfocusedBorderColor = Slate700
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = { viewModel.runTerminalCommand() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Spustit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Package Manager (Termux pkg / pip / npm)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Dostupné a nainstalované balíčky prostředí", style = MaterialTheme.typography.titleMedium, color = Slate100)
                    Text("Instalujte kompilátory (clang, rust), runtime prostředí (python, nodejs) nebo nástroje přímo do prostředí.", fontSize = 12.sp, color = Slate400)

                    packages.forEach { pkg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(pkg.name, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = CyanBright, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Badge(containerColor = Slate800) {
                                            Text(pkg.category, fontSize = 9.sp, color = Slate300)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Verze: ${pkg.version} • Velikost: ${pkg.sizeMb} MB", fontSize = 11.sp, color = Slate400)
                                }

                                if (pkg.isInstalled) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Nainstalováno", fontSize = 10.sp, color = EmeraldSuccess) },
                                        leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp)) }
                                    )
                                } else {
                                    Button(
                                        onClick = {
                                            viewModel.installTermuxPackage(pkg.name) { msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Slate950),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Instalovat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Oracle Cloud Host Configuration
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                    Text("Oracle Cloud Infrastructure (OCI)", style = MaterialTheme.typography.titleMedium, color = Slate100)
                                    Text("Propojeno s Always Free Compute instancí", style = MaterialTheme.typography.bodySmall, color = Slate400)
                                }
                                Badge(containerColor = EmeraldSuccess) {
                                    Text("SSH PŘIPOJENO", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Veřejná IP adresa: ${oracleHost.hostIp}", fontFamily = FontFamily.Monospace, color = CyanBright, fontSize = 12.sp)
                            Text("Region: ${oracleHost.region}", fontSize = 12.sp, color = Slate300)
                            Text("Uživatelské jméno: ${oracleHost.username}", fontSize = 12.sp, color = Slate300)
                            Text("Typ instance: ${oracleHost.instanceType}", fontSize = 12.sp, color = Slate300)

                            Spacer(modifier = Modifier.height(14.dp))

                            FilledTonalButton(
                                onClick = {
                                    viewModel.setTerminalExecutionTarget(TerminalExecutionTarget.ORACLE_CLOUD_OCI)
                                    viewModel.updateTerminalInput("uname -a && free -h")
                                    viewModel.runTerminalCommand()
                                    selectedTab = 0
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Slate800, contentColor = CyanBright),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Otevřít vzdálenou SSH relaci")
                            }
                        }
                    }
                }
            }
        }
    }
}
