package io.mobidoo.a3app.ui.startfragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.R
import io.mobidoo.a3app.adapters.WallpaperCategoriesAdapter
import io.mobidoo.a3app.databinding.FragmentWallpaperCategoriesBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.entity.startcollectionitem.SubCategoryRecyclerItem
import io.mobidoo.a3app.entity.uistate.allcollectionstate.WallCategoriesUIState
import io.mobidoo.a3app.ui.WallpaperActivity
import io.mobidoo.a3app.ui.testInterAd
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModel
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModelFactory
import io.mobidoo.domain.common.Constants
import io.mobidoo.domain.common.Constants.WALLS_HEIGHT_TO_WIDTH_DIMENSION
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class WallCategoriesFragment : Fragment() {

    companion object{
        private const val widthScale = 0.32f
        private const val heightToWidthScale = WALLS_HEIGHT_TO_WIDTH_DIMENSION
    }
    private var _binding: FragmentWallpaperCategoriesBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var factory: WallCategoriesViewModelFactory
    private lateinit var viewModel: WallCategoriesViewModel
    private val nativeAds = arrayListOf<NativeAd>()
    private var adsInitialized = false
    private var adsLoadingNow  = false
    private var arraySize = 0
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialLoaded = false
    private var interIsShowing = false
    private var selectedWallpaperItem: Wallpaper? = null

    override fun onAttach(context: Context) {
        (activity?.application as Injector).createWallpaperSubComponent().inject(this)
        viewModel = ViewModelProvider(viewModelStore, factory)[WallCategoriesViewModel::class.java]
        super.onAttach(context)
    }

    private var categoriesAdapter: WallpaperCategoriesAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWallpaperCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        loadInterAd()
        binding.tvCategoriesName.text = arguments?.getString(StartCollectionFragment.ARG_NAME)?.let{
             it
        }?: resources.getString(R.string.liveCategories)

        categoriesAdapter = WallpaperCategoriesAdapter({l, n ->
            getAllWalls(l, n)
        },
            {
                openWallpaper(it)
            })
        binding.rvWallCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoriesAdapter
        }
        lifecycleScope.launch {
            viewModel.uiStateFlow.collect(){
                handleUIState(it)
            }
        }
        arguments?.getString(StartCollectionFragment.ARG_LINK)?.let {
            viewModel.getCategories(it)
        } ?: viewModel.getCategories(AppUtils.createFullLink(AppUtils.liveWallpaperUrl))

        binding.ibBackCategories.setOnClickListener {
            activity?.onBackPressed()
        }
    }
    private fun loadInterAd(){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(requireContext(), testInterAd, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.i("SplashScreen", "filed to load interstitial")
                mInterstitialAd = null
                interstitialLoaded = true

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
                        startActivity(
                            WallpaperActivity.getIntent(requireActivity(),
                                selectedWallpaperItem!!.url,
                                resources.getString(R.string.common_folder),
                                AppUtils.getWallpaperTypeFromLink(selectedWallpaperItem?.url!!)
                            ))
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
                if(!interIsShowing){
                    mInterstitialAd?.show(requireActivity())
                }
            }
        })
    }

    private fun loadAds(count: Int){
        Log.i("WallpaperCategory", "loadAds count $count")
        nativeAds.forEach {
            it.destroy()
        }
        val builder = AdLoader.Builder(requireContext(), BuildConfig.AD_MOB_KEY)
            .forNativeAd { nativeAd ->
                Log.i("WallpaperCategory", "native ad $nativeAd")
                if(isDetached){
                    nativeAd.destroy()
                    return@forNativeAd
                }

                nativeAds.add(nativeAd)
                categoriesAdapter?.setNativeAd(nativeAd)
            }
            .withAdListener(object : AdListener(){
                override fun onAdLoaded() {
                    super.onAdLoaded()

                    Log.i("WallpaperCategory", "onAdLoaded")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {

                    Log.i("WallpaperCategory", "nativeAd failed ${p0.message}")
                }
            })
            .build()
        val request = AdRequest.Builder()
            .build()
        builder.loadAds(request, count)
    }
    private fun openWallpaper(item: Wallpaper) {
//        selectedWallpaperItem = item
//        if (interstitialLoaded && mInterstitialAd != null){
//            interIsShowing = true
//            mInterstitialAd?.show(requireActivity())
//        }else if(interstitialLoaded && mInterstitialAd == null){
//            startActivity(
//                WallpaperActivity.getIntent(requireActivity(), item.url, resources.getString(R.string.common_folder),
//                    AppUtils.getWallpaperTypeFromLink(item.url)
//                ))
//        }
        startActivity(
            WallpaperActivity.getIntent(requireActivity(), item.url, resources.getString(R.string.common_folder),
                AppUtils.getWallpaperTypeFromLink(item.url)
            ))
    }

    private fun handleUIState(uiState: WallCategoriesUIState) {
        if (uiState.isLoading){

        }else{
            binding.shimmerCategories.stop()
        }
        val list = arrayListOf<SubCategoryRecyclerItem>()
        list.addAll(uiState.list.map { SubCategoryRecyclerItem(it.name, it.array, it.wallLink, false) })
        var adCount = 0
        for (i in 1..list.lastIndex){
            if(i%(Constants.AD_FREQUENCY_CATEGORIES) == 0){
                list.add(index = i+adCount, SubCategoryRecyclerItem("", emptyList(), "", true))
                adCount++
            }
        }
        loadAds(list.size/Constants.AD_FREQUENCY_CATEGORIES)
        categoriesAdapter?.setList(list)
    }

    private fun getAllWalls(link: String, name: String){
        binding.rvWallCategories.findNavController().navigate(R.id.action_wallCategoriesFragment_to_selectedCategoryWallpapersFragment, Bundle().apply {
            putString(StartCollectionFragment.ARG_NAME, name)
            putString(StartCollectionFragment.ARG_LINK, link)
        })
    }

    override fun onDestroy() {
        nativeAds.forEach {
            it.destroy()
        }

        super.onDestroy()
    }
}