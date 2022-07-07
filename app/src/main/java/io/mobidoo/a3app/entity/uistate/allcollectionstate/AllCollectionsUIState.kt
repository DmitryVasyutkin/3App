package io.mobidoo.a3app.entity.uistate.allcollectionstate

import io.mobidoo.a3app.entity.startcollectionitem.CollectionItem
import io.mobidoo.domain.common.Constants
import io.mobidoo.domain.entities.StartCollection


data class AllCollectionsUIState(
    var isLoading: Boolean = true,
    val flashCall: CollectionItem? = null,
    val live: CollectionItem? = null,
    val new: CollectionItem? = null,
    val all: CollectionItem? = null,
    val popular: CollectionItem? = null,
    val abstract: CollectionItem? = null,
    val pattern: CollectionItem? = null,
    val badConnection: Boolean = false
) {
}

fun StartCollection.toUIState() : AllCollectionsUIState{
    return AllCollectionsUIState(
        flashCall = CollectionItem(flashCalls.wallLink, flashCalls.array),
        live = CollectionItem(liveWallpapers.subCategoryLink, liveWallpapers.array),
        new = CollectionItem(new.subCategoryLink, new.array),
        all = CollectionItem(all.subCategoryLink, all.array),
        popular = CollectionItem(popular.subCategoryLink, popular.array),
        abstract = CollectionItem(abstract.wallLink, abstract.array),
        pattern = CollectionItem(pattern.wallLink, pattern.array)
    )
}