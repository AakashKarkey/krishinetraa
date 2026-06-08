package com.ace.krishinetra_mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ace.krishinetra_mobile.data.local.AppDatabase
import com.ace.krishinetra_mobile.data.model.ChatMessage
import com.ace.krishinetra_mobile.data.repository.ChatRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(
        AppDatabase.getDatabase(application).chatMessageDao()
    )

    val messages: LiveData<List<ChatMessage>> = MutableLiveData()

    private val _isTyping = MutableLiveData(false)
    val isTyping: LiveData<Boolean> = _isTyping

    init {
        viewModelScope.launch {
            repository.getAllMessages().collectLatest { msgList ->
                (messages as MutableLiveData).postValue(msgList)
            }
        }
        sendWelcomeMessage()
    }

    private fun sendWelcomeMessage() {
        viewModelScope.launch {
            val currentMessages = messages.value
            if (currentMessages.isNullOrEmpty()) {
                repository.sendMessage(
                    "Hello! I'm your Plant AI Assistant. I can help you with plant care tips, disease prevention, and gardening advice. How can I help you today?",
                    isUser = false
                )
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            repository.sendMessage(text, isUser = true)
            _isTyping.postValue(true)

            kotlinx.coroutines.delay(1000 + (text.length * 20).toLong())

            val response = generateResponse(text)
            repository.sendMessage(response, isUser = false)
            _isTyping.postValue(false)
        }
    }

    fun sendQuickQuestion(question: String) {
        sendMessage(question)
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.deleteAllMessages()
            sendWelcomeMessage()
        }
    }

    private fun generateResponse(query: String): String {
        val lowerQuery = query.lowercase()

        return when {
            lowerQuery.contains("water") && (lowerQuery.contains("often") || lowerQuery.contains("frequent")) ->
                "Most houseplants thrive when watered once every 7-10 days. However, this varies by plant type—succulents need less water (every 2-3 weeks), while tropical plants may need more. Always check the soil moisture first by sticking your finger about an inch deep. If it's dry, it's time to water."

            lowerQuery.contains("overwater") || (lowerQuery.contains("yellow") && lowerQuery.contains("leaf")) ->
                "Yellowing leaves are often a sign of overwatering. Other signs include wilting despite wet soil, brown leaf tips, and mold on soil surface. Let the soil dry out completely before watering again, and ensure your pot has proper drainage holes."

            lowerQuery.contains("disease") || lowerQuery.contains("prevent") ->
                "Prevent plant diseases by: 1) Watering at the base rather than overhead, 2) Ensuring good air circulation, 3) Using clean pots and fresh soil, 4) Removing dead leaves promptly, 5) Quarantining new plants for 2 weeks, 6) Applying neem oil spray as a preventive measure."

            lowerQuery.contains("fertilizer") || lowerQuery.contains("fertilize") ->
                "For most houseplants, use a balanced liquid fertilizer (10-10-10) diluted to half strength every 2-4 weeks during the growing season (spring and summer). Reduce to once every 2 months in fall and winter. Organic options include compost tea, fish emulsion, and seaweed extract."

            lowerQuery.contains("humidity") ->
                "Many houseplants prefer 50-60% humidity. Increase humidity by: 1) Grouping plants together, 2) Using a pebble tray with water, 3) Misting leaves regularly, 4) Using a room humidifier, 5) Placing plants in naturally humid rooms like bathrooms."

            lowerQuery.contains("light") || lowerQuery.contains("sun") ->
                "Most houseplants need bright, indirect light. South or east-facing windows are ideal. Signs of too much light: scorched or faded leaves. Signs of too little light: leggy growth, small leaves, or leaf drop. Consider using grow lights during winter months."

            lowerQuery.contains("pest") || lowerQuery.contains("bug") || lowerQuery.contains("insect") ->
                "Common houseplant pests include spider mites, aphids, mealybugs, and fungus gnats. Treat with: 1) Insecticidal soap spray, 2) Neem oil solution, 3) Rubbing alcohol on cotton swabs for visible pests, 4) Yellow sticky traps for gnats. Isolate affected plants immediately."

            lowerQuery.contains("repot") || lowerQuery.contains("pot") ->
                "Repot your plants when roots grow through drainage holes or become root-bound. Choose a pot 2 inches larger in diameter. Best time to repot is spring. Use fresh potting mix appropriate for your plant type, and water thoroughly after repotting."

            else -> "That's a great question! For specific plant care advice, I recommend checking our disease analysis feature—upload a photo of your plant and I'll provide detailed diagnosis and treatment recommendations. You can also check our extensive disease database for more information. Is there anything specific about your plant's symptoms you'd like to describe?"
        }
    }
}
