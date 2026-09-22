package com.example.data.model

object MarketplaceCatalog {
    val items: List<MarketplaceItem> = listOf(
        // ==================== OFICIÁLNÍ PLUGINY ====================
        MarketplaceItem(
            id = "plugin_git_integration_pro",
            name = "Git Integrace Pro",
            type = MarketItemType.PLUGIN,
            source = MarketSource.OFFICIAL,
            author = "OpenCode Core Team",
            version = "2.4.0",
            description = "Oficiální modul pro kompletní správu verzí Git: interaktivní vizuální commit, diff, větvení a synchronizace.",
            category = "Git",
            githubUrl = "https://github.com/opencode-ide/plugin-git-pro",
            rating = 4.9f,
            downloadsCount = "48.2k",
            isVerified = true,
            tags = listOf("git", "vcs", "diff", "branches"),
            permissionsJson = "[\"Čtení a zápis do pracovního prostoru\", \"Spouštění Git příkazů v terminálu\"]",
            toolsJson = "[\"gitCommit\", \"gitDiff\", \"gitCheckout\", \"gitPush\"]",
            changelog = "Přidán split-view vizualizátor konfliktů a podpora rebase."
        ),
        MarketplaceItem(
            id = "plugin_docker_explorer",
            name = "Docker & Podman Explorer",
            type = MarketItemType.PLUGIN,
            source = MarketSource.OFFICIAL,
            author = "OpenCode Core Team",
            version = "1.8.0",
            description = "Správa kontejnerů, procházení image repozitářů, sledování živých logů a spouštění Docker Compose souborů.",
            category = "Nástroje",
            githubUrl = "https://github.com/opencode-ide/plugin-docker-explorer",
            rating = 4.8f,
            downloadsCount = "31.5k",
            isVerified = true,
            tags = listOf("docker", "containers", "podman", "compose"),
            permissionsJson = "[\"Síťová komunikace\", \"Přístup k Docker soketu\"]",
            toolsJson = "[\"dockerPs\", \"dockerLogs\", \"dockerComposeUp\"]",
            changelog = "Podpora pro Podman socket na Linuxu a Android terminálu."
        ),
        MarketplaceItem(
            id = "plugin_flutter_studio",
            name = "Flutter & Dart Toolkit",
            type = MarketItemType.PLUGIN,
            source = MarketSource.OFFICIAL,
            author = "OpenCode Core Team",
            version = "1.2.1",
            description = "Nástroje pro vývoj ve Flutteru, formátování Dart kódu, widget tree inspektor a podpora Hot Reload.",
            category = "Editor",
            githubUrl = "https://github.com/opencode-ide/plugin-flutter-studio",
            rating = 4.8f,
            downloadsCount = "24.1k",
            isVerified = true,
            tags = listOf("flutter", "dart", "mobile", "crossplatform"),
            permissionsJson = "[\"Spouštění Dart SDK\", \"Formátování kódu\"]",
            toolsJson = "[\"dartFormat\", \"flutterHotReload\", \"pubGet\"]",
            changelog = "Optimalizováno pro Flutter 3.24 a Dart 3.5."
        ),
        MarketplaceItem(
            id = "plugin_graphql_inspector",
            name = "GraphQL & REST Inspector",
            type = MarketItemType.PLUGIN,
            source = MarketSource.OFFICIAL,
            author = "OpenCode Core Team",
            version = "3.0.0",
            description = "Interaktivní Playground pro testování GraphQL dotazů, introspekci schémat a mockování REST koncových bodů.",
            category = "Nástroje",
            githubUrl = "https://github.com/opencode-ide/plugin-graphql-inspector",
            rating = 4.9f,
            downloadsCount = "19.8k",
            isVerified = true,
            tags = listOf("graphql", "api", "rest", "schema"),
            permissionsJson = "[\"Internetová komunikace (HTTP/HTTPS)\"]",
            toolsJson = "[\"executeGraphQL\", \"introspectSchema\", \"mockEndpoint\"]",
            changelog = "Generátor TypeScript a Kotlin typů přímo z GraphQL schématu."
        ),
        MarketplaceItem(
            id = "plugin_rust_cargo",
            name = "Rust Cargo Analyzer",
            type = MarketItemType.PLUGIN,
            source = MarketSource.OFFICIAL,
            author = "OpenCode Core Team",
            version = "1.5.0",
            description = "Rychlá syntaktická analýza, cargo check integrace, správa závislostí a linter Clippy.",
            category = "Editor",
            githubUrl = "https://github.com/opencode-ide/plugin-rust-cargo",
            rating = 4.9f,
            downloadsCount = "27.3k",
            isVerified = true,
            tags = listOf("rust", "cargo", "clippy", "compiler"),
            permissionsJson = "[\"Čtení Cargo.toml\", \"Kompilace Rustu\"]",
            toolsJson = "[\"cargoCheck\", \"cargoTest\", \"cargoBuild\"]",
            changelog = "Přidána přímá diagnostika borrow-check chyb s českým vysvětlením."
        ),

        // ==================== KOMUNITNÍ PLUGINY ====================
        MarketplaceItem(
            id = "plugin_tailwind_studio",
            name = "Tailwind CSS Studio",
            type = MarketItemType.PLUGIN,
            source = MarketSource.COMMUNITY,
            author = "@adam_w",
            version = "1.4.2",
            description = "Automatické našeptávání Tailwind tříd, náhled barev přímo v editoru a generování Compose barevných stylů.",
            category = "Editor",
            githubUrl = "https://github.com/community-opencode/tailwind-studio",
            rating = 4.9f,
            downloadsCount = "14.2k",
            isVerified = false,
            tags = listOf("tailwind", "css", "styling", "ui"),
            permissionsJson = "[\"Čtení konfiguračních souborů\"]",
            toolsJson = "[\"tailwindAutocomplete\", \"generateTokens\"]",
            changelog = "Podpora Tailwind CSS v4.0 alpha."
        ),
        MarketplaceItem(
            id = "plugin_vim_keybindings",
            name = "Vim Keybindings",
            type = MarketItemType.PLUGIN,
            source = MarketSource.COMMUNITY,
            author = "@vim_fan",
            version = "2.0.1",
            description = "Kompletní modální ovládání Vim (Normal, Insert, Visual režimy) přizpůsobené pro dotykový mobilní editor.",
            category = "Editor",
            githubUrl = "https://github.com/community-opencode/vim-opencode",
            rating = 4.8f,
            downloadsCount = "22.5k",
            isVerified = false,
            tags = listOf("vim", "keybindings", "editor", "productivity"),
            permissionsJson = "[\"Přístup k editoru\"]",
            toolsJson = "[\"vimCommandExecute\", \"vimChangeMode\"]",
            changelog = "Zpřesněny textové objekty a macro nahrávání."
        ),
        MarketplaceItem(
            id = "plugin_regex_tester",
            name = "RegEx Live Tester",
            type = MarketItemType.PLUGIN,
            source = MarketSource.COMMUNITY,
            author = "@regex_guru",
            version = "1.0.5",
            description = "Vizuální testování regulárních výrazů v reálném čase se zvýrazněním zachycených skupin a vysvětlením syntaxe.",
            category = "Nástroje",
            githubUrl = "https://github.com/community-opencode/regex-live-tester",
            rating = 4.9f,
            downloadsCount = "11.4k",
            isVerified = false,
            tags = listOf("regex", "tester", "patterns", "parsing"),
            permissionsJson = "[\"Lokální spouštění\"]",
            toolsJson = "[\"evaluateRegex\", \"explainRegex\"]",
            changelog = "Přidána knihovna připravených vzorů pro e-maily, URL a datumy."
        ),
        MarketplaceItem(
            id = "plugin_markdown_preview",
            name = "Markdown Live Previewer",
            type = MarketItemType.PLUGIN,
            source = MarketSource.COMMUNITY,
            author = "@doc_writer",
            version = "1.3.0",
            description = "Okamžitý živý náhled Markdown dokumentů, tabulek, Mermaid diagramů a matematických vzorců v KaTeX.",
            category = "Editor",
            githubUrl = "https://github.com/community-opencode/markdown-previewer",
            rating = 4.8f,
            downloadsCount = "19.1k",
            isVerified = false,
            tags = listOf("markdown", "preview", "mermaid", "katex"),
            permissionsJson = "[\"Čtení souborů\"]",
            toolsJson = "[\"renderMarkdown\", \"exportHtml\"]",
            changelog = "Podpora generování vývojářské dokumentace do formátu PDF."
        ),

        // ==================== OFICIÁLNÍ DOVEDNOSTI (SKILLS) ====================
        MarketplaceItem(
            id = "skill_compose_master",
            name = "Jetpack Compose Master",
            type = MarketItemType.SKILL,
            source = MarketSource.OFFICIAL,
            author = "OpenCode Android Lab",
            version = "2.1.0",
            description = "Oficiální vývojářská dovednost pro pokročilé komponenty Jetpack Compose, Material 3, plynulé animace a recomposition profiling.",
            category = "Android UI",
            githubUrl = "https://github.com/opencode-ide/skill-compose-master",
            rating = 5.0f,
            downloadsCount = "62.4k",
            isVerified = true,
            tags = listOf("compose", "android", "m3", "animations"),
            systemPrompt = "Jsi expertní Android Jetpack Compose architekt. Vždy piš moderní Kotlin s Material 3, striktním dodržováním WindowInsets, správou stavu přes StateFlow a vyhýbej se zbytečným rekompozicím pomocí remember a derivedStateOf."
        ),
        MarketplaceItem(
            id = "skill_android_security",
            name = "Android Security Auditor",
            type = MarketItemType.SKILL,
            source = MarketSource.OFFICIAL,
            author = "OpenCode Security Lab",
            version = "1.5.0",
            description = "Penetrační analýza oprávnění v AndroidManifest.xml, kontrola zranitelností v Intent filtrech, bezpečné ukládání tokenů v EncryptedSharedPreferences.",
            category = "Bezpečnost",
            githubUrl = "https://github.com/opencode-ide/skill-android-security",
            rating = 4.9f,
            downloadsCount = "38.9k",
            isVerified = true,
            tags = listOf("security", "audit", "owasp", "permissions"),
            systemPrompt = "Při jakékoliv revizi kódu prováděj důsledný bezpečnostní audit podle standardů OWASP Mobile. Kontroluj zranitelnosti SQL injection, nezabezpečené Intent komunikace a úniky citlivých klíčů."
        ),
        MarketplaceItem(
            id = "skill_clean_architecture",
            name = "Clean Architecture & SOLID",
            type = MarketItemType.SKILL,
            source = MarketSource.OFFICIAL,
            author = "OpenCode Core Team",
            version = "2.0.0",
            description = "Vedení modelu k čisté architektuře, striktnímu oddělení Domain, Data a UI vrstev a uplatňování návrhových vzorů SOLID.",
            category = "Architektura",
            githubUrl = "https://github.com/opencode-ide/skill-clean-arch",
            rating = 4.9f,
            downloadsCount = "51.0k",
            isVerified = true,
            tags = listOf("architecture", "solid", "clean", "mvvm"),
            systemPrompt = "Všechny navrhované úpravy organizuj podle principů Clean Architecture. Odděluj rozhraní (Interfaces) od implementací a udržuj byznys logiku v doménových UseCases."
        ),
        MarketplaceItem(
            id = "skill_cloud_kubernetes",
            name = "Cloud & Kubernetes Architect",
            type = MarketItemType.SKILL,
            source = MarketSource.OFFICIAL,
            author = "OpenCode Cloud Team",
            version = "1.4.0",
            description = "Návrh cloudových mikroslužeb na GCP/AWS, psaní deklarativních manifestů Kubernetes a optimalizace Dockerfile vícefázových sestavení.",
            category = "Cloud & DevOps",
            githubUrl = "https://github.com/opencode-ide/skill-cloud-k8s",
            rating = 4.8f,
            downloadsCount = "29.7k",
            isVerified = true,
            tags = listOf("kubernetes", "k8s", "docker", "gcp", "aws"),
            systemPrompt = "Generuj produkčně validní Kubernetes manifesty (Deployment, Service, Ingress), Helm charty a zabezpečené vícefázové Dockerfile pro kontejnerizaci."
        ),

        // ==================== KOMUNITNÍ DOVEDNOSTI (SKILLS) ====================
        MarketplaceItem(
            id = "skill_kmp_wizard",
            name = "Kotlin Multiplatform (KMP)",
            type = MarketItemType.SKILL,
            source = MarketSource.COMMUNITY,
            author = "@mobile_pro",
            version = "1.3.0",
            description = "Specializovaná pravidla pro sdílení byznys logiky mezi Androidem a iOS přes KMP, Ktor klient a Compose Multiplatform.",
            category = "Multiplatform",
            githubUrl = "https://github.com/community-opencode/skill-kmp-wizard",
            rating = 4.8f,
            downloadsCount = "15.6k",
            isVerified = false,
            tags = listOf("kmp", "kotlin", "ios", "multiplatform"),
            systemPrompt = "Při návrhu sdíleného kódu používej moderní Kotlin Multiplatform vzory, expect/actual deklarace a sdílené ViewModel vrstvy kompatibilní s iOS i Androidem."
        ),
        MarketplaceItem(
            id = "skill_prompt_engineer",
            name = "Prompt Engineering Specialist",
            type = MarketItemType.SKILL,
            source = MarketSource.COMMUNITY,
            author = "@ai_wizard",
            version = "2.2.0",
            description = "Dovednost pro optimalizaci promptů, Chain-of-Thought uvažování, Few-Shot šablony a zamezení halucinacím velkých jazykových modelů.",
            category = "AI & LLM",
            githubUrl = "https://github.com/community-opencode/skill-prompt-engineer",
            rating = 4.9f,
            downloadsCount = "28.3k",
            isVerified = false,
            tags = listOf("prompt", "ai", "llm", "cot"),
            systemPrompt = "Analyzuj a optimalizuj prompty uživatele tak, aby dosahovaly maximální přesnosti, strukturovaného výstupu ve formátu JSON a jasných systémových hranic."
        ),
        MarketplaceItem(
            id = "skill_cybersec_pentest",
            name = "Cybersecurity PenTester",
            type = MarketItemType.SKILL,
            source = MarketSource.COMMUNITY,
            author = "@sec_lab",
            version = "1.1.0",
            description = "Identifikace zranitelností XSS, CSRF, insecure deserialization a kontrola konfigurace CORS v API rozhraních.",
            category = "Bezpečnost",
            githubUrl = "https://github.com/community-opencode/skill-pentest",
            rating = 4.7f,
            downloadsCount = "9.4k",
            isVerified = false,
            tags = listOf("security", "pentest", "xss", "csrf"),
            systemPrompt = "Při analýze backendových a webových služeb hledej slabá místa v autentizaci, manipulaci se session tokeny a nezabezpečené hlavičky CORS."
        ),

        // ==================== OFICIÁLNÍ MCP SERVERY ====================
        MarketplaceItem(
            id = "mcp_github_official",
            name = "GitHub MCP Server",
            type = MarketItemType.MCP,
            source = MarketSource.OFFICIAL,
            author = "Model Context Protocol & GitHub",
            version = "1.0.0",
            description = "Oficiální MCP server umožňující AI asistentovi číst repozitáře, vytvářet Pull Requesty, procházet Issues a komentovat commity.",
            category = "Verzování",
            githubUrl = "https://github.com/modelcontextprotocol/servers/tree/main/src/github",
            rating = 4.9f,
            downloadsCount = "78.4k",
            isVerified = true,
            tags = listOf("mcp", "github", "git", "pr", "issues"),
            transport = "STDIO",
            endpointOrCommand = "npx -y @modelcontextprotocol/server-github",
            toolsJson = "[\"create_issue\", \"list_pull_requests\", \"get_file_contents\", \"create_pull_request\"]"
        ),
        MarketplaceItem(
            id = "mcp_sqlite_postgres",
            name = "Database MCP (SQLite & Postgres)",
            type = MarketItemType.MCP,
            source = MarketSource.OFFICIAL,
            author = "Model Context Protocol Team",
            version = "1.2.0",
            description = "Přímý přístup k SQL databázím: čtení schémat, provádění selektivních dotazů, kontrola indexů a vysvětlení exekučních plánů.",
            category = "Databáze",
            githubUrl = "https://github.com/modelcontextprotocol/servers/tree/main/src/postgres",
            rating = 4.9f,
            downloadsCount = "65.1k",
            isVerified = true,
            tags = listOf("mcp", "sql", "sqlite", "postgres", "database"),
            transport = "STDIO",
            endpointOrCommand = "npx -y @modelcontextprotocol/server-postgres",
            toolsJson = "[\"read_query\", \"describe_table\", \"list_tables\", \"execute_explain\"]"
        ),
        MarketplaceItem(
            id = "mcp_filesystem_extended",
            name = "FileSystem Extended MCP",
            type = MarketItemType.MCP,
            source = MarketSource.OFFICIAL,
            author = "Model Context Protocol Team",
            version = "1.1.0",
            description = "Pokročilá práce se souborovým systémem: hromadné vyhledávání podle glob vzorů, výpočet hashe souborů a diff analýza.",
            category = "Systém",
            githubUrl = "https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem",
            rating = 4.8f,
            downloadsCount = "52.3k",
            isVerified = true,
            tags = listOf("mcp", "filesystem", "files", "search"),
            transport = "STDIO",
            endpointOrCommand = "npx -y @modelcontextprotocol/server-filesystem /workspace",
            toolsJson = "[\"read_file\", \"write_file\", \"list_directory\", \"search_files\", \"get_file_info\"]"
        ),
        MarketplaceItem(
            id = "mcp_puppeteer_scraper",
            name = "Puppeteer Web Automator MCP",
            type = MarketItemType.MCP,
            source = MarketSource.OFFICIAL,
            author = "Model Context Protocol Team",
            version = "1.0.4",
            description = "Spouštění headless prohlížeče Chrome: pořizování snímků obrazovky, interakce s formuláři a extrakce dynamického JS obsahu.",
            category = "Web",
            githubUrl = "https://github.com/modelcontextprotocol/servers/tree/main/src/puppeteer",
            rating = 4.8f,
            downloadsCount = "36.8k",
            isVerified = true,
            tags = listOf("mcp", "puppeteer", "browser", "automation", "scrape"),
            transport = "STDIO",
            endpointOrCommand = "npx -y @modelcontextprotocol/server-puppeteer",
            toolsJson = "[\"navigate\", \"screenshot\", \"click\", \"fill\", \"evaluate_script\"]"
        ),

        // ==================== KOMUNITNÍ MCP SERVERY ====================
        MarketplaceItem(
            id = "mcp_notion_workspace",
            name = "Notion Workspace MCP",
            type = MarketItemType.MCP,
            source = MarketSource.COMMUNITY,
            author = "@pkm_enthusiast",
            version = "1.0.8",
            description = "Synchronizace poznámek, čtení databází projektů a automatické vytváření úkolů přímo v Notion workspace.",
            category = "Produktivita",
            githubUrl = "https://github.com/community-opencode/mcp-notion-server",
            rating = 4.9f,
            downloadsCount = "8.1k",
            isVerified = false,
            tags = listOf("mcp", "notion", "notes", "tasks"),
            transport = "SSE",
            endpointOrCommand = "http://localhost:3001/sse",
            toolsJson = "[\"search_notion\", \"append_page_block\", \"create_database_item\"]"
        ),
        MarketplaceItem(
            id = "mcp_slack_discord",
            name = "Slack & Discord Notifier MCP",
            type = MarketItemType.MCP,
            source = MarketSource.COMMUNITY,
            author = "@devops_team",
            version = "1.2.0",
            description = "Odesílání build notifikací, hlášení chyb a zpráv ze stavu vývoje do týmových kanálů na Slacku i Discordu.",
            category = "Komunikace",
            githubUrl = "https://github.com/community-opencode/mcp-slack-notifier",
            rating = 4.7f,
            downloadsCount = "14.0k",
            isVerified = false,
            tags = listOf("mcp", "slack", "discord", "notifications"),
            transport = "STDIO",
            endpointOrCommand = "npx -y @community/mcp-team-notifier",
            toolsJson = "[\"send_slack_message\", \"send_discord_webhook\", \"list_channels\"]"
        ),
        MarketplaceItem(
            id = "mcp_linear_issues",
            name = "Linear Issues MCP",
            type = MarketItemType.MCP,
            source = MarketSource.COMMUNITY,
            author = "@product_lead",
            version = "1.1.2",
            description = "Napojení na systém řízení projektů Linear: vyhledávání ticketů, aktualizace stavů a přiřazování úkolů vývojářům.",
            category = "Projekt",
            githubUrl = "https://github.com/community-opencode/mcp-linear",
            rating = 4.8f,
            downloadsCount = "9.8k",
            isVerified = false,
            tags = listOf("mcp", "linear", "issues", "agile"),
            transport = "STDIO",
            endpointOrCommand = "npx -y @community/mcp-linear",
            toolsJson = "[\"get_issue\", \"list_issues\", \"create_issue\", \"update_issue_status\"]"
        )
    )

    fun parseGithubUrl(rawUrl: String): ParsedGithubRepo {
        val trimmed = rawUrl.trim()
        val cleaned = trimmed
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("github.com/")
            .trim('/')

        val parts = cleaned.split("/")
        val owner = parts.getOrNull(0) ?: "github-user"
        val repo = parts.getOrNull(1)?.replace(".git", "") ?: "opencode-extension"

        // Heuristic detection based on keywords
        val lower = cleaned.lowercase()
        val detectedType = when {
            lower.contains("mcp") || lower.contains("modelcontextprotocol") || lower.contains("server") -> MarketItemType.MCP
            lower.contains("skill") || lower.contains("prompt") || lower.contains("agent") -> MarketItemType.SKILL
            else -> MarketItemType.PLUGIN
        }

        val name = repo.split("-", "_").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        return ParsedGithubRepo(
            owner = owner,
            repoName = repo,
            fullUrl = "https://github.com/$owner/$repo",
            suggestedName = name,
            detectedType = detectedType
        )
    }
}

data class ParsedGithubRepo(
    val owner: String,
    val repoName: String,
    val fullUrl: String,
    val suggestedName: String,
    val detectedType: MarketItemType
)
