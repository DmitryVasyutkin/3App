package io.mobidoo.a3app.ui.ringtone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.mobidoo.a3app.databinding.FragmentRingtonesBinding
import io.mobidoo.a3app.databinding.FragmentStartCollectionBinding

class RingtoneFragment : Fragment() {

    private var _binding: FragmentRingtonesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRingtonesBinding.inflate(inflater, container, false)
        return binding.root
    }
}