package io.mobidoo.domain.entities.ringtone

data class Ringtone(
    val url: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val nameCategory: String ="",
    val linkToFullCategory: String = "",
    val isAdvertising: Boolean = false,
    var isPlayedNow: Boolean = false
)