package com.example.avaride_1.presentation.screens.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avaride_1.presentation.components.GlowingMeshGradient
import com.example.avaride_1.presentation.screens.search.SearchLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun BookingStatusScreen(
    destination: SearchLocation,
    onUnlock: () -> Unit
) {
    var isFinding by remember { mutableStateOf(true) }
    var avId by remember { mutableStateOf("") }
    var etaMinutes by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        // Simulate "Finding AV" State
        delay(2000) 
        avId = "AV-SG-${(1000..9999).random()}"
        etaMinutes = 2 // Hardcode 2 minutes as requested
        isFinding = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowingMeshGradient()

        if (isFinding) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF0A84FF),
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Finding your AV...",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Authorized payment. Searching for nearest vehicle...",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            AVFoundContent(
                avId = avId,
                etaMinutes = etaMinutes,
                destination = destination,
                onUnlock = onUnlock
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val color = if (message.isUser) Color(0xFF0A84FF) else Color(0xFF333333)
    val cornerRadius = 16.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = color,
            shape = RoundedCornerShape(
                topStart = cornerRadius,
                topEnd = cornerRadius,
                bottomStart = if (message.isUser) cornerRadius else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else cornerRadius
            ),
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 0.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                modifier = Modifier.padding(12.dp),
                fontSize = 15.sp
            )
        }
    }
}

// Simple mock AI response logic
fun getMockAIResponse(query: String): String {
    val q = query.lowercase()
    return when {
        "hello" in q || "hi" in q -> "Hello there! I'm driving autonomously to your pickup point."
        "long" in q || "time" in q || "wait" in q -> "Traffic is a bit heavy, but I'm optimizing my route. ETA is accurate!"
        "music" in q -> "I can play your favorite playlist. Just ask when you get in!"
        "temp" in q || "cold" in q -> "I've set the temperature to a comfortable 22°C."
        else -> "I understand. I'm focused on driving safely to you!"
    }
}

@Composable
private fun AVFoundContent(
    avId: String,
    etaMinutes: Int,
    destination: SearchLocation,
    onUnlock: () -> Unit
) {
    var chatMessages by remember { mutableStateOf(listOf(
        ChatMessage("👋 Hi! I'm Ava, your AV assistant. I'm on my way!", false)
    )) }
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // Timer state
    var timeRemainingSeconds by remember { mutableStateOf(120) } // 2 minutes
    val isArrived = timeRemainingSeconds <= 0

    // Countdown effect
    LaunchedEffect(Unit) {
        while (timeRemainingSeconds > 0) {
            delay(1000)
            timeRemainingSeconds--
            
            // Optional: AI updates when close
            if (timeRemainingSeconds == 60) {
                chatMessages = chatMessages + ChatMessage("I'm just 1 minute away now!", false)
            }
            if (timeRemainingSeconds == 0) {
                 chatMessages = chatMessages + ChatMessage("I've arrived! Please unlock the vehicle.", false)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp)
            .imePadding(), // Adjust for keyboard
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- TOP: AV Status ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isArrived) "AV Arrived!" else "AV on the way",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            val minutes = timeRemainingSeconds / 60
            val seconds = timeRemainingSeconds % 60
            val timeString = String.format("%d:%02d", minutes, seconds)
            
            Text(
                text = if (isArrived) "Ready to board" else "Arriving in $timeString",
                color = if (isArrived) Color(0xFF30D158) else Color(0xFF0A84FF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // AV Details Card
            Surface(
                color = Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isArrived) Color(0xFF30D158).copy(alpha = 0.2f) else Color(0xFF0A84FF).copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🚗", fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = avId,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Standard AV • 4 Seats",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "$12.50",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(8.dp))

        // --- MIDDLE: Chat Interface ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true, // Show newest at bottom (requires reversing list)
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(chatMessages.reversed()) { message ->
                ChatBubble(message)
            }
        }

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask something...", color = Color.White.copy(alpha = 0.5f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF0A84FF)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank()) {
                        val userMsg = ChatMessage(inputText, true)
                        chatMessages = chatMessages + userMsg
                        val query = inputText // capture for coroutine
                        inputText = ""
                        
                        // Mock AI Response
                        scope.launch {
                            delay(1000)
                            val response = getMockAIResponse(query)
                            chatMessages = chatMessages + ChatMessage(response, false)
                        }
                    }
                })
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val userMsg = ChatMessage(inputText, true)
                        chatMessages = chatMessages + userMsg
                        val query = inputText // capture
                        inputText = ""
                        
                        scope.launch {
                            delay(1000)
                            val response = getMockAIResponse(query)
                            chatMessages = chatMessages + ChatMessage(response, false)
                        }
                    }
                },
                modifier = Modifier
                    .background(Color(0xFF0A84FF), CircleShape)
                    .size(50.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }

        // --- BOTTOM: Proceed Button ---
        Button(
            onClick = onUnlock,
            enabled = isArrived, // Only enabled when arrived
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF30D158), // Green for go
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                disabledContentColor = Color.White.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isArrived) {
                Text(
                    text = "Unlock Vehicle",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Arriving in ${timeRemainingSeconds}s...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
