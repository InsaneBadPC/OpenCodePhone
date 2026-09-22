package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CloudServiceIntegration
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*

@Composable
fun CloudIntegrationsView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val services by viewModel.cloudServices.collectAsState()
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Vše") }

    val categories = listOf("Vše", "Úložiště & Záloha", "Databáze & Auth", "AI Design", "Dokumenty & Data", "Cloud VM")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
    ) {
        // Header
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
                    Column {
                        Text("Integrace & Cloud Connectory", style = MaterialTheme.typography.titleMedium, color = Slate100)
                        Text("Propojte OpenCode s Google Diskem, Supabase, Firefly, NotebookLM a OCI", fontSize = 11.sp, color = Slate400)
                    }
                    Badge(containerColor = CyanBright) {
                        Text("${services.count { it.isConnected }} PŘIPOJENO", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                    containerColor = Slate800,
                    contentColor = CyanBright,
                    edgePadding = 0.dp,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    categories.forEach { cat ->
                        Tab(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            text = { Text(cat, fontSize = 11.sp, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filtered = if (selectedCategory == "Vše") services else services.filter { it.serviceCategory == selectedCategory }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { service ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (service.isConnected) CyanBright.copy(alpha = 0.4f) else Slate800,
                            shape = RoundedCornerShape(12.dp)
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (service.isConnected) Slate800 else Color(0xFF131B2E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (service.id) {
                                            "cs-gdrive" -> Icons.Default.CloudQueue
                                            "cs-gworkspace" -> Icons.Default.Description
                                            "cs-supabase" -> Icons.Default.Storage
                                            "cs-firefly" -> Icons.Default.Palette
                                            "cs-notebooklm" -> Icons.Default.MenuBook
                                            "cs-gcp" -> Icons.Default.Cloud
                                            else -> Icons.Default.Computer
                                        },
                                        contentDescription = null,
                                        tint = if (service.isConnected) CyanBright else Slate500,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(service.name, style = MaterialTheme.typography.titleSmall, color = Slate100, fontWeight = FontWeight.Bold)
                                    Text(service.serviceCategory, fontSize = 10.sp, color = Slate400)
                                }
                            }

                            Switch(
                                checked = service.isConnected,
                                onCheckedChange = {
                                    viewModel.toggleCloudService(service.id)
                                    val status = if (!service.isConnected) "Služba ${service.name} připojena" else "Služba ${service.name} odpojena"
                                    Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Slate950,
                                    checkedTrackColor = CyanBright,
                                    uncheckedThumbColor = Slate400,
                                    uncheckedTrackColor = Slate800
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(service.description, fontSize = 11.sp, color = Slate300)

                        if (service.isConnected && service.connectedAccountOrProject != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF090D17),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = service.connectedAccountOrProject,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Slate300
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Available Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            service.availableActions.forEach { act ->
                                FilledTonalButton(
                                    onClick = {
                                        Toast.makeText(context, "${service.name}: Provádím akci '$act'", Toast.LENGTH_SHORT).show()
                                    },
                                    enabled = service.isConnected,
                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Slate800, contentColor = Slate200),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(act, fontSize = 9.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
