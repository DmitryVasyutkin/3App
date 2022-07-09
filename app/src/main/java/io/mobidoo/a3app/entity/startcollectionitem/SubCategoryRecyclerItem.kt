package io.mobidoo.a3app.entity.startcollectionitem

import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class SubCategoryRecyclerItem(
    val name: String,
    val array: List<Wallpaper>,
    val wallLink: String,
    val isAdvertising: Boolean
)
