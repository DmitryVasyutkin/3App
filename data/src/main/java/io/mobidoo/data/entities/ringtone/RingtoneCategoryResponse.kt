package io.mobidoo.data.entities.ringtone

import com.google.gson.annotations.SerializedName

data class RingtoneCategoryResponse(
    val id: Long,
    val name: String,
    @SerializedName("ringtones")
    val ringtones: List<RingtoneItem>
)
