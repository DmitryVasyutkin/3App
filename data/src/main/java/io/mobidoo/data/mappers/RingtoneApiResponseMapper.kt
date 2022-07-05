package io.mobidoo.data.mappers

import io.mobidoo.data.entities.ringtone.RingtoneCategoryListResponse
import io.mobidoo.data.entities.ringtone.RingtoneCategoryResponse
import io.mobidoo.data.entities.ringtone.RingtoneItem
import io.mobidoo.data.entities.ringtone.RingtonesResponse
import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.ringtone.RingtoneCategory

class RingtoneApiResponseMapper {

    fun toRingtoneList(response: RingtonesResponse) : List<Ringtone>{
        return response.items.map { it.toRingtone() }
    }

    private fun RingtoneItem.toRingtone(): Ringtone{
        return Ringtone(url, imageUrl, title)
    }

    fun toRingtoneCategoryList(response: RingtoneCategoryListResponse) : List<RingtoneCategory>{
        return response.items.map { toRingtoneCategory(it) }
    }

    fun toRingtoneCategory(response: RingtoneCategoryResponse) : RingtoneCategory{
        return RingtoneCategory(response.id, response.name, response.ringtones.map { it.toRingtone() })
    }
}