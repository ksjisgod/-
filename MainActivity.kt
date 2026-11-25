package com.premi.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private val Purple = Color(0xFF8065D8)
private val Background = Color(0xFF101018)
private val Panel = Color(0xFF1C1B27)
private val White = Color(0xFFF6F4FF)

data class ChatMessage(val role: String, val text: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PremiApp() }
    }
}

@Composable
fun PremiApp() {
    val messages = remember {
        mutableStateListOf(
            ChatMessage("assistant", "안녕! 나는 premi야 😎\\n궁금한 거 편하게 물어봐. 같이 해결해보자!")
        )
    }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Purple,
        background = Background,
        surface = Panel,
        onBackground = White,
        onSurface = White
    )) {
        Column(
            modifier = Modifier.fillMaxSize().background(Background).imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Panel).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(Purple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("P", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("premi", color = White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("너의 AI 대화 친구", color = Color.LightGray, fontSize = 12.sp)
                }
                Spacer(Modifier.weight(1f))
                Text(if (loading) "답변 작성 중…" else "온라인 AI", color = Color(0xFFB7A6FF), fontSize = 12.sp)
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    val mine = message.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier.widthIn(max = 310.dp)
                                .background(if (mine) Purple else Panel, RoundedCornerShape(18.dp))
                                .padding(horizontal = 14.dp, vertical = 11.dp)
                        ) {
                            Text(if (mine) "나" else "premi", color = Color(0xFFD2C8FF), fontSize = 11.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(message.text, color = White, fontSize = 15.sp, lineHeight = 21.sp)
                        }
                    }
                }
                if (error.isNotBlank()) {
                    item { Text(error, color = Color(0xFFFF8D8D), fontSize = 12.sp) }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(Panel).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f).background(Background, RoundedCornerShape(22.dp)).padding(14.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = White, fontSize = 15.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Purple),
                    decorationBox = { inner ->
                        if (input.isEmpty()) Text("premi에게 메시지 보내기…", color = Color.Gray, fontSize = 14.sp)
                        inner()
                    },
                    enabled = !loading
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val prompt = input.trim()
                        if (prompt.isNotEmpty() && !loading) {
                            input = ""
                            error = ""
                            messages.add(ChatMessage("user", prompt))
                            loading = true
                            scope.launch {
                                try {
                                    val reply = withContext(Dispatchers.IO) {
                                        sendToPremi(messages.toList())
                                    }
                                    messages.add(ChatMessage("assistant", reply))
                                } catch (e: Exception) {
                                    error = "연결에 실패했어. 서버 주소와 인터넷 연결을 확인해줘."
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    },
                    enabled = input.isNotBlank() && !loading,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp)
                ) { Text("전송", color = Color.White) }
            }
        }
    }
}

private fun sendToPremi(history: List<ChatMessage>): String {
    val base = BuildConfig.SERVER_URL.trimEnd('/')
    require(base.startsWith("https://")) { "Set an HTTPS server URL in app/build.gradle.kts" }

    val url = URL("$base/chat")
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20000
        readTimeout = 60000
        doOutput = true
        setRequestProperty("Content-Type", "application/json; charset=utf-8")
    }

    val body = JSONObject().apply {
        put("messages", JSONArray().apply {
            history.takeLast(20).forEach { msg ->
                put(JSONObject().put("role", msg.role).put("content", msg.text))
            }
        })
    }

    try {
        connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val responseText = stream.bufferedReader().use { it.readText() }
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("Server error ${connection.responseCode}: $responseText")
        }
        return JSONObject(responseText).optString("reply", "답변을 받아오지 못했어.")
    } finally {
        connection.disconnect()
    }
}
