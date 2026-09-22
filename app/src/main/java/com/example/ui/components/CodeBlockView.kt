package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CodeBlockView(
    code: String,
    language: String = "kotlin",
    onApplyToFile: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lines = code.lines()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("code_block_${language}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Slate950
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DataObject,
                        contentDescription = null,
                        tint = CyanBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = language.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanBright,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${lines.size} řádků)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                            Toast.makeText(context, "Kód byl zkopírován do schránky", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("copy_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Kopírovat kód",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Code lines with syntax highlighting
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = highlightCode(code, language),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

fun highlightCode(code: String, language: String): AnnotatedString {
    return buildAnnotatedString {
        append(code)

        val keywords = listOf(
            "val", "var", "fun", "class", "interface", "object", "return",
            "if", "else", "when", "for", "while", "package", "import",
            "def", "import", "from", "as", "async", "await", "function",
            "const", "let", "select", "from", "where", "insert", "create"
        )

        val fullText = code

        // Highlight keywords
        for (kw in keywords) {
            val regex = Regex("\\b$kw\\b")
            regex.findAll(fullText).forEach { matchResult ->
                addStyle(
                    style = SpanStyle(color = CodeKeyword),
                    start = matchResult.range.first,
                    end = matchResult.range.last + 1
                )
            }
        }

        // Highlight strings
        val stringRegex = Regex("\"(\\\\.|[^\"])*\"")
        stringRegex.findAll(fullText).forEach { matchResult ->
            addStyle(
                style = SpanStyle(color = CodeString),
                start = matchResult.range.first,
                end = matchResult.range.last + 1
            )
        }

        // Highlight single-line comments
        val commentRegex = Regex("(//.*|#.*)")
        commentRegex.findAll(fullText).forEach { matchResult ->
            addStyle(
                style = SpanStyle(color = CodeComment),
                start = matchResult.range.first,
                end = matchResult.range.last + 1
            )
        }

        // Highlight numbers
        val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
        numberRegex.findAll(fullText).forEach { matchResult ->
            addStyle(
                style = SpanStyle(color = CodeNumber),
                start = matchResult.range.first,
                end = matchResult.range.last + 1
            )
        }
    }
}
