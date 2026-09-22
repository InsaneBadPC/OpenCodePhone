package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CiPipeline
import com.example.data.models.LocalLlmModel
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*

// =========================================================================
// 1. VISUÁLNÍ ARCHITEKTONICKÝ AST GRAF
// =========================================================================
@Composable
fun ArchitectureDiagramView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val diagram by viewModel.architectureDiagram.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(diagram.title, style = MaterialTheme.typography.titleMedium, color = Slate100)
                Text("Vizuální AST mapa závislostí mezi UI, ViewModelem, Repozitářem a Room SQLite", fontSize = 11.sp, color = Slate400)

                Spacer(modifier = Modifier.height(14.dp))

                // Canvas Graph Drawing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF070B14))
                        .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw connection lines
                        drawLine(
                            color = Color(0xFF38BDF8),
                            start = Offset(size.width * 0.5f, size.height * 0.15f),
                            end = Offset(size.width * 0.5f, size.height * 0.40f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0xFFA855F7),
                            start = Offset(size.width * 0.5f, size.height * 0.40f),
                            end = Offset(size.width * 0.5f, size.height * 0.65f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0xFF10B981),
                            start = Offset(size.width * 0.5f, size.height * 0.65f),
                            end = Offset(size.width * 0.25f, size.height * 0.88f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0xFF10B981),
                            start = Offset(size.width * 0.5f, size.height * 0.65f),
                            end = Offset(size.width * 0.75f, size.height * 0.88f),
                            strokeWidth = 3f
                        )
                    }

                    // Floating Interactive Nodes
                    diagram.nodes.forEach { node ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate800,
                            modifier = Modifier
                                .align(
                                    when (node.type) {
                                        "COMPOSABLE" -> Alignment.TopCenter
                                        "VIEWMODEL" -> Alignment.Center
                                        "REPOSITORY" -> Alignment.BottomCenter
                                        "DATABASE" -> Alignment.BottomStart
                                        else -> Alignment.BottomEnd
                                    }
                                )
                                .padding(12.dp)
                                .border(1.dp, Color(android.graphics.Color.parseColor(node.color)), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(node.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate100)
                                Text(node.type, fontSize = 9.sp, color = Color(android.graphics.Color.parseColor(node.color)))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Hrany toku dat (Edges):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate200)
                diagram.edges.forEach { edge ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CyanBright, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${edge.fromId} ➔ ${edge.toId}: ", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Slate400)
                        Text(edge.label, fontSize = 11.sp, color = Slate200)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. OFFLINE LOCAL LLM RUNTIME (GGUF / ONNX)
// =========================================================================
@Composable
fun OfflineLlmView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val models by viewModel.localLlmModels.collectAsState()
    val isOfflineMode by viewModel.isOfflineModeActive.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
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
                        Text("Lokální Offline LLM Runtime", style = MaterialTheme.typography.titleMedium, color = Slate100)
                        Text("Spouštějte modely přímo na zařízení bez internetu", fontSize = 11.sp, color = Slate400)
                    }
                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = {
                            viewModel.isOfflineModeActive.value = it
                            Toast.makeText(context, if (it) "Offline režim aktivován" else "Přepnuto na Cloud Gemini API", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Slate950, checkedTrackColor = EmeraldSuccess)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                models.forEach { model ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
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
                                    Text(model.name, fontWeight = FontWeight.Bold, color = Slate100, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge(containerColor = if (model.isLoadedInRam) EmeraldSuccess else Slate700) {
                                        Text(if (model.isLoadedInRam) "V RAM" else "DISK", color = if (model.isLoadedInRam) Slate950 else Slate300, fontSize = 9.sp)
                                    }
                                }
                                Text("${model.quantFormat} • ${model.parameterSize} param • ${model.sizeOnDiskMb} MB", fontSize = 10.sp, color = Slate400)
                                Text("Rychlost: ${model.tokensPerSec} tokenů/s", fontSize = 10.sp, color = CyanBright)
                            }

                            FilledTonalButton(
                                onClick = {
                                    viewModel.toggleOfflineLlmModel(model.id)
                                    Toast.makeText(context, "${model.name}: Stav paměti změněn", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = if (model.isLoadedInRam) Slate700 else EmeraldSuccess)
                            ) {
                                Text(if (model.isLoadedInRam) "Uvolnit" else "Nahrát", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. INTERAKTIVNÍ DEBUGGER
// =========================================================================
@Composable
fun DebuggerView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val breakpoints by viewModel.breakpoints.collectAsState()
    val watchExpressions by viewModel.watchExpressions.collectAsState()
    val isDebugging by viewModel.isDebugging.collectAsState()
    val debugLine by viewModel.currentDebugLine.collectAsState()
    val stack by viewModel.callStack.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
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
                        Text("Interaktivní Debugger", style = MaterialTheme.typography.titleMedium, color = Slate100)
                        Text(if (isDebugging) "Pozastaveno na řádku $debugLine" else "Debugger neaktivní", fontSize = 11.sp, color = if (isDebugging) AmberWarning else Slate400)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!isDebugging) {
                            Button(
                                onClick = { viewModel.startDebugging() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Slate950)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Debug", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            IconButton(onClick = { viewModel.stepOverDebug() }) {
                                Icon(Icons.Default.Redo, contentDescription = "Krok", tint = CyanBright)
                            }
                            IconButton(onClick = { viewModel.stopDebugging() }) {
                                Icon(Icons.Default.Stop, contentDescription = "Zastavit", tint = RoseError)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Watch Proměnné (Inspektor):", fontWeight = FontWeight.Bold, color = Slate200, fontSize = 12.sp)
                watchExpressions.forEach { w ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(w.expression, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CyanBright)
                        Text("${w.value} (${w.type})", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = EmeraldBright)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Body přerušení (Breakpoints):", fontWeight = FontWeight.Bold, color = Slate200, fontSize = 12.sp)
                breakpoints.forEach { bp ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RoseError))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${bp.filePath}:${bp.lineNumber}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Slate100)
                        if (bp.condition.isNotBlank()) {
                            Text(" [${bp.condition}]", fontSize = 10.sp, color = AmberWarning)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 4. CI/CD PIPELINES & WEBHOOKY
// =========================================================================
@Composable
fun CiPipelineView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val pipelines by viewModel.ciPipelines.collectAsState()
    val isRunning by viewModel.isCiRunning.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
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
                        Text("CI/CD Pipeline & Webhooky", style = MaterialTheme.typography.titleMedium, color = Slate100)
                        Text(".opencode-ci.yml simulace sestavení a testů", fontSize = 11.sp, color = Slate400)
                    }

                    Button(
                        onClick = { viewModel.runCiPipeline() },
                        enabled = !isRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                    ) {
                        Text(if (isRunning) "Běží..." else "Spustit CI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                pipelines.forEach { pipe ->
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
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${pipe.id} (${pipe.branch}) • ${pipe.triggeredAt}", fontWeight = FontWeight.Bold, color = Slate100, fontSize = 12.sp)
                                Badge(containerColor = if (pipe.status == "PASSED") EmeraldSuccess else RoseError) {
                                    Text(pipe.status, color = Slate950, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            pipe.steps.forEach { step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(step.name, fontSize = 11.sp, color = Slate200, modifier = Modifier.weight(1f))
                                    Text("${step.durationSeconds}s", fontSize = 10.sp, color = Slate400)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. REGEX PLAYGROUND & WEBSOCKET STUDIO
// =========================================================================
@Composable
fun RegexAndSocketsView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val pattern by viewModel.regexPattern.collectAsState()
    val testStr by viewModel.regexTestString.collectAsState()
    val matches by viewModel.regexMatches.collectAsState()
    val wsConnected by viewModel.wsConnected.collectAsState()
    val wsLogs by viewModel.wsLogs.collectAsState()
    var wsMsgInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Regex Playground
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Regex Playground s AI validací", style = MaterialTheme.typography.titleMedium, color = Slate100)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pattern,
                    onValueChange = {
                        viewModel.regexPattern.value = it
                        viewModel.evaluateRegex()
                    },
                    label = { Text("Regulární výraz") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = testStr,
                    onValueChange = {
                        viewModel.regexTestString.value = it
                        viewModel.evaluateRegex()
                    },
                    label = { Text("Testovací text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("Nalezené shody (${matches.size}):", fontWeight = FontWeight.Bold, color = CyanBright, fontSize = 12.sp)
                matches.forEach { m ->
                    Text("• ${m.matchText} (skupiny: ${m.groups.joinToString(", ")})", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = EmeraldBright)
                }
            }
        }

        // WebSocket & GraphQL Studio
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
                    Text("WebSocket Studio", style = MaterialTheme.typography.titleMedium, color = Slate100)
                    FilledTonalButton(
                        onClick = { viewModel.toggleWebSocket() },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = if (wsConnected) RoseError else EmeraldSuccess)
                    ) {
                        Text(if (wsConnected) "Odpojit" else "Připojit", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0A0F1D),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState())) {
                        wsLogs.forEach { log ->
                            Text(
                                text = "${if (log.isIncoming) "⬅️ " else "➡️ "}${log.timestamp}: ${log.payload}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = if (log.isIncoming) EmeraldBright else CyanBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = wsMsgInput,
                        onValueChange = { wsMsgInput = it },
                        placeholder = { Text("Zpráva pro WebSocket...", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(onClick = {
                        viewModel.sendWebSocketMessage(wsMsgInput)
                        wsMsgInput = ""
                    }) {
                        Text("Odeslat", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. AI HLASOVÝ VELÍN & DIKTOVÁNÍ KÓDU
// =========================================================================
@Composable
fun VoiceCodingView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.voiceHistory.collectAsState()
    val isListening by viewModel.isVoiceListening.collectAsState()
    var simulatedVoiceInput by remember { mutableStateOf("Vytvoř funkci pro odeslání HTTP požadavku") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("AI Hlasové programování (Voice Coding)", style = MaterialTheme.typography.titleMedium, color = Slate100)
                Text("Diktujte kód, spouštějte testy nebo ovládejte IDE hlasem v češtině", fontSize = 11.sp, color = Slate400)

                Spacer(modifier = Modifier.height(16.dp))

                FilledIconButton(
                    onClick = {
                        viewModel.triggerVoiceCommand(simulatedVoiceInput)
                    },
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (isListening) RoseError else CyanBright)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Mikrofon", tint = Slate950, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = simulatedVoiceInput,
                    onValueChange = { simulatedVoiceInput = it },
                    label = { Text("Simulovaný hlasový příkaz") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(onClick = { viewModel.triggerVoiceCommand("Spusť všechny unit testy") }) {
                        Text("Spustit testy", fontSize = 10.sp)
                    }
                    FilledTonalButton(onClick = { viewModel.triggerVoiceCommand("Ulož aktuální soubor") }) {
                        Text("Uložit soubor", fontSize = 10.sp)
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Historie hlasových příkazů:", fontWeight = FontWeight.Bold, color = Slate200, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                history.forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Slate800, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text("\"${item.recognizedText}\"", fontWeight = FontWeight.Bold, color = CyanBright, fontSize = 12.sp)
                        Text("Akce: ${item.executedAction} • ${item.timestamp}", fontSize = 10.sp, color = Slate300)
                    }
                }
            }
        }
    }
}
