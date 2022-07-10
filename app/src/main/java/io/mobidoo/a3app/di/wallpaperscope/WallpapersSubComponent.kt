package io.mobidoo.a3app.di.wallpaperscope

import dagger.Subcomponent
import io.mobidoo.a3app.entity.startcollectionitem.SelectedCategoryWallpapersFragment
import io.mobidoo.a3app.ui.flashcall.FlashCallCollectionFragment
import io.mobidoo.a3app.ui.startfragment.StartCollectionFragment
import io.mobidoo.a3app.ui.startfragment.WallCategoriesFragment

@WallpaperScope
@Subcomponent(modules = [WallpaperModule::class])
interface WallpapersSubComponent {
    fun inject(startCollectionFragment: StartCollectionFragment)
    fun inject(wallCategoriesFragment: WallCategoriesFragment)
    fun inject(wallpapersFragment: SelectedCategoryWallpapersFragment)
    fun inject(flashCallFragment: FlashCallCollectionFragment)

    @Subcomponent.Factory
    interface Factory{
        fun create() : WallpapersSubComponent
    }
}