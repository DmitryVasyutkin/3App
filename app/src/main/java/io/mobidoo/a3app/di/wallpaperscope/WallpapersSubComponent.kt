package io.mobidoo.a3app.di.wallpaperscope

import dagger.Subcomponent
import io.mobidoo.a3app.ui.startfragment.StartCollectionFragment

@WallpaperScope
@Subcomponent(modules = [WallpaperModule::class])
interface WallpapersSubComponent {
    fun inject(startCollectionFragment: StartCollectionFragment)

    @Subcomponent.Factory
    interface Factory{
        fun create() : WallpapersSubComponent
    }
}