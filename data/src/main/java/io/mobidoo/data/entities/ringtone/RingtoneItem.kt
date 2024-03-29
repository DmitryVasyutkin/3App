package io.mobidoo.data.entities.ringtone

import com.google.gson.annotations.SerializedName

data class RingtoneItem(
    @SerializedName("url")
    val url: String,
    @SerializedName("image")
    val imageUrl: String,
    @SerializedName("title")
    val title: String
)