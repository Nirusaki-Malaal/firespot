package com.fireemu.domain.usecases

import com.fireemu.data.repository.GameRepository

class UpdatePlayStatsUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(gameId: Long, sessionDurationSeconds: Long) {
        gameRepository.updatePlayStats(gameId, sessionDurationSeconds)
    }
}
