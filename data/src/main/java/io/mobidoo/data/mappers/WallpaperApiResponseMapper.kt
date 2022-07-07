package io.mobidoo.data.mappers

import io.mobidoo.data.entities.StartCollectionResponse
import io.mobidoo.data.entities.wallpaper.*
import io.mobidoo.domain.entities.StartCollection
import io.mobidoo.domain.entities.wallpaper.Category
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper


class WallpaperApiResponseMapper {

    fun transformWallpaperItem(item: WallpaperItem) : Wallpaper{
        return Wallpaper(item.url, item.image)
    }
    fun transformWallpapersList(response: WallpapersResponse) : List<Wallpaper>{
        return response.items.map { transformWallpaperItem(it) }
    }

    fun transformSubCategoryResponse(response: SubCategoryResponse) : SubCategory{
        return SubCategory(response.name, response.array.map { transformWallpaperItem(it) }, response.type, response.wallpapersLink)
    }

    fun transformSubCategoriesList(response: SubCategoryListResponse) : List<SubCategory>{
        return response.items.map { transformSubCategoryResponse(it) }
    }

    fun transformCategoryResponse(response: CategoryResponse) : Category{
        return Category(response.subCategoriesLink, response.array.map { transformWallpaperItem(it) })
    }

    fun transformStartCollection(response: StartCollectionResponse) : StartCollection{
        return StartCollection(
            flashCalls = transformSubCategoryResponse(response.flashCalls),
            popular = transformCategoryResponse(response.popular),
            new =transformCategoryResponse(response.new),
            all = transformCategoryResponse(response.all),
            liveWallpapers = transformCategoryResponse(response.liveWallpapers),
            abstract = transformSubCategoryResponse(response.abstract),
            pattern = transformSubCategoryResponse(response.pattern)
        )
    }
}