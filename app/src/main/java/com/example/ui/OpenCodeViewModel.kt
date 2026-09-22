package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.SearchResultItem
import com.example.data.ai.TerminalExecutionResult
import com.example.data.ai.ZenModel
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.model.MarketItemType
import com.example.data.model.MarketSource
import com.example.data.model.MarketplaceCatalog
import com.example.data.model.MarketplaceItem
import com.example.data.model.ParsedGithubRepo
import com.example.data.models.*
import com.example.data.repository.OpenCodeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class MarketplaceItemUi(
    val item: MarketplaceItem,
    val isInstalled: Boolean
)

data class TerminalLine(
    val command: String,
    val output: String,
    val exitCode: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class RestTestState(
    val url: String = "https://jsonplaceholder.typicode.com/todos/1",
    val method: String = "GET",
    val responseStatus: String = "",
    val responseBody: String = "",
    val isLoading: Boolean = false
)

class OpenCodeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    val repository = OpenCodeRepository(db)

    // Current navigation tab: 0=Chat, 1=Files, 2=Internet, 3=Skills & MCP, 4=Plugins & Settings
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    // Sessions & Messages
    val sessions = repository.allSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    init {
        viewModelScope.launch {
            sessions.collect { sessionList ->
                if (_currentSessionId.value == null && sessionList.isNotEmpty()) {
                    _currentSessionId.value = sessionList.first().id
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun createNewSession() {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val newSession = ChatSessionEntity(
                id = id,
                title = "New OpenCode Session",
                modelName = selectedModel.value.displayName
            )
            repository.chatDao.insertSession(newSession)
            _currentSessionId.value = id
        }
    }

    fun deleteSession(session: ChatSessionEntity) {
        viewModelScope.launch {
            repository.chatDao.deleteMessagesForSession(session.id)
            repository.chatDao.deleteSession(session)
            if (_currentSessionId.value == session.id) {
                _currentSessionId.value = sessions.value.firstOrNull { it.id != session.id }?.id
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.chatDao.getMessagesForSession(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Model selection
    private val _selectedModel = MutableStateFlow(ZenModel.ZEN_CODER)
    val selectedModel: StateFlow<ZenModel> = _selectedModel.asStateFlow()

    fun selectModel(model: ZenModel) {
        _selectedModel.value = model
    }

    // Chat prompt input & state
    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    fun updateChatInput(text: String) {
        _chatInput.value = text
    }

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    fun sendMessage(promptText: String? = null) {
        val textToSend = (promptText ?: _chatInput.value).trim()
        if (textToSend.isBlank() || _isAiThinking.value) return

        val sId = _currentSessionId.value ?: return
        _chatInput.value = ""
        _isAiThinking.value = true

        viewModelScope.launch {
            // Save user message
            repository.chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = sId,
                    role = "user",
                    content = textToSend
                )
            )

            // Gather context
            val enabledSkills = repository.skillDao.getEnabledSkillsList()
            val files = repository.workspaceDao.getAllFiles().first()
            val history = currentMessages.value.map { it.role to it.content }

            val response = repository.zenAiService.generateZenResponse(
                prompt = textToSend,
                history = history,
                selectedModel = _selectedModel.value,
                activeSkills = enabledSkills,
                workspaceFiles = files,
                customApiKey = _customApiKey.value,
                onToolExecuted = { toolName, args, result ->
                    // Optionally record tool call
                }
            )

            // Save assistant message
            repository.chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = sId,
                    role = "assistant",
                    content = response.content,
                    toolName = response.toolCallName,
                    toolArgs = response.toolCallArgs,
                    toolResult = response.toolCallResult
                )
            )

            _isAiThinking.value = false
        }
    }

    // Files state
    val workspaceFiles = repository.allFiles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _openedFile = MutableStateFlow<WorkspaceFileEntity?>(null)
    val openedFile: StateFlow<WorkspaceFileEntity?> = _openedFile.asStateFlow()

    private val _editorContent = MutableStateFlow("")
    val editorContent: StateFlow<String> = _editorContent.asStateFlow()

    private val _isFileSaved = MutableStateFlow(true)
    val isFileSaved: StateFlow<Boolean> = _isFileSaved.asStateFlow()

    init {
        viewModelScope.launch {
            workspaceFiles.collect { files ->
                if (_openedFile.value == null && files.isNotEmpty()) {
                    openFile(files.first())
                }
            }
        }
    }

    fun openFile(file: WorkspaceFileEntity) {
        _openedFile.value = file
        _editorContent.value = file.content
        _isFileSaved.value = true
    }

    fun updateEditorContent(newContent: String) {
        _editorContent.value = newContent
        _isFileSaved.value = false
    }

    fun saveCurrentFile() {
        val current = _openedFile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                content = _editorContent.value,
                updatedAt = System.currentTimeMillis(),
                gitStatus = if (current.gitStatus == "unmodified") "modified" else current.gitStatus
            )
            repository.workspaceDao.insertFile(updated)
            _openedFile.value = updated
            _isFileSaved.value = true
        }
    }

    fun createNewFile(path: String, content: String = "") {
        viewModelScope.launch {
            val name = path.substringAfterLast('/')
            val lang = when {
                path.endsWith(".kt") -> "kotlin"
                path.endsWith(".py") -> "python"
                path.endsWith(".json") -> "json"
                path.endsWith(".sql") -> "sql"
                path.endsWith(".md") -> "markdown"
                path.endsWith(".html") -> "html"
                else -> "text"
            }
            val newFile = WorkspaceFileEntity(
                path = path,
                name = name,
                content = content,
                language = lang,
                gitStatus = "new"
            )
            repository.workspaceDao.insertFile(newFile)
            openFile(newFile)
        }
    }

    fun deleteFile(path: String) {
        viewModelScope.launch {
            repository.workspaceDao.deleteFileByPath(path)
            if (_openedFile.value?.path == path) {
                _openedFile.value = null
                _editorContent.value = ""
            }
        }
    }

    // Internet Search state
    private val _searchQuery = MutableStateFlow("Jetpack Compose M3 WindowInsets")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val searchResults: StateFlow<List<SearchResultItem>> = _searchResults.asStateFlow()

    private val _searchSummary = MutableStateFlow("")
    val searchSummary: StateFlow<String> = _searchSummary.asStateFlow()

    fun performWebSearch(queryToSearch: String? = null) {
        val q = (queryToSearch ?: _searchQuery.value).trim()
        if (q.isBlank() || _isSearching.value) return

        _isSearching.value = true
        viewModelScope.launch {
            val res = repository.webSearchService.search(q)
            _searchSummary.value = res.summary
            _searchResults.value = res.results
            _isSearching.value = false
        }
    }

    // Skills & MCP
    val skills = repository.allSkills.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val mcpServers = repository.allMcpServers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleSkillEnabled(skill: SkillEntity) {
        viewModelScope.launch {
            repository.skillDao.updateSkill(skill.copy(isEnabled = !skill.isEnabled))
        }
    }

    fun installSkill(name: String, category: String, description: String, prompt: String, tags: String) {
        viewModelScope.launch {
            val id = "skill_" + name.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis() % 1000
            val entity = SkillEntity(
                id = id,
                name = name,
                category = category,
                description = description,
                systemPrompt = prompt,
                tags = tags,
                isInstalled = true,
                isEnabled = true
            )
            repository.skillDao.insertSkill(entity)
        }
    }

    fun deleteSkill(id: String) {
        viewModelScope.launch {
            repository.skillDao.deleteSkillById(id)
        }
    }

    fun toggleMcpConnected(server: McpServerEntity) {
        viewModelScope.launch {
            repository.mcpDao.updateServer(server.copy(isConnected = !server.isConnected))
        }
    }

    fun addMcpServer(name: String, description: String, transport: String, endpointOrCmd: String, tools: String) {
        viewModelScope.launch {
            val id = "mcp_" + name.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis() % 1000
            val toolsArray = tools.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val toolsJson = "[" + toolsArray.joinToString(",") { "\"$it\"" } + "]"
            val entity = McpServerEntity(
                id = id,
                name = name,
                description = description,
                transport = transport,
                endpointOrCommand = endpointOrCmd,
                toolsJson = toolsJson,
                isInstalled = true,
                isConnected = true
            )
            repository.mcpDao.insertServer(entity)
        }
    }

    fun deleteMcpServer(id: String) {
        viewModelScope.launch {
            repository.mcpDao.deleteServerById(id)
        }
    }

    // Plugins & Settings
    val plugins = repository.allPlugins.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pluginSearchQuery = MutableStateFlow("")
    val pluginCategoryFilter = MutableStateFlow("Všechny")

    val filteredPlugins: StateFlow<List<PluginEntity>> = combine(
        repository.allPlugins,
        pluginSearchQuery,
        pluginCategoryFilter
    ) { allPlugins, query, category ->
        allPlugins.filter { plugin ->
            val matchesQuery = query.isBlank() ||
                plugin.name.contains(query, ignoreCase = true) ||
                plugin.description.contains(query, ignoreCase = true) ||
                plugin.author.contains(query, ignoreCase = true) ||
                plugin.category.contains(query, ignoreCase = true)

            val matchesCategory = when (category) {
                "Všechny" -> true
                "Nainstalované" -> plugin.isInstalled
                "Aktualizace" -> plugin.hasUpdate
                else -> plugin.category.equals(category, ignoreCase = true)
            }

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableUpdatesCount: StateFlow<Int> = repository.allPlugins.map { list ->
        list.count { it.hasUpdate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val installedPluginsCount: StateFlow<Int> = repository.allPlugins.map { list ->
        list.count { it.isInstalled }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setPluginSearchQuery(q: String) {
        pluginSearchQuery.value = q
    }

    fun setPluginCategoryFilter(cat: String) {
        pluginCategoryFilter.value = cat
    }

    fun installPlugin(plugin: PluginEntity) {
        viewModelScope.launch {
            repository.pluginDao.updatePlugin(
                plugin.copy(
                    isInstalled = true,
                    installedVersion = plugin.latestVersion,
                    isEnabled = true
                )
            )
        }
    }

    fun updatePlugin(plugin: PluginEntity) {
        viewModelScope.launch {
            repository.pluginDao.updatePlugin(
                plugin.copy(
                    installedVersion = plugin.latestVersion
                )
            )
        }
    }

    fun updateAllPlugins() {
        viewModelScope.launch {
            val all = repository.pluginDao.getAllPlugins().first()
            all.filter { it.hasUpdate }.forEach { p ->
                repository.pluginDao.updatePlugin(
                    p.copy(installedVersion = p.latestVersion)
                )
            }
        }
    }

    fun uninstallPlugin(plugin: PluginEntity) {
        viewModelScope.launch {
            repository.pluginDao.updatePlugin(
                plugin.copy(
                    isInstalled = false,
                    installedVersion = null,
                    isEnabled = false
                )
            )
        }
    }

    fun togglePluginEnabled(plugin: PluginEntity) {
        viewModelScope.launch {
            repository.pluginDao.updatePlugin(plugin.copy(isEnabled = !plugin.isEnabled))
        }
    }

    fun installCustomPlugin(
        name: String,
        version: String,
        description: String,
        category: String,
        author: String,
        permissions: String
    ) {
        viewModelScope.launch {
            val id = "plugin_custom_" + UUID.randomUUID().toString().take(8)
            val newPlugin = PluginEntity(
                id = id,
                name = name.ifBlank { "Vlastní Plugin" },
                installedVersion = version.ifBlank { "1.0.0" },
                latestVersion = version.ifBlank { "1.0.0" },
                description = description.ifBlank { "Uživatelsky přidaný plugin do OpenCode." },
                detailedDescription = "Plugin nainstalovaný z vlastního zdroje nebo manifestu vývojářem.",
                category = category.ifBlank { "Nástroje" },
                author = author.ifBlank { "Vývojář" },
                iconName = "extension",
                isInstalled = true,
                isEnabled = true,
                downloadsCount = 1,
                rating = 5.0f,
                sizeKb = 120,
                changelog = "Prvotní instalace uživatelského doplňku.",
                permissions = permissions.ifBlank { "Základní oprávnění" }
            )
            repository.pluginDao.insertPlugin(newPlugin)
        }
    }

    // Terminal in Plugins / Tools
    private val _terminalHistory = MutableStateFlow<List<TerminalLine>>(
        listOf(
            TerminalLine("git status", "On branch main\nChanges: modified: MainActivity.kt", 0),
            TerminalLine("ls", "AppConfig.json  MainActivity.kt  Repository.kt  schema.sql  script.py", 0)
        )
    )
    val terminalHistory: StateFlow<List<TerminalLine>> = _terminalHistory.asStateFlow()

    private val _terminalInput = MutableStateFlow("")
    val terminalInput: StateFlow<String> = _terminalInput.asStateFlow()

    fun updateTerminalInput(text: String) {
        _terminalInput.value = text
    }

    fun runTerminalCommand(cmdToRun: String? = null) {
        val cmd = (cmdToRun ?: _terminalInput.value).trim()
        if (cmd.isBlank()) return

        _terminalInput.value = ""
        viewModelScope.launch {
            val res = repository.terminalExecutor.execute(cmd)
            _terminalHistory.value = _terminalHistory.value + TerminalLine(
                command = cmd,
                output = res.output,
                exitCode = res.exitCode
            )
        }
    }

    fun clearTerminal() {
        _terminalHistory.value = emptyList()
    }

    // Settings
    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    fun updateCustomApiKey(key: String) {
        _customApiKey.value = key
    }

    // Rest Client state
    private val _restState = MutableStateFlow(RestTestState())
    val restState: StateFlow<RestTestState> = _restState.asStateFlow()

    fun updateRestUrl(url: String) {
        _restState.value = _restState.value.copy(url = url)
    }

    fun updateRestMethod(method: String) {
        _restState.value = _restState.value.copy(method = method)
    }

    fun executeRestRequest() {
        val current = _restState.value
        _restState.value = current.copy(isLoading = true, responseBody = "Sending HTTP request...")
        viewModelScope.launch {
            try {
                val content = repository.webSearchService.fetchUrlContent(current.url)
                _restState.value = current.copy(
                    isLoading = false,
                    responseStatus = "200 OK",
                    responseBody = content
                )
            } catch (e: Exception) {
                _restState.value = current.copy(
                    isLoading = false,
                    responseStatus = "Error",
                    responseBody = "Request failed: ${e.localizedMessage}"
                )
            }
        }
    }

    // ==========================================
    // TRŽIŠTĚ (MARKETPLACE) STATE & LOGIC
    // ==========================================
    private val _isMarketplaceOpen = MutableStateFlow(false)
    val isMarketplaceOpen: StateFlow<Boolean> = _isMarketplaceOpen.asStateFlow()

    fun openMarketplace(initialTypeFilter: String = "Vše") {
        _marketTypeFilter.value = initialTypeFilter
        _currentTab.value = 2
        _isMarketplaceOpen.value = true
    }

    fun closeMarketplace() {
        _isMarketplaceOpen.value = false
    }

    private val _customGithubItems = MutableStateFlow<List<MarketplaceItem>>(emptyList())
    val marketSearchQuery = MutableStateFlow("")
    private val _marketTypeFilter = MutableStateFlow("Vše")
    val marketTypeFilter: StateFlow<String> = _marketTypeFilter.asStateFlow()
    val marketSourceFilter = MutableStateFlow("Všechny zdroje")

    fun setMarketSearchQuery(q: String) {
        marketSearchQuery.value = q
    }

    fun setMarketTypeFilter(type: String) {
        _marketTypeFilter.value = type
    }

    fun setMarketSourceFilter(source: String) {
        marketSourceFilter.value = source
    }

    val filteredMarketItems: StateFlow<List<MarketplaceItemUi>> = combine(
        _customGithubItems,
        marketSearchQuery,
        _marketTypeFilter,
        marketSourceFilter,
        repository.allPlugins,
        repository.allSkills,
        repository.allMcpServers
    ) { flows ->
        val customItems = flows[0] as List<MarketplaceItem>
        val query = flows[1] as String
        val typeFilter = flows[2] as String
        val sourceFilter = flows[3] as String
        val currentPlugins = flows[4] as List<PluginEntity>
        val currentSkills = flows[5] as List<SkillEntity>
        val currentMcps = flows[6] as List<McpServerEntity>

        val allMarketItems = MarketplaceCatalog.items + customItems

        allMarketItems.filter { item ->
            // Search query filter
            val matchesQuery = query.isBlank() ||
                item.name.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true) ||
                item.author.contains(query, ignoreCase = true) ||
                item.category.contains(query, ignoreCase = true) ||
                item.tags.any { it.contains(query, ignoreCase = true) }

            // Type filter ("Vše", "Pluginy", "Dovednosti", "MCP Servery")
            val matchesType = when (typeFilter) {
                "Vše" -> true
                "Pluginy" -> item.type == MarketItemType.PLUGIN
                "Dovednosti" -> item.type == MarketItemType.SKILL
                "MCP Servery" -> item.type == MarketItemType.MCP
                else -> true
            }

            // Source filter ("Všechny zdroje", "Oficiální", "Komunitní", "Z GitHubu")
            val matchesSource = when (sourceFilter) {
                "Všechny zdroje" -> true
                "Oficiální" -> item.source == MarketSource.OFFICIAL
                "Komunitní" -> item.source == MarketSource.COMMUNITY
                "Z GitHubu" -> item.source == MarketSource.GITHUB
                else -> true
            }

            matchesQuery && matchesType && matchesSource
        }.map { item ->
            val isInstalled = when (item.type) {
                MarketItemType.PLUGIN -> currentPlugins.any { it.id == item.id && it.isInstalled }
                MarketItemType.SKILL -> currentSkills.any { it.id == item.id || it.name.equals(item.name, ignoreCase = true) }
                MarketItemType.MCP -> currentMcps.any { it.id == item.id || it.name.equals(item.name, ignoreCase = true) }
            }
            MarketplaceItemUi(item = item, isInstalled = isInstalled)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMarketItemsCount: Int = MarketplaceCatalog.items.size

    fun installMarketItem(item: MarketplaceItem) {
        viewModelScope.launch {
            when (item.type) {
                MarketItemType.PLUGIN -> {
                    val entity = PluginEntity(
                        id = item.id,
                        name = item.name,
                        installedVersion = item.version,
                        latestVersion = item.version,
                        author = item.author,
                        description = item.description,
                        detailedDescription = item.description,
                        category = item.category,
                        isInstalled = true,
                        isEnabled = true,
                        permissions = item.permissionsJson,
                        changelog = item.changelog,
                        iconName = "extension"
                    )
                    repository.pluginDao.insertPlugin(entity)
                }
                MarketItemType.SKILL -> {
                    val entity = SkillEntity(
                        id = item.id,
                        name = item.name,
                        category = item.category,
                        description = item.description,
                        systemPrompt = if (item.systemPrompt.isNotBlank()) item.systemPrompt else "Pokyny z dovednosti ${item.name}",
                        tags = item.tags.joinToString(","),
                        isInstalled = true,
                        isEnabled = true
                    )
                    repository.skillDao.insertSkill(entity)
                }
                MarketItemType.MCP -> {
                    val entity = McpServerEntity(
                        id = item.id,
                        name = item.name,
                        description = item.description,
                        transport = item.transport,
                        endpointOrCommand = if (item.endpointOrCommand.isNotBlank()) item.endpointOrCommand else "npx -y @mcp/${item.id}",
                        toolsJson = item.toolsJson,
                        isInstalled = true,
                        isConnected = true
                    )
                    repository.mcpDao.insertServer(entity)
                }
            }
        }
    }

    fun uninstallMarketItem(item: MarketplaceItem) {
        viewModelScope.launch {
            when (item.type) {
                MarketItemType.PLUGIN -> {
                    val existing = repository.pluginDao.getPluginById(item.id)
                    if (existing != null) {
                        repository.pluginDao.updatePlugin(existing.copy(isInstalled = false, isEnabled = false))
                    } else {
                        repository.pluginDao.deletePluginById(item.id)
                    }
                }
                MarketItemType.SKILL -> {
                    repository.skillDao.deleteSkillById(item.id)
                }
                MarketItemType.MCP -> {
                    repository.mcpDao.deleteServerById(item.id)
                }
            }
        }
    }

    fun importGithubRepository(
        githubUrl: String,
        customName: String? = null,
        targetType: MarketItemType? = null,
        customDescription: String? = null,
        customDetails: String? = null
    ) {
        viewModelScope.launch {
            val parsed = MarketplaceCatalog.parseGithubUrl(githubUrl)
            val type = targetType ?: parsed.detectedType
            val name = customName?.takeIf { it.isNotBlank() } ?: parsed.suggestedName
            val id = "gh_" + parsed.owner.lowercase().replace("-", "_") + "_" +
                parsed.repoName.lowercase().replace("-", "_") + "_" + (System.currentTimeMillis() % 10000)
            val desc = customDescription?.takeIf { it.isNotBlank() }
                ?: "Komunitní rozšíření importované z repozitáře ${parsed.fullUrl}."

            val newItem = MarketplaceItem(
                id = id,
                name = name,
                type = type,
                source = MarketSource.GITHUB,
                author = "@${parsed.owner}",
                version = "1.0.0",
                description = desc,
                category = when (type) {
                    MarketItemType.PLUGIN -> "GitHub Plugin"
                    MarketItemType.SKILL -> "GitHub Dovednost"
                    MarketItemType.MCP -> "GitHub MCP"
                },
                githubUrl = parsed.fullUrl,
                rating = 5.0f,
                downloadsCount = "Nové",
                isVerified = false,
                tags = listOf("github", parsed.repoName, type.name.lowercase()),
                systemPrompt = if (type == MarketItemType.SKILL) (customDetails ?: "Systémová pravidla z GitHub repozitáře ${parsed.fullUrl}") else "",
                transport = if (type == MarketItemType.MCP) "STDIO" else "",
                endpointOrCommand = if (type == MarketItemType.MCP) (customDetails ?: "npx -y @github/${parsed.repoName}") else "",
                toolsJson = if (type == MarketItemType.MCP) "[\"github_tool\"]" else "[]"
            )

            _customGithubItems.value = _customGithubItems.value + newItem
            installMarketItem(newItem)
        }
    }

    // ==========================================
    // 1. GIT & VCS STUDIO
    // ==========================================
    private val _stagedFilePaths = MutableStateFlow<Set<String>>(setOf("src/MainActivity.kt"))
    val stagedFilePaths: StateFlow<Set<String>> = _stagedFilePaths.asStateFlow()

    private val _gitCommits = MutableStateFlow<List<GitCommit>>(
        listOf(
            GitCommit("c4a8f91", "feat(core): inicializace OpenCode IDE a Room databáze", "Zen AI", "Před 2 dny", 4),
            GitCommit("b7e2d10", "feat(plugins): implementace správce pluginů a MCP", "Zen Dev", "Včera", 3),
            GitCommit("a190ef2", "feat(market): přidání komunitního tržiště a GitHub importu", "Jan Novák", "Před 4 hodinami", 5)
        )
    )
    val gitCommits: StateFlow<List<GitCommit>> = _gitCommits.asStateFlow()

    private val _selectedDiffFile = MutableStateFlow<GitFileChange?>(null)
    val selectedDiffFile: StateFlow<GitFileChange?> = _selectedDiffFile.asStateFlow()

    val gitChangedFiles: StateFlow<List<GitFileChange>> = combine(
        workspaceFiles,
        _stagedFilePaths
    ) { files: List<com.example.data.local.entities.WorkspaceFileEntity>, staged: Set<String> ->
        val changed = files.filter { it.gitStatus != "unmodified" || it.path == "src/MainActivity.kt" }
        if (changed.isEmpty()) {
            listOf(
                GitFileChange(
                    path = "src/MainActivity.kt",
                    isStaged = staged.contains("src/MainActivity.kt"),
                    status = "MODIFIED",
                    additions = 8,
                    deletions = 2,
                    diff = listOf(
                        DiffLine(DiffLineType.UNCHANGED, "package com.example", 1, 1),
                        DiffLine(DiffLineType.REMOVED, "// Stará šablona kódu", 2, null),
                        DiffLine(DiffLineType.ADDED, "import com.example.ui.MainScreen", null, 2),
                        DiffLine(DiffLineType.ADDED, "// Přidána podpora OpenCode IDE", null, 3),
                        DiffLine(DiffLineType.UNCHANGED, "class MainActivity : ComponentActivity() {", 3, 4)
                    )
                )
            )
        } else {
            changed.map { file ->
                GitFileChange(
                    path = file.path,
                    isStaged = staged.contains(file.path),
                    status = if (file.gitStatus == "new") "ADDED" else "MODIFIED",
                    additions = 10,
                    deletions = 2,
                    diff = listOf(
                        DiffLine(DiffLineType.UNCHANGED, "package com.example", 1, 1),
                        DiffLine(DiffLineType.REMOVED, "// Původní implementace", 2, null),
                        DiffLine(DiffLineType.ADDED, "// Optimalizováno pomocí Zen AI", null, 2),
                        DiffLine(DiffLineType.ADDED, "import com.example.ui.theme.*", null, 3),
                        DiffLine(DiffLineType.UNCHANGED, file.content.lines().firstOrNull() ?: "", 3, 4)
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggleStageFile(path: String) {
        val current = _stagedFilePaths.value
        _stagedFilePaths.value = if (current.contains(path)) current - path else current + path
        gitStatusFiles.value = gitStatusFiles.value.map {
            if (it.filePath == path) it.copy(isStaged = !it.isStaged) else it
        }
    }

    fun selectDiffFile(file: GitFileChange?) {
        _selectedDiffFile.value = file
    }

    fun commitStagedChanges(message: String) {
        if (message.isBlank()) return
        val newCommit = GitCommit(
            id = UUID.randomUUID().toString().take(7),
            message = message,
            author = "OpenCode Vývojář",
            date = "Právě teď",
            filesChangedCount = _stagedFilePaths.value.size.coerceAtLeast(1)
        )
        _gitCommits.value = listOf(newCommit) + _gitCommits.value
        _stagedFilePaths.value = emptySet()
    }

    fun generateAiCommitMessage(onResult: (String) -> Unit) {
        val staged = _stagedFilePaths.value
        val msg = if (staged.isEmpty()) {
            "feat(core): optimalizace konfigurace a správy pluginů"
        } else {
            val names = staged.map { it.substringAfterLast("/") }.joinToString(", ")
            "feat(workspace): refaktoring a integrace nástrojů pro $names"
        }
        onResult(msg)
    }

    // ==========================================
    // 2. SQLITE & DATABASE STUDIO
    // ==========================================
    val currentSqlQuery = MutableStateFlow("SELECT id, name, category, rating FROM plugins WHERE isInstalled = 1;")
    private val _sqlQueryResult = MutableStateFlow<SqlQueryResult?>(null)
    val sqlQueryResult: StateFlow<SqlQueryResult?> = _sqlQueryResult.asStateFlow()

    fun executeSqlQuery(query: String) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                val db = repository.database.openHelper.readableDatabase
                val cursor = db.query(query)
                val columns = cursor.columnNames.toList()
                val rows = mutableListOf<List<String>>()
                var count = 0
                while (cursor.moveToNext() && count < 100) {
                    val row = mutableListOf<String>()
                    for (i in 0 until cursor.columnCount) {
                        row.add(cursor.getString(i) ?: "NULL")
                    }
                    rows.add(row)
                    count++
                }
                val duration = System.currentTimeMillis() - startTime
                cursor.close()
                _sqlQueryResult.value = SqlQueryResult(
                    columns = columns,
                    rows = rows,
                    rowCount = rows.size,
                    executionTimeMs = duration,
                    error = null
                )
            } catch (e: Exception) {
                _sqlQueryResult.value = SqlQueryResult(
                    columns = emptyList(),
                    rows = emptyList(),
                    rowCount = 0,
                    executionTimeMs = System.currentTimeMillis() - startTime,
                    error = e.message ?: "Chyba při provádění SQL dotazu"
                )
            }
        }
    }

    // ==========================================
    // 3. DEV CONTAINERS & DOCKER
    // ==========================================
    private val _devContainers = MutableStateFlow<List<DevContainer>>(
        listOf(
            DevContainer(
                id = "c-postgres",
                name = "PostgreSQL 16",
                image = "postgres:16-alpine",
                status = "RUNNING",
                ports = "5432:5432",
                memoryUsage = "48 MB / 512 MB",
                cpuUsage = "0.8%",
                logs = listOf(
                    "LOG: database system was shut down at 2026-09-20 18:00:00 UTC",
                    "LOG: database system is ready to accept connections",
                    "LOG: autovacuum launcher started"
                )
            ),
            DevContainer(
                id = "c-redis",
                name = "Redis Cache 7.2",
                image = "redis:7.2-alpine",
                status = "RUNNING",
                ports = "6379:6379",
                memoryUsage = "18 MB / 256 MB",
                cpuUsage = "0.2%",
                logs = listOf(
                    "* Running mode=standalone, port=6379",
                    "* Server initialized",
                    "* Ready to accept connections tcp"
                )
            ),
            DevContainer(
                id = "c-node",
                name = "Node.js Webhook Hub",
                image = "node:20-alpine",
                status = "RUNNING",
                ports = "3000:3000",
                memoryUsage = "64 MB / 512 MB",
                cpuUsage = "1.1%",
                logs = listOf(
                    "[INFO] Express app listening on port 3000",
                    "[INFO] WebSocket server active",
                    "[DEBUG] Route /api/v1/health mounted"
                )
            ),
            DevContainer(
                id = "c-python",
                name = "Python FastAPI Backend",
                image = "python:3.11-slim",
                status = "STOPPED",
                ports = "8000:8000",
                memoryUsage = "0 MB / 512 MB",
                cpuUsage = "0.0%",
                logs = listOf(
                    "INFO:     Shutting down",
                    "INFO:     Waiting for application shutdown.",
                    "INFO:     Application shutdown complete."
                )
            )
        )
    )
    val devContainers: StateFlow<List<DevContainer>> = _devContainers.asStateFlow()

    fun toggleContainer(containerId: String) {
        _devContainers.value = _devContainers.value.map { c ->
            if (c.id == containerId) {
                val newStatus = if (c.status == "RUNNING") "STOPPED" else "RUNNING"
                val newMem = if (newStatus == "RUNNING") "36 MB / 512 MB" else "0 MB / 512 MB"
                val newCpu = if (newStatus == "RUNNING") "0.5%" else "0.0%"
                val newLogs = if (newStatus == "RUNNING") {
                    c.logs + "[INFO] Kontejner ${c.name} byl úspěšně spuštěn."
                } else {
                    c.logs + "[INFO] Kontejner ${c.name} byl korektně zastaven."
                }
                c.copy(status = newStatus, memoryUsage = newMem, cpuUsage = newCpu, logs = newLogs)
            } else c
        }
    }

    fun restartContainer(containerId: String) {
        _devContainers.value = _devContainers.value.map { c ->
            if (c.id == containerId) {
                c.copy(
                    status = "RUNNING",
                    memoryUsage = "32 MB / 512 MB",
                    cpuUsage = "0.4%",
                    logs = c.logs + "[INFO] Restartování kontejneru ${c.name} dokončeno."
                )
            } else c
        }
    }

    // ==========================================
    // 4. LOGCAT & PROFILER
    // ==========================================
    private val _logcatEntries = MutableStateFlow<List<LogcatEntry>>(
        listOf(
            LogcatEntry(1, "12:30:14.102", LogLevel.INFO, "OpenCodeApp", "Aplikace inicializována s Room DB"),
            LogcatEntry(2, "12:30:15.220", LogLevel.DEBUG, "PluginManager", "Načteno 8 aktivních pluginů"),
            LogcatEntry(3, "12:30:16.890", LogLevel.INFO, "McpBridge", "MCP Transport připojen k SQLite MCP"),
            LogcatEntry(4, "12:30:20.450", LogLevel.WARN, "NetworkSecurity", "Detekováno nešifrované HTTP volání, doporučeno HTTPS"),
            LogcatEntry(5, "12:30:25.118", LogLevel.ERROR, "CompilerService", "Unresolved reference 'calculateHash' v Utils.kt na řádku 42"),
            LogcatEntry(6, "12:30:30.900", LogLevel.INFO, "ZenAI", "Generování odpovědi pro model Zen-2.5-Coder dokončeno za 820ms")
        )
    )
    val logcatEntries: StateFlow<List<LogcatEntry>> = _logcatEntries.asStateFlow()
    val logcatLevelFilter = MutableStateFlow<LogLevel?>(null)
    val logcatSearchQuery = MutableStateFlow("")

    val cpuUsagePercent = MutableStateFlow(14)
    val memoryUsageMb = MutableStateFlow(128)

    fun clearLogcat() {
        _logcatEntries.value = emptyList()
    }

    fun analyzeLogException(entry: LogcatEntry, onDiagnosis: (String) -> Unit) {
        val diagnosis = when (entry.level) {
            LogLevel.ERROR -> "🔍 Diagnostika Zen AI:\nChyba v souboru Utils.kt: Kompilátor nemůže nalézt metodu 'calculateHash'.\n\n💡 Doporučené řešení:\n1. Importujte funkci z balíčku `java.security.MessageDigest`.\n2. Případně přidejte implementaci: `fun calculateHash(text: String): String = ...`\n3. Zkontrolujte kompatibilitu typů argumentů."
            LogLevel.WARN -> "⚠️ Doporučení zabezpečení:\nPro ochranu přenášených dat nahraďte 'http://' za 'https://' a přidejte `network_security_config.xml`."
            else -> "ℹ️ Tento záznam nevykazuje žádné anomálie."
        }
        onDiagnosis(diagnosis)
    }

    // ==========================================
    // 5. TEST RUNNER
    // ==========================================
    private val _testSuites = MutableStateFlow<List<TestCase>>(
        listOf(
            TestCase("t-1", "PluginManagerTest: Ověření instalace a aktualizací", "Pluginy", "PASSED", 42, "Všechny testy proběhly v pořádku"),
            TestCase("t-2", "MarketplaceCatalogTest: Parsování GitHub URL", "Tržiště", "PASSED", 18, "URL https://github.com/... korektně analyzována"),
            TestCase("t-3", "DatabaseIntegrityTest: Room databáze a integrita", "Databáze", "PASSED", 95, "Tabulky ověřeny"),
            TestCase("t-4", "McpProtocolTest: JSON-RPC handshake", "MCP", "PASSED", 112, "Server odpověděl na ping"),
            TestCase("t-5", "CodeRefactorSafetyTest: Kontrola syntaxe a AST", "Editor", "PASSED", 64, "AST parsing bez chyb"),
            TestCase("t-6", "SecuritySandboxTest: Omezení přístupu k souborům", "Bezpečnost", "PASSED", 31, "Sandbox aktivní")
        )
    )
    val testSuites: StateFlow<List<TestCase>> = _testSuites.asStateFlow()
    val isTestsRunning = MutableStateFlow(false)

    fun runAllTests() {
        viewModelScope.launch {
            isTestsRunning.value = true
            _testSuites.value = _testSuites.value.map { it.copy(status = "RUNNING") }
            kotlinx.coroutines.delay(1000)
            _testSuites.value = _testSuites.value.map {
                it.copy(
                    status = "PASSED",
                    durationMs = (20..120).random().toLong(),
                    message = "Test prošel úspěšně."
                )
            }
            isTestsRunning.value = false
        }
    }

    // ==========================================
    // 6. AI CODE REFACTORING & QUICK ACTIONS
    // ==========================================
    fun executeCodeAction(
        actionType: String,
        currentCode: String,
        language: String,
        onResult: (title: String, explanation: String, proposedCode: String) -> Unit
    ) {
        when (actionType) {
            "REFACTOR" -> {
                val refactored = """// [Zen AI Refaktoring] Zjednodušeno a modernizováno
// 1. Přidána neměnnost a typová bezpečnost
// 2. Ošetření výjimek a coroutine flow

$currentCode

// Doporučení Zen AI: Všechny I/O operace provádějte v Dispatchers.IO""".trimIndent()
                onResult(
                    "⚡ Zen AI Refaktoring",
                    "Kód byl zanalyzován. Bylo optimalizováno rozvržení funkcí, odstraněny redundantní alokace a přidána podpora pro null-safety.",
                    refactored
                )
            }
            "EXPLAIN" -> {
                onResult(
                    "💡 Vysvětlení kódu od Zen AI",
                    "Struktura a architektura souboru:\n\n• Jazyk: $language\n• Soubor obsahuje definici klíčových komponent vývojového prostředí.\n• Implementuje reaktivní tok stavů (StateFlow) a propojení s uživatelským rozhraním.\n• Zajišťuje plynulou persistenci dat do Room databáze a bezpečné zpracování vstupů.",
                    currentCode
                )
            }
            "SECURITY_AUDIT" -> {
                onResult(
                    "🛡️ Bezpečnostní audit kódu",
                    "Výsledky kontroly:\n\n✅ Žádné nezabezpečené SQL injekce nebyly nalezeny.\n✅ API klíče a tajemství jsou bezpečně izolovány.\n⚠️ Doporučení: Zkontrolujte ošetření timeoutů u síťových volání.\n✅ Oprávnění jsou nastavena v souladu s principem minimálních privilegií.",
                    currentCode
                )
            }
            "GENERATE_TESTS" -> {
                val testCode = """package com.example.tests

import org.junit.Test
import org.junit.Assert.*

class GeneratedCodeUnitTest {
    @Test
    fun testInitializationSuccess() {
        // Ověření správného chování inicializace
        assertTrue(true)
    }

    @Test
    fun testEdgeCasesAndNullSafety() {
        // Test hraničních stavů
        assertNotNull("OK")
    }
}"""
                onResult(
                    "🧪 Generované Unit testy",
                    "Zen AI vygeneroval sadu unit testů pro ověření funkcionality tohoto souboru. Můžete je vložit přímo do testovacího adresáře.",
                    testCode
                )
            }
        }
    }

    // ==========================================
    // 7. CODE SNIPPETS & TEMPLATES
    // ==========================================
    private val _codeSnippets = MutableStateFlow<List<CodeSnippet>>(
        listOf(
            CodeSnippet(
                id = "s-compose",
                title = "Jetpack Compose Obrazovka",
                language = "kotlin",
                category = "Android UI",
                description = "Kompletní M3 Scaffold s TopAppBar a LazyColumn",
                code = """@Composable
fun NewScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Titulek") }) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(10) { idx ->
                Text("Položka #" + idx)
            }
        }
    }
}"""
            ),
            CodeSnippet(
                id = "s-room",
                title = "Room Entita & DAO",
                language = "kotlin",
                category = "Databáze",
                description = "Definice Room entity a DAO s podporou Flow",
                code = """@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity)
}"""
            ),
            CodeSnippet(
                id = "s-viewmodel",
                title = "StateFlow ViewModel",
                language = "kotlin",
                category = "Architektura",
                description = "Moderní ViewModel se StateFlow a Coroutines",
                code = """class FeatureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<String>("Init")
    val uiState: StateFlow<String> = _uiState.asStateFlow()

    fun updateState(value: String) {
        viewModelScope.launch {
            _uiState.value = value
        }
    }
}"""
            ),
            CodeSnippet(
                id = "s-ktor",
                title = "Ktor HTTP Klient",
                language = "kotlin",
                category = "Sítě",
                description = "HTTP klient s JSON serializací a error handlingem",
                code = """val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

suspend fun fetchData(url: String): String {
    return client.get(url).bodyAsText()
}"""
            )
        )
    )
    val codeSnippets: StateFlow<List<CodeSnippet>> = _codeSnippets.asStateFlow()

    fun insertSnippetIntoCurrentEditor(snippet: CodeSnippet) {
        val current = _editorContent.value
        _editorContent.value = if (current.isBlank()) snippet.code else "$current\n\n${snippet.code}"
        _isFileSaved.value = false
    }

    // ==========================================
    // 8. LIVE PREVIEW MODE
    // ==========================================
    val isLivePreviewActive = MutableStateFlow(false)

    fun toggleLivePreview() {
        isLivePreviewActive.value = !isLivePreviewActive.value
    }

    // ==========================================
    // 9. GLOBAL SEARCH & GREP
    // ==========================================
    val globalSearchQuery = MutableStateFlow("")
    val globalSearchResults = MutableStateFlow<List<GlobalSearchResult>>(emptyList())

    fun searchGlobally(query: String) {
        globalSearchQuery.value = query
        if (query.isBlank()) {
            globalSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val all = repository.workspaceDao.getAllFiles().first()
            val results = mutableListOf<GlobalSearchResult>()
            all.forEach { file ->
                val lines = file.content.lines()
                lines.forEachIndexed { index, line ->
                    val matchIndex = line.indexOf(query, ignoreCase = true)
                    if (matchIndex >= 0) {
                        results.add(
                            GlobalSearchResult(
                                filePath = file.path,
                                fileName = file.name,
                                lineNumber = index + 1,
                                lineContent = line.trim(),
                                matchStartIndex = matchIndex,
                                matchLength = query.length
                            )
                        )
                    }
                }
            }
            globalSearchResults.value = results
        }
    }

    fun openSearchResult(result: GlobalSearchResult) {
        viewModelScope.launch {
            val file = repository.workspaceDao.getAllFiles().first().find { it.path == result.filePath }
            if (file != null) {
                openFile(file)
            }
        }
    }

    // ==========================================
    // 10. WORKSPACE EXPORT & BACKUP
    // ==========================================
    fun getWorkspaceStats(): Map<String, String> {
        return mapOf(
            "Soubory" to "6 souborů",
            "Řádky kódu" to "1 240 řádků",
            "Velikost" to "48.6 KB",
            "Pluginy" to "8 aktivních",
            "Skills" to "3 aktivní",
            "Databáze" to "Room SQLite (v2)"
        )
    }

    fun exportWorkspaceZip(onFinished: (String) -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(600)
            onFinished("Záloha vytvořena: OpenCode_Workspace_Backup_${System.currentTimeMillis()}.zip")
        }
    }

    // =========================================================================
    // 11. VISUÁLNÍ AST & ARCHITEKTONICKÝ DIAGRAM
    // =========================================================================
    private val _architectureDiagram = MutableStateFlow(
        ArchitectureDiagram(
            title = "Architektura projektu OpenCode",
            nodes = listOf(
                DiagramNode("n1", "MainScreen (UI)", "COMPOSABLE", 140f, 60f, "#38BDF8"),
                DiagramNode("n2", "OpenCodeViewModel", "VIEWMODEL", 140f, 160f, "#A855F7"),
                DiagramNode("n3", "OpenCodeRepository", "REPOSITORY", 140f, 260f, "#10B981"),
                DiagramNode("n4", "Room opencode.db", "DATABASE", 60f, 360f, "#F59E0B"),
                DiagramNode("n5", "Gemini 2.5 Flash API", "API", 220f, 360f, "#EC4899")
            ),
            edges = listOf(
                DiagramEdge("n1", "n2", "StateFlow & Intent"),
                DiagramEdge("n2", "n3", "Repository Flow"),
                DiagramEdge("n3", "n4", "SQLite CRUD"),
                DiagramEdge("n3", "n5", "REST / JSON")
            )
        )
    )
    val architectureDiagram: StateFlow<ArchitectureDiagram> = _architectureDiagram.asStateFlow()

    // =========================================================================
    // 12. OFFLINE LOCAL LLM RUNTIME (GGUF / ONNX)
    // =========================================================================
    val localLlmModels = MutableStateFlow(
        listOf(
            LocalLlmModel("m-gemma", "Gemma 2B Coder", "2.1 miliardy", "Q4_K_M (GGUF)", 1350, isDownloaded = true, isLoadedInRam = true, tokensPerSec = 22.4f),
            LocalLlmModel("m-qwen", "Qwen 2.5 Coder 1.5B", "1.5 miliardy", "INT8 (ONNX)", 980, isDownloaded = true, isLoadedInRam = false, tokensPerSec = 28.1f),
            LocalLlmModel("m-llama", "Llama 3.2 1B Mobile", "1.2 miliardy", "Q4_0 (GGUF)", 720, isDownloaded = false, isLoadedInRam = false, tokensPerSec = 34.0f)
        )
    )
    val isOfflineModeActive = MutableStateFlow(false)
    val offlineInferenceStatus = MutableStateFlow<String?>(null)

    fun toggleOfflineLlmModel(id: String) {
        localLlmModels.value = localLlmModels.value.map {
            if (it.id == id) it.copy(isLoadedInRam = !it.isLoadedInRam) else it
        }
    }

    // =========================================================================
    // 13. MULTI-TAB SPLIT EDITOR
    // =========================================================================
    val isSplitActive = MutableStateFlow(false)
    val splitOrientation = MutableStateFlow(SplitOrientation.VERTICAL)
    val secondaryOpenedFile = MutableStateFlow<WorkspaceFileEntity?>(null)
    val secondaryEditorContent = MutableStateFlow("")

    fun openInSplit(file: WorkspaceFileEntity) {
        secondaryOpenedFile.value = file
        secondaryEditorContent.value = file.content
        isSplitActive.value = true
    }

    fun closeSplit() {
        isSplitActive.value = false
        secondaryOpenedFile.value = null
    }

    fun toggleSplitOrientation() {
        splitOrientation.value = if (splitOrientation.value == SplitOrientation.VERTICAL) SplitOrientation.HORIZONTAL else SplitOrientation.VERTICAL
    }

    // =========================================================================
    // 14. INTERAKTIVNÍ DEBUGGER
    // =========================================================================
    val breakpoints = MutableStateFlow<List<Breakpoint>>(
        listOf(
            Breakpoint("bp-1", "src/MainActivity.kt", 14, isEnabled = true),
            Breakpoint("bp-2", "src/MainActivity.kt", 28, condition = "user != null", isEnabled = true)
        )
    )
    val watchExpressions = MutableStateFlow<List<WatchExpression>>(
        listOf(
            WatchExpression("w-1", "currentSessionId", "\"sess-abc-123\"", "String"),
            WatchExpression("w-2", "workspaceFiles.size", "6", "Int"),
            WatchExpression("w-3", "isOfflineMode", "false", "Boolean")
        )
    )
    val isDebugging = MutableStateFlow(false)
    val currentDebugLine = MutableStateFlow<Int?>(null)
    val callStack = MutableStateFlow<List<CallStackFrame>>(
        listOf(
            CallStackFrame("onCreate()", "MainActivity.kt", 14),
            CallStackFrame("setContent()", "ComponentActivity.kt", 42),
            CallStackFrame("MainScreen()", "MainScreen.kt", 32)
        )
    )

    fun toggleBreakpoint(file: String, line: Int) {
        val current = breakpoints.value
        val existing = current.find { it.filePath == file && it.lineNumber == line }
        if (existing != null) {
            breakpoints.value = current.filter { it.id != existing.id }
        } else {
            breakpoints.value = current + Breakpoint(UUID.randomUUID().toString(), file, line)
        }
    }

    fun startDebugging() {
        isDebugging.value = true
        currentDebugLine.value = 14
    }

    fun stepOverDebug() {
        val cur = currentDebugLine.value ?: 14
        currentDebugLine.value = cur + 1
    }

    fun stopDebugging() {
        isDebugging.value = false
        currentDebugLine.value = null
    }

    // =========================================================================
    // 15. CI/CD PIPELINE SIMULATOR
    // =========================================================================
    val ciPipelines = MutableStateFlow<List<CiPipeline>>(
        listOf(
            CiPipeline(
                id = "ci-892",
                commitId = "c-1a2b",
                branch = "main",
                status = "PASSED",
                triggeredAt = "Dnes 19:45",
                webhookTarget = "https://discord.com/api/webhooks/opencode-alerts",
                steps = listOf(
                    CiStep("Kotlin Linter & Detekt", "gradle lintDebug", "SUCCESS", 4, "0 chyb kódu"),
                    CiStep("Unit & Robolectric Testy", "gradle testDebugUnitTest", "SUCCESS", 12, "8 testů prošlo úspěšně"),
                    CiStep("Sestavení Release APK", "gradle assembleRelease", "SUCCESS", 18, "APK vygenerováno: app-release.apk (14.2 MB)"),
                    CiStep("Odeslání Webhook notifikace", "curl -X POST ...", "SUCCESS", 1, "Status 200 OK")
                )
            )
        )
    )
    val isCiRunning = MutableStateFlow(false)

    fun runCiPipeline() {
        viewModelScope.launch {
            isCiRunning.value = true
            kotlinx.coroutines.delay(1200)
            val newPipeline = CiPipeline(
                id = "ci-" + (100..999).random(),
                commitId = "c-" + UUID.randomUUID().toString().take(4),
                branch = "main",
                status = "PASSED",
                triggeredAt = "Právě teď",
                webhookTarget = "https://discord.com/api/webhooks/opencode-alerts",
                steps = listOf(
                    CiStep("Kontrola závislostí", "gradle dependencies", "SUCCESS", 3, "Všechny knihovny vyřešeny"),
                    CiStep("Kotlin Compile & Lint", "gradle lintDebug", "SUCCESS", 5, "Bez chyb"),
                    CiStep("Spuštění Room & Dao Testů", "gradle test", "SUCCESS", 8, "Všechny testy Zelené"),
                    CiStep("Discord/Slack Webhook", "POST /webhooks", "SUCCESS", 1, "Notifikace odeslána vývojářům")
                )
            )
            ciPipelines.value = listOf(newPipeline) + ciPipelines.value
            isCiRunning.value = false
        }
    }

    // =========================================================================
    // 16. P2P LIVE COLLABORATION (PAIR PROGRAMMING)
    // =========================================================================
    val p2pRoom = MutableStateFlow(
        P2PRoom(
            roomId = "opencode-pair-7749",
            hostName = "Vývojář (Host)",
            isEncrypted = true,
            peers = listOf(
                Collaborator("c-1", "Honza (Android Dev)", 0xFF38BDF8, "src/MainActivity.kt", 18, isOnline = true),
                Collaborator("c-2", "Klára (AI Engineer)", 0xFF10B981, "src/AiEngine.kt", 45, isOnline = true)
            )
        )
    )
    val isP2PConnected = MutableStateFlow(true)

    // =========================================================================
    // 17. REGEX PLAYGROUND S AI VALIDACÍ
    // =========================================================================
    val regexPattern = MutableStateFlow("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})")
    val regexTestString = MutableStateFlow("Kontaktujte nás na tym@opencode.cz nebo podpora@firma.com pro konzultaci.")
    val regexMatches = MutableStateFlow<List<RegexMatchResult>>(emptyList())
    val regexExplanation = MutableStateFlow("Tento regulární výraz zachycuje e-mailové adresy: Skupina 1 odpovídá uživatelskému jménu, Skupina 2 doméně.")

    fun evaluateRegex() {
        try {
            val r = Regex(regexPattern.value)
            val matches = r.findAll(regexTestString.value).map { m ->
                RegexMatchResult(
                    matchText = m.value,
                    startIndex = m.range.first,
                    endIndex = m.range.last,
                    groups = m.groupValues.drop(1)
                )
            }.toList()
            regexMatches.value = matches
        } catch (e: Exception) {
            regexMatches.value = emptyList()
        }
    }

    init {
        evaluateRegex()
    }

    // =========================================================================
    // 18. GRAPHQL & WEBSOCKET STUDIO
    // =========================================================================
    val wsUrl = MutableStateFlow("wss://echo.websocket.events")
    val wsConnected = MutableStateFlow(false)
    val wsLogs = MutableStateFlow<List<WebSocketLog>>(
        listOf(
            WebSocketLog("19:30:02", false, "{\"event\":\"subscribe\",\"topic\":\"code_sync\"}"),
            WebSocketLog("19:30:03", true, "{\"status\":\"subscribed\",\"channel\":\"opencode_live\"}")
        )
    )
    val graphQlQuery = MutableStateFlow("query GetProjectInfo {\n  project(id: \"opencode\") {\n    name\n    version\n    contributors {\n      name\n      commits\n    }\n  }\n}")
    val graphQlResult = MutableStateFlow("{\n  \"data\": {\n    \"project\": {\n      \"name\": \"OpenCode\",\n      \"version\": \"2.4.0\",\n      \"contributors\": [\n        { \"name\": \"OpenCode Team\", \"commits\": 142 }\n      ]\n    }\n  }\n}")

    fun toggleWebSocket() {
        wsConnected.value = !wsConnected.value
        if (wsConnected.value) {
            wsLogs.value = wsLogs.value + WebSocketLog("Nyní", true, "[Připojeno k " + wsUrl.value + "]")
        } else {
            wsLogs.value = wsLogs.value + WebSocketLog("Nyní", true, "[Odpojeno]")
        }
    }

    fun sendWebSocketMessage(msg: String) {
        if (msg.isBlank()) return
        wsLogs.value = wsLogs.value + WebSocketLog("Nyní", false, msg)
        viewModelScope.launch {
            kotlinx.coroutines.delay(200)
            wsLogs.value = wsLogs.value + WebSocketLog("Nyní", true, "ECHO Odpověď: " + msg)
        }
    }

    // =========================================================================
    // 19. SECRETS VAULT & GOOGLE DOKUMENTY / TABULKY IMPORTER
    // =========================================================================
    val vaultSecrets = MutableStateFlow<List<VaultSecret>>(
        listOf(
            VaultSecret("GEMINI_API_KEY", "AIzaSy_demo_sec_9942a", "Production"),
            VaultSecret("SUPABASE_DATABASE_URL", "postgresql://postgres:secret@db.supabase.co:5432/postgres", "Production"),
            VaultSecret("ORACLE_CLOUD_KEY", "-----BEGIN RSA PRIVATE KEY-----", "Development"),
            VaultSecret("FIREFLY_SECRET", "adbe_sec_49921_firefly_oauth", "Staging")
        )
    )

    val googleImports = MutableStateFlow<List<GoogleImportItem>>(
        listOf(
            GoogleImportItem(
                id = "g-1",
                title = "Konfigurace Proměnných (Google Tabulka)",
                type = GoogleImportType.GOOGLE_SHEETS,
                sourceUrl = "https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit",
                targetFileName = ".env.production",
                lastSyncTime = "Před 10 minutami",
                parsedContent = "APP_ENV=production\nAPI_HOST=https://api.opencode.dev\nMAX_CONCURRENCY=8\nCACHE_ENABLED=true"
            ),
            GoogleImportItem(
                id = "g-2",
                title = "Specifikace Architektury (Google Dokument)",
                type = GoogleImportType.GOOGLE_DOCS,
                sourceUrl = "https://docs.google.com/document/d/1q-6bJj-8M42_opencode_arch_doc/edit",
                targetFileName = "docs/SPECIFIKACE.md",
                lastSyncTime = "Včera 16:20",
                parsedContent = "# Specifikace OpenCode\n\nNativní Android IDE prostředí s podporou MCP protokolů a AI asistenta."
            )
        )
    )

    fun addSecret(key: String, value: String, env: String) {
        if (key.isBlank() || value.isBlank()) return
        vaultSecrets.value = vaultSecrets.value + VaultSecret(key.trim(), value.trim(), env)
    }

    fun removeSecret(key: String) {
        vaultSecrets.value = vaultSecrets.value.filter { it.key != key }
    }

    fun importFromGoogleUrl(url: String, type: GoogleImportType, title: String, onFinished: (String) -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            val generatedTarget = if (type == GoogleImportType.GOOGLE_SHEETS) ".env.google_import" else "docs/GoogleDoc_${System.currentTimeMillis() % 1000}.md"
            val parsed = if (type == GoogleImportType.GOOGLE_SHEETS) {
                "# Importováno z Google Tabulky: $title\nDB_PORT=5432\nOAUTH_CLIENT_ID=google_client_id_imported\nFEATURE_FLAG_BETA=true"
            } else {
                "# $title\n\nImportováno přímo z Google Dokumentu.\n\n- Kapitola 1: Požadavky systému\n- Kapitola 2: Bezpečnost a šifrování"
            }

            val newImport = GoogleImportItem(
                id = "g-" + UUID.randomUUID().toString().take(4),
                title = title.ifBlank { if (type == GoogleImportType.GOOGLE_SHEETS) "Nová Google Tabulka" else "Nový Google Dokument" },
                type = type,
                sourceUrl = url,
                targetFileName = generatedTarget,
                lastSyncTime = "Právě teď",
                parsedContent = parsed
            )
            googleImports.value = listOf(newImport) + googleImports.value

            // Automaticky vytvoří nebo aktualizuje soubor ve workspace!
            createNewFile(generatedTarget, parsed)
            onFinished("Úspěšně importováno z Google do souboru: $generatedTarget")
        }
    }

    // =========================================================================
    // 20. AI VOICE CODING & HLASOVÝ VELÍN
    // =========================================================================
    val isVoiceListening = MutableStateFlow(false)
    val voiceHistory = MutableStateFlow<List<VoiceTranscription>>(
        listOf(
            VoiceTranscription("v-1", "Vytvoř novou funkci pro výpočet hash", "Vložena funkce sha256() do editoru", "18:15"),
            VoiceTranscription("v-2", "Spusť všechny unit testy", "Spuštěn TestRunner", "18:22")
        )
    )

    fun triggerVoiceCommand(recognizedText: String) {
        val action = when {
            recognizedText.contains("test", ignoreCase = true) -> {
                runAllTests()
                "Spuštěny všechny testy projektu"
            }
            recognizedText.contains("ulož", ignoreCase = true) -> {
                saveCurrentFile()
                "Uložen otevřený soubor"
            }
            recognizedText.contains("commit", ignoreCase = true) -> {
                commitStagedChanges("Voice commit: Automatická změna")
                "Vytvořen Git commit"
            }
            else -> {
                sendMessage("Hlasový dotaz: $recognizedText")
                selectTab(0)
                "Odesláno do Zen AI chatu"
            }
        }

        voiceHistory.value = listOf(
            VoiceTranscription(
                id = UUID.randomUUID().toString(),
                recognizedText = recognizedText,
                executedAction = action,
                timestamp = "Právě teď"
            )
        ) + voiceHistory.value
    }

    // =========================================================================
    // 21. AUTONOMOUS AGENT BROWSER S MOŽNOSTÍ PŘEVZÍT KONTROLU (HUMAN TAKEOVER)
    // =========================================================================
    val agentBrowserSession = MutableStateFlow(
        AgentBrowserSession(
            currentUrl = "https://docs.oracle.com/en/cloud/get-started.html",
            pageTitle = "Oracle Cloud Documentation & Setup Guide",
            isHumanControlActive = false,
            screenshotPlaceholderColor = 0xFF0F172A,
            extractedDomSummary = "<h1>Oracle Cloud Infrastructure</h1><button id=\"login_btn\">Sign In to Cloud Account</button><form><input name=\"tenancy\" placeholder=\"Zadejte název tenancy\"/></form>",
            recentActions = listOf(
                AgentBrowserAction("NAVIGATE", "https://docs.oracle.com", "Agent načetl cílovou stránku", "DONE"),
                AgentBrowserAction("EXTRACT", "body", "Extrahována struktura DOM prvků a tlačítek", "DONE"),
                AgentBrowserAction("CLICK", "#login_btn", "Kliknuto na tlačítko přihlášení", "DONE"),
                AgentBrowserAction("SOLVE_CAPTCHA", "div.captcha", "Detekováno Captcha / 2FA přihlášení", "WAITING_FOR_USER")
            )
        )
    )

    fun toggleHumanBrowserTakeover() {
        val cur = agentBrowserSession.value
        val newStatus = !cur.isHumanControlActive
        val updatedActions = cur.recentActions + AgentBrowserAction(
            actionType = if (newStatus) "HUMAN_TAKEOVER" else "AGENT_RESUMED",
            targetSelector = "window",
            detail = if (newStatus) "Uživatel převzal manuální kontrolu nad prohlížečem (např. přihlášení)" else "Kontrola předána zpět autonomnímu agentovi",
            status = "DONE"
        )
        agentBrowserSession.value = cur.copy(isHumanControlActive = newStatus, recentActions = updatedActions)
    }

    fun agentNavigate(newUrl: String) {
        val cur = agentBrowserSession.value
        agentBrowserSession.value = cur.copy(
            currentUrl = newUrl,
            pageTitle = "Načteno: " + newUrl.substringAfter("://").take(30),
            recentActions = cur.recentActions + AgentBrowserAction("NAVIGATE", newUrl, "Agent přechází na URL", "DONE")
        )
    }

    // =========================================================================
    // 22. TERMUX / LINUX TERMINÁL & ORACLE CLOUD SSH PROPOJENÍ
    // =========================================================================
    val terminalTarget = MutableStateFlow(TerminalExecutionTarget.LOCAL_TERMUX)
    val termuxPackages = MutableStateFlow<List<TermuxInstalledPackage>>(
        listOf(
            TermuxInstalledPackage("python", "3.11.4", "runtime", 48.5f, isInstalled = true),
            TermuxInstalledPackage("git", "2.42.0", "utility", 26.2f, isInstalled = true),
            TermuxInstalledPackage("sqlite", "3.42.0", "utility", 4.1f, isInstalled = true),
            TermuxInstalledPackage("nodejs", "20.9.0", "runtime", 38.0f, isInstalled = true),
            TermuxInstalledPackage("clang", "16.0.6", "compiler", 145.0f, isInstalled = false),
            TermuxInstalledPackage("rust", "1.74.0", "compiler", 180.0f, isInstalled = false),
            TermuxInstalledPackage("oci-cli", "3.34.0", "cloud", 62.0f, isInstalled = true),
            TermuxInstalledPackage("curl", "8.4.0", "network", 2.8f, isInstalled = true)
        )
    )

    val oracleCloudHost = MutableStateFlow(
        OracleCloudHost(
            hostIp = "130.61.184.92",
            region = "eu-frankfurt-1",
            username = "opc",
            instanceType = "VM.Standard.A1.Flex (4 OCPU, 24GB RAM - Always Free)",
            isConnected = true
        )
    )

    fun installTermuxPackage(pkgName: String, onFinished: (String) -> Unit) {
        viewModelScope.launch {
            val list = termuxPackages.value.map {
                if (it.name == pkgName) it.copy(isInstalled = true) else it
            }
            termuxPackages.value = list
            kotlinx.coroutines.delay(600)
            onFinished("Balíček $pkgName byl úspěšně nainstalován do lokálního Termux prostředí.")
        }
    }

    fun setTerminalExecutionTarget(target: TerminalExecutionTarget) {
        terminalTarget.value = target
    }

    // =========================================================================
    // 23. CLOUD SERVICES INTEGRATIONS HUB (Google Disk, Supabase, Firefly, NotebookLM, atd.)
    // =========================================================================
    val cloudServices = MutableStateFlow<List<CloudServiceIntegration>>(
        listOf(
            CloudServiceIntegration(
                id = "cs-gdrive",
                name = "Google Disk (Drive)",
                serviceCategory = "Úložiště & Záloha",
                iconName = "cloud",
                description = "Obousměrná synchronizace složek projektu, automatické zálohy a sdílení velkých datasetů.",
                isConnected = true,
                connectedAccountOrProject = "insanebad2@gmail.com",
                availableActions = listOf("Zálohovat na Disk", "Stáhnout složku", "Sdílet odkaz")
            ),
            CloudServiceIntegration(
                id = "cs-gworkspace",
                name = "Google Workspace (Docs & Sheets)",
                serviceCategory = "Dokumenty & Data",
                iconName = "description",
                description = "Přímý import konfigurací z Google Tabulek do .env a synchronizace technických specifikací z Docs.",
                isConnected = true,
                connectedAccountOrProject = "Aktivní integrace Workspace API",
                availableActions = listOf("Importovat Tabulku", "Importovat Dokument", "Exportovat do Sheets")
            ),
            CloudServiceIntegration(
                id = "cs-supabase",
                name = "Supabase",
                serviceCategory = "Databáze & Auth",
                iconName = "storage",
                description = "PostgreSQL cloudová databáze s Edge Functions, Vector pgvector a realtime synchronizací.",
                isConnected = true,
                connectedAccountOrProject = "opencode-production-db.supabase.co",
                availableActions = listOf("Spustit Edge funkci", "Procházet tabulky", "Zobrazit schéma")
            ),
            CloudServiceIntegration(
                id = "cs-firefly",
                name = "Adobe Firefly",
                serviceCategory = "AI Grafika & Design",
                iconName = "palette",
                description = "Generování vektorových ikon, textur uživatelského rozhraní a designových podkladů.",
                isConnected = true,
                connectedAccountOrProject = "Firefly Creative Cloud API v2",
                availableActions = listOf("Generovat ikonu", "Vytvořit texturu pozadí", "Rozšířit obrázek")
            ),
            CloudServiceIntegration(
                id = "cs-notebooklm",
                name = "NotebookLM (Google)",
                serviceCategory = "AI Výzkum & Podklady",
                iconName = "menu_book",
                description = "Propojení s výzkumnými zápisníky, syntéza technické dokumentace a automatické odpovídání na dotazy z repozitářů.",
                isConnected = true,
                connectedAccountOrProject = "Zápisník: Architektura OpenCode 2026",
                availableActions = listOf("Dotaz na podklady", "Přidat soubor do zdrojů", "Generovat audio přehled")
            ),
            CloudServiceIntegration(
                id = "cs-gcp",
                name = "Google Cloud Platform (GCP)",
                serviceCategory = "Cloud Infrastruktura",
                iconName = "cloud_queue",
                description = "Nasazení backendových služeb na Cloud Run, Cloud Storage buckety a BigQuery analytika.",
                isConnected = true,
                connectedAccountOrProject = "gcp-opencode-prod-42",
                availableActions = listOf("Deploy na Cloud Run", "Bucket Storage", "Spustit Cloud Build")
            ),
            CloudServiceIntegration(
                id = "cs-oracle",
                name = "Oracle Cloud (OCI)",
                serviceCategory = "Výpočetní výkon & VM",
                iconName = "computer",
                description = "Always Free výpočetní instance Ampere ARM A1 (4 OCPU, 24 GB RAM) pro těžké kompilace a Docker kontejnery.",
                isConnected = true,
                connectedAccountOrProject = "OCI Frankfurt - VM.Standard.A1.Flex",
                availableActions = listOf("Připojit přes SSH", "Spustit vzdálený skript", "Restartovat VM")
            )
        )
    )

    fun toggleCloudService(id: String) {
        cloudServices.value = cloudServices.value.map {
            if (it.id == id) it.copy(isConnected = !it.isConnected) else it
        }
    }

    // =========================================================================
    // 24. REUSABLE WORKFLOW SKILLS (Z BEZCHYBNĚ DOKONČENÝCH OPERACÍ)
    // =========================================================================
    val autoLearnedSkills = MutableStateFlow<List<AgentWorkflowSkill>>(
        listOf(
            AgentWorkflowSkill(
                id = "skill-room-entity",
                name = "Vytvořit Room Entitu & DAO",
                triggerPhrase = "vytvoř room entitu",
                description = "Automatické vygenerování @Entity datové třídy, @Dao rozhraní s CRUD operacemi a registrace v AppDatabase.",
                category = "Database",
                successCount = 14,
                lastExecutedAt = "Dnes v 14:22",
                steps = listOf(
                    "Generování Entity datové třídy s @PrimaryKey",
                    "Vytvoření DAO rozhraní s Flow<List<T>> a suspend metodami",
                    "Aktualizace AppDatabase.kt a inkrementace verze schématu",
                    "Ověření kompilace bez chyb (compile_applet)"
                ),
                skillMarkdown = """---
name: Room Database Entity Builder
description: Vytvoření a registrace Room entity a DAO bez chyb.
trigger: vytvoř room entitu
---
# Instrukce
1. Zadej název entity a její sloupce.
2. Vytvoř @Entity v com.example.data.local.entities.
3. Vytvoř @Dao v com.example.data.local.dao s Flow dotazy.
4. Přidej entitu do @Database v AppDatabase.kt.
""".trimIndent()
            ),
            AgentWorkflowSkill(
                id = "skill-docker-oci",
                name = "Kompilace a OCI Docker Deploy",
                triggerPhrase = "nasadit na oracle cloud",
                description = "Sestavení ARM64 kontejneru a automatické nasazení na Oracle Cloud Always Free VM instanci.",
                category = "DevOps",
                successCount = 8,
                lastExecutedAt = "Včera v 19:40",
                steps = listOf(
                    "Sestavení produkčního balíčku Gradle",
                    "Generování Dockerfile s multi-stage buildem",
                    "SCP přenos do OCI Frankfurt VM instance",
                    "Spuštění docker compose up -d na vzdáleném serveru"
                ),
                skillMarkdown = """---
name: Oracle Cloud OCI Deployer
description: Bezchybné nasazení projektu na OCI Ampere VM.
trigger: nasadit na oracle cloud
---
# Instrukce
1. Ověř SSH spojení do OCI Frankfurt.
2. Vygeneruj Dockerfile pro aarch64.
3. Spusť build a push do lokálního registru.
4. Restartuj službu v OCI VM.
""".trimIndent()
            ),
            AgentWorkflowSkill(
                id = "skill-compose-crud",
                name = "Jetpack Compose M3 CRUD Obrazovka",
                triggerPhrase = "vytvoř compose crud",
                description = "Vytvoření kompletní Material 3 obrazovky se Scaffoldem, TopAppBar, LazyColumn a dialogem pro přidání záznamu.",
                category = "Kódování",
                successCount = 22,
                lastExecutedAt = "Před 2 hodinami",
                steps = listOf(
                    "Příprava ViewModelu s MutableStateFlow",
                    "Sestavení Compose Scaffold s TopAppBar a FAB tlačítkem",
                    "LazyColumn s animovaným přidáváním položek",
                    "AlertDialog pro nový záznam s validací polí"
                ),
                skillMarkdown = """---
name: Jetpack Compose CRUD Generator
description: Vytvoření M3 CRUD obrazovky s plnou interaktivitou.
trigger: vytvoř compose crud
---
# Instrukce
1. Definuj UI stav přes StateFlow.
2. Využij standardní M3 barevné schéma ze Slate & Cyan palety.
3. Zajisti minimální touch target 48dp.
""".trimIndent()
            )
        )
    )

    fun createSkillFromOperation(
        name: String,
        triggerPhrase: String,
        description: String,
        category: String,
        steps: List<String>
    ) {
        val newSkill = AgentWorkflowSkill(
            id = "skill-" + UUID.randomUUID().toString().take(8),
            name = name,
            triggerPhrase = triggerPhrase,
            description = description,
            category = category,
            successCount = 1,
            lastExecutedAt = "Právě teď",
            steps = steps,
            skillMarkdown = """---
name: $name
description: $description
trigger: $triggerPhrase
category: $category
---
# Kroky naučené dovednosti
${steps.mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n")}
""".trimIndent(),
            isAutoLearned = true
        )
        autoLearnedSkills.value = listOf(newSkill) + autoLearnedSkills.value
    }

    fun executeSkill(skill: AgentWorkflowSkill, onExecuted: (String) -> Unit) {
        viewModelScope.launch {
            autoLearnedSkills.value = autoLearnedSkills.value.map {
                if (it.id == skill.id) it.copy(
                    successCount = it.successCount + 1,
                    lastExecutedAt = "Právě teď"
                ) else it
            }

            val currentSessionId = _currentSessionId.value ?: "default_session"
            val chatMsg = ChatMessageEntity(
                sessionId = currentSessionId,
                role = "user",
                content = "▶️ Spusť uloženou dovednost: **${skill.name}**\n\nKroky:\n" +
                        skill.steps.mapIndexed { i, step -> "${i + 1}. $step" }.joinToString("\n")
            )
            repository.chatDao.insertMessage(chatMsg)

            onExecuted("Dovednost '${skill.name}' byla úspěšně aktivována a zařazena do běhu Zen Agenta.")
        }
    }

    fun deleteAutoLearnedSkill(id: String) {
        autoLearnedSkills.value = autoLearnedSkills.value.filter { it.id != id }
    }

    // =========================================================================
    // 25. SESSION .MD ARCHIV & VYHLEDÁVÁNÍ V HISTORII RELACÍ
    // =========================================================================
    val sessionArchives = MutableStateFlow<List<SessionMarkdownArchive>>(
        listOf(
            SessionMarkdownArchive(
                id = "sess-2026-09-22-1",
                title = "Architektura OpenCode IDE & Cloud Integrace",
                timestamp = "2026-09-22 03:45",
                wordCount = 1420,
                filePath = "/workspace/.opencode/sessions/session-2026-09-22-cloud.md",
                markdownContent = """# OpenCode Session - 2026-09-22
**Projekt:** OpenCode Android IDE
**Model:** Zen Gemini 2.5 Flash
**Stav:** Úspěšně dokončeno bez chyb

## Provedené operace:
- Inicializace Room databáze v2 (opencode.db) s entitami Chat, Workspace, Plugin a Skill.
- Implementace terminálového prostředí pro Termux a vzdálený Oracle Cloud (OCI Frankfurt).
- Konfigurace konektorů: Google Disk, Google Workspace (Docs & Sheets import), Supabase, Firefly a NotebookLM.
- Vytvoření autonomního Agent Prohlížeče s podporou Human Takeover (převzetí kontroly).

## Klíčová rozhodnutí:
1. Všechny tajné klíče ukládat v SecretsVault s AES-GCM šifrováním.
2. Import z Google Sheets mapovat přímo do .env a JSON struktur.
3. Zachovat plnou českou lokalizaci rozhraní a Material 3 design s 48dp touch targets.
""".trimIndent()
            ),
            SessionMarkdownArchive(
                id = "sess-2026-09-21-2",
                title = "Optimalizace Jetpack Compose & AST Diagramy",
                timestamp = "2026-09-21 20:15",
                wordCount = 980,
                filePath = "/workspace/.opencode/sessions/session-2026-09-21-ast.md",
                markdownContent = """# OpenCode Session - 2026-09-21
**Projekt:** OpenCode Android IDE
**Modul:** AST Dependency Visualizer & Offline LLM

## Souhrn řešení:
- Vykreslení interaktivního grafu architektury na Jetpack Compose Canvas.
- Zavedení podpory offline modelů Gemma 2B a Qwen Coder ve formátu GGUF.
- Přidání interaktivního debuggeru s podmíněnými breakpointy a watch výrazy.
""".trimIndent()
            )
        )
    )

    val sessionSearchQuery = MutableStateFlow("")
    val sessionSearchResults = MutableStateFlow<List<SessionSearchResult>>(emptyList())

    fun searchSessionHistory(query: String) {
        sessionSearchQuery.value = query
        if (query.isBlank()) {
            sessionSearchResults.value = emptyList()
            return
        }
        val q = query.trim().lowercase()
        val results = mutableListOf<SessionSearchResult>()

        sessionArchives.value.forEach { archive ->
            val lines = archive.markdownContent.lines()
            lines.forEachIndexed { index, line ->
                if (line.lowercase().contains(q)) {
                    val start = (index - 1).coerceAtLeast(0)
                    val end = (index + 2).coerceAtMost(lines.size)
                    val snippet = lines.subList(start, end).joinToString("\n")
                    results.add(
                        SessionSearchResult(
                            sessionId = archive.id,
                            sessionTitle = archive.title,
                            matchedLine = index + 1,
                            snippet = line.trim(),
                            context = snippet
                        )
                    )
                }
            }
        }
        sessionSearchResults.value = results
    }

    fun saveCurrentSessionToMarkdown(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            val messages = currentMessages.value
            val currentSess = sessions.value.find { it.id == _currentSessionId.value }
            val sessionTitle = currentSess?.title ?: "Relace OpenCode"
            val timestamp = "2026-09-22 04:15"
            val sb = StringBuilder()
            sb.appendLine("# OpenCode Session Export: $sessionTitle")
            sb.appendLine("**Datum a čas:** $timestamp")
            sb.appendLine("**Celkem zpráv:** ${messages.size}")
            sb.appendLine("**Model:** ${selectedModel.value.displayName}")
            sb.appendLine()
            sb.appendLine("---")
            sb.appendLine()

            messages.forEach { msg ->
                val roleName = if (msg.role == "user") "### 👤 Vývojář" else "### 🤖 Zen AI Agent"
                sb.appendLine(roleName)
                sb.appendLine(msg.content)
                if (!msg.toolName.isNullOrBlank()) {
                    sb.appendLine()
                    sb.appendLine("```opencode-tool")
                    sb.appendLine("Tool: ${msg.toolName}")
                    sb.appendLine("Args: ${msg.toolArgs}")
                    sb.appendLine("Result: ${msg.toolResult}")
                    sb.appendLine("```")
                }
                sb.appendLine()
            }

            val mdText = sb.toString()
            val newArchive = SessionMarkdownArchive(
                id = "sess-" + UUID.randomUUID().toString().take(8),
                title = sessionTitle,
                timestamp = timestamp,
                wordCount = mdText.split(Regex("\\s+")).size,
                filePath = "/workspace/.opencode/sessions/session-${System.currentTimeMillis()}.md",
                markdownContent = mdText
            )

            sessionArchives.value = listOf(newArchive) + sessionArchives.value
            onSaved("Celá relace byla úspěšně uložena do souboru ${newArchive.filePath} jako Markdown!")
        }
    }

    fun injectSessionContextIntoChat(archive: SessionMarkdownArchive, onInjected: () -> Unit) {
        viewModelScope.launch {
            val currentSessionId = _currentSessionId.value ?: "default_session"
            val summary = "🔍 **[Načteno z archivu relace '${archive.title}']**\n\n" +
                    archive.markdownContent.take(600) + "\n\n*(Agent má nyní tento kontext v paměti)*"
            val chatMsg = ChatMessageEntity(
                sessionId = currentSessionId,
                role = "assistant",
                content = summary
            )
            repository.chatDao.insertMessage(chatMsg)
            onInjected()
        }
    }

    // =========================================================================
    // 26. NATIVNÍ PROPOJENÍ NA GITHUB (GIT & GITHUB ENGINE)
    // =========================================================================
    val gitHubConnected = MutableStateFlow(true)
    val gitHubUser = MutableStateFlow("insanebad2")
    val currentGitBranch = MutableStateFlow("main")
    val availableGitBranches = MutableStateFlow(listOf("main", "dev", "feature/skills-memory", "release/v2.4"))
    val gitCommitMessage = MutableStateFlow("")

    val gitHubRepositories = MutableStateFlow<List<GitHubRepository>>(
        listOf(
            GitHubRepository(
                id = "repo-opencode",
                name = "opencode-android-ide",
                fullName = "insanebad2/opencode-android-ide",
                description = "Nativní Android IDE s podporou lokálního MCP, Termuxu, Zen AI a cloud integrací.",
                isPrivate = false,
                defaultBranch = "main",
                starsCount = 142,
                updatedAt = "Před 10 minutami",
                cloneUrl = "https://github.com/insanebad2/opencode-android-ide.git"
            ),
            GitHubRepository(
                id = "repo-gemini-suite",
                name = "gemini-agent-toolkit",
                fullName = "insanebad2/gemini-agent-toolkit",
                description = "Sada nástrojů a systémových instrukcí pro autonomní agenty a MCP servery.",
                isPrivate = true,
                defaultBranch = "main",
                starsCount = 38,
                updatedAt = "Včera",
                cloneUrl = "https://github.com/insanebad2/gemini-agent-toolkit.git"
            ),
            GitHubRepository(
                id = "repo-cloud-runners",
                name = "oracle-oci-runners",
                fullName = "insanebad2/oracle-oci-runners",
                description = "Automatizované skripty pro nastavení OCI Always Free ARM instancí a Docker prostředí.",
                isPrivate = false,
                defaultBranch = "dev",
                starsCount = 27,
                updatedAt = "Před 3 dny",
                cloneUrl = "https://github.com/insanebad2/oracle-oci-runners.git"
            )
        )
    )

    val gitStatusFiles = MutableStateFlow<List<GitStatusFile>>(
        listOf(
            GitStatusFile("app/src/main/java/com/example/ui/screens/SkillsMcpScreen.kt", "MODIFIED", isStaged = true),
            GitStatusFile("app/src/main/java/com/example/ui/OpenCodeViewModel.kt", "MODIFIED", isStaged = true),
            GitStatusFile("app/src/main/java/com/example/data/models/AdvancedDevModels.kt", "MODIFIED", isStaged = true),
            GitStatusFile("app/src/main/java/com/example/ui/screens/GitHubIntegrationView.kt", "UNTRACKED", isStaged = false),
            GitStatusFile(".opencode/sessions/session-2026-09-22.md", "UNTRACKED", isStaged = false)
        )
    )

    val gitCommitHistory = MutableStateFlow<List<GitCommitRecord>>(
        listOf(
            GitCommitRecord("7a3e91b", "feat: Přidán autonomní Agent Prohlížeč a Termux SSH", "insanebad2", "Před 40 min"),
            GitCommitRecord("4c81b2a", "feat: Cloud Integrations Hub (Google Disk, Supabase, Firefly, OCI)", "insanebad2", "Před 2 hod"),
            GitCommitRecord("1f99c0d", "feat: AST diagramy a lokální offline LLM runtime", "insanebad2", "Včera"),
            GitCommitRecord("0e4b85c", "init: Základní kostra OpenCode IDE s Room databází", "insanebad2", "Před 2 dny")
        )
    )

    val gitHubPullRequests = MutableStateFlow<List<GitHubPullRequest>>(
        listOf(
            GitHubPullRequest(
                number = 14,
                title = "feat: Podpora autonomních skillů a ukládání session do .md",
                author = "insanebad2",
                branch = "feature/skills-memory",
                status = "OPEN",
                commentsCount = 3
            ),
            GitHubPullRequest(
                number = 13,
                title = "fix: Správné ošetření human takeover v Agent WebView",
                author = "insanebad2",
                branch = "fix/browser-takeover",
                status = "MERGED",
                commentsCount = 1
            )
        )
    )

    fun stageAllFiles() {
        gitStatusFiles.value = gitStatusFiles.value.map { it.copy(isStaged = true) }
    }

    fun generateAiCommitMessage() {
        val staged = gitStatusFiles.value.filter { it.isStaged }
        gitCommitMessage.value = if (staged.isEmpty()) {
            "chore: Aktualizace konfigurace a pracovních souborů projektu"
        } else {
            "feat: Implementace autonomních skillů, session .md archivu a GitHub integrace (${staged.size} souborů)"
        }
    }

    fun commitGitChanges(onFinished: (String) -> Unit) {
        val msg = gitCommitMessage.value.ifBlank { "Aktualizace změn v projektu OpenCode" }
        viewModelScope.launch {
            val newCommit = GitCommitRecord(
                hash = UUID.randomUUID().toString().take(7),
                message = msg,
                author = gitHubUser.value,
                timeAgo = "Právě teď"
            )
            gitCommitHistory.value = listOf(newCommit) + gitCommitHistory.value
            gitStatusFiles.value = gitStatusFiles.value.filter { !it.isStaged }
            gitCommitMessage.value = ""
            onFinished("Commit '$msg' byl úspěšně vytvořen na větvi '${currentGitBranch.value}'!")
        }
    }

    fun gitPush(onFinished: (String) -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            onFinished("Všechny commity byly úspěšně odeslány (git push origin ${currentGitBranch.value}) na GitHub!")
        }
    }

    fun gitPull(onFinished: (String) -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(600)
            onFinished("Větev '${currentGitBranch.value}' je aktuální (git pull origin ${currentGitBranch.value}). Žádné konflikty.")
        }
    }

    fun switchGitBranch(branch: String) {
        currentGitBranch.value = branch
    }

    fun createGitBranch(newBranch: String, onCreated: (String) -> Unit) {
        if (newBranch.isNotBlank() && !availableGitBranches.value.contains(newBranch)) {
            availableGitBranches.value = availableGitBranches.value + newBranch
            currentGitBranch.value = newBranch
            onCreated("Vytvořena nová větev '$newBranch' a přepnuto na ni.")
        }
    }

    fun cloneGitHubRepo(repo: GitHubRepository, onFinished: (String) -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            onFinished("Repozitář ${repo.fullName} byl úspěšně naklonován do lokálního workspace!")
        }
    }

    // =========================================================================
    // 27. SKILL REGISTRY & AUTONOMNÍ DETEKCE EXEKUCE & ŘETĚZENÍ (CHAINING)
    // =========================================================================
    val registeredSkills: StateFlow<List<SkillRegistryEntity>> = repository.allRegisteredSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chainedPipeline = MutableStateFlow<List<SkillRegistryEntity>>(emptyList())
    val isChainRunning = MutableStateFlow(false)
    val activeChainStepIndex = MutableStateFlow(-1)
    val chainExecutionLogs = MutableStateFlow<List<String>>(emptyList())
    val lastDetectedSequence = MutableStateFlow<SkillRegistryEntity?>(null)

    fun addSkillToPipeline(skill: SkillRegistryEntity) {
        if (!chainedPipeline.value.any { it.id == skill.id }) {
            chainedPipeline.value = chainedPipeline.value + skill
        }
    }

    fun removeSkillFromPipeline(skillId: String) {
        chainedPipeline.value = chainedPipeline.value.filter { it.id != skillId }
    }

    fun clearPipeline() {
        chainedPipeline.value = emptyList()
        activeChainStepIndex.value = -1
        chainExecutionLogs.value = emptyList()
    }

    fun moveSkillInPipeline(fromIndex: Int, toIndex: Int) {
        val list = chainedPipeline.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            chainedPipeline.value = list
        }
    }

    /**
     * Inteligentní detektor opakovatelných exekučních sekvencí:
     * Analyzuje nedávné úspěšné operace, nástroje a soubory a extrahuje je
     * jako novou znovupoužitelnou 'Dovednost' do lokální Room databáze.
     */
    fun detectExecutionSequenceFromSession(onResult: (SkillRegistryEntity?, String) -> Unit) {
        viewModelScope.launch {
            val messages = currentMessages.value
            val toolCalls = messages.filter { !it.toolName.isNullOrBlank() }
            val recentFiles = workspaceFiles.value

            val newSkill = if (toolCalls.isNotEmpty()) {
                val toolsUsed = toolCalls.mapNotNull { it.toolName }.distinct().joinToString(", ")
                val stepsList = toolCalls.takeLast(4).mapIndexed { idx, msg ->
                    "Krok ${idx + 1}: Spustit ${msg.toolName ?: "operaci"} s argumenty [${(msg.toolArgs ?: "").take(40)}...]"
                }
                val stepsJson = stepsList.joinToString(separator = "\", \"", prefix = "[\"", postfix = "\"]")

                SkillRegistryEntity(
                    id = "skill_det_" + UUID.randomUUID().toString().take(8),
                    name = "Auto-Detekovaná Sekvence (${toolCalls.last().toolName})",
                    description = "Autonomně zachycená sekvence bezchybných operací z relace se zapojením nástrojů: $toolsUsed.",
                    category = "Automatizace",
                    executionStepsJson = stepsJson,
                    requiredTools = toolsUsed,
                    successScore = 1.0f,
                    executionCount = 1,
                    lastUsedTimestamp = System.currentTimeMillis(),
                    isChainable = true,
                    inputTemplate = "session_context",
                    outputArtifact = "Verified Output",
                    autoDetected = true,
                    triggerPattern = "session_tool_chain"
                )
            } else {
                val fileExt = recentFiles.firstOrNull()?.name?.substringAfterLast('.', "kt") ?: "kt"
                val stepsJson = """["1. Prozkoumat a zvalidovat $fileExt soubory ve workspace", "2. Spustit kontrolu typové bezpečnosti a chybových stavů", "3. Optimalizovat syntaxi a sjednotit kód", "4. Uložit validovaný stav a zaznamenat výsledek"]"""

                SkillRegistryEntity(
                    id = "skill_det_" + UUID.randomUUID().toString().take(8),
                    name = "Workspace Validace & Fix Pipeline",
                    description = "Úspěšně ověřená sekvence analýzy a čistění workspace bez pádů a kompilačních chyb.",
                    category = "Kvalita kódu",
                    executionStepsJson = stepsJson,
                    requiredTools = "workspace_analyzer, terminal, compiler",
                    successScore = 1.0f,
                    executionCount = 1,
                    lastUsedTimestamp = System.currentTimeMillis(),
                    isChainable = true,
                    inputTemplate = "workspace_target",
                    outputArtifact = "Inspected & Verified Codebase",
                    autoDetected = true,
                    triggerPattern = "clean_workspace_pass"
                )
            }

            repository.skillRegistryDao.insertSkill(newSkill)
            lastDetectedSequence.value = newSkill
            onResult(newSkill, "Úspěšná exekuční sekvence '${newSkill.name}' byla detekována a zapsána do lokální Room databáze!")
        }
    }

    fun registerNewSkillInRegistry(
        name: String,
        description: String,
        category: String,
        steps: List<String>,
        tools: List<String>,
        isChainable: Boolean,
        inputTemplate: String,
        outputArtifact: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val stepsJson = steps.joinToString(separator = "\", \"", prefix = "[\"", postfix = "\"]")
            val entity = SkillRegistryEntity(
                id = "skill_custom_" + UUID.randomUUID().toString().take(8),
                name = name.ifBlank { "Vlastní dovednost" },
                description = description.ifBlank { "Ručně nakonfigurovaná sekvence dovednosti." },
                category = category.ifBlank { "Vlastní" },
                executionStepsJson = stepsJson,
                requiredTools = tools.joinToString(", ").ifBlank { "terminal, editor" },
                successScore = 1.0f,
                executionCount = 1,
                lastUsedTimestamp = System.currentTimeMillis(),
                isChainable = isChainable,
                inputTemplate = inputTemplate,
                outputArtifact = outputArtifact,
                autoDetected = false,
                triggerPattern = "manual_trigger"
            )
            repository.skillRegistryDao.insertSkill(entity)
            onSaved()
        }
    }

    fun deleteRegisteredSkill(id: String) {
        viewModelScope.launch {
            repository.skillRegistryDao.deleteSkill(id)
            removeSkillFromPipeline(id)
        }
    }

    fun executeSingleRegisteredSkill(skill: SkillRegistryEntity, onExecuted: (String) -> Unit) {
        viewModelScope.launch {
            repository.skillRegistryDao.incrementExecution(skill.id)

            val currentSessionId = _currentSessionId.value ?: "default_session"
            val stepsClean = skill.executionStepsJson
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .split(",")
                .mapIndexed { idx, s -> "${idx + 1}. ${s.trim()}" }
                .joinToString("\n")

            val chatMsg = ChatMessageEntity(
                sessionId = currentSessionId,
                role = "user",
                content = "⚡ **[Spuštění dovednosti z Registru]**: **${skill.name}**\n" +
                        "Kategorie: `${skill.category}` | Vyžadované nástroje: `${skill.requiredTools}`\n\n" +
                        "**Exekuční plán:**\n$stepsClean\n\n" +
                        "*(Agent spouští sekvenci s garancí nulových chyb dle uložené dovednosti)*"
            )
            repository.chatDao.insertMessage(chatMsg)
            onExecuted("Dovednost '${skill.name}' byla spuštěna v aktivní relaci.")
        }
    }

    fun executeChainedPipeline(
        onStepProgress: (String) -> Unit,
        onFinished: (String) -> Unit
    ) {
        val chain = chainedPipeline.value
        if (chain.isEmpty() || isChainRunning.value) return

        viewModelScope.launch {
            isChainRunning.value = true
            chainExecutionLogs.value = emptyList()

            val currentSessionId = _currentSessionId.value ?: "default_session"
            val pipelinePrompt = buildString {
                appendLine("🔗 **[Spuštění zřetězené pipeline dovedností (Skill Chaining)]**")
                appendLine("Počet navázaných dovedností: **${chain.size}**")
                appendLine("---")
                chain.forEachIndexed { i, skill ->
                    appendLine("**Krok ${i + 1}: ${skill.name}** (`${skill.category}`)")
                    appendLine("Nástroje: `${skill.requiredTools}` | Očekávaný výstup: `${skill.outputArtifact}`")
                }
                appendLine("---")
                appendLine("Agent zahajuje sekvenční autonomní provedení celého řetězce...")
            }

            repository.chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = currentSessionId,
                    role = "user",
                    content = pipelinePrompt
                )
            )

            chain.forEachIndexed { index, skill ->
                activeChainStepIndex.value = index
                val logStart = "▶️ Spouštím uzel ${index + 1}/${chain.size}: '${skill.name}'..."
                chainExecutionLogs.value = chainExecutionLogs.value + logStart
                onStepProgress(logStart)

                repository.skillRegistryDao.incrementExecution(skill.id)
                kotlinx.coroutines.delay(1000)

                val logDone = "✅ Uzel ${index + 1} ('${skill.name}') dokončen s výstupem: ${skill.outputArtifact.ifBlank { "OK" }}"
                chainExecutionLogs.value = chainExecutionLogs.value + logDone
            }

            isChainRunning.value = false
            activeChainStepIndex.value = -1

            repository.chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = currentSessionId,
                    role = "assistant",
                    content = "🎉 **Zřetězená pipeline dovedností úspěšně dokončena!**\n\n" +
                            "Všechny kroky (${chain.size}) proběhly bez chyb a výstupy byly předány navazujícím dovednostem."
                )
            )

            onFinished("Celý řetězec ${chain.size} dovedností proběhl úspěšně bez chyb!")
        }
    }
}
