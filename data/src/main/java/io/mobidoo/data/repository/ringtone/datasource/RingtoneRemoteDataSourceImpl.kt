package io.mobidoo.data.repository.ringtone.datasource

import io.mobidoo.data.api.ApiService
import io.mobidoo.data.entities.ringtone.RingtoneCategoryListResponse
import io.mobidoo.data.entities.ringtone.RingtoneCategoryResponse
import io.mobidoo.data.entities.ringtone.RingtonesResponse
import retrofit2.Response

class RingtoneRemoteDataSourceImpl(
    private val apiService: ApiService
) : RingtoneRemoteDataSource{

    override suspend fun getRingtoneCategories(): Response<List<RingtoneCategoryResponse>> {
        return apiService.getRingtoneCategories()
    }

    override suspend fun getRingtones(link: String): Response<RingtonesResponse> {
        return apiService.getRingtones(link)
    }
}