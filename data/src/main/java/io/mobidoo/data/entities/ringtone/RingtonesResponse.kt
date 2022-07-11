package io.mobidoo.data.entities.ringtone

data class RingtonesResponse(
    val id: Long,
    val link: String,
    val nameCategory: String,
    val ringtones: List<RingtoneItem>
)