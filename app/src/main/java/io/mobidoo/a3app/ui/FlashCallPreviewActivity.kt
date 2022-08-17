package io.mobidoo.a3app.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivityFlashCallPreviewBinding
import io.mobidoo.a3app.databinding.ActivityWallpaperBinding
import io.mobidoo.a3app.utils.Constants.interAdKeyList

class FlashCallPreviewActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_WALLPAPER_LINK = "wallpaper_link"
        const val EXTRA_CATEGORY_NAME = "category_name"
        fun getIntent(context: Context, link: String, categoryName: String) = Intent(context, FlashCallPreviewActivity::class.java).apply {
            putExtra(EXTRA_WALLPAPER_LINK, link)
            putExtra(EXTRA_CATEGORY_NAME, categoryName)
        }
    }
    private lateinit var binding: ActivityFlashCallPreviewBinding
    private lateinit var navController: NavController
    private var activityWasPaused = false

    private var loadInterAdAttempt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashCallPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.flashcall_nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun loadInterAd(key: String){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, key, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.i("SplashScreen", "filed to load interstitial attempt $loadInterAdAttempt")
                loadInterAdAttempt++
                if (loadInterAdAttempt <= interAdKeyList.size - 1){
                    try {
                        loadInterAd(interAdKeyList[loadInterAdAttempt])
                    }catch (e: Exception){

                    }
                }

            }

            override fun onAdLoaded(p0: InterstitialAd) {
                Log.i("SplashScreen", "interstitial loaded $p0")
                super.onAdLoaded(p0)
                p0.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d("SplashScreen", "Ad was clicked.")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        Log.d("SplashScreen", "Ad dismissed fullscreen content.")
                        activityWasPaused = false
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        Log.d("SplashScreen", "Ad failed to show fullscreen content.")

                    }

                    override fun onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d("SplashScreen", "Ad recorded an impression.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("SplashScreen", "Ad showed fullscreen content.")
                    }

                }

                if(activityWasPaused) p0.show(this@FlashCallPreviewActivity)
            }
        })
    }

    override fun onPause() {
        Log.d("MainActivity", "onPause")
        activityWasPaused = true
        loadInterAdAttempt = 0
        super.onPause()
    }
    override fun onRestart() {
        Log.d("MainActivity", "onRestart")
        loadInterAdAttempt = 0
        if(activityWasPaused) loadInterAd(interAdKeyList[loadInterAdAttempt])
        super.onRestart()
    }
}