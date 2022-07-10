package io.mobidoo.a3app.ui.startfragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.mobidoo.a3app.R
import io.mobidoo.a3app.adapters.WallpaperCategoriesAdapter
import io.mobidoo.a3app.databinding.FragmentWallpaperCategoriesBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.entity.startcollectionitem.SubCategoryRecyclerItem
import io.mobidoo.a3app.entity.uistate.allcollectionstate.WallCategoriesUIState
import io.mobidoo.a3app.ui.WallpaperActivity
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModel
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModelFactory
import io.mobidoo.domain.common.Constants
import io.mobidoo.domain.common.Constants.WALLS_HEIGHT_TO_WIDTH_DIMENSION
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import kotlinx.coroutines.launch
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
        binding.tvCategoriesName.text = arguments?.getString(StartCollectionFragment.ARG_NAME)
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
        }
        binding.ibBackCategories.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun openWallpaper(it: Wallpaper) {
        startActivity(
            WallpaperActivity.getIntent(
                requireActivity(), it.url, it.categoryName?: resources.getString(R.string.common_folder),
                AppUtils.getWallpaperTypeFromLink(it.url)
            )
        )
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
        categoriesAdapter?.setList(list)
    }

    private fun getAllWalls(link: String, name: String){
        binding.rvWallCategories.findNavController().navigate(R.id.action_wallCategoriesFragment_to_selectedCategoryWallpapersFragment, Bundle().apply {
            putString(StartCollectionFragment.ARG_NAME, name)
            putString(StartCollectionFragment.ARG_LINK, link)
        })
    }
}