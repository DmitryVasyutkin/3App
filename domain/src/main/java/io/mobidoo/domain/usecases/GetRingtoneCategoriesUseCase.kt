package io.mobidoo.domain.usecases

import io.mobidoo.domain.repositories.RingtoneRepository
import io.mobidoo.domain.repositories.WallpaperRepository
import kotlinx.coroutines.flow.callbackFlow

class GetRingtoneCategoriesUseCase(
    private val ringtoneRepository: RingtoneRepository
) {
    suspend operator fun invoke() = ringtoneRepository.getRingtoneCategories()
}