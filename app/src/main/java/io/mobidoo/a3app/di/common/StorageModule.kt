package io.mobidoo.a3app.di.common

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import io.mobidoo.a3app.utils.Constants
import javax.inject.Singleton

@Module
class StorageModule {

    @Singleton
    @Provides
    fun provideSharedPrefers(context: Context):SharedPreferences{
        return context.getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE)
    }

}