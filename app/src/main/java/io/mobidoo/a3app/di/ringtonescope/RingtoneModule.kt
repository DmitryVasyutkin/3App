package io.mobidoo.a3app.di.ringtonescope

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import io.mobidoo.a3app.viewmodels.MainActivityViewModelFactory
import io.mobidoo.a3app.viewmodels.RingtonesViewModelFactory
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModelFactory
import io.mobidoo.domain.usecases.*
import javax.inject.Singleton


@Module
class RingtoneModule {

    @Provides
    fun provideRingtoneViewModelFactory(context: Context, getRingtoneCategoriesUseCase: GetRingtoneCategoriesUseCase, getRingtonesUseCase: GetRingtonesUseCase) : RingtonesViewModelFactory {
        return RingtonesViewModelFactory(context.applicationContext as Application, getRingtoneCategoriesUseCase, getRingtonesUseCase)
    }

}