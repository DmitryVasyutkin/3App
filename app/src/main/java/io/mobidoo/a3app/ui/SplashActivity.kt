package io.mobidoo.a3app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.ActivitySplashBinding
import io.mobidoo.a3app.databinding.LayoutGreetingBinding
import io.mobidoo.a3app.utils.AppUtils.expand
import java.util.*


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var handler: Handler? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>

    private lateinit var ibCloseGreeting: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler = Handler(Looper.getMainLooper())

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetParent)
        bottomSheetBehavior.isDraggable = false

        handler?.postDelayed({
            showSecondSplash()
        }, 1500)
        ibCloseGreeting = findViewById(R.id.ibCloseGreeting)
        ibCloseGreeting.setOnClickListener {
            openMainActivity()
        }

    }

    @SuppressLint("HardwareIds")
    private fun openSecondScreen() {
        val request = RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("34A6AF4C95E8EC517667A12EF589AB8B")).build()
        MobileAds.setRequestConfiguration(request)
        MobileAds.initialize(this, object: OnInitializationCompleteListener{
            override fun onInitializationComplete(p0: InitializationStatus) {
                bottomSheetBehavior.expand()
                loadAd()
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
        handler?.postDelayed({ openSecondScreen() }, 100)
    }

    private fun loadAd(){
        val builder = AdLoader.Builder(this, BuildConfig.AD_MOB_KEY)
            .forNativeAd { nativeAd ->
                val adView = layoutInflater.inflate(R.layout.layout_ad_splash, null) as NativeAdView

                adView.findViewById<TextView>(R.id.ad_headline).text = nativeAd.headline
                adView.findViewById<ImageView>(R.id.ad_app_icon).load(nativeAd.icon?.drawable)
                Log.i("SplashScreen", "nativeAd $nativeAd")
            }
            .withAdListener(object : AdListener(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.i("SplashScreen", "nativeAd failed ${p0.message}")
                }
            })
            .build()
        val request = AdRequest.Builder()
            .build()
        Log.i("SplashScreen", "is Test device ${request.isTestDevice(this)}")
        builder.loadAd(request)
    }
}