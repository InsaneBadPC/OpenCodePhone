package com.example.data.models

data class GitCommit(
    val id: String,
    val message: String,
    val author: String,
    val date: String,
    val filesChangedCount: Int
)

enum class DiffLineType {
    ADDED, REMOVED, UNCHANGED
}

data class DiffLine(
    val type: DiffLineType,
    val text: String,
    val oldLineNo: Int? = null,
    val newLineNo: Int? = null
)

data class GitFileChange(
    val path: String,
    val isStaged: Boolean,
    val status: String, // "MODIFIED", "ADDED", "DELETED"
    val additions: Int,
    val deletions: Int,
    val diff: List<DiffLine>
)

data class SqlQueryResult(
    val columns: List<String>,
    val rows: List<List<String>>,
    val rowCount: Int,
    val executionTimeMs: Long,
    val error: String? = null
)

data class CodeSnippet(
    val id: String,
    val title: String,
    val language: String,
    val category: String,
    val description: String,
    val code: String
)

data class DevContainer(
    val id: String,
    val name: String,
    val image: String,
    val status: String, // "RUNNING", "STOPPED"
    val ports: String,
    val memoryUsage: String,
    val cpuUsage: String,
    val logs: List<String>
)

enum class LogLevel {
    VERBOSE, DEBUG, INFO, WARN, ERROR
}

data class LogcatEntry(
    val id: Long,
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String
)

data class TestCase(
    val id: String,
    val name: String,
    val category: String,
    val status: String, // "PASSED", "FAILED", "RUNNING", "IDLE"
    val durationMs: Long,
    val message: String = ""
)

data class GlobalSearchResult(
    val filePath: String,
    val fileName: String,
    val lineNumber: Int,
    val lineContent: String,
    val matchStartIndex: Int,
    val matchLength: Int
)
