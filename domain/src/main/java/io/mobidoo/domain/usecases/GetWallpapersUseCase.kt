package io.mobidoo.domain.usecases

import io.mobidoo.domain.repositories.WallpaperRepository
import kotlinx.coroutines.flow.callbackFlow

class GetWallpapersUseCase(
    private val wallpaperRepository: WallpaperRepository
) {
    suspend operator fun invoke(link: String) = wallpaperRepository.getWallpapers(link)
}