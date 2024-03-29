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

import io.mobidoo.a3app.ui.wallpaperpreview.WallpaperPreviewFragment
import io.mobidoo.a3app.utils.AppUtils.getWallpaperTypeFromLink
import io.mobidoo.a3app.utils.Constants.nativeAdKeyList
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

    private var loadNativeAdAttempt = 0
    private var nativeAdsCount = 0

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
        binding.tvWallpapersCategoryName.text = arguments?.getString(StartCollectionFragment.ARG_NAME)

        wallpapersAdapter = WallpaperRecyclerItemAdapter(){
            startActivity(WallpaperActivity.getIntent(requireActivity(), it, arguments?.getString(StartCollectionFragment.ARG_NAME)!!, getWallpaperTypeFromLink(it)) )
        }
        loadNativeAdAttempt = 0
        lifecycleScope.launch {
            viewModel.wallUiStateFlow.collect(){
                if(!it.isLoading){
                    binding.shimmerSelectedCategoryWallpapers.stop()
                }
                wallpapersAdapter?.setList(createList(it.array))
                arraySize = it.array.size
                nativeAdsCount = it.array.size / (Constants.AD_FREQUENCY_WALLPAPERS * 3)
                loadAds(nativeAdsCount, nativeAdKeyList[loadNativeAdAttempt])
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


    private fun loadAds(count: Int, key: String){

        nativeAds.forEach {
            it.destroy()
        }
        val builder = AdLoader.Builder(requireContext(), key)
            .forNativeAd { nativeAd ->

                if(isDetached){
                    nativeAd.destroy()
                    return@forNativeAd
                }

                nativeAds.add(nativeAd)
                wallpapersAdapter?.setNativeAd(nativeAd)
            }
            .withAdListener(object : AdListener(){

                override fun onAdFailedToLoad(p0: LoadAdError) {

                    Log.i("SelectedCategory", "nativeAd failed ${p0.message}, attempt $loadNativeAdAttempt")
                    loadNativeAdAttempt++
                    if (loadNativeAdAttempt <= io.mobidoo.a3app.utils.Constants.nativeAdKeyList.size - 1){
                        try {
                            loadAds(
                                nativeAdsCount,
                                io.mobidoo.a3app.utils.Constants.nativeAdKeyList[loadNativeAdAttempt]
                            )
                        }catch (e: Exception){

                        }
                    }
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