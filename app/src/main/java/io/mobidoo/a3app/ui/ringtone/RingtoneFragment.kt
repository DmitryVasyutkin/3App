package io.mobidoo.a3app.ui.ringtone

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import io.mobidoo.a3app.BuildConfig
import io.mobidoo.a3app.adapters.RingtoneCategoriesAdapter
import io.mobidoo.a3app.databinding.FragmentRingtonesBinding
import io.mobidoo.a3app.databinding.FragmentStartCollectionBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.entity.startcollectionitem.RingtoneCategoryRecyclerItem
import io.mobidoo.a3app.entity.startcollectionitem.SubCategoryRecyclerItem
import io.mobidoo.a3app.entity.startcollectionitem.WallpaperRecyclerItem
import io.mobidoo.a3app.ui.MainActivity
import io.mobidoo.a3app.ui.RingtoneCategoryItemsActivity
import io.mobidoo.a3app.ui.startfragment.stop
import io.mobidoo.a3app.utils.Constants.nativeAdKeyList
import io.mobidoo.a3app.viewmodels.RingtonesViewModel
import io.mobidoo.a3app.viewmodels.RingtonesViewModelFactory
import io.mobidoo.domain.common.Constants
import io.mobidoo.domain.entities.ringtone.RingtoneCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import kotlinx.coroutines.launch
import javax.inject.Inject

class RingtoneFragment : Fragment() {

    private var _binding: FragmentRingtonesBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var factory: RingtonesViewModelFactory
    private lateinit var viewModel: RingtonesViewModel

    private lateinit var categoriesAdapter: RingtoneCategoriesAdapter

    private var loadNativeAdAttempt = 0
    private var adsCount = 0

    override fun onAttach(context: Context) {
        ((activity as MainActivity).application as Injector).createRingtoneSubComponent().inject(this)
        viewModel = ViewModelProvider(viewModelStore, factory)[RingtonesViewModel::class.java]
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRingtonesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoriesAdapter = RingtoneCategoriesAdapter({link, categName ->
            val intent = RingtoneCategoryItemsActivity.getIntent(requireActivity(), link, categName)
            startActivity(intent)
        },{link, name, ringtone, p ->
            val intent = RingtoneCategoryItemsActivity.getIntent(requireActivity(), link, name, ringtone, p)
            startActivity(intent)
        })
        loadNativeAdAttempt = 0
        binding.rvRingtoneCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoriesAdapter
        }

        lifecycleScope.launch {
            viewModel.categoriesState.collect(){
                if (!it.isLoading){
                    binding.shimmerRingtoneCategories.stop()
                    val list = createList(it.categories)
                    adsCount = list.size/Constants.AD_FREQUENCY_CATEGORIES
                    loadAds(adsCount, nativeAdKeyList[loadNativeAdAttempt])
                    categoriesAdapter.setList(list)
                }
            }
        }
        viewModel.getCategories()
    }
    private fun loadAds(count: Int, key: String){
        Log.i("RingtoneCategory", "loadAds count $count")
        val builder = AdLoader.Builder(requireContext(), key)
            .forNativeAd { nativeAd ->
                Log.i("RingtoneCategory", "native ad $nativeAd")
                if(isDetached){
                    nativeAd.destroy()
                    return@forNativeAd
                }

                categoriesAdapter.setNativeAd(nativeAd)
            }
            .withAdListener(object : AdListener(){
                override fun onAdLoaded() {
                    super.onAdLoaded()

                    Log.i("RingtoneCategory", "onAdLoaded")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.i("RingtoneCategory", "nativeAd failed ${p0.message}, attempt $loadNativeAdAttempt")
                    loadNativeAdAttempt++
                    if (loadNativeAdAttempt <= nativeAdKeyList.size - 1){
                        try {
                            loadAds(adsCount, nativeAdKeyList[loadNativeAdAttempt])
                        }catch (r: Exception){

                        }
                    }
                }
            })
            .build()
        val request = AdRequest.Builder()
            .build()
        builder.loadAds(request, count)
    }
    private fun createList(list: List<RingtoneCategory>): List<RingtoneCategoryRecyclerItem>{
        val result = arrayListOf<RingtoneCategoryRecyclerItem>()
        Log.i("RingtoneFr", "recycler LIst $result")
        result.addAll(list.map { RingtoneCategoryRecyclerItem(it.name, it.ringtones, it.link, false) })
        var adCount = 0
        for (i in 1..list.lastIndex){
            if(i%(Constants.AD_FREQUENCY_CATEGORIES) == 0){
                result.add(index = i+adCount, RingtoneCategoryRecyclerItem(isAdvertising = true, name = "", ringLink = "", array = emptyList()))
                adCount++
            }
        }
        Log.i("RingtoneFr", "recycler LIst $result")
        return result
    }
}