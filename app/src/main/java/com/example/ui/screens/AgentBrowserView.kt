package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.models.AgentBrowserAction
import com.example.ui.OpenCodeViewModel
import com.example.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AgentBrowserView(
    viewModel: OpenCodeViewModel,
    modifier: Modifier = Modifier
) {
    val session by viewModel.agentBrowserSession.collectAsState()
    val context = LocalContext.current
    var inputUrl by remember(session.currentUrl) { mutableStateOf(session.currentUrl) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var pageTitle by remember { mutableStateOf(session.pageTitle) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(12.dp)
    ) {
        // Human Takeover Alert Banner & Mode Switcher
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (session.isHumanControlActive) AmberWarning.copy(alpha = 0.15f) else CyanBright.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = if (session.isHumanControlActive) AmberWarning else CyanBright,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (session.isHumanControlActive) AmberWarning else EmeraldSuccess)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (session.isHumanControlActive) "👤 Režim manuálního převzetí (Human Takeover)" else "🤖 Autonomní agent řídí prohlížeč",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (session.isHumanControlActive) AmberWarning else CyanBright
                        )
                        Text(
                            text = if (session.isHumanControlActive) "Máte plnou kontrolu (přihlášení, 2FA, Captcha, klikání)." else "Agent samostatně prochází web, hledá dokumentaci a analyzuje DOM.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate300,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        viewModel.toggleHumanBrowserTakeover()
                        val msg = if (!session.isHumanControlActive) "Převzali jste kontrolu nad prohlížečem!" else "Kontrola předána zpět agentovi."
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (session.isHumanControlActive) EmeraldSuccess else AmberWarning,
                        contentColor = Slate950
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("browser_takeover_button")
                ) {
                    Icon(
                        imageVector = if (session.isHumanControlActive) Icons.Default.SmartToy else Icons.Default.PanTool,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (session.isHumanControlActive) "Předat agentovi" else "Převzít kontrolu",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // URL bar & Navigation controls
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Slate900,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { webViewInstance?.goBack() },
                    enabled = webViewInstance?.canGoBack() == true,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zpět", tint = Slate300, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = { webViewInstance?.reload() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Obnovit", tint = Slate300, modifier = Modifier.size(18.dp))
                }

                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    singleLine = true,
                    placeholder = { Text("Zadejte URL (např. https://github.com)", color = Slate500, fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Slate100),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedBorderColor = CyanBright,
                        unfocusedBorderColor = Slate700
                    )
                )

                Button(
                    onClick = {
                        val formatted = if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
                            "https://$inputUrl"
                        } else inputUrl
                        inputUrl = formatted
                        viewModel.agentNavigate(formatted)
                        webViewInstance?.loadUrl(formatted)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanBright, contentColor = Slate950),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Přejít", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = CyanBright,
                trackColor = Slate800
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Embedded Interactive WebView (AndroidView)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                url?.let {
                                    inputUrl = it
                                    pageTitle = view?.title ?: it
                                }
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                title?.let { pageTitle = it }
                            }
                        }
                        loadUrl(session.currentUrl)
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    // Keep in sync with session url if changed externally
                    if (webView.url != session.currentUrl && !isLoading) {
                        webView.loadUrl(session.currentUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // When agent is in control, show floating indicator badge
            if (!session.isHumanControlActive) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate950.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(10.dp),
                            color = CyanBright,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Agent snímá DOM...", fontSize = 10.sp, color = CyanBright)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Agent Action History & Quick Dispatch to Zen Chat
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = CyanBright, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Záznam akcí agenta v prohlížeči", style = MaterialTheme.typography.titleSmall, color = Slate100, fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            viewModel.sendMessage("Zanalyzuj tuto webovou stránku z prohlížeče:\nURL: ${inputUrl}\nTitulek: $pageTitle")
                            viewModel.selectTab(0)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Slate800, contentColor = CyanBright)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Poslat do Chatu", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(session.recentActions.reversed()) { action ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate800, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(
                                containerColor = when (action.actionType) {
                                    "HUMAN_TAKEOVER" -> AmberWarning
                                    "CLICK" -> VioletPurple
                                    "NAVIGATE" -> CyanBright
                                    else -> EmeraldSuccess
                                }
                            ) {
                                Text(action.actionType, fontSize = 9.sp, color = Slate950, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = action.detail,
                                fontSize = 11.sp,
                                color = Slate200,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
