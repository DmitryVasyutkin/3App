package io.mobidoo.data.mappers

import io.mobidoo.data.entities.ringtone.RingtoneCategoryListResponse
import io.mobidoo.data.entities.ringtone.RingtoneCategoryResponse
import io.mobidoo.data.entities.ringtone.RingtoneItem
import io.mobidoo.data.entities.ringtone.RingtonesResponse
import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.ringtone.RingtoneCategory

class RingtoneApiResponseMapper {

    fun toRingtoneList(response: RingtonesResponse) : List<Ringtone>{
        return response.ringtones.map { Ringtone(it.url, it.imageUrl, it.title, response.nameCategory, response.link) }
    }

    private fun RingtoneItem.toRingtone(): Ringtone{
        return Ringtone(url, imageUrl, title)
    }

    fun toRingtoneCategoryList(response: List<RingtoneCategoryResponse>) : List<RingtoneCategory>{
        return response.map { toRingtoneCategory(it) }
    }

    fun toRingtoneCategory(response: RingtoneCategoryResponse) : RingtoneCategory{
        return RingtoneCategory(id = response.id, link = response.link, name = response.name, ringtones = response.ringtones.map{it.toRingtone()} )
    }
}