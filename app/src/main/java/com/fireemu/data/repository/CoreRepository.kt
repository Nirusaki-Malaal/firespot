package com.fireemu.data.repository

import com.fireemu.domain.models.ConsoleType

interface CoreRepository {
    fun getAvailableConsoles(): List<ConsoleType>
    fun getCorePath(console: ConsoleType): String
    fun isCoreAvailable(console: ConsoleType): Boolean
    fun getBiosDirectory(): String
}
