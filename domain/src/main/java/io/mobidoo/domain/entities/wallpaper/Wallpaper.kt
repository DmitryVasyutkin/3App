package io.mobidoo.domain.entities.wallpaper

import io.mobidoo.domain.common.Constants

data class Wallpaper(
    val url: String,
    val previewUrl: String,
    var type: String = Constants.TYPE_WALLPAPER
)

