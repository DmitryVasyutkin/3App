package io.mobidoo.a3app.entity.uistate.ringtonestate

import io.mobidoo.domain.entities.ringtone.Ringtone

data class RingtonesUIState(
    var isLoading: Boolean = true,
    var ringtones: List<Ringtone> = emptyList(),
    var badConnection: Boolean = false,
) {
}