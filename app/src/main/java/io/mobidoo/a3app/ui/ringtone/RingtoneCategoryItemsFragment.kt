package io.mobidoo.a3app.ui.ringtone

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.exoplayer2.ExoPlayer
import io.mobidoo.a3app.R
import io.mobidoo.a3app.adapters.RingtoneCategoryItemsAdapter
import io.mobidoo.a3app.databinding.ExoPlaybackControlViewLayoutBinding
import io.mobidoo.a3app.databinding.FragmentRingtoneCategoryItemsBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.entity.uistate.ringtonestate.RingtonesUIState
import io.mobidoo.a3app.ui.RingtoneCategoryItemsActivity
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.a3app.viewmodels.RingtonesViewModel
import io.mobidoo.a3app.viewmodels.RingtonesViewModelFactory
import io.mobidoo.domain.common.Constants
import io.mobidoo.domain.entities.ringtone.Ringtone
import kotlinx.coroutines.*
import javax.inject.Inject

class RingtoneCategoryItemsFragment : Fragment() {

    private var _binding: FragmentRingtoneCategoryItemsBinding? = null
    private val binding get() = _binding!!

    private var controlBinding: ExoPlaybackControlViewLayoutBinding? = null

    @Inject lateinit var factory: RingtonesViewModelFactory
    private lateinit var viewModel: RingtonesViewModel

    private var link = ""
    private var name = ""

    private lateinit var ringtonesAdapter: RingtoneCategoryItemsAdapter

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaPlayer: MediaPlayer
    private var audioFullDuration = 0
    private var audioTrackingJob: Job? = null
    private var currentPlayingPosition = -1
    private var playedRingtone: Ringtone? = null

    override fun onAttach(context: Context) {
        ((activity as RingtoneCategoryItemsActivity).application as Injector).createRingtoneSubComponent().inject(this)
        viewModel = ViewModelProvider(viewModelStore, factory)[RingtonesViewModel::class.java]
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRingtoneCategoryItemsBinding.inflate(inflater, container, false)
        controlBinding = ExoPlaybackControlViewLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        link = requireActivity().intent?.extras?.getString(RingtoneCategoryItemsActivity.EXTRA_RINGTONE_LINK).toString()
        name = requireActivity().intent?.extras?.getString(RingtoneCategoryItemsActivity.EXTRA_RINGTONE_CATEGORY_NAME).toString()
        binding.tvRingtonesToolbar.text = name

//        exoPlayer = ExoPlayer.Builder(requireContext())
//            .build()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener {
            playRingtone()
        }
        mediaPlayer.setOnCompletionListener {
            stopRingtone()
        }
        binding.seekBarAudioControl.setOnSeekBarChangeListener(seekBarChangeListener)
        ringtonesAdapter = RingtoneCategoryItemsAdapter({ playRingtone, position ->
            try {
                if (mediaPlayer.isPlaying && currentPlayingPosition == position){
                    pauseRingtone()
                }else if(!mediaPlayer.isPlaying && currentPlayingPosition == position){
                    playRingtone()
                }else if(mediaPlayer.isPlaying && currentPlayingPosition != position){
                    pauseRingtone()
                    currentPlayingPosition = position
                    setAudioControlTitles(playRingtone)
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(requireContext(), Uri.parse(createFullLink(playRingtone.url)))
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare()
                }else{
                    currentPlayingPosition = position
                    setAudioControlTitles(playRingtone)
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(requireContext(), Uri.parse(createFullLink(playRingtone.url)))
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare()
                }
            }catch (e: Exception){
                mediaPlayer.release()
                stopRingtone()
                Toast.makeText(requireContext().applicationContext, "playback error, try again", Toast.LENGTH_SHORT).show()
            }


        }, { actionRington ->

        })

        binding.ibPlayRingtoneControls.setOnClickListener {
            if (mediaPlayer.isPlaying){
                pauseRingtone()
            }else{
                playRingtone()
            }
        }

