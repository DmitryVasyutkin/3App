package io.mobidoo.a3app.ui.livewall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.mobidoo.a3app.databinding.FragmentLiveWallpapersBinding
import io.mobidoo.a3app.databinding.FragmentStartCollectionBinding

class LiveWallpaperFragment : Fragment() {

    private var _binding: FragmentLiveWallpapersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLiveWallpapersBinding.inflate(inflater, container, false)
        return binding.root
    }
}