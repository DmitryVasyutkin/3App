package io.mobidoo.a3app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.FragmentRingtonesBinding
import io.mobidoo.a3app.databinding.FragmentSettingsMainBinding
import io.mobidoo.a3app.databinding.LayoutAdCollectionsBinding
import io.mobidoo.a3app.databinding.LayoutAdSuccessSavingBinding
import io.mobidoo.a3app.utils.Constants
import io.mobidoo.a3app.utils.Constants.nativeAdKeyList

class SettingsMainFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentSettingsMainBinding? = null
    private val binding get() = _binding!!

    private var loadNativeAdAttempt = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadNativeAdAttempt = 0
        binding.ibBackMainSettings.setOnClickListener(this)
        binding.btnSettingsUseCond.setOnClickListener(this)
        binding.btnSettingsPrivacy.setOnClickListener(this)
        binding.btnSettingsSupport.setOnClickListener(this)
        binding.btnSettingsRateUs.setOnClickListener(this)
        binding.btnSettingsInvite.setOnClickListener(this)
        binding.btnSettingsClearCache.setOnClickListener(this)
        loadAd(nativeAdKeyList[loadNativeAdAttempt])

    }
    private fun loadAd(key: String){
        val builder = AdLoader.Builder(requireContext(), key)
            .forNativeAd { nativeAd ->

                try{
                    if(this.isDetached){
                        nativeAd.destroy()
                        return@forNativeAd
                    }
                    val adBinding = activity?.layoutInflater?.let {
                        LayoutAdCollectionsBinding.inflate(
                            it
                        )
                    }
                    adBinding?.let { populateNativeAdView(nativeAd, it) }
                    binding.adViewSettings.addView(adBinding?.root)
                }catch (e:Exception){

                }

            }
            .withAdListener(object : AdListener(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.i("SettingsFragment", "failed to load native ad attempt $loadNativeAdAttempt")
                    loadNativeAdAttempt++
                    if (loadNativeAdAttempt <= nativeAdKeyList.size - 1){
                        try {
                            loadAd(nativeAdKeyList[loadNativeAdAttempt])
                        }catch (e: Exception){

                        }
                    }
                }
            })
            .build()
        val request = AdRequest.Builder()
            .build()
        builder.loadAd(request)
    }

    override fun onClick(view: View?) {
        when(view){
            binding.ibBackMainSettings -> {
                activity?.onBackPressed()
            }
            binding.btnSettingsUseCond ->{}
            binding.btnSettingsPrivacy ->{}
            binding.btnSettingsSupport ->{}
            binding.btnSettingsRateUs ->{
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.shareLink)))
            }
            binding.btnSettingsInvite ->{
                val link = Constants.shareLink
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, link)
                }
                startActivity(Intent.createChooser(intent, resources.getString(R.string.shareLink)))
            }
            binding.btnSettingsClearCache ->{}

        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adViewBinding: LayoutAdCollectionsBinding){
        val nativeAdView = adViewBinding.root

        nativeAdView.bodyView = adViewBinding.adHeadlineCollections
        nativeAdView.iconView = adViewBinding.adAppIconCollections
        nativeAdView.callToActionView = adViewBinding.btnNativeAdActionCollections

        adViewBinding.adHeadlineCollections.text = nativeAd.body
        if (nativeAd.mediaContent != null){
            adViewBinding.adAppIconCollections.load(nativeAd.mediaContent?.mainImage)
        }else{
            adViewBinding.adAppIconCollections.load(nativeAd.icon?.uri)
        }

        adViewBinding.btnNativeAdActionCollections.text = nativeAd.callToAction

    }
}