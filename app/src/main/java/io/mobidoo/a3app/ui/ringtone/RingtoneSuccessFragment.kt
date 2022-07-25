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
import coil.load
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.FragmentWallpaperSuccessBinding
import io.mobidoo.a3app.databinding.LayoutAdSuccessSavingBinding
import io.mobidoo.a3app.databinding.LayoutWallpaperSavedBinding

class RingtoneSuccessFragment : Fragment() {
    companion object{
        const val ARG_SAVED_VALUE = "arg_saved_value"
    }

    private var _binding: FragmentWallpaperSuccessBinding? = null
    private val binding get() = _binding!!

    private var layoutBinding: LayoutWallpaperSavedBinding? = null

    private var thanksPressed = false
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
        loadAd()
        val value = arguments?.getString(ARG_SAVED_VALUE)
        val tvSavedValue = view.findViewById<TextView>(R.id.greeting_layoutHeader1)
        tvSavedValue.text = String.format(resources.getString(R.string.successfullySavedWallpaper), value)
        layoutBinding?.btnThanksSuccessSaving?.setOnClickListener {
            thanksPressed = true
            showAdvertising()
        }
        binding.ibCloseSuccessSaving.setOnClickListener {
            activity?.onBackPressed()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!thanksPressed)
                        layoutBinding?.btnThanksSuccessSaving?.performClick()
                    else activity?.supportFragmentManager?.popBackStack()
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

    private fun loadAd(){
        val builder = AdLoader.Builder(requireContext(), BuildConfig.AD_MOB_KEY)
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
                    }!!
                    populateNativeAdView(nativeAd, adBinding)
                    binding.adViewWallpaperSaved.addView(adBinding.root)
                }catch (e: Exception){

                }

            }
            .withAdListener(object : AdListener(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    if (thanksPressed) showAdvertising()

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