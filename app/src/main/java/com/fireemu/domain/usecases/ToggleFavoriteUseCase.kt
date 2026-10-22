package com.fireemu.domain.usecases

import com.fireemu.data.repository.GameRepository

class ToggleFavoriteUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(gameId: Long, isFavorite: Boolean) {
        gameRepository.updateFavorite(gameId, isFavorite)
    }
}