        binding.rvRingtones.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ringtonesAdapter
        }

        lifecycleScope.launch {
            viewModel.ringtonesState.collect(){
                refreshUi(it)
            }
        }
        binding.ibBackRingtones.setOnClickListener {
            activity?.onBackPressed()
        }
        viewModel.getRingtones(link)
    }

    private fun setAudioControlTitles(playRingtone: Ringtone) {
        binding.tvRingtoneTitle1Control.text = playRingtone.title
        binding.tvRingtoneTitle2Control.text = playRingtone.url.split("/").last().split(".").last()
        binding.ivRingtonesCategoryControl.load(createFullLink(playRingtone.imageUrl))
        binding.ivRingtonesCategory.load(createFullLink(playRingtone.imageUrl))
    }

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

        }

        override fun onStartTrackingTouch(p0: SeekBar?) {

        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
            if (mediaPlayer.isPlaying){
                mediaPlayer.seekTo(p0?.progress?.times(100) ?: 0)
            }
        }
    }

    private fun showAudioProgressFrom(progr: Int){
        Log.i("RingtoneCat", "showAudioProgress")
        audioTrackingJob = lifecycleScope.launch(Dispatchers.IO) {
            var count = audioFullDuration
            while (count > 0){
                delay(1000)
                Log.i("RingtoneCat", "(mediaPlayer.currentPosition + 1000) = ${(mediaPlayer.currentPosition + 1000)}")
                withContext(Dispatchers.Main) {
                    if ((mediaPlayer.currentPosition + 1000) < audioFullDuration) {
                        binding.seekBarAudioControl.progress = (mediaPlayer.currentPosition + 1000) / 100
                    } else {
                        binding.seekBarAudioControl.progress = audioFullDuration
                    }
                }
                count-=1000
            }
        }
    }

    private fun playRingtone(){
        mediaPlayer.start()
        audioFullDuration = mediaPlayer.duration
        binding.seekBarAudioControl.max = audioFullDuration/100
        binding.ibPlayRingtoneControls.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_pause_ringtone, null))
        ringtonesAdapter.setItemPlayed(currentPlayingPosition)
        showAudioProgressFrom(0)
    }

    private fun pauseRingtone(){
        mediaPlayer.pause()
        audioTrackingJob?.cancel()
        ringtonesAdapter.setItemPaused(currentPlayingPosition)
        binding.ibPlayRingtoneControls.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_play_ringtone, null))
    }

    private fun stopRingtone(){
        mediaPlayer.stop()
        audioTrackingJob?.cancel()
        binding.seekBarAudioControl.progress = 0
        binding.ibPlayRingtoneControls.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_play_ringtone, null))
    }

    private fun refreshUi(uiState: RingtonesUIState) {
        if(uiState.ringtones.isNotEmpty()){
            val ringtone = uiState.ringtones.first()
            binding.ivRingtonesCategory.load(createFullLink(ringtone.imageUrl))
            controlBinding?.ivRingtonesCategoryControl?.load(createFullLink(ringtone.imageUrl))
            controlBinding?.tvRingtoneTitle1Control?.text = ringtone.title
            controlBinding?.tvRingtoneTitle2Control?.text = ringtone.url.split("/").last().split(".").last()
        }

        ringtonesAdapter.setList(createList(uiState.ringtones))
    }

    private fun createList(list: List<Ringtone>): List<Ringtone>{
        val result = arrayListOf<Ringtone>()
        result.addAll(list)
        var adCount = 0
        for (i in 1..list.lastIndex){
            if(i%(Constants.AD_FREQUENCY_CATEGORIES) == 0){
                result.add(index = i+adCount, Ringtone(isAdvertising = true))
                adCount++
            }
        }
        return result
    }



    override fun onDetach() {
      //  exoPlayer.release()
        if (mediaPlayer.isPlaying){
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        super.onDetach()
    }
}

















