package io.mobidoo.a3app.entity.startcollectionitem

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.mobidoo.a3app.adapters.WallpaperRecyclerItemAdapter
import io.mobidoo.a3app.databinding.FragmentSelectedCategoryWallpapersBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.ui.startfragment.StartCollectionFragment
import io.mobidoo.a3app.ui.startfragment.stop
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModel
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModelFactory
import io.mobidoo.domain.common.Constants
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import kotlinx.coroutines.launch
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


    override fun onAttach(context: Context) {
        (activity?.application as Injector).createWallpaperSubComponent().inject(this)
        viewModel = ViewModelProvider(viewModelStore, factory)[WallCategoriesViewModel::class.java]
        super.onAttach(context)
    }

    private var wallpapersAdapter: WallpaperRecyclerItemAdapter? = null

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
        wallpapersAdapter = WallpaperRecyclerItemAdapter()
        lifecycleScope.launch {
            viewModel.wallUiStateFlow.collect(){
                if(!it.isLoading){
                    binding.shimmerSelectedCategoryWallpapers.stop()
                }
                wallpapersAdapter?.setList(createList(it.array))
            }
        }
        binding.rvSelectedCategoryRecyclerItem.apply{
            layoutManager = LinearLayoutManager(requireContext())
            adapter = wallpapersAdapter
        }
        arguments?.getString(StartCollectionFragment.ARG_LINK)?.let { viewModel.getWallpapers(it) }
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

class ResizedGridLayoutManager(context: Context, private val widthScale: Float, private val heightToWidthScale: Int, count: Int) : GridLayoutManager(context, count){

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        lp?.width = (width * widthScale).toInt()
        lp?.height = (width * 0.30).toInt() * heightToWidthScale
        return true
    }
}