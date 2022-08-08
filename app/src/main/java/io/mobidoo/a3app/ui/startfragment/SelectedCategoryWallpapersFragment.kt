package io.mobidoo.a3app.entity.startcollectionitem

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.R
import io.mobidoo.a3app.adapters.WallpaperRecyclerItemAdapter
import io.mobidoo.a3app.databinding.FragmentSelectedCategoryWallpapersBinding
import io.mobidoo.a3app.databinding.LayoutAdCollectionsBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.ui.WallpaperActivity
import io.mobidoo.a3app.ui.startfragment.StartCollectionFragment
import io.mobidoo.a3app.ui.startfragment.stop
import io.mobidoo.a3app.ui.testInterAd
import io.mobidoo.a3app.ui.wallpaperpreview.WallpaperPreviewFragment
import io.mobidoo.a3app.utils.AppUtils.getWallpaperTypeFromLink
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModel
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModelFactory
import io.mobidoo.domain.common.Constants
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class SelectedCategoryWallpapersFragment : Fragment(){
    companion object{
        const val widthScale = 0.32f
        const val heightToWidthScale = Constants.WALLS_HEIGHT_TO_WIDTH_DIMENSION

    }
    private var _binding: FragmentSelectedCategoryWallpapersBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var factory: WallCategoriesViewModelFactory
    private lateinit var viewModel: WallCategoriesViewModel
    private var wallpapersAdapter: WallpaperRecyclerItemAdapter? = null
    private var categoryName =""
    private val nativeAds = arrayListOf<NativeAd>()
    private var adsInitialized = false
    private var adsLoadingNow  = false
    private var arraySize = 0

    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialLoaded = false
    private var interIsShowing = false
    private var selectedWallpaperItemUrl: String? = null

    override fun onAttach(context: Context) {
        (activity?.application as Injector).createWallpaperSubComponent().inject(this)
        viewModel = ViewModelProvider(viewModelStore, factory)[WallCategoriesViewModel::class.java]
        super.onAttach(context)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectedCategoryWallpapersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        loadInterAd()
        binding.tvWallpapersCategoryName.text = arguments?.getString(StartCollectionFragment.ARG_NAME)

        wallpapersAdapter = WallpaperRecyclerItemAdapter(){
//            selectedWallpaperItemUrl = it
//            if (interstitialLoaded && mInterstitialAd != null){
//                interIsShowing = true
//                mInterstitialAd?.show(requireActivity())
//            }else if(interstitialLoaded && mInterstitialAd == null){
//                startActivity(
//                    WallpaperActivity.getIntent(requireActivity(), it, resources.getString(R.string.common_folder), getWallpaperTypeFromLink(it)))
//            }
            startActivity(WallpaperActivity.getIntent(requireActivity(), it, arguments?.getString(StartCollectionFragment.ARG_NAME)!!, getWallpaperTypeFromLink(it)) )
        }
        lifecycleScope.launch {
            viewModel.wallUiStateFlow.collect(){
                if(!it.isLoading){
                    binding.shimmerSelectedCategoryWallpapers.stop()
                }
                wallpapersAdapter?.setList(createList(it.array))
                arraySize = it.array.size
                loadAds(it.array.size / (Constants.AD_FREQUENCY_WALLPAPERS * 3))
            }
        }
        binding.ibBackWallpapers.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.rvSelectedCategoryRecyclerItem.apply{
            layoutManager = LinearLayoutManager(requireContext())
            adapter = wallpapersAdapter
        }
        arguments?.getString(StartCollectionFragment.ARG_LINK)?.let { viewModel.getWallpapers(it) }
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
                                selectedWallpaperItemUrl!!,
                                resources.getString(R.string.common_folder),
                                getWallpaperTypeFromLink(selectedWallpaperItemUrl!!)))
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
        Log.i("SelectedCategory", "loadAds count $count")
        nativeAds.forEach {
            it.destroy()
        }
        val builder = AdLoader.Builder(requireContext(), BuildConfig.AD_MOB_KEY)
            .forNativeAd { nativeAd ->

                if(isDetached){
                    nativeAd.destroy()
                    return@forNativeAd
                }

                nativeAds.add(nativeAd)
                wallpapersAdapter?.setNativeAd(nativeAd)
            }
            .withAdListener(object : AdListener(){
                override fun onAdLoaded() {
                    super.onAdLoaded()

                    Log.i("SelectedCategory", "onAdLoaded")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {

                    Log.i("SelectedCategory", "nativeAd failed ${p0.message}")
                }
            })
            .build()
        val request = AdRequest.Builder()
            .build()
        builder.loadAds(request, count)
    }
    private fun createList(list: List<Wallpaper>): List<WallpaperRecyclerItem>{
        val result = arrayListOf<WallpaperRecyclerItem>()
        val subList = list.chunked(Constants.AD_FREQUENCY_WALLPAPERS * 3)
        subList.forEach {
            val element = WallpaperRecyclerItem(it, false)
            result.add(element)
            result.add(WallpaperRecyclerItem(isAdvertising = true))
        }
        return result
    }

    override fun onDestroy() {
        nativeAds.forEach {
            it.destroy()
        }
        super.onDestroy()
    }
}

class ResizedGridLayoutManager(context: Context, private val widthScale: Float, private val heightToWidthScale: Int, count: Int) : GridLayoutManager(context, count){

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        lp?.width = (width * widthScale).toInt()
        lp?.height = (width * 0.30).toInt() * heightToWidthScale
        return true
    }
}