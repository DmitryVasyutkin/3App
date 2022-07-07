package io.mobidoo.a3app.ui.startfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.mobidoo.a3app.databinding.FragmentStartCollectionBinding
import io.mobidoo.a3app.databinding.FragmentWallpaperCategoriesBinding

class WallCategoriesFragment : Fragment() {

    private var _binding: FragmentWallpaperCategoriesBinding? = null
    private val binding get() = _binding!!


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
        binding.tvCategoriesName.text = arguments?.getString("name")
    }
}