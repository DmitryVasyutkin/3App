package io.mobidoo.a3app.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivityRingtoneCategoryItemsBinding
import io.mobidoo.a3app.databinding.ActivityWallpaperBinding
import io.mobidoo.a3app.utils.Constants
import io.mobidoo.domain.entities.ringtone.Ringtone

class RingtoneCategoryItemsActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_RINGTONE_LINK = "ringtone_link"
        const val EXTRA_RINGTONE_CATEGORY_NAME = "ringtone_category_name"
        const val EXTRA_RINGTONE = "extra_ringtone"
        const val EXTRA_RINGTONE_POS = "extra_ringtone_pos"

        fun getIntent(context: Context, link: String, categoryName: String, ringtone: Ringtone? = null, pos: Int? = null) = Intent(context, RingtoneCategoryItemsActivity::class.java).apply {
            putExtra(EXTRA_RINGTONE_LINK, link)
            putExtra(EXTRA_RINGTONE_CATEGORY_NAME, categoryName)
            putExtra(EXTRA_RINGTONE, ringtone)
            putExtra(EXTRA_RINGTONE_POS, pos)
        }
    }
    private lateinit var binding: ActivityRingtoneCategoryItemsBinding

    private lateinit var navController: NavController
    private var activityWasPaused = false
    private var loadInterAdAttempt = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRingtoneCategoryItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val  hostFragment = supportFragmentManager.findFragmentById(R.id.ringtone_nav_host_fragment_container) as NavHostFragment
        navController = hostFragment.findNavController()
    }


    private fun loadInterAd(key: String){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, key, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.i("SplashScreen", "filed to load interstitial attempt $loadInterAdAttempt")
                loadInterAdAttempt++
                if (loadInterAdAttempt <= Constants.interAdKeyList.size - 1){
                    loadInterAd(Constants.interAdKeyList[loadInterAdAttempt])
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

                if(activityWasPaused) p0.show(this@RingtoneCategoryItemsActivity)
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
        if(activityWasPaused) loadInterAd(Constants.interAdKeyList[loadInterAdAttempt])
        super.onRestart()
    }
}