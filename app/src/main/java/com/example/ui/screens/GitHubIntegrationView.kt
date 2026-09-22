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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.GitHubPullRequest
import com.example.data.models.GitHubRepository
import com.example.data.models.GitStatusFile
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*

@Composable
fun GitHubIntegrationView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val isConnected by viewModel.gitHubConnected.collectAsState()
    val gitUser by viewModel.gitHubUser.collectAsState()
    val currentBranch by viewModel.currentGitBranch.collectAsState()
    val availableBranches by viewModel.availableGitBranches.collectAsState()
    val commitMsg by viewModel.gitCommitMessage.collectAsState()
    val repositories by viewModel.gitHubRepositories.collectAsState()
    val statusFiles by viewModel.gitStatusFiles.collectAsState()
    val commitHistory by viewModel.gitCommitHistory.collectAsState()
    val pullRequests by viewModel.gitHubPullRequests.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0 = Změny (Git Status & Commit), 1 = Repozitáře, 2 = Větve & PR, 3 = Historie commitů
    var showNewBranchDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(14.dp)
    ) {
        // GitHub Profile & Connection Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF24292E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Code, contentDescription = "GitHub", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "GitHub / @$gitUser",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(EmeraldSuccess, CircleShape)
                                )
                            }
                            Text(
                                text = "Nativní Git SSH & Personal Access Token ověřen",
                                fontSize = 11.sp,
                                color = EmeraldSuccess
                            )
                        }
                    }

                    // Push / Pull Quick Action Group
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.gitPull { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Slate800, contentColor = Slate200),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Pull", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.gitPush { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Push", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Active Branch Indicator
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Slate950,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ForkRight, contentDescription = null, tint = CyanBright, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aktivní větev: ", fontSize = 11.sp, color = Slate400)
                            Text(currentBranch, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanBright, fontFamily = FontFamily.Monospace)
                        }

                        Text(
                            text = "Repozitář: opencode-android-ide",
                            fontSize = 11.sp,
                            color = Slate300
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub-tabs Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Slate900,
            contentColor = CyanBright
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Změny (${statusFiles.size})", fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Repozitáře (${repositories.size})", fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("PR & Větve", fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Commity", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> {
                // Git Changes & Commit View
                Column(modifier = Modifier.fillMaxSize()) {
                    // Staging Actions Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stagedCount = statusFiles.count { it.isStaged }
                        Text(
                            text = "Pracovní strom ($stagedCount z ${statusFiles.size} připraveno)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate300
                        )

                        TextButton(
                            onClick = { viewModel.stageAllFiles() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Připravit vše (git add .)", color = CyanBright, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(statusFiles) { file ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleStageFile(file.filePath) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Checkbox(
                                            checked = file.isStaged,
                                            onCheckedChange = { viewModel.toggleStageFile(file.filePath) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = CyanBright,
                                                uncheckedColor = Slate600,
                                                checkmarkColor = Slate950
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = file.filePath,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Slate100,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = if (file.isStaged) "Připraveno ke commitu (Staged)" else "Nepřipraveno (Unstaged)",
                                                fontSize = 10.sp,
                                                color = if (file.isStaged) EmeraldSuccess else Slate400
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (file.status == "MODIFIED") AmberWarning.copy(alpha = 0.2f) else EmeraldSuccess.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = file.status,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (file.status == "MODIFIED") AmberWarning else EmeraldSuccess,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Commit Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate900,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Vytvořit Git Commit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate200)

                                TextButton(
                                    onClick = { viewModel.generateAiCommitMessage() },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanBright, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Zpráva", fontSize = 11.sp, color = CyanBright)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = commitMsg,
                                onValueChange = { viewModel.gitCommitMessage.value = it },
                                placeholder = { Text("Zadejte popis změn (např. feat: přidaná podpora pro skilly)...", fontSize = 11.sp, color = Slate500) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2,
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Slate100),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate950,
                                    unfocusedContainerColor = Slate950,
                                    focusedBorderColor = CyanBright,
                                    unfocusedBorderColor = Slate800
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    viewModel.commitGitChanges { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Commitnout změny na '$currentBranch'", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Repositories List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(repositories, key = { it.id }) { repo ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Folder, contentDescription = null, tint = CyanBright, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = repo.fullName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate100
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (repo.isPrivate) AmberWarning.copy(alpha = 0.2f) else Slate800
                                    ) {
                                        Text(
                                            text = if (repo.isPrivate) "Private" else "Public",
                                            fontSize = 10.sp,
                                            color = if (repo.isPrivate) AmberWarning else Slate300,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = repo.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate300,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⭐ ${repo.starsCount} • ${repo.updatedAt}", fontSize = 11.sp, color = Slate400)

                                    Button(
                                        onClick = {
                                            viewModel.cloneGitHubRepo(repo) { msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = CyanBright),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Klonovat do workspace", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Branches & Pull Requests
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Branch Selector Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Větve projektu", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Slate100)

                                TextButton(
                                    onClick = { showNewBranchDialog = true },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = CyanBright, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Nová větev", color = CyanBright, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            availableBranches.forEach { branch ->
                                val isSelected = branch == currentBranch
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) Slate800 else Slate950,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable { viewModel.switchGitBranch(branch) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.ForkRight,
                                                contentDescription = null,
                                                tint = if (isSelected) CyanBright else Slate500,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = branch,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) CyanBright else Slate300,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        if (isSelected) {
                                            Text("Aktivní", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Pull Requests
                    Text("Pull Requesty (${pullRequests.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate300)

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pullRequests) { pr ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#${pr.number} ${pr.title}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate100
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (pr.status == "OPEN") EmeraldSuccess.copy(alpha = 0.2f) else Slate800
                                        ) {
                                            Text(
                                                text = pr.status,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (pr.status == "OPEN") EmeraldSuccess else Slate400,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Větev: ${pr.branch} • Autor: ${pr.author} • Komentáře: ${pr.commentsCount}",
                                        fontSize = 10.sp,
                                        color = Slate400
                                    )
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Commit History
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(commitHistory) { commit ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = commit.message,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate100
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Slate800
                                    ) {
                                        Text(
                                            text = commit.hash,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = CyanBright,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Commitoval ${commit.author} • ${commit.timeAgo}",
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewBranchDialog) {
        var branchName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewBranchDialog = false },
            title = { Text("Vytvořit novou Git větev", color = Slate100, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    label = { Text("Název větve (např. feature/nova-funkce)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (branchName.isNotBlank()) {
                            viewModel.createGitBranch(branchName) { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                            showNewBranchDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950)
                ) {
                    Text("Vytvořit větev")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewBranchDialog = false }) {
                    Text("Zrušit", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}
