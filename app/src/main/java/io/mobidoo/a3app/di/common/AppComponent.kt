package io.mobidoo.a3app.di.common

import dagger.Component
import io.mobidoo.a3app.di.ringtonescope.RingtoneSubComponent
import io.mobidoo.a3app.di.wallpaperscope.WallpapersSubComponent
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, NetworkModule::class, RepositoryModule::class, UseCasesModule::class, StorageModule::class])
interface AppComponent {

    fun wallpaperSubComponent() : WallpapersSubComponent.Factory
    fun ringtoneSubComponent() : RingtoneSubComponent.Factory
}