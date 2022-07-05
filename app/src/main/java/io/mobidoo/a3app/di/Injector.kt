package io.mobidoo.a3app.di

import io.mobidoo.a3app.di.wallpaperscope.WallpapersSubComponent

interface Injector {

    fun createWallpaperSubComponent() : WallpapersSubComponent
}