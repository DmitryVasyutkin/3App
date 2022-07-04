package io.mobidoo.domain.usecases

import io.mobidoo.domain.repositories.RingtoneRepository
import io.mobidoo.domain.repositories.WallpaperRepository
import kotlinx.coroutines.flow.callbackFlow

class GetRingtonesUseCase(
    private val ringtoneRepository: RingtoneRepository
) {
    suspend operator fun invoke(id: Long) = ringtoneRepository.getRingtones(id)
}