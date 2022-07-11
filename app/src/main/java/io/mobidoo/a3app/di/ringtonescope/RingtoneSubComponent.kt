package io.mobidoo.a3app.di.ringtonescope

import dagger.Subcomponent
import io.mobidoo.a3app.entity.startcollectionitem.SelectedCategoryWallpapersFragment
import io.mobidoo.a3app.ui.flashcall.FlashCallCollectionFragment
import io.mobidoo.a3app.ui.ringtone.RingtoneCategoryItemsFragment
import io.mobidoo.a3app.ui.ringtone.RingtoneFragment
import io.mobidoo.a3app.ui.startfragment.StartCollectionFragment
import io.mobidoo.a3app.ui.startfragment.WallCategoriesFragment

@RingtoneScope
@Subcomponent(modules = [RingtoneModule::class])
interface RingtoneSubComponent {
    fun inject(ringtoneFragment: RingtoneFragment)
    fun inject(ringtoneCategoryItemsFragment: RingtoneCategoryItemsFragment)

    @Subcomponent.Factory
    interface Factory{
        fun create() : RingtoneSubComponent
    }
}