package io.mobidoo.domain.entities.ringtone

data class RingtoneCategory(
    val id: Long,
    val link: String,
    val name: String,
    val ringtones: List<Ringtone>
)
