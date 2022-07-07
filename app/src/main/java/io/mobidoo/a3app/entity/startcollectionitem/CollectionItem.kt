package io.mobidoo.a3app.entity.startcollectionitem

import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class CollectionItem(
    val linkForFull: String,
    val startArray: List<Wallpaper>
)
