package io.mobidoo.a3app.entity.uistate.allcollectionstate

import io.mobidoo.a3app.entity.startcollectionitem.CollectionItem
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class WallCategoriesUIState(
    var isLoading: Boolean = true,
    val list: List<SubCategory> = emptyList(),
    val badConnection: Boolean = false
)

