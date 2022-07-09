package io.mobidoo.a3app.entity.uistate.allcollectionstate

import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class WallpapersUIState(
    val isLoading: Boolean = false,
    val array: List<Wallpaper> = emptyList(),
    val badConnection: Boolean = false
) {
}