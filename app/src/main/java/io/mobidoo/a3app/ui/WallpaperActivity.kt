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
import io.mobidoo.a3app.databinding.ActivityWallpaperBinding

class WallpaperActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_WALLPAPER_LINK = "wallpaper_link"
        const val EXTRA_CATEGORY_NAME = "category_name"
        const val EXTRA_WALLPAPER_TYPE = "wallpaper_type"
        fun getIntent(context: Context, link: String, categoryName: String, type: Int) = Intent(context, WallpaperActivity::class.java).apply {
            putExtra(EXTRA_WALLPAPER_LINK, link)
            putExtra(EXTRA_CATEGORY_NAME, categoryName)
            putExtra(EXTRA_WALLPAPER_TYPE, type)
        }
    }
    private lateinit var binding: ActivityWallpaperBinding

    private lateinit var navController: NavController
    private var activityWasPaused = false
    var isLiveWall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //makeStatusBarTransparent()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.walls_nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

    }

    private fun loadInterAd(){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, testInterAd, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.i("SplashScreen", "filed to load interstitial")

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

                if(activityWasPaused) p0.show(this@WallpaperActivity)
            }
        })
    }

    override fun onPause() {
        Log.d("MainActivity", "onPause")
        activityWasPaused = true
        super.onPause()
    }

    override fun onRestart() {
        Log.d("MainActivity", "onRestart $activityWasPaused, $isLiveWall")
        if(activityWasPaused && !isLiveWall) loadInterAd()
        super.onRestart()
    }
}