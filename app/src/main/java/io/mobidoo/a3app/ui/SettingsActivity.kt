package io.mobidoo.a3app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivitySettingsBinding
import io.mobidoo.a3app.utils.Constants
import io.mobidoo.a3app.utils.Constants.interAdKeyList

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var navController: NavController
    private var activityWasPaused = false
    private var loadInterAdAttempt = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val  hostFragment = supportFragmentManager.findFragmentById(R.id.fragmentSettingsContainerView) as NavHostFragment
        navController = hostFragment.navController
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

                if(activityWasPaused) p0.show(this@SettingsActivity)
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
        if(activityWasPaused) loadInterAd(interAdKeyList[loadInterAdAttempt])
        super.onRestart()
    }
}