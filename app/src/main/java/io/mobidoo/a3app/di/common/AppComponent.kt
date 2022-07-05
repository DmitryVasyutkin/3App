package io.mobidoo.a3app.di.common

import dagger.Component
import io.mobidoo.a3app.di.wallpaperscope.WallpapersSubComponent
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, NetworkModule::class, RepositoryModule::class, UseCasesModule::class])
interface AppComponent {

    fun wallpaperSubComponent() : WallpapersSubComponent.Factory
}