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
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import io.mobidoo.a3app.R
import io.mobidoo.a3app.adapters.StartFlashCallsAdapters
import io.mobidoo.a3app.adapters.StartWallpaperCollectionsAdapter
import io.mobidoo.a3app.databinding.FragmentStartCollectionBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.entity.uistate.allcollectionstate.AllCollectionsUIState
import io.mobidoo.a3app.ui.MainActivity
import io.mobidoo.a3app.viewmodels.MainActivityViewModel
import io.mobidoo.a3app.viewmodels.MainActivityViewModelFactory
import io.mobidoo.domain.common.Constants.WALLS_HEIGHT_TO_WIDTH_DIMENSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.observeOn
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

    private var uiState: AllCollectionsUIState? = null

    private var uiUpdatesJob: Job? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeRecyclers()
        binding.btnSeeAllLive.setOnClickListener(this)
        binding.btnSeeAllNew.setOnClickListener(this)
        binding.btnSeeAllAbstract.setOnClickListener(this)
        binding.btnSeeAllAll.setOnClickListener(this)
        binding.btnSeeAllCalm.setOnClickListener(this)
        binding.btnSeeAllPopular.setOnClickListener(this)
        binding.btnSeeAllFlash.setOnClickListener(this)
        uiUpdatesJob = lifecycleScope.launchWhenResumed {
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



    private fun handleUIState(it: AllCollectionsUIState) {

        it.live?.startArray?.let { it1 ->
            liveWallsAdapter.setList(it1)
            Log.i(TAG, "liveList $it1")
        }
        it.flashCall?.startArray?.let { it1 ->
            flashCallAdapter.setList(it1)
            Log.i(TAG, "flashList $it1")
        }
        it.new?.startArray?.let { it1 -> newWallsAdapter.setList(it1) }
        it.all?.startArray?.let { it1 -> allWallsAdapter.setList(it1) }
        it.popular?.startArray?.let { it1 -> popularWallsAdapter.setList(it1) }
        it.pattern?.startArray?.let { it1 -> calmWallsAdapter.setList(it1) }
        it.abstract?.startArray?.let { it1 -> abstractWallsAdapter.setList(it1) }

    }

    private fun initializeRecyclers() {
        liveWallsAdapter = StartWallpaperCollectionsAdapter()
        flashCallAdapter = StartFlashCallsAdapters()
        newWallsAdapter = StartWallpaperCollectionsAdapter()
        allWallsAdapter = StartWallpaperCollectionsAdapter()
        popularWallsAdapter = StartWallpaperCollectionsAdapter()
        calmWallsAdapter = StartWallpaperCollectionsAdapter()
        abstractWallsAdapter = StartWallpaperCollectionsAdapter()
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

    override fun onResume() {
        super.onResume()
        viewModel.getStartCollection()
    }

    override fun onStop() {
        uiUpdatesJob?.cancel()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(p0: View?) {
        when(p0){
            binding.btnSeeAllLive -> {
                if(uiState != null){
                    p0.findNavController().navigate(R.id.action_startCollectionFragment_to_wallCategoriesFragment, Bundle().apply {
                        putString(ARG_NAME, resources.getString(R.string.liveCategories))
                        putSerializable(ARG_LINK, uiState?.live?.linkForFull)
                    })
                }
            }
            binding.btnSeeAllAbstract -> {
                if(uiState != null){
                    //go to wallpapers
                }
            }
            binding.btnSeeAllAll -> {
                if(uiState != null){
                    p0.findNavController().navigate(R.id.action_startCollectionFragment_to_wallCategoriesFragment, Bundle().apply {
                        putString(ARG_NAME, resources.getString(R.string.allCategories))
                        putSerializable(ARG_LINK, uiState?.all?.linkForFull)
                    })
                }
            }
            binding.btnSeeAllNew -> {
                if(uiState != null){
                    p0.findNavController().navigate(R.id.action_startCollectionFragment_to_wallCategoriesFragment, Bundle().apply {
                        putString(ARG_NAME, resources.getString(R.string.newCategories))
                        putSerializable(ARG_LINK, uiState?.new?.linkForFull)
                    })
                }
            }
            binding.btnSeeAllCalm -> {
                if(uiState != null){
                    //go to wallpapers
                }
            }
            binding.btnSeeAllPopular -> {
                if(uiState != null){
                    p0.findNavController().navigate(R.id.action_startCollectionFragment_to_wallCategoriesFragment, Bundle().apply {
                        putString(ARG_NAME, resources.getString(R.string.popularCategories))
                        putSerializable(ARG_LINK, uiState?.popular?.linkForFull)
                    })
                }
            }
            binding.btnSeeAllFlash -> {

            }
        }
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