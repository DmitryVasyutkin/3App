package io.mobidoo.a3app.ui.wallpaperpreview

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
import io.mobidoo.a3app.databinding.LayoutAdWallPreviewBinding
import io.mobidoo.a3app.databinding.LayoutWallpaperSavedBinding
import io.mobidoo.a3app.ui.WallpaperActivity
import io.mobidoo.a3app.utils.Constants.nativeAdKeyList

class WallpaperPreviewSuccessFragment : Fragment() {
    companion object{
        const val ARG_SAVED_VALUE = "arg_saved_value"
    }

    private var _binding: FragmentWallpaperSuccessBinding? = null
    private val binding get() = _binding!!

    private var layoutBinding: LayoutWallpaperSavedBinding? = null
    private var value = ""
    private var thanksPressed = false

    private var mNativeAd: NativeAd? = null
    private var nativeAdLoaded = false
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialLoaded = false
    private var closeButtonPressed = false

    private var loadNativeAdAttempt = 0

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
        loadAd(nativeAdKeyList[loadNativeAdAttempt])
        value = arguments?.getString(ARG_SAVED_VALUE).toString()
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
                Log.i("SuccessFragment", "$nativeAdLoaded, $mNativeAd")
            }

        }

        binding.ibCloseSuccessSaving.setOnClickListener {
            openMainActivity()

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!thanksPressed)
                        layoutBinding?.btnThanksSuccessSaving?.performClick()
                    else {
                        openMainActivity()
//                        if (value == resources.getString(R.string.flashCall))
//                            activity?.supportFragmentManager?.popBackStack()
//                        else activity?.finish()
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



    private fun openMainActivity() {
        if (activity is WallpaperActivity) {
            (activity as WallpaperActivity).isLiveWall = true
            Navigation.findNavController(requireView()).popBackStack(R.id.wallpaperActionFragment, false)
        }else{
           // activity?.finish()
            Navigation.findNavController(requireView()).popBackStack(R.id.flashCallPreviewFragment, false)
        }
    }

    private fun loadAd(key: String){
        val builder = AdLoader.Builder(requireContext(), key)
            .forNativeAd { nativeAd ->
                nativeAdLoaded = true
                mNativeAd = nativeAd
                try {
                    if (thanksPressed)
                        showAdvertising()
                    if(this.isDetached){
                        nativeAd.destroy()
                        return@forNativeAd
                    }

                }catch (e: Exception){

                }
            }
            .withAdListener(object : AdListener(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.i("WallpaperSuccessFragment", "failed load native ad attempt $loadNativeAdAttempt")
                    loadNativeAdAttempt++
                    if (loadNativeAdAttempt > nativeAdKeyList.size - 1){
                        nativeAdLoaded = true
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
        nativeAdView.imageView = adViewBinding.adAppIconSuccess
        nativeAdView.callToActionView = adViewBinding.btnNativeAdActionSuccess
        adViewBinding.adHeadlineSuccess.text = nativeAd.body ?: nativeAd.headline

        if (nativeAd.mediaContent != null){
            adViewBinding.adAppIconSuccess.load(nativeAd.mediaContent?.mainImage)
        }else{
            adViewBinding.adAppIconSuccess.load(nativeAd.icon?.uri)
        }
        if(nativeAd.callToAction != null)
            adViewBinding.btnNativeAdActionSuccess.text = nativeAd.callToAction
    }

    private fun showAdvertising() {
        Log.i(this::class.simpleName, "showAdvertising")
        binding.rlWallpaperSavedAdvertisiing.visibility = View.VISIBLE
        binding.ibCloseSuccessSaving.visibility = View.VISIBLE
        val adBinding = activity?.layoutInflater?.let {
            LayoutAdSuccessSavingBinding.inflate(
                it
            )
        }
        mNativeAd?.let {
            if (adBinding != null) {
                populateNativeAdView(it, adBinding)
                binding.adViewWallpaperSaved.addView(adBinding.root)
            }
        }
    }

    override fun onDestroy() {
        mNativeAd?.destroy()
        super.onDestroy()
    }
}