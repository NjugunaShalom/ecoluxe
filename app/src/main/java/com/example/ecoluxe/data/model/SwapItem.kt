package com.example.ecoluxe.data.model

data class SwapItem(
    val id: String = "",
    val imageUrl: String = "",
    val extraImages: List<String> = emptyList(),
    val name: String = "",
    val size: String = "",
    val color: String = "",
    val uploader: String = "",
    val uploaderName: String="",
    val status: String = "Available"
)
