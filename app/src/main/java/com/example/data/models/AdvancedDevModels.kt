package com.example.data.models

// ==========================================
// 1. VISUAL AST & ARCHITECTURE DIAGRAMS
// ==========================================
data class DiagramNode(
    val id: String,
    val label: String,
    val type: String, // "COMPOSABLE", "VIEWMODEL", "REPOSITORY", "DATABASE", "API"
    val x: Float,
    val y: Float,
    val color: String
)

data class DiagramEdge(
    val fromId: String,
    val toId: String,
    val label: String = ""
)

data class ArchitectureDiagram(
    val title: String,
    val nodes: List<DiagramNode>,
    val edges: List<DiagramEdge>
)

// ==========================================
// 2. OFFLINE LOCAL LLM RUNTIME
// ==========================================
data class LocalLlmModel(
    val id: String,
    val name: String,
    val parameterSize: String, // "Gemma 2B", "Qwen 2.5 Coder 1.5B", "Llama 3.2 1B"
    val quantFormat: String, // "Q4_K_M (GGUF)", "INT8 (ONNX)", "FP16"
    val sizeOnDiskMb: Int,
    val isDownloaded: Boolean,
    val isLoadedInRam: Boolean,
    val tokensPerSec: Float
)

// ==========================================
// 3. SPLIT EDITOR
// ==========================================
enum class SplitOrientation {
    HORIZONTAL, VERTICAL
}

// ==========================================
// 4. INTERACTIVE DEBUGGER
// ==========================================
data class Breakpoint(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val condition: String = "",
    val isEnabled: Boolean = true
)

data class WatchExpression(
    val id: String,
    val expression: String,
    val value: String,
    val type: String
)

data class CallStackFrame(
    val functionName: String,
    val fileName: String,
    val lineNumber: Int
)

// ==========================================
// 5. CI/CD PIPELINE & WEBHOOKS
// ==========================================
data class CiStep(
    val name: String,
    val command: String,
    val status: String, // "SUCCESS", "FAILED", "RUNNING", "PENDING"
    val durationSeconds: Int,
    val output: String
)

data class CiPipeline(
    val id: String,
    val commitId: String,
    val branch: String,
    val status: String, // "PASSED", "FAILED", "RUNNING"
    val steps: List<CiStep>,
    val triggeredAt: String,
    val webhookTarget: String
)

// ==========================================
// 6. CODE SHARING & P2P COLLABORATION
// ==========================================
data class Collaborator(
    val id: String,
    val name: String,
    val avatarColor: Long,
    val currentFile: String,
    val cursorLine: Int,
    val isOnline: Boolean
)

data class P2PRoom(
    val roomId: String,
    val hostName: String,
    val isEncrypted: Boolean,
    val peers: List<Collaborator>
)

// ==========================================
// 7. REGEX PLAYGROUND
// ==========================================
data class RegexMatchResult(
    val matchText: String,
    val startIndex: Int,
    val endIndex: Int,
    val groups: List<String>
)

// ==========================================
// 8. GRAPHQL & WEBSOCKET STUDIO
// ==========================================
data class WebSocketLog(
    val timestamp: String,
    val isIncoming: Boolean,
    val payload: String
)

// ==========================================
// 9. SECRETS VAULT & GOOGLE DOCS/SHEETS IMPORTER
// ==========================================
data class VaultSecret(
    val key: String,
    val value: String,
    val environment: String, // "Development", "Staging", "Production"
    val isRevealed: Boolean = false
)

enum class GoogleImportType {
    GOOGLE_DOCS, GOOGLE_SHEETS
}

data class GoogleImportItem(
    val id: String,
    val title: String,
    val type: GoogleImportType,
    val sourceUrl: String,
    val targetFileName: String,
    val lastSyncTime: String,
    val parsedContent: String
)

// ==========================================
// 10. AI VOICE CODING
// ==========================================
data class VoiceTranscription(
    val id: String,
    val recognizedText: String,
    val executedAction: String,
    val timestamp: String
)

