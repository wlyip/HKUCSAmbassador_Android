package com.example.hkucsambassador.api

data class TextMessage(
        val Agent: String,
        val Name: String,
        val ResolvedQuery: String,
        val Responses: List<TextResponseItem>,
        val Score: Float,
        val Speech: String,
        val Success: Boolean,
        val messages: List<TextMessageItem>
)