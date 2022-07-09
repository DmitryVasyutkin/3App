package io.mobidoo.domain.entities.wallpaper

import io.mobidoo.domain.entities.wallpaper.Wallpaper
import java.io.Serializable

data class SubCategory(
    val name: String,
    val array: List<Wallpaper>,
    val type: String,
    val wallLink: String
) : Serializable
