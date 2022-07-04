package io.mobidoo.data.repository.ringtone

import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.ringtone.RingtoneCategory
import retrofit2.Response

interface RingtoneRemoteDataSource {

    suspend fun getRingtoneCategories() : Response<List<RingtoneCategory>>
    suspend fun getRingtones(id: Long) : Response<List<Ringtone>>
}