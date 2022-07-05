package io.mobidoo.a3app

import android.app.Application
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.di.common.AppComponent
import io.mobidoo.a3app.di.common.AppModule
import io.mobidoo.a3app.di.common.DaggerAppComponent
import io.mobidoo.a3app.di.wallpaperscope.WallpapersSubComponent
import javax.inject.Inject

class App : Application(), Injector {

    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
       appComponent = DaggerAppComponent.builder()
           .appModule(AppModule(this))
           .build()
    }

    override fun createWallpaperSubComponent(): WallpapersSubComponent {
        return appComponent.wallpaperSubComponent().create()
    }
}