// ==========================================
// 11. AUTONOMOUS AGENT BROWSER WITH HUMAN TAKEOVER
// ==========================================
data class AgentBrowserAction(
    val actionType: String, // "NAVIGATE", "CLICK", "TYPE", "EXTRACT", "SOLVE_CAPTCHA"
    val targetSelector: String,
    val detail: String,
    val status: String // "DONE", "ACTIVE", "WAITING_FOR_USER"
)

data class AgentBrowserSession(
    val currentUrl: String,
    val pageTitle: String,
    val isHumanControlActive: Boolean, // True = user has taken over control, False = AI Agent controls
    val screenshotPlaceholderColor: Long,
    val extractedDomSummary: String,
    val recentActions: List<AgentBrowserAction>
)

// ==========================================
// 12. FULL TERMUX / LINUX TERMINAL & ORACLE CLOUD
// ==========================================
enum class TerminalExecutionTarget {
    LOCAL_TERMUX, ORACLE_CLOUD_OCI, DOCKER_CONTAINER
}

data class TermuxInstalledPackage(
    val name: String,
    val version: String,
    val category: String, // "compiler", "utility", "runtime", "network"
    val sizeMb: Float,
    val isInstalled: Boolean
)

data class OracleCloudHost(
    val hostIp: String,
    val region: String, // "eu-frankfurt-1", "us-phoenix-1"
    val username: String, // "opc" / "ubuntu"
    val instanceType: String, // "VM.Standard.A1.Flex (4 OCPU, 24GB RAM - Always Free)"
    val isConnected: Boolean
)

// ==========================================
// 13. CLOUD INTEGRATIONS HUB
// ==========================================
data class CloudServiceIntegration(
    val id: String,
    val name: String,
    val serviceCategory: String, // "Storage", "Database", "AI Design", "Docs", "Cloud VM"
    val iconName: String,
    val description: String,
    val isConnected: Boolean,
    val connectedAccountOrProject: String?,
    val availableActions: List<String>
)

// ==========================================
// 14. AUTONOMNÍ SKILLY Z BEZCHYBNÝCH OPERACÍ (REUSABLE WORKFLOW SKILLS)
// ==========================================
data class AgentWorkflowSkill(
    val id: String,
    val name: String,
    val triggerPhrase: String,
    val description: String,
    val category: String, // "Workflow", "Kódování", "DevOps", "Database", "Testing"
    val successCount: Int,
    val lastExecutedAt: String,
    val steps: List<String>,
    val skillMarkdown: String, // Plný obsah SKILL.md s YAML hlavičkou
    val isAutoLearned: Boolean = true
)

// ==========================================
// 15. SESSION .MD ARCHIV & VYHLEDÁVÁNÍ V HISTORII
// ==========================================
data class SessionMarkdownArchive(
    val id: String,
    val title: String,
    val timestamp: String,
    val wordCount: Int,
    val filePath: String,
    val markdownContent: String
)

data class SessionSearchResult(
    val sessionId: String,
    val sessionTitle: String,
    val matchedLine: Int,
    val snippet: String,
    val context: String
)

// ==========================================
// 16. NATIVNÍ PROPOJENÍ NA GITHUB (GIT & GITHUB ENGINE)
// ==========================================
data class GitHubRepository(
    val id: String,
    val name: String,
    val fullName: String,
    val description: String,
    val isPrivate: Boolean,
    val defaultBranch: String,
    val starsCount: Int,
    val updatedAt: String,
    val cloneUrl: String
)

data class GitStatusFile(
    val filePath: String,
    val status: String, // "MODIFIED", "STAGED", "UNTRACKED", "DELETED"
    val isStaged: Boolean
)

data class GitHubPullRequest(
    val number: Int,
    val title: String,
    val author: String,
    val branch: String,
    val status: String, // "OPEN", "MERGED", "CLOSED"
    val commentsCount: Int
)

data class GitCommitRecord(
    val hash: String,
    val message: String,
    val author: String,
    val timeAgo: String
)
