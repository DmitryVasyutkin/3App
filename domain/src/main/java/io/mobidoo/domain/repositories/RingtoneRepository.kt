package io.mobidoo.domain.repositories

import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.ringtone.RingtoneCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper

interface RingtoneRepository {
    suspend fun getRingtoneCategories() : ResultData<List<RingtoneCategory>>
    suspend fun getRingtones(id: Long) : ResultData<List<Ringtone>>
}