package io.mobidoo.a3app.ui.ringtone

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import coil.load
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.FragmentWallpaperSuccessBinding
import io.mobidoo.a3app.databinding.LayoutAdSuccessSavingBinding
import io.mobidoo.a3app.databinding.LayoutWallpaperSavedBinding

import io.mobidoo.a3app.utils.Constants.interAdKeyList
import io.mobidoo.a3app.utils.Constants.nativeAdKeyList

class RingtoneSuccessFragment : Fragment() {
    companion object{
        const val ARG_SAVED_VALUE = "arg_saved_value"
    }

    private var _binding: FragmentWallpaperSuccessBinding? = null
    private val binding get() = _binding!!

    private var layoutBinding: LayoutWallpaperSavedBinding? = null
    private var mNativeAd: NativeAd? = null
    private var nativeAdLoaded = false
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialLoaded = false
    private var onBackPressed = false
    private var closeButtonPressed = false
    private var thanksPressed = false

    private var loadNativeAdAttempt = 0
    private var loadInterAdAttempt = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWallpaperSuccessBinding.inflate(layoutInflater, container, false)
        layoutBinding = LayoutWallpaperSavedBinding.bind(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view)
        loadNativeAdAttempt = 0
        loadInterAdAttempt = 0
        loadInterAd(interAdKeyList[loadInterAdAttempt])
        loadAd(nativeAdKeyList[loadNativeAdAttempt])
        val value = arguments?.getString(ARG_SAVED_VALUE)
        val tvSavedValue = view.findViewById<TextView>(R.id.greeting_layoutHeader1)
        tvSavedValue.text = String.format(resources.getString(R.string.successfullySavedWallpaper), value)
        layoutBinding?.btnThanksSuccessSaving?.setOnClickListener {
            thanksPressed = true
            if (nativeAdLoaded && mNativeAd != null){
                showAdvertising()
            }else if(nativeAdLoaded && mNativeAd == null){
                binding.rlWallpaperSavedAdvertisiing.visibility = View.VISIBLE
                binding.ibCloseSuccessSaving.visibility = View.VISIBLE
            }else{

            }
        }
        binding.ibCloseSuccessSaving.setOnClickListener {
            if (interstitialLoaded && mInterstitialAd != null){
                mInterstitialAd?.show(requireActivity())

            }else if(interstitialLoaded && mInterstitialAd == null){
               // activity?.supportFragmentManager?.popBackStack()
                Navigation.findNavController(requireView()).popBackStack()
            }else{
                closeButtonPressed = true
                binding.ibCloseSuccessSaving.visibility = View.GONE
                binding.pbCloseSuccessWallpFr.visibility = View.VISIBLE
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!thanksPressed)
                        layoutBinding?.btnThanksSuccessSaving?.performClick()
                    else {
                        //activity?.finish()
                    }
                }
            }
        )
    }

    private fun setInsets(view: View) {
        view.setOnApplyWindowInsetsListener { view, windowInsets ->
            val botInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).bottom
            } else {
                windowInsets.stableInsetBottom
            }
            val topInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).top
            } else {
                windowInsets.stableInsetTop
            }
            val params1 = binding.rlWallpaperSaved.layoutParams as ViewGroup.MarginLayoutParams
            params1.setMargins(0, topInset, 0, botInset)
            binding.rlWallpaperSaved.layoutParams = params1

            val params2 = binding.rlWallpaperSavedAdvertisiing.layoutParams as ViewGroup.MarginLayoutParams
            params2.setMargins(0, topInset, 0, botInset)
            binding.rlWallpaperSavedAdvertisiing.layoutParams = params2
            return@setOnApplyWindowInsetsListener windowInsets
        }
    }

    private fun loadInterAd(key : String){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(requireContext(), key, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.i("SplashScreen", "filed to load interstitial attempt $loadInterAdAttempt")
                loadInterAdAttempt++
                if (loadInterAdAttempt > interAdKeyList.size - 1){
                    mInterstitialAd = null
                    interstitialLoaded = true
                    binding.ibCloseSuccessSaving.visibility = View.VISIBLE
                    binding.pbCloseSuccessWallpFr.visibility = View.GONE
                    if (closeButtonPressed){
                        Navigation.findNavController(requireView()).popBackStack()
                    }
                }else{
                    loadInterAd(interAdKeyList[loadInterAdAttempt])
                }
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
                      //  activity?.supportFragmentManager?.popBackStack()
                        Navigation.findNavController(requireView()).popBackStack()
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
                if(closeButtonPressed){
                    mInterstitialAd?.show(requireActivity())
                }
            }
        })
    }

    private fun loadAd(key: String){
        val builder = AdLoader.Builder(requireContext(), key)
            .forNativeAd { nativeAd ->

                try {
                    if (thanksPressed)
                        showAdvertising()
                    if(this.isDetached){
                        nativeAd.destroy()
                        return@forNativeAd
                    }
                    val adBinding = activity?.layoutInflater?.let {
                        LayoutAdSuccessSavingBinding.inflate(
                            it
                        )
                    }
                    adBinding?.let { populateNativeAdView(nativeAd, it) }
                    binding.adViewWallpaperSaved.addView(adBinding?.root)
                }catch (e: Exception){

                }

            }
            .withAdListener(object : AdListener(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.i("RingtoneSuccessFragment", "failed to load native attempt $loadNativeAdAttempt")
                    loadNativeAdAttempt++
                    if (loadNativeAdAttempt > nativeAdKeyList.size - 1){
                        if (thanksPressed) showAdvertising()
                    }else{
                        loadAd(nativeAdKeyList[loadNativeAdAttempt])
                    }
                }
            })
            .build()
        val request = AdRequest.Builder()
            .build()
        builder.loadAd(request)
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adViewBinding: LayoutAdSuccessSavingBinding){
        val nativeAdView = adViewBinding.root

        nativeAdView.bodyView = adViewBinding.adHeadlineSuccess
        nativeAdView.iconView = adViewBinding.adAppIconSuccess
        nativeAdView.callToActionView = adViewBinding.btnNativeAdActionSuccess

        if (nativeAd.body != null){
            adViewBinding.adHeadlineSuccess.text = nativeAd.body
        }else if (nativeAd.headline != null){
            adViewBinding.adHeadlineSuccess.text = nativeAd.headline
        }

        if (nativeAd.mediaContent != null){
            adViewBinding.adAppIconSuccess.load(nativeAd.mediaContent?.mainImage)
        }else{
            adViewBinding.adAppIconSuccess.load(nativeAd.icon?.uri)
        }
        if (nativeAd.callToAction != null)
            adViewBinding.btnNativeAdActionSuccess.text = nativeAd.callToAction

    }

    private fun showAdvertising() {
        Log.i(this::class.simpleName, "showAdvertising")
        binding.rlWallpaperSavedAdvertisiing.visibility = View.VISIBLE
        binding.ibCloseSuccessSaving.visibility = View.VISIBLE
    }

}