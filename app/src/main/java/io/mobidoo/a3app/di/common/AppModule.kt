package io.mobidoo.a3app.di.common

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val context: Context) {

    @Singleton
    @Provides
    fun provideApplication(): Context = context.applicationContext

}