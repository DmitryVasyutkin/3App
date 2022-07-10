package io.mobidoo.a3app.ui.flashcall

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.mobidoo.a3app.R
import io.mobidoo.a3app.adapters.FlashCallRecyclerItemAdapter
import io.mobidoo.a3app.adapters.WallpaperRecyclerItemAdapter
import io.mobidoo.a3app.databinding.FragmentFlashcallsBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.entity.startcollectionitem.WallpaperRecyclerItem
import io.mobidoo.a3app.ui.FlashCallPreviewActivity
import io.mobidoo.a3app.ui.startfragment.stop
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.a3app.viewmodels.MainActivityViewModel
import io.mobidoo.a3app.viewmodels.MainActivityViewModelFactory
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModel
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModelFactory
import io.mobidoo.domain.common.Constants
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import kotlinx.coroutines.launch
import javax.inject.Inject


class FlashCallCollectionFragment : Fragment() {

    private var _binding: FragmentFlashcallsBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerItemAdapter: FlashCallRecyclerItemAdapter
    @Inject lateinit var factory: WallCategoriesViewModelFactory
    private lateinit var viewModel: WallCategoriesViewModel

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
        _binding = FragmentFlashcallsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerItemAdapter = FlashCallRecyclerItemAdapter {
            openFlashCall(it)
        }
        binding.rvAllFlashCalls.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recyclerItemAdapter
        }
        viewModel.getWallpapers(createFullLink(AppUtils.flashCallSubsUrl))
        lifecycleScope.launch {
            viewModel.wallUiStateFlow.collect(){
                if (it.isLoading){
                    binding.shimmerFlashCallWallpapers.startShimmer()
                }
                else{
                    binding.shimmerFlashCallWallpapers.stop()
                }
                recyclerItemAdapter.setList(createList(it.array))
            }
        }
    }

    private fun openFlashCall(url: String) {
        startActivity(FlashCallPreviewActivity.getIntent(requireActivity(), url, resources.getString(
            R.string.flash_calls)))
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
}