package com.example.data.repository

import com.example.data.ai.*
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class OpenCodeRepository(
    val database: AppDatabase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val db get() = database
    val chatDao = db.chatDao()
    val workspaceDao = db.workspaceDao()
    val skillDao = db.skillDao()
    val mcpDao = db.mcpDao()
    val pluginDao = db.pluginDao()
    val skillRegistryDao = db.skillRegistryDao()

    val webSearchService = WebSearchService()
    val terminalExecutor = TerminalExecutor(workspaceDao)
    val zenAiService = ZenAiService(webSearchService, terminalExecutor)

    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()
    val allFiles: Flow<List<WorkspaceFileEntity>> = workspaceDao.getAllFiles()
    val allSkills: Flow<List<SkillEntity>> = skillDao.getAllSkills()
    val allMcpServers: Flow<List<McpServerEntity>> = mcpDao.getAllServers()
    val allPlugins: Flow<List<PluginEntity>> = pluginDao.getAllPlugins()
    val allRegisteredSkills: Flow<List<SkillRegistryEntity>> = skillRegistryDao.getAllRegisteredSkills()

    init {
        scope.launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        // 1. Files
        val existingFiles = workspaceDao.getAllFiles().first()
        if (existingFiles.isEmpty()) {
            workspaceDao.insertFiles(
                listOf(
                    WorkspaceFileEntity(
                        path = "src/MainActivity.kt",
                        name = "MainActivity.kt",
                        content = """
                            package com.example

                            import android.os.Bundle
                            import androidx.activity.ComponentActivity
                            import androidx.activity.compose.setContent
                            import androidx.compose.material3.*
                            import com.example.ui.theme.OpenCodeTheme

                            class MainActivity : ComponentActivity() {
                                override fun onCreate(savedInstanceState: Bundle?) {
                                    super.onCreate(savedInstanceState)
                                    setContent {
                                        OpenCodeTheme {
                                            Surface {
                                                Text("OpenCode Native Environment Ready")
                                            }
                                        }
                                    }
                                }
                            }
                        """.trimIndent(),
                        language = "kotlin",
                        gitStatus = "unmodified"
                    ),
                    WorkspaceFileEntity(
                        path = "src/Repository.kt",
                        name = "Repository.kt",
                        content = """
                            package com.example.data

                            import kotlinx.coroutines.flow.Flow
                            import kotlinx.coroutines.flow.flow

                            class ProjectRepository {
                                fun observeZenStream(query: String): Flow<String> = flow {
                                    emit("Connecting to Zen reasoning engine...")
                                    emit("Synthesizing solution...")
                                    emit("Result generated successfully.")
                                }
                            }
                        """.trimIndent(),
                        language = "kotlin",
                        gitStatus = "modified"
                    ),
                    WorkspaceFileEntity(
                        path = "config/AppConfig.json",
                        name = "AppConfig.json",
                        content = """
                            {
                              "projectName": "OpenCode-Workspace",
                              "version": "2.4.0",
                              "targetRuntime": "Android 15",
                              "zenModel": "gemini-3.1-pro-preview",
                              "features": {
                                "mcpEnabled": true,
                                "internetSearch": true,
                                "terminalSandbox": true
                              }
                            }
                        """.trimIndent(),
                        language = "json",
                        gitStatus = "unmodified"
                    ),
                    WorkspaceFileEntity(
                        path = "database/schema.sql",
                        name = "schema.sql",
                        content = """
                            CREATE TABLE IF NOT EXISTS projects (
                                id VARCHAR(64) PRIMARY KEY,
                                name VARCHAR(255) NOT NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                            );

                            CREATE TABLE IF NOT EXISTS skills (
                                id VARCHAR(64) PRIMARY KEY,
                                category VARCHAR(100),
                                prompt_injection TEXT
                            );
                        """.trimIndent(),
                        language = "sql",
                        gitStatus = "modified"
                    ),
                    WorkspaceFileEntity(
                        path = "scripts/script.py",
                        name = "script.py",
                        content = """
                            import sys

                            def main():
                                print("Hello from OpenCode Python Engine!")
                                print(f"Python interpreter: {sys.version}")

                            if __name__ == "__main__":
                                main()
                        """.trimIndent(),
                        language = "python",
                        gitStatus = "new"
                    ),
                    WorkspaceFileEntity(
                        path = "README.md",
                        name = "README.md",
                        content = """
                            # Vývojářské prostředí OpenCode pro Android

                            Vítejte v plnohodnotném nativním vývojářském prostředí:
                            - 💬 **Zen AI Chat**: Inteligentní programovací asistent se specializovanými modely.
                            - 📁 **Soubory**: Plnohodnotný editor s číslováním řádků a git sledováním.
                            - 🌐 **Internet**: Vyhledávání v technických dokumentacích a načítání URL.
                            - 🧠 **Dovednosti (Skills)**: Přepínatelná systémová pravidla pro kód.
                            - 🔌 **MCP**: Protokol Model Context Protocol s podporou STDIO a SSE.
                            - 🧩 **Správa pluginů**: Katalog rozšíření s instalací, aktualizacemi a odinstalací.
                            - 💻 **Terminál & REST**: Vestavěná vývojářská konzole a tester API požadavků.
                        """.trimIndent(),
                        language = "markdown",
                        gitStatus = "unmodified"
                    )
                )
            )
        }

        // 2. Skills
        val existingSkills = skillDao.getAllSkills().first()
        if (existingSkills.isEmpty()) {
            skillDao.insertSkills(
                listOf(
                    SkillEntity(
                        id = "skill_compose_architect",
                        name = "Architekt Jetpack Compose",
                        category = "Android UI",
                        description = "Špičkový Material 3 UI design, správa stavu, kreslení na Canvas a plynulé animace.",
                        systemPrompt = "Jsi specialista na Jetpack Compose a Material Design 3. Piš čisté Composables se správným ošetřením WindowInsets a responzivním rozvržením.",
                        tags = "compose,android,m3,ui",
                        isEnabled = true
                    ),
                    SkillEntity(
                        id = "skill_mcp_builder",
                        name = "Tvůrce MCP Protokolu",
                        category = "AI & MCP",
                        description = "Specialista na specifikaci Model Context Protocol (MCP), STDIO/SSE transporty a JSON-RPC nástroje.",
                        systemPrompt = "Dokonale ovládáš specifikaci MCP. Vytvářej validní JSON-RPC schémata a bezpečné handlery pro připojení nástrojů.",
                        tags = "mcp,protokol,ai,nastroje",
                        isEnabled = true
                    ),
                    SkillEntity(
                        id = "skill_fullstack_kotlin_python",
                        name = "Fullstack Kotlin & Python",
                        category = "Fullstack",
                        description = "Multiplatformní architektura, asynchronní toky Flow, Ktor/Retrofit, FastAPI a backendové API.",
                        systemPrompt = "Navrhuj čistou doménovou architekturu, typovaná repository rozhraní a odolné ošetření chyb v coroutines.",
                        tags = "kotlin,python,backend,api",
                        isEnabled = false
                    ),
                    SkillEntity(
                        id = "skill_security_audit",
                        name = "Bezpečnostní Auditor & Hledač chyb",
                        category = "Bezpečnost",
                        description = "Hloubková kontrola úniků API klíčů, SQL injection, paměťových leaků a zranitelností.",
                        systemPrompt = "Před potvrzením kódu zkontroluj bezpečnostní rizika, ošetření vstupů, null-safety a potenciální pády aplikace.",
                        tags = "bezpecnost,audit,chyby,stabilita",
                        isEnabled = true
                    ),
                    SkillEntity(
                        id = "skill_perf_profiler",
                        name = "Optimalizátor výkonu a paměti",
                        category = "Výkon",
                        description = "Odstranění záseků rozhraní, optimalizace Room dotazů, minimalizace recomposition a plynulých 120 FPS.",
                        systemPrompt = "Zaměř se na nulové alokace v horkých cestách, použití derivedStateOf a indexované SQL dotazy pro maximální plynulost.",
                        tags = "vykon,pamet,optimalizace",
                        isEnabled = false
                    )
                )
            )
        }

        // 3. MCP Servers
        val existingMcp = mcpDao.getAllServers().first()
        if (existingMcp.isEmpty()) {
            mcpDao.insertServers(
                listOf(
                    McpServerEntity(
                        id = "mcp_filesystem",
                        name = "Filesystem MCP",
                        description = "Bezpečný přístup k adresářům projektu, čtení, zápis a diffování přes STDIO.",
                        transport = "STDIO",
                        endpointOrCommand = "npx -y @modelcontextprotocol/server-filesystem /workspace",
                        toolsJson = """["read_file", "write_file", "list_directory", "move_file", "search_files"]""",
                        isInstalled = true,
                        isConnected = true
                    ),
                    McpServerEntity(
                        id = "mcp_github",
                        name = "GitHub MCP",
                        description = "Přímá integrace s repozitáři, commity, pull requesty a správou issues.",
                        transport = "SSE",
                        endpointOrCommand = "https://mcp.github.com/v1/sse",
                        toolsJson = """["get_file_contents", "create_or_update_file", "list_issues", "search_code"]""",
                        isInstalled = true,
                        isConnected = true
                    ),
                    McpServerEntity(
                        id = "mcp_postgres",
                        name = "PostgreSQL MCP",
                        description = "Provádění analytických a vývojářských SQL dotazů v lokální databázi.",
                        transport = "STDIO",
                        endpointOrCommand = "docker run -i --rm mcp/postgres:latest",
                        toolsJson = """["query_sql", "describe_table", "list_tables", "explain_query"]""",
                        isInstalled = true,
                        isConnected = false
                    ),
                    McpServerEntity(
                        id = "mcp_brave_search",
                        name = "Brave Web Search MCP",
                        description = "Umožňuje OpenCode AI vyhledávat na internetu, stahovat dokumentaci a citovat zdroje.",
                        transport = "SSE",
                        endpointOrCommand = "https://api.search.brave.com/res/v1/web/search",
                        toolsJson = """["brave_web_search", "brave_local_search"]""",
                        isInstalled = true,
                        isConnected = true
                    )
                )
            )
        }

        // 4. Plugins
        val existingPlugins = pluginDao.getAllPlugins().first()
        if (existingPlugins.isEmpty()) {
            pluginDao.insertPlugins(
                listOf(
                    PluginEntity(
                        id = "plugin_linter",
                        name = "Code Linter & Formatter",
                        installedVersion = "1.2.0",
                        latestVersion = "1.4.2",
                        description = "Automatická statická analýza a formátování kódu pro Kotlin, Python a JSON.",
                        detailedDescription = "Kontroluje kvalitu kódu, odhaluje nepoužité importy a nesrovnalosti ve formátování podle standardů Kotlin Style Guide a PEP8.",
                        category = "Nástroje",
                        author = "OpenCode Vývojový Tým",
                        iconName = "brush",
                        isInstalled = true,
                        isEnabled = true,
                        downloadsCount = 4820,
                        rating = 4.9f,
                        sizeKb = 320,
                        changelog = "Přidána podpora pro Kotlin 2.1, nová pravidla pro recomposition a 35% zrychlení analýzy.",
                        permissions = "Čtení a zápis souborů projektu",
                        websiteUrl = "https://github.com/opencode/plugin-linter"
                    ),
                    PluginEntity(
                        id = "plugin_git_lens",
                        name = "Git Lens & Časová osa",
                        installedVersion = "2.0.0",
                        latestVersion = "2.1.4",
                        description = "Vizuální historie změn, git blame na úrovni řádků a interaktivní zobrazení commitů.",
                        detailedDescription = "Zobrazuje historii autorství každého řádku přímo v editoru, porovnává vizuální diff a umožňuje rychlé přepínání větví.",
                        category = "Verzování",
                        author = "GitForge Studio",
                        iconName = "history",
                        isInstalled = true,
                        isEnabled = true,
                        downloadsCount = 3910,
                        rating = 4.8f,
                        sizeKb = 410,
                        changelog = "Barevný graf commitů a nová integrace s MCP GitHub serverem.",
                        permissions = "Čtení git repozitáře, Terminál",
                        websiteUrl = "https://github.com/opencode/plugin-gitlens"
                    ),
                    PluginEntity(
                        id = "plugin_terminal_repl",
                        name = "Interaktivní Shell Terminál",
                        installedVersion = "2.1.0",
                        latestVersion = "2.1.0",
                        description = "Plnohodnotný emulátor příkazové řádky se správou souborů, git příkazy a spouštěním skriptů.",
                        detailedDescription = "Poskytuje vývojářskou konzoli s podporou příkazů ls, cat, git status, python, zen i mcp pro rychlé operace v sandboxu.",
                        category = "Nástroje",
                        author = "OpenCode Jádro",
                        iconName = "terminal",
                        isInstalled = true,
                        isEnabled = true,
                        downloadsCount = 6120,
                        rating = 5.0f,
                        sizeKb = 180,
                        changelog = "Podpora vlastních aliasů a barevného výstupu chybových kódů.",
                        permissions = "Spouštění terminálových procesů",
                        websiteUrl = "https://github.com/opencode/terminal-repl"
                    ),
                    PluginEntity(
                        id = "plugin_rest_client",
                        name = "REST & HTTP API Klient",
                        installedVersion = "1.2.0",
                        latestVersion = "1.2.0",
                        description = "Integrovaný tester síťových požadavků, HTTP hlaviček a JSON odpovědí.",
                        detailedDescription = "Umožňuje testovat REST API, posílat GET/POST požadavky, sledovat stavové kódy a přímo předávat získaná data do chatu Zen AI.",
                        category = "Nástroje",
                        author = "DevNetwork",
                        iconName = "http",
                        isInstalled = true,
                        isEnabled = true,
                        downloadsCount = 2840,
                        rating = 4.7f,
                        sizeKb = 250,
                        changelog = "Podpora vlastních hlaviček a automatické formátování JSON těla.",
                        permissions = "Přístup k internetu",
                        websiteUrl = "https://github.com/opencode/rest-client"
                    ),
                    PluginEntity(
                        id = "plugin_ai_pair",
                        name = "Zen AI Autocomplete Copilot",
                        installedVersion = null,
                        latestVersion = "2.0.0",
                        description = "Kontextové doplňování kódu a generování dokumentačních komentářů na jedno kliknutí.",
                        detailedDescription = "Propojuje editor s modely Zen Coder a Zen Fast. Automaticky generuje KDoc dokumentaci a navrhuje implementace funkcí.",
                        category = "AI & Asistenti",
                        author = "OpenCode AI Labs",
                        iconName = "auto_awesome",
                        isInstalled = false,
                        isEnabled = false,
                        downloadsCount = 7890,
                        rating = 4.9f,
                        sizeKb = 520,
                        changelog = "Integrace s Gemini 2.5 Flash pro predikci celých bloků kódu.",
                        permissions = "Přístup k editoru kódu, Síť",
                        websiteUrl = "https://github.com/opencode/ai-copilot"
                    ),
                    PluginEntity(
                        id = "plugin_sql_studio",
                        name = "SQL Databázové Studio",
                        installedVersion = null,
                        latestVersion = "1.3.0",
                        description = "Správa SQL schémat, syntaxe SQLite/PostgreSQL a vizuální spouštění dotazů.",
                        detailedDescription = "Podporuje kontrolu syntaxe v souborech .sql, automatické doplňování tabulek a sloupců a propojení s PostgreSQL MCP serverem.",
                        category = "Databáze",
                        author = "DataTools CZ",
                        iconName = "storage",
                        isInstalled = false,
                        isEnabled = false,
                        downloadsCount = 2150,
                        rating = 4.6f,
                        sizeKb = 340,
                        changelog = "Vizuální prohlížeč tabulek a export výsledků do CSV a JSON.",
                        permissions = "Čtení a zápis databází",
                        websiteUrl = "https://github.com/opencode/sql-studio"
                    ),
                    PluginEntity(
                        id = "plugin_regex_tester",
                        name = "Regex Laboratoř & Analyzátor",
                        installedVersion = "1.0.5",
                        latestVersion = "1.0.5",
                        description = "Interaktivní testování regulárních výrazů s okamžitým zvýrazněním zachycených skupin.",
                        detailedDescription = "Pomocník pro tvorbu a ladění regulárních výrazů s vysvětlením jednotlivých částí vzoru a ukázkami v Kotlinu a Pythonu.",
                        category = "Jazyky",
                        author = "RegexStudio",
                        iconName = "find_in_page",
                        isInstalled = true,
                        isEnabled = false,
                        downloadsCount = 1950,
                        rating = 4.5f,
                        sizeKb = 150,
                        changelog = "Přidána knihovna předpřipravených vzorů pro e-maily, URL a IP adresy.",
                        permissions = "Žádná zvláštní oprávnění",
                        websiteUrl = "https://github.com/opencode/regex-lab"
                    ),
                    PluginEntity(
                        id = "plugin_docker_inspector",
                        name = "Docker & Kontejnerový Manažer",
                        installedVersion = null,
                        latestVersion = "1.0.8",
                        description = "Přehled běžících kontejnerů, kontrola logů a správa vývojových obrazů.",
                        detailedDescription = "Rozhraní pro sledování stavu kontejnerů využívaných lokálními MCP servery a databázovými službami.",
                        category = "Nástroje",
                        author = "CloudNative Ops",
                        iconName = "developer_board",
                        isInstalled = false,
                        isEnabled = false,
                        downloadsCount = 1680,
                        rating = 4.4f,
                        sizeKb = 290,
                        changelog = "Sledování spotřeby CPU a paměti u kontejnerů.",
                        permissions = "Přístup k Docker soketu / MCP",
                        websiteUrl = "https://github.com/opencode/docker-manager"
                    ),
                    PluginEntity(
                        id = "plugin_theme_cyber",
                        name = "Cyberpunk & Midnight Neon Téma",
                        installedVersion = null,
                        latestVersion = "1.1.0",
                        description = "Neonové barevné schéma s vysokým kontrastem pro pohodlné noční programování.",
                        detailedDescription = "Kolekce moderních barevných palet šetrných k očím s purpurovými a tyrkysovými akcenty pro editor a terminál.",
                        category = "Vzhled",
                        author = "DesignCraft",
                        iconName = "palette",
                        isInstalled = false,
                        isEnabled = false,
                        downloadsCount = 3450,
                        rating = 4.8f,
                        sizeKb = 95,
                        changelog = "Optimalizace kontrastu pro OLED obrazovky.",
                        permissions = "Přizpůsobení rozhraní",
                        websiteUrl = "https://github.com/opencode/cyber-theme"
                    ),
                    PluginEntity(
                        id = "plugin_ast_grapher",
                        name = "AST Graf & Architektura modulů",
                        installedVersion = null,
                        latestVersion = "0.9.5",
                        description = "Vizuální zobrazení stromu závislostí a hierarchie tříd v reálném čase.",
                        detailedDescription = "Pomáhá pochopit složité projektové architektury a odhalit cyklické závislosti mezi komponentami.",
                        category = "Jazyky",
                        author = "CompilerTeam",
                        iconName = "account_tree",
                        isInstalled = false,
                        isEnabled = false,
                        downloadsCount = 1420,
                        rating = 4.7f,
                        sizeKb = 460,
                        changelog = "Interaktivní přibližování a export diagramu do SVG.",
                        permissions = "Čtení souborů projektu",
                        websiteUrl = "https://github.com/opencode/ast-grapher"
                    )
                )
            )
        }

        // 5. Initial Chat Session
        val existingSessions = chatDao.getAllSessions().first()
        if (existingSessions.isEmpty()) {
            val initialSessionId = UUID.randomUUID().toString()
            chatDao.insertSession(
                ChatSessionEntity(
                    id = initialSessionId,
                    title = "Inicializace OpenCode Workspace",
                    modelName = ZenModel.ZEN_CODER.displayName
                )
            )
            chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = initialSessionId,
                    role = "assistant",
                    content = """
                        Vítejte v **OpenCode**! Jsem váš inteligentní programovací asistent s modely **Zen**.

                        Prostředí je plně připraveno:
                        - 📁 **Soubory**: Projektové soubory ve workspace (`MainActivity.kt`, `Repository.kt`, `schema.sql`, `AppConfig.json`, `script.py`).
                        - 🌐 **Internet**: Vyhledávání v dokumentacích a inspekce URL přes `/search <dotaz>`.
                        - 🧠 **Dovednosti (Skills)**: Předinstalované specializace pro Jetpack Compose, MCP a Bezpečnostní audit.
                        - 🔌 **MCP**: Připojeny servery Filesystem, GitHub a Brave Search.
                        - 🧩 **Správce pluginů**: Katalog s vyhledáváním, instalací, aktualizacemi a podrobnostmi o doplňcích.
                        - 💻 **Terminál**: Spouštění příkazů přes `/terminal <příkaz>` (např. `/terminal git status` nebo `/terminal ls`).

                        S jakým vývojářským úkolem dnes začneme?
                    """.trimIndent()
                )
            )
        }

        // 6. Initial Registered Skills in Skill Registry (Local Room DB)
        val existingRegisteredSkills = skillRegistryDao.getAllRegisteredSkills().first()
        if (existingRegisteredSkills.isEmpty()) {
            skillRegistryDao.insertSkills(
                listOf(
                    SkillRegistryEntity(
                        id = "skill_reg_git_pipeline",
                        name = "Git Test-Verify-Commit Pipeline",
                        description = "Sekvence pro validaci kódu, kontrolu linteru, staging změn a vytvoření strukturovaného Git commitu.",
                        category = "DevOps",
                        executionStepsJson = """["Spustit gradle compile_applet a zkontrolovat syntaxi", "Zkontrolovat git status v pracovním adresáři", "Označit modifikované soubory (git add -A)", "Vygenerovat sémantickou commit zprávu podle konvence Conventional Commits", "Vytvořit commit v lokálním repozitáři"]""",
                        requiredTools = "terminal, git, linter",
                        successScore = 1.0f,
                        executionCount = 6,
                        lastUsedTimestamp = System.currentTimeMillis() - 3600000,
                        isChainable = true,
                        inputTemplate = "commit_type, commit_summary",
                        outputArtifact = "Verified Commit & Clean Working Tree",
                        autoDetected = true,
                        triggerPattern = "compile_pass && git_status_dirty"
                    ),
                    SkillRegistryEntity(
                        id = "skill_reg_code_refactor",
                        name = "Compose UI Refactoring & M3 Cleanup",
                        description = "Automatické ověření parametrů Modifier, doplnění accessibility contentDescription a migrace na M3 colorScheme.",
                        category = "Refactoring",
                        executionStepsJson = """["Analyzovat Composable funkce v cílovém souboru", "Zajistit předávání Modifieru jako prvního volitelného parametru", "Doplnit unikátní Modifier.testTag pro testovatelnost", "Nahradit pevné hex barvy za MaterialTheme.colorScheme", "Zkontrolovat minimální touch target 48dp"]""",
                        requiredTools = "file_editor, syntax_analyzer",
                        successScore = 0.98f,
                        executionCount = 4,
                        lastUsedTimestamp = System.currentTimeMillis() - 7200000,
                        isChainable = true,
                        inputTemplate = "target_composable_file",
                        outputArtifact = "Refactored Clean Composable",
                        autoDetected = true,
                        triggerPattern = "compose_ui_detected"
                    ),
                    SkillRegistryEntity(
                        id = "skill_reg_security_scan",
                        name = "Secrets & Hardcoded Keys Scanner",
                        description = "Skenování souborů workspace na přítomnost nezašifrovaných API klíčů, tokenů a hesel před publikací.",
                        category = "Bezpečnost",
                        executionStepsJson = """["Prohledat kód na regex vzory API klíčů (AIza, sk-, ghp_)", "Ověřit zda klíče nejsou v git commit historii", "Přesunout nalezená tajemství do Secrets panelu / .env", "Nahradit volání bezpečným BuildConfig odkazem"]""",
                        requiredTools = "grep, regex_scanner, file_patch",
                        successScore = 1.0f,
                        executionCount = 8,
                        lastUsedTimestamp = System.currentTimeMillis() - 14400000,
                        isChainable = true,
                        inputTemplate = "scan_directory",
                        outputArtifact = "Zero Exposed Secrets Report",
                        autoDetected = true,
                        triggerPattern = "pre_commit_hook"
                    ),
                    SkillRegistryEntity(
                        id = "skill_reg_doc_sync",
                        name = "Architecture & Mermaid Diagram Generator",
                        description = "Vygenerování aktuálního Markdown popisu a Mermaid diagramu datových toků po úspěšné refaktorizaci.",
                        category = "Dokumentace",
                        executionStepsJson = """["Prozkoumat závislosti mezi ViewModel, Repository a DAO", "Sestavit textový Mermaid class/flow diagram", "Aktualizovat ARCHITECTURE.md v kořeni projektu", "Zaznamenat verzi do changelogu"]""",
                        requiredTools = "diagram_engine, file_writer",
                        successScore = 0.95f,
                        executionCount = 3,
                        lastUsedTimestamp = System.currentTimeMillis() - 86400000,
                        isChainable = true,
                        inputTemplate = "module_scope",
                        outputArtifact = "Updated ARCHITECTURE.md with Mermaid Graph",
                        autoDetected = true,
                        triggerPattern = "feature_completed"
                    ),
                    SkillRegistryEntity(
                        id = "skill_reg_db_migration",
                        name = "Room Entity & Migration Validator",
                        description = "Ověření kompatibility schématu Room databáze, inkrementace verze a kontrola DAO metod.",
                        category = "Databáze",
                        executionStepsJson = """["Zkontrolovat primární klíče u všech @Entity tříd", "Ověřit flow návratové typy u DAO selekcí", "Zkontrolovat fallbackToDestructiveMigration nebo Migration skript", "Spustit testovací dotaz na SQLite jádro"]""",
                        requiredTools = "sqlite_validator, terminal",
                        successScore = 1.0f,
                        executionCount = 5,
                        lastUsedTimestamp = System.currentTimeMillis() - 172800000,
                        isChainable = true,
                        inputTemplate = "database_class",
                        outputArtifact = "Verified Database Schema",
                        autoDetected = true,
                        triggerPattern = "entity_modified"
                    )
                )
            )
        }
    }
}
