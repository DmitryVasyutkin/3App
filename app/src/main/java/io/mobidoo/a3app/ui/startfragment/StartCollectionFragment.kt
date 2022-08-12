
package io.mobidoo.a3app.ui.startfragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.R
import io.mobidoo.a3app.adapters.StartFlashCallsAdapters
import io.mobidoo.a3app.adapters.StartWallpaperCollectionsAdapter
import io.mobidoo.a3app.databinding.FragmentStartCollectionBinding
import io.mobidoo.a3app.databinding.LayoutAdCollectionsBinding
import io.mobidoo.a3app.databinding.LayoutAdSplashBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.entity.uistate.allcollectionstate.AllCollectionsUIState
import io.mobidoo.a3app.ui.*
import io.mobidoo.a3app.utils.AppUtils.expand
import io.mobidoo.a3app.utils.AppUtils.getWallpaperTypeFromLink
import io.mobidoo.a3app.utils.Constants.nativeAdKeyList
import io.mobidoo.a3app.viewmodels.MainActivityViewModel
import io.mobidoo.a3app.viewmodels.MainActivityViewModelFactory
import io.mobidoo.domain.common.Constants.WALLS_HEIGHT_TO_WIDTH_DIMENSION
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class StartCollectionFragment : Fragment(), View.OnClickListener {

    companion object{
        const val TAG = "StartCollectionFragment"
        const val ARG_NAME = "name"
        const val ARG_LINK = "link"
        private const val widthScale = 0.35f
        private const val heightToWidthScale = WALLS_HEIGHT_TO_WIDTH_DIMENSION
    }
    @Inject
    lateinit var factory: MainActivityViewModelFactory
    private lateinit var viewModel: MainActivityViewModel

    private var _binding: FragmentStartCollectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var liveWallsAdapter: StartWallpaperCollectionsAdapter
    private lateinit var flashCallAdapter: StartFlashCallsAdapters
    private lateinit var newWallsAdapter: StartWallpaperCollectionsAdapter
    private lateinit var allWallsAdapter: StartWallpaperCollectionsAdapter
    private lateinit var popularWallsAdapter: StartWallpaperCollectionsAdapter
    private lateinit var calmWallsAdapter: StartWallpaperCollectionsAdapter
    private lateinit var abstractWallsAdapter: StartWallpaperCollectionsAdapter
    private var nativeAds = arrayListOf<NativeAd>()
    private lateinit var adViewLayout1: FrameLayout
    private lateinit var adViewLayout2: FrameLayout
    private var uiState: AllCollectionsUIState? = null

    private var uiUpdatesJob: Job? = null

    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialLoaded = false
    private var interIsShowing = false
    private var selectedWallpaperItem: Wallpaper? = null
    private var selectedFlashCallUrl: String? = null

    private var fragmentWasPaused = false
    private var loadNativeAdAttempt = 0
    private var loadInterAdAttempt = 0

    override fun onAttach(context: Context) {
        (activity?.application as Injector).createWallpaperSubComponent().inject(this)
        viewModel = ViewModelProvider(viewModelStore, factory)[MainActivityViewModel::class.java]
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onPause() {
        fragmentWasPaused = true
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadNativeAdAttempt = 0
        loadAds(nativeAdKeyList[loadNativeAdAttempt])
//        loadInterAd()
        initializeRecyclers()
        binding.btnSeeAllLive.setOnClickListener(this)
        binding.btnSeeAllNew.setOnClickListener(this)
        binding.btnSeeAllAbstract.setOnClickListener(this)
        binding.btnSeeAllAll.setOnClickListener(this)
        binding.btnSeeAllCalm.setOnClickListener(this)
        binding.btnSeeAllPopular.setOnClickListener(this)
        binding.btnSeeAllFlash.setOnClickListener(this)
        binding.ibSettings.setOnClickListener(this)
        adViewLayout1 = view.findViewById(R.id.ad_layout_collections1)
        adViewLayout2 = view.findViewById(R.id.ad_layout_collections2)
        uiUpdatesJob = lifecycleScope.launch {
            viewModel.uiStateFlow.collect(){
                Log.i(TAG, "uiState $it")
                uiState = it
                if (it.isLoading){
//                    binding.shimmerFlash.startShimmer()
//                    binding.shimmerLive.startShimmer()
//                    binding.shimmerNew.startShimmer()
//                    binding.shimmerAbstract.startShimmer()
//                    binding.shimmerCalmr.startShimmer()
//                    binding.shimmerPopular.startShimmer()
//                    binding.shimmerRecomendation.startShimmer()
                }else{
                    binding.shimmerFlash.stop()
                    binding.shimmerLive.stop()
                    binding.shimmerNew.stop()
                    binding.shimmerAbstract.stop()
                    binding.shimmerCalmr.stop()
                    binding.shimmerPopular.stop()
                    binding.shimmerRecomendation.stop()
                }
                handleUIState(it)
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.errorMessage.collect(){

            }
        }
    }



    private fun loadAds(key: String){
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
                val adBinding = activity?.layoutInflater?.let {
                    LayoutAdCollectionsBinding.inflate(
                        it
                    )
                }
                if (adBinding != null) {
                    populateNativeAdView(nativeAd, adBinding)
                }
                if(nativeAds.size == 1){
                    adViewLayout1.removeAllViews()
                    adViewLayout1.addView(adBinding?.root)
                }else if(nativeAds.size == 2){
                    adViewLayout2.removeAllViews()
                    adViewLayout2.addView(adBinding?.root)
                }

            }
            .withAdListener(object : AdListener(){
                override fun onAdLoaded() {
                    super.onAdLoaded()

                    Log.i("StartCollections", "onAdLoaded")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {

                    Log.i("StartCollections", "nativeAd failed ${p0.message}, attempt $loadNativeAdAttempt")
                    loadNativeAdAttempt++
                    if (loadNativeAdAttempt <= nativeAdKeyList.size - 1){
                        loadAds(nativeAdKeyList[loadNativeAdAttempt])
                    }
                }
            })
            .build()
        val request = AdRequest.Builder()
            .build()
        builder.loadAds(request, 3)
    }

    private fun handleUIState(it: AllCollectionsUIState) {

        it.live?.startArray?.let { it1 ->
            liveWallsAdapter.setList(it1)
        }
        it.flashCall?.startArray?.let { it1 ->
            flashCallAdapter.setList(it1)
        }
        it.new?.startArray?.let { it1 -> newWallsAdapter.setList(it1) }
        it.all?.startArray?.let { it1 -> allWallsAdapter.setList(it1) }
        it.popular?.startArray?.let { it1 -> popularWallsAdapter.setList(it1) }
        it.pattern?.startArray?.let { it1 -> calmWallsAdapter.setList(it1) }
        it.abstract?.startArray?.let { it1 -> abstractWallsAdapter.setList(it1) }

    }
    private fun handleIemClick(item: Wallpaper){
        startActivity(
            WallpaperActivity.getIntent(requireActivity(), item.url, resources.getString(R.string.common_folder), getWallpaperTypeFromLink(item.url)))

    }
    private fun initializeRecyclers() {
        liveWallsAdapter = StartWallpaperCollectionsAdapter {
            handleIemClick(it)
        }
        flashCallAdapter = StartFlashCallsAdapters(){
            handleFlashCallItemClick(it)
        }
        newWallsAdapter = StartWallpaperCollectionsAdapter{
            handleIemClick(it)
        }
        allWallsAdapter = StartWallpaperCollectionsAdapter{
            handleIemClick(it)
        }
        popularWallsAdapter = StartWallpaperCollectionsAdapter{
            handleIemClick(it)
        }
        calmWallsAdapter = StartWallpaperCollectionsAdapter{
            handleIemClick(it)
        }
        abstractWallsAdapter = StartWallpaperCollectionsAdapter{
            handleIemClick(it)
        }
        binding.rvCollectionLive.apply {
            layoutManager = HorizontalLayoutManager(requireContext(), widthScale, heightToWidthScale)
            adapter = liveWallsAdapter
            setHasFixedSize(true)
        }
        binding.rvCollectionFlashCalls.apply {
            layoutManager = HorizontalLayoutManager(requireContext(), widthScale, heightToWidthScale)
            adapter = flashCallAdapter
            setHasFixedSize(true)
        }
        binding.rvCollectionNew.apply {
            layoutManager = HorizontalLayoutManager(requireContext(), widthScale, heightToWidthScale)
            adapter = newWallsAdapter
            setHasFixedSize(true)
        }
        binding.rvCollectionAll.apply {
            layoutManager = HorizontalLayoutManager(requireContext(), widthScale, heightToWidthScale)
            adapter = allWallsAdapter
            setHasFixedSize(true)
        }
        binding.rvCollectionPopular.apply {
            layoutManager = HorizontalLayoutManager(requireContext(), widthScale, heightToWidthScale)
            adapter = popularWallsAdapter
            setHasFixedSize(true)
        }
        binding.rvCollectionCalm.apply {
            layoutManager = HorizontalLayoutManager(requireContext(), widthScale, heightToWidthScale)
            adapter = calmWallsAdapter
            setHasFixedSize(true)
        }
        binding.rvCollectionAbstract.apply {
            layoutManager = HorizontalLayoutManager(requireContext(), widthScale, heightToWidthScale)
            adapter = abstractWallsAdapter
            setHasFixedSize(true)
        }
    }

    private fun handleFlashCallItemClick(url: String) {
//        selectedFlashCallUrl = url
//        if (interstitialLoaded && mInterstitialAd != null){
//            interIsShowing = true
//            mInterstitialAd?.show(requireActivity())
//        }else if(interstitialLoaded && mInterstitialAd == null){
//            startActivity(FlashCallPreviewActivity.getIntent(requireActivity(), url, resources.getString(R.string.flash_calls)))
//        }
        startActivity(FlashCallPreviewActivity.getIntent(requireActivity(), url, resources.getString(R.string.flash_calls)))
    }

    override fun onResume() {
        super.onResume()
        nativeAds.clear()
        viewModel.getStartCollection()
        selectedFlashCallUrl = null
        selectedWallpaperItem = null

    }

    override fun onStop() {
        uiUpdatesJob?.cancel()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        nativeAds.forEach{
            it.destroy()
        }
        nativeAds.clear()
        super.onDestroy()
    }

    override fun onClick(p0: View?) {
        when(p0){
            binding.btnSeeAllLive -> {
                if(uiState?.live != null){
//                    p0.findNavController().navigate(R.id.action_startCollectionFragment_to_wallCategoriesFragment, Bundle().apply {
//                        putString(ARG_NAME, resources.getString(R.string.liveCategories))
//                        putSerializable(ARG_LINK, uiState?.live?.linkForFull)
//                    })
                    (activity as MainActivity).navigateToLiveWalls()
                }
            }
            binding.btnSeeAllAbstract -> {
                if(uiState?.abstract != null){
                    p0.findNavController().navigate(R.id.action_startCollectionFragment_to_selectedCategoryWallpapersFragment, Bundle().apply {
                        putString(StartCollectionFragment.ARG_NAME, resources.getString(R.string.Abstract))
                        putString(StartCollectionFragment.ARG_LINK, uiState?.abstract?.linkForFull)
                    })
                }
            }
            binding.btnSeeAllAll -> {
                if(uiState?.all != null){
                    p0.findNavController().navigate(R.id.action_startCollectionFragment_to_wallCategoriesFragment, Bundle().apply {
                        putString(ARG_NAME, resources.getString(R.string.allCategories))
                        putSerializable(ARG_LINK, uiState?.all?.linkForFull)
                    })
                }
            }
            binding.btnSeeAllNew -> {
                if(uiState?.new != null){
                    p0.findNavController().navigate(R.id.action_startCollectionFragment_to_wallCategoriesFragment, Bundle().apply {
                        putString(ARG_NAME, resources.getString(R.string.newCategories))
                        putSerializable(ARG_LINK, uiState?.new?.linkForFull)
                    })
                }
            }
            binding.btnSeeAllCalm -> {
                if(uiState?.pattern != null){
                    p0.findNavController().navigate(R.id.action_startCollectionFragment_to_selectedCategoryWallpapersFragment, Bundle().apply {
                        putString(StartCollectionFragment.ARG_NAME, resources.getString(R.string.calm))
                        putString(StartCollectionFragment.ARG_LINK, uiState?.abstract?.linkForFull)
                    })
                }
            }
            binding.btnSeeAllPopular -> {
                if(uiState?.popular != null){
                    p0.findNavController().navigate(R.id.action_startCollectionFragment_to_wallCategoriesFragment, Bundle().apply {
                        putString(ARG_NAME, resources.getString(R.string.popularCategories))
                        putSerializable(ARG_LINK, uiState?.popular?.linkForFull)
                    })
                }
            }
            binding.btnSeeAllFlash -> {
                (activity as MainActivity).navigateToFlashCalls()
            }
            binding.ibSettings -> {
                startActivity(Intent(requireActivity(), SettingsActivity::class.java))
            }
        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adViewBinding: LayoutAdCollectionsBinding){
        val nativeAdView = adViewBinding.root

        nativeAdView.bodyView = adViewBinding.adHeadlineCollections
        nativeAdView.imageView = adViewBinding.adAppIconCollections
        nativeAdView.callToActionView = adViewBinding.btnNativeAdActionCollections

        if (nativeAd.body != null){
            adViewBinding.adHeadlineCollections.text = nativeAd.body
        }else if(nativeAd.headline != null){
            adViewBinding.adHeadlineCollections.text = nativeAd.headline
        }

        if (nativeAd.mediaContent != null){
            adViewBinding.adAppIconCollections.load(nativeAd.mediaContent?.mainImage)
        }else{
            adViewBinding.adAppIconCollections.load(nativeAd.icon?.uri)
        }

        if (nativeAd.callToAction != null)
            adViewBinding.btnNativeAdActionCollections.text = nativeAd.callToAction

    }
}
 class HorizontalLayoutManager(context: Context, private val widthScale: Float, private val heightToWidthScale: Int) : LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false){

     override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
         lp?.width = (width * widthScale).toInt()
         lp?.height = (width * 0.30).toInt() * heightToWidthScale
         return true
     }
 }

fun ShimmerFrameLayout.stop(){
    this.stopShimmer()
    this.visibility = View.GONE
}