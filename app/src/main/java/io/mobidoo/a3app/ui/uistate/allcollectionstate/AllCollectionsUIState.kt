package io.mobidoo.a3app.ui.uistate.allcollectionstate

import io.mobidoo.domain.entities.FlashCall
import io.mobidoo.domain.entities.wallpaper.Category
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class AllCollectionsUIState(
    val isLoading: Boolean = true,
    val flashCallArray: List< FlashCall> = emptyList(),
    val newPapersArray: List<SubCategory> = emptyList(),
    val livePapersArray: List<Wallpaper> = emptyList(),
    val categories: List<Category> = emptyList(),
    val all: List<SubCategory> = emptyList(),
    val badConnection: Boolean = false
) {
}