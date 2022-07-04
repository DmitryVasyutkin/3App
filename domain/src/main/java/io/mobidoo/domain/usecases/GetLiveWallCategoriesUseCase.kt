package io.mobidoo.domain.usecases

import io.mobidoo.domain.repositories.WallpaperRepository
import kotlinx.coroutines.flow.callbackFlow

class GetLiveWallCategoriesUseCase(
    private val wallpaperRepository: WallpaperRepository
) {
    suspend operator fun invoke() = wallpaperRepository.getLiveCategories()
}