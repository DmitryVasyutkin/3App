package io.mobidoo.data.entities.tmppackage

data class TmpStartResponse(
    val categories_all: List<CategoriesAll>,
    val categories_new: List<CategoriesNew>,
    val categories_popular_4k: List<CategoriesPopular4k>,
    val category_live: CategoryLive,
    val wallpapers_flash_call: WallpapersFlashCall
)