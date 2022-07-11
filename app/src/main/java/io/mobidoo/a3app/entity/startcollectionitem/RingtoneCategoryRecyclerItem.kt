package io.mobidoo.a3app.entity.startcollectionitem

import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class RingtoneCategoryRecyclerItem(
    val name: String,
    val array: List<Ringtone>,
    val ringLink: String,
    val isAdvertising: Boolean
)
