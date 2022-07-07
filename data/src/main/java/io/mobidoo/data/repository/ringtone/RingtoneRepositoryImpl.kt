package io.mobidoo.data.repository.ringtone

import io.mobidoo.data.mappers.RingtoneApiResponseMapper
import io.mobidoo.data.repository.ringtone.datasource.RingtoneRemoteDataSource
import io.mobidoo.data.utils.TransformUtils.toResultData
import io.mobidoo.data.utils.TransformUtils.transformResult
import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.ringtone.RingtoneCategory
import io.mobidoo.domain.repositories.RingtoneRepository

class RingtoneRepositoryImpl(
    private val mapper: RingtoneApiResponseMapper,
    private val ringtoneRemoteDataSource: RingtoneRemoteDataSource
) : RingtoneRepository {

    override suspend fun getRingtoneCategories(): ResultData<List<RingtoneCategory>> {
        return transformResult(ringtoneRemoteDataSource.getRingtoneCategories().toResultData()){
            mapper.toRingtoneCategoryList(it)
        }
    }

    override suspend fun getRingtones(link: String): ResultData<List<Ringtone>> {
        return transformResult(ringtoneRemoteDataSource.getRingtones(link).toResultData()){
            mapper.toRingtoneList(it)
        }
    }
}