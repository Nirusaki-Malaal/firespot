package com.fireemu

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.fireemu.data.local.datastore.SettingsStore
import com.fireemu.data.local.room.AppDatabase
import com.fireemu.data.repository.CoreRepository
import com.fireemu.data.repository.CoreRepositoryImpl
import com.fireemu.data.repository.GameRepository
import com.fireemu.data.repository.GameRepositoryImpl
import com.fireemu.data.repository.SaveStateRepository
import com.fireemu.data.repository.SaveStateRepositoryImpl
import com.fireemu.data.repository.SettingsRepository
import com.fireemu.data.repository.SettingsRepositoryImpl
import com.fireemu.data.scanner.RomScanner
import com.fireemu.domain.usecases.GetGamesUseCase
import com.fireemu.domain.usecases.SaveStateUseCase
import com.fireemu.domain.usecases.ScanRomsUseCase
import com.fireemu.domain.usecases.SettingsUseCase
import com.fireemu.domain.usecases.ToggleFavoriteUseCase
import com.fireemu.domain.usecases.UpdatePlayStatsUseCase

class FireEmuApp : Application(), ImageLoaderFactory {

    companion object {
        lateinit var instance: FireEmuApp
            private set
    }

    // Database & Stores
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val settingsStore: SettingsStore by lazy { SettingsStore(this) }
    val romScanner: RomScanner by lazy { RomScanner() }

    // Repositories
    val gameRepository: GameRepository by lazy { GameRepositoryImpl(database.gameDao()) }
    val coreRepository: CoreRepository by lazy { CoreRepositoryImpl(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(this, settingsStore) }
    val saveStateRepository: SaveStateRepository by lazy { SaveStateRepositoryImpl(this) }

    // Use Cases
    val scanRomsUseCase: ScanRomsUseCase by lazy {
        ScanRomsUseCase(gameRepository, settingsRepository, romScanner)
    }
    val getGamesUseCase: GetGamesUseCase by lazy {
        GetGamesUseCase(gameRepository)
    }
    val toggleFavoriteUseCase: ToggleFavoriteUseCase by lazy {
        ToggleFavoriteUseCase(gameRepository)
    }
    val updatePlayStatsUseCase: UpdatePlayStatsUseCase by lazy {
        UpdatePlayStatsUseCase(gameRepository)
    }
    val saveStateUseCase: SaveStateUseCase by lazy {
        SaveStateUseCase(saveStateRepository)
    }
    val settingsUseCase: SettingsUseCase by lazy {
        SettingsUseCase(settingsRepository)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    /**
     * Configures Coil for low-memory Fire OS hardware:
     * Memory cache is limited to max 15% of available heap.
     * Disk cache is capped at 50 MB.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}
