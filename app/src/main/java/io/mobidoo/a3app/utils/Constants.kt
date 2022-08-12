package io.mobidoo.a3app.utils

import android.os.Looper
import io.mobidoo.a3app.R

object Constants {
    const val SP_NEED_SECOND_GREETING ="need_second_greeting"
    const val APP_PREFS = "APP_PREFS"
    const val SP_FLASH_CAL_URI = "flash_uri"

    private const val NATIVE_AD_KEY_0 = "ca-app-pub-3940256099942544/2247696110"
    private const val NATIVE_AD_KEY_1 = "ca-app-pub-3940256099942544/2247696110"
    private const val NATIVE_AD_KEY_2 = "ca-app-pub-3940256099942544/2247696110"

    private const val INTER_AD_KEY_0 = "ca-app-pub-3940256099942544/1033173712"
    private const val INTER_AD_KEY_1 = "ca-app-pub-3940256099942544/1033173712"
    private const val INTER_AD_KEY_2 = "ca-app-pub-3940256099942544/1033173712"

    val nativeAdKeyList = arrayListOf(NATIVE_AD_KEY_0, NATIVE_AD_KEY_1, NATIVE_AD_KEY_2)
    val interAdKeyList = arrayListOf(INTER_AD_KEY_0, INTER_AD_KEY_1, INTER_AD_KEY_2)

    const val shareLink = "market://details?id=..."
}
