package io.mobidoo.domain.usecases

import io.mobidoo.domain.repositories.WallpaperRepository
import kotlinx.coroutines.flow.callbackFlow

class GetSubCategoriesUseCase(
    private val wallpaperRepository: WallpaperRepository
) {
    suspend operator fun invoke(id: Long) = wallpaperRepository.getSubcategories(id)
}