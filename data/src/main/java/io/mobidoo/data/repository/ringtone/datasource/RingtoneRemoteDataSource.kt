package io.mobidoo.data.repository.ringtone.datasource

import io.mobidoo.data.entities.ringtone.RingtoneCategoryListResponse
import io.mobidoo.data.entities.ringtone.RingtoneCategoryResponse
import io.mobidoo.data.entities.ringtone.RingtonesResponse
import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.ringtone.RingtoneCategory
import retrofit2.Response

interface RingtoneRemoteDataSource {

    suspend fun getRingtoneCategories() : Response<RingtoneCategoryListResponse>
    suspend fun getRingtones(id: Long) : Response<RingtonesResponse>
}