package io.mobidoo.a3app.ui.flashcall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.mobidoo.a3app.databinding.FragmentFlashcallsBinding
import io.mobidoo.a3app.databinding.FragmentStartCollectionBinding

class FlashCallFragment : Fragment() {

    private var _binding: FragmentFlashcallsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFlashcallsBinding.inflate(inflater, container, false)
        return binding.root
    }
}