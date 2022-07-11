package io.mobidoo.a3app.entity.uistate.ringtonestate

import io.mobidoo.domain.entities.ringtone.RingtoneCategory

data class RingtoneCategoriesUIState(
    var isLoading: Boolean = true,
    var categories: List<RingtoneCategory> = emptyList(),
    var badConnection: Boolean = false
) {
}