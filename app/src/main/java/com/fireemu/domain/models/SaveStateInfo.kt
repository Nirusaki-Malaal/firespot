package com.fireemu.domain.models

data class SaveStateInfo(
    val slot: Int,
    val filePath: String,
    val exists: Boolean,
    val timestamp: Long = 0L,
    val thumbnailPath: String? = null
)
