package io.mobidoo.domain.entities.wallpaper

import io.mobidoo.domain.common.Constants
import java.io.Serializable

data class Wallpaper(
    val url: String,
    val previewUrl: String,
    var type: String = Constants.TYPE_WALLPAPER
) : Serializable

