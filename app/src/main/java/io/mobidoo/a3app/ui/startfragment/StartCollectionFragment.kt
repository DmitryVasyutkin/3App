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
import io.mobidoo.a3app.databinding.FragmentStartCollectionBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.ui.MainActivity
import io.mobidoo.a3app.viewmodels.MainActivityViewModel
import io.mobidoo.a3app.viewmodels.MainActivityViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.observeOn
import javax.inject.Inject

class StartCollectionFragment : Fragment() {

    companion object{
        const val TAG = "StartCollectionFragment"
    }
    @Inject
    lateinit var factory: MainActivityViewModelFactory
    private lateinit var viewModel: MainActivityViewModel

    private var _binding: FragmentStartCollectionBinding? = null
    private val binding get() = _binding!!

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

        uiUpdatesJob = lifecycleScope.launchWhenResumed {
            viewModel.uiStateFlow.collect(){
                Log.i(TAG, "uiState $it")
            }
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
}