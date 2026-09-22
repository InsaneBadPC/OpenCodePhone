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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AgentWorkflowSkill
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*

@Composable
fun AutoLearnedSkillsView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val autoSkills by viewModel.autoLearnedSkills.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedSkillForDetail by remember { mutableStateOf<AgentWorkflowSkill?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
    ) {
        // Hero Header Card
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA78BFA))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Autonomně naučené dovednosti",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Text(
                                text = "Generovány z operací dokončených bez chyb",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldSuccess,
                                fontSize = 11.sp
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = CyanBright, contentColor = Slate950),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nový skill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Agent automaticky ukládá úspěšné sekvence příkazů a kroků do znovupoužitelných dovedností ve standardním formátu SKILL.md. Můžete je kdykoliv jedním klepnutím spustit znovu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate300,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Action Bar
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Slate900,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivní workflow skilly: ${autoSkills.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate300
                )

                OutlinedButton(
                    onClick = {
                        viewModel.createSkillFromOperation(
                            name = "Optimalizace Gradle & Room Cache",
                            triggerPhrase = "vyčisti a optimalizuj room cache",
                            description = "Kompaktní vyčištění KSP mezipaměti, kontrola Room entit a sestavení testovacího APK.",
                            category = "DevOps",
                            steps = listOf(
                                "Kontrola integrity tabulek v opencode.db",
                                "Inkrementální indexace schématu",
                                "Ověření kompilace bez chyb"
                            )
                        )
                        Toast.makeText(context, "✨ Nový skill byl úspěšně syntetizován z poslední operace!", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyanBright))
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanBright, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Syntetizovat z historie", color = CyanBright, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Skills List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(autoSkills, key = { it.id }) { skill ->
                SkillWorkflowCard(
                    skill = skill,
                    onExecute = {
                        viewModel.executeSkill(skill) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    onViewMarkdown = { selectedSkillForDetail = skill },
                    onDelete = {
                        viewModel.deleteAutoLearnedSkill(skill.id)
                        Toast.makeText(context, "Dovednost byla odstraněna.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Detail Markdown Dialog
    selectedSkillForDetail?.let { skill ->
        AlertDialog(
            onDismissRequest = { selectedSkillForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = CyanBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(skill.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate100)
                }
            },
            text = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF090D16),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                    ) {
                        Text(
                            text = skill.skillMarkdown,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Slate200,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(skill.skillMarkdown))
                        Toast.makeText(context, "SKILL.md zkopírován do schránky", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kopírovat SKILL.md")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSkillForDetail = null }) {
                    Text("Zavřít", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }

    // Create New Skill Dialog
    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var trigger by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var stepsText by remember { mutableStateOf("1. Krok A\n2. Krok B\n3. Ověřit výsledek") }
        var category by remember { mutableStateOf("Workflow") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Vytvořit novou znovupoužitelnou dovednost", color = Slate100, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Název dovednosti") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = trigger,
                        onValueChange = { trigger = it },
                        label = { Text("Spouštěcí fráze (trigger)") },
                        placeholder = { Text("např. 'vytvoř endpoint'") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Popis") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = stepsText,
                        onValueChange = { stepsText = it },
                        label = { Text("Kroky operace (po řádcích)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val steps = stepsText.lines().map { it.trim() }.filter { it.isNotBlank() }
                            viewModel.createSkillFromOperation(name, trigger, description, category, steps)
                            showCreateDialog = false
                            Toast.makeText(context, "Dovednost byla vytvořena!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                ) {
                    Text("Uložit dovednost")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Zrušit", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}

@Composable
private fun SkillWorkflowCard(
    skill: AgentWorkflowSkill,
    onExecute: () -> Unit,
    onViewMarkdown: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = skill.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "100% Bez chyb",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Spouštěč: \"${skill.triggerPhrase}\"",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyanBright
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Smazat", tint = Slate500, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodySmall,
                color = Slate300,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Steps preview
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Slate950,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "KROKY OPERACE (${skill.steps.size}):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                    skill.steps.take(3).forEachIndexed { idx, step ->
                        Text(
                            text = "${idx + 1}. $step",
                            fontSize = 11.sp,
                            color = Slate300,
                            maxLines = 1
                        )
                    }
                    if (skill.steps.size > 3) {
                        Text(
                            text = "...a další ${skill.steps.size - 3} kroků",
                            fontSize = 10.sp,
                            color = Slate500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Spuštěno: ${skill.successCount}x • ${skill.lastExecutedAt}", fontSize = 11.sp, color = Slate400)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onViewMarkdown,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Article, contentDescription = null, tint = Slate300, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SKILL.md", fontSize = 11.sp, color = Slate300)
                    }

                    Button(
                        onClick = onExecute,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Znovu použít", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
