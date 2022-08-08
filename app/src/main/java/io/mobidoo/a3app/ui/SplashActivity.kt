package io.mobidoo.a3app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.progressindicator.CircularProgressIndicator
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivitySplashBinding
import io.mobidoo.a3app.databinding.LayoutAdSplashBinding
import io.mobidoo.a3app.databinding.LayoutGreetingBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.utils.AppUtils.expand
import io.mobidoo.a3app.utils.Constants
import java.util.*
import javax.inject.Inject

const val testInterAd = "ca-app-pub-3940256099942544/1033173712"
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var handler: Handler? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private lateinit var pbCloseGreeting: CircularProgressIndicator

    private lateinit var ibCloseGreeting: ImageButton
    private var currentNativeAd: NativeAd? = null
    private lateinit var adViewLayout: FrameLayout
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialLoaded = false
    private var closeButtonPressed = false
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as Injector).createWallpaperSubComponent().inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler = Handler(Looper.getMainLooper())
        adViewLayout = findViewById(R.id.ad_view_splash)
        pbCloseGreeting = findViewById(R.id.pb_close_greeting_splash)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetParent)
        bottomSheetBehavior.isDraggable = false
        if (sharedPreferences.getBoolean(Constants.SP_NEED_SECOND_GREETING, false)){
            findViewById<ConstraintLayout>(R.id.layout_greeting2).visibility = View.VISIBLE
        }
        handler?.postDelayed({
            showSecondSplash()
        }, 1500)
        ibCloseGreeting = findViewById(R.id.ibCloseGreeting)
        ibCloseGreeting.setOnClickListener {

            with(sharedPreferences.edit()){
                putBoolean(Constants.SP_NEED_SECOND_GREETING, true)
                commit()
            }
          //  openMainActivity()
            Log.d("SplashScreen", "ibClose interstitial $mInterstitialAd")

            if (interstitialLoaded && mInterstitialAd != null){
                mInterstitialAd?.show(this)

            }else if(interstitialLoaded && mInterstitialAd == null){
                openMainActivity()
            }else{
                closeButtonPressed = true
                ibCloseGreeting.visibility = View.GONE
                pbCloseGreeting.visibility = View.VISIBLE
            }
        }

    }

    @SuppressLint("HardwareIds")
    private fun openSecondScreen() {
        val request = RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("34A6AF4C95E8EC517667A12EF589AB8B")).build()
        MobileAds.setRequestConfiguration(request)
        MobileAds.initialize(this, object: OnInitializationCompleteListener{
            override fun onInitializationComplete(p0: InitializationStatus) {
             //   bottomSheetBehavior.expand()
                loadAd()
                loadInterAd()
            }
        })
    }

    private fun openMainActivity() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        finish()
    }

    private fun showSecondSplash(){
        binding.tvSplashMessageHeader.text = resources.getString(R.string.splashMessageHeader2)
        binding.tvSplashMessage.text = resources.getString(R.string.splashMessage3)
        handler?.postDelayed({ openSecondScreen() }, 500)
    }

    private fun loadInterAd(){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, testInterAd, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.i("SplashScreen", "filed to load interstitial")
                mInterstitialAd = null
                interstitialLoaded = true
                if (closeButtonPressed){
                    openMainActivity()
                }
               // openMainActivity()
            }

            override fun onAdLoaded(p0: InterstitialAd) {
                Log.i("SplashScreen", "interstitial loaded $p0")
                super.onAdLoaded(p0)
                mInterstitialAd = p0
                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d("SplashScreen", "Ad was clicked.")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        Log.d("SplashScreen", "Ad dismissed fullscreen content.")
                        mInterstitialAd = null
                        openMainActivity()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        Log.d("SplashScreen", "Ad failed to show fullscreen content.")
                        mInterstitialAd = null
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
                interstitialLoaded = true
                if (closeButtonPressed){
                    mInterstitialAd?.show(this@SplashActivity)
                }
            }
        })
    }

    private fun loadAd(){
        val builder = AdLoader.Builder(this, BuildConfig.AD_MOB_KEY)
            .forNativeAd { nativeAd ->


                if(isDestroyed){
                    nativeAd.destroy()
                    return@forNativeAd
                }
                currentNativeAd?.destroy()
                currentNativeAd = nativeAd
                val adBinding = LayoutAdSplashBinding.inflate(layoutInflater)
                populateNativeAdView(nativeAd, adBinding)
                adViewLayout.removeAllViews()
                adViewLayout.addView(adBinding.root)

            }
            .withAdListener(object : AdListener(){
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    bottomSheetBehavior.expand()
                    Log.i("SplashScreen", "onAdLoaded")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    bottomSheetBehavior.expand()
                    Log.i("SplashScreen", "nativeAd failed ${p0.message}")
                }
            })
            .build()
        val request = AdRequest.Builder()
            .build()
        Log.i("SplashScreen", "is Test device ${request.isTestDevice(this)}")
        builder.loadAd(request)
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adViewBinding: LayoutAdSplashBinding){
        val nativeAdView = adViewBinding.root as NativeAdView

        nativeAdView.bodyView = adViewBinding.adHeadlineSplash
        nativeAdView.imageView = adViewBinding.adAppIconSplash
        nativeAdView.callToActionView = adViewBinding.btnNativeAdActionSplash
//        Log.i("SplashScreen", "advertiser ${nativeAd.advertiser}")
//        Log.i("SplashScreen", "store ${nativeAd.store}")
//        Log.i("SplashScreen", "extras ${nativeAd.extras}")
//        Log.i("SplashScreen", "callToAction ${nativeAd.callToAction}")
        if (nativeAd.body != null){
            adViewBinding.adHeadlineSplash.text = nativeAd.body
        }else if (nativeAd.headline != null){
            adViewBinding.adHeadlineSplash.text = nativeAd.headline
        }

        if (nativeAd.mediaContent != null){
            adViewBinding.adAppIconSplash.load(nativeAd.mediaContent?.mainImage)
        }else{
            adViewBinding.adAppIconSplash.load(nativeAd.icon?.drawable)
        }


        adViewBinding.btnNativeAdActionSplash.text = nativeAd.callToAction

    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        mInterstitialAd = null
        super.onDestroy()
    }
}


























