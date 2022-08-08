package io.mobidoo.a3app.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivityMainBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.viewmodels.MainActivityViewModel
import io.mobidoo.a3app.viewmodels.MainActivityViewModelFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

 //   private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var navHostFragment: NavHostFragment? = null

    private lateinit var bottomNavView: BottomNavigationView
    private var activityWasPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     //   binding = ActivityMainBinding.inflate(layoutInflater)
        Log.i("MainActivityTimes", "before setcontent")
        setContentView(R.layout.activity_main)
        Log.i("MainActivityTimes", "after setcontent")
        bottomNavView = findViewById(R.id.bottomNavView)
        bottomNavView.itemIconTintList = null
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        Log.i("MainActivityTimes", "after findFragmentById")
        navController = navHostFragment?.navController!!
        Log.i("MainActivityTimes", "after navHostFragment?.navController!!")
        NavigationUI.setupWithNavController(bottomNavView, navController)
        Log.i("MainActivityTimes", "after setupWithNavController")
    }

    fun navigateToFlashCalls(){
        bottomNavView.selectedItemId = R.id.flashcalls
    }

    fun navigateToLiveWalls(){
        bottomNavView.selectedItemId = R.id.live
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

                if(activityWasPaused) p0.show(this@MainActivity)
            }
        })
    }

    override fun onPause() {
        Log.d("MainActivity", "onPause")
        activityWasPaused = true
        super.onPause()
    }

    override fun onStop() {
        Log.d("MainActivity", "onStop")
        super.onStop()
    }

    override fun onStart() {
        Log.d("MainActivity", "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d("MainActivity", "onResume")
        super.onResume()
    }
    override fun onRestart() {
        Log.d("MainActivity", "onRestart")
        if(activityWasPaused) loadInterAd()
        super.onRestart()
    }
}