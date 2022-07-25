package io.mobidoo.a3app.ui.ringtone

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Contacts
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.material.button.MaterialButton
import com.google.common.reflect.Reflection.getPackageName
import io.mobidoo.a3app.R
import io.mobidoo.a3app.adapters.RingtoneCategoryItemsAdapter
import io.mobidoo.a3app.databinding.ExoPlaybackControlViewLayoutBinding
import io.mobidoo.a3app.databinding.FragmentRingtoneActionsBinding
import io.mobidoo.a3app.databinding.FragmentRingtoneCategoryItemsBinding
import io.mobidoo.a3app.di.Injector
import io.mobidoo.a3app.entity.uistate.ringtonestate.RingtonesUIState
import io.mobidoo.a3app.ui.RingtoneCategoryItemsActivity
import io.mobidoo.a3app.ui.bottomsheet.AllowChangeSettingsPermissions
import io.mobidoo.a3app.ui.bottomsheet.RingtoneDownloadedDialog
import io.mobidoo.a3app.ui.wallpaperpreview.WallpaperPreviewFragment
import io.mobidoo.a3app.ui.wallpaperpreview.WallpaperPreviewSuccessFragment
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.a3app.utils.FileDownloaderListener
import io.mobidoo.a3app.utils.MediaLoadManager
import io.mobidoo.a3app.viewmodels.RingtonesViewModel
import io.mobidoo.a3app.viewmodels.RingtonesViewModelFactory
import io.mobidoo.domain.common.Constants
import io.mobidoo.domain.entities.ringtone.Ringtone
import kotlinx.coroutines.*
import javax.inject.Inject

class RingtoneCategoryItemsFragment : Fragment(), View.OnClickListener {

    companion object{
        const val ARG_RINGTONE = "arg_ringtone"
        private const val RC_PERMISSIONS = 1234
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
        private const val BTN_RINGTONE_CALLED = 11
        private const val BTN_NOTIFICATION_CALLED = 12
        private const val BTN_ALARM_CALLED = 13
        private const val STATE_IDLE = -1
        private const val STATE_PLAY = 0
        private const val STATE_PAUSE = 1
        private const val STATE_STOP = 2
    }


    private var _binding: FragmentRingtoneCategoryItemsBinding? = null
    private val binding get() = _binding!!

  //  private var controlBinding: ExoPlaybackControlViewLayoutBinding? = null
    private var _actionBinding: FragmentRingtoneActionsBinding? = null
    private val actionBinding get() = _actionBinding!!
    @Inject lateinit var factory: RingtonesViewModelFactory
    private lateinit var viewModel: RingtonesViewModel

    private var link = ""
    private var name = ""

    private lateinit var ringtonesAdapter: RingtoneCategoryItemsAdapter
    private var playerState = STATE_IDLE
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaPlayer: MediaPlayer
    private var audioFullDuration = 0
    private var audioTrackingJob: Job? = null
    private var currentPlayingPosition = -1
    private var currentPlayingRingtone: Ringtone? = null
    private var currentDuration = 0
    private var mediaLoadManager: MediaLoadManager? = null

    private var actionLayoutShown = false
    private var actionButtonState = -1
    private var downloadedRingtoneUri = ""


    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private var dialogLoadedFragment: RingtoneDownloadedDialog? = null

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
        _actionBinding = FragmentRingtoneActionsBinding.bind(binding.root)
     //   controlBinding = ExoPlaybackControlViewLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("RingtonePermission", "onViewCreated")
        Log.i("RingtonePermission", "actionLayoutShown $actionLayoutShown")
        Log.i("RingtonePermission", "currentPlayingRingtone $currentPlayingRingtone")
        link = requireActivity().intent?.extras?.getString(RingtoneCategoryItemsActivity.EXTRA_RINGTONE_LINK).toString()
        name = requireActivity().intent?.extras?.getString(RingtoneCategoryItemsActivity.EXTRA_RINGTONE_CATEGORY_NAME).toString()
        if (requireActivity().intent?.extras?.getSerializable(RingtoneCategoryItemsActivity.EXTRA_RINGTONE) != null){
            actionLayoutShown = true
            currentPlayingRingtone = requireActivity().intent?.extras?.getSerializable(RingtoneCategoryItemsActivity.EXTRA_RINGTONE)!! as Ringtone
            currentPlayingPosition = requireActivity().intent?.extras?.getInt(RingtoneCategoryItemsActivity.EXTRA_RINGTONE_POS)!!
        }
        binding.tvRingtonesToolbar.text = name
        mediaLoadManager = MediaLoadManager(requireContext().contentResolver, resources,null)
        initializeMediaPlayer()
        binding.seekBarAudioControl.setOnSeekBarChangeListener(seekBarChangeListener)
        initializeRingtonesAdapter()
        binding.ibPlayRingtoneControls.setOnClickListener(this)
        binding.ibBackRingtones.setOnClickListener(this)
        actionBinding.ibRingtonePlayActions.setOnClickListener(this)
        actionBinding.btnSetAsRingtone.setOnClickListener(this)
        actionBinding.btnSetContactRingtone.setOnClickListener(this)
        actionBinding.btnSetAsNotificationSound.setOnClickListener(this)
        actionBinding.btnSetAsAlarm.setOnClickListener(this)
        actionBinding.btnDownloadRingtone.setOnClickListener(this)
        initializeRingtoneRecycler()

        collectUIState()

        viewModel.getRingtones(link)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if(actionLayoutShown){
                        binding.layoutRingtoneActionsFragment.visibility = View.GONE
                        actionLayoutShown = false
                    }else{
                        activity?.finish()
                    }
                }
            }
        )
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
            if (isGranted){
                if (permissionGranted(REQUIRED_PERMISSIONS[2])){
                    openContactPicker()
                }else{
                    requestPermissionLauncher.launch(REQUIRED_PERMISSIONS[2])
                }
            }else{
                when{
                    shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[0]) -> {
                        Log.i("RingtonePermission", "shouldShowRequestPermissionRationale")
                        val fr = AllowChangeSettingsPermissions(){
                            startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                    Uri.parse("package:" + requireContext().packageName)
                                )
                            )
                        }
                        fr.show(activity?.supportFragmentManager?.beginTransaction()!!, "allow_permissions")
                    }
                    else -> {
                        Log.i("RingtonePermission", "launch")

                    }
                }
            }
        }
        if (actionLayoutShown){
            currentPlayingRingtone?.let {
                Log.i("RingtonePermission", "setAudioControlTitles $it")
                setAudioControlTitles(it)
            }
            currentPlayingRingtone?.let {
                Log.i("RingtonePermission", "setActionsLayoutTitle $it")
                setActionsLayoutTitle(it)
            }
            mediaPlayer.reset()
            mediaPlayer.setDataSource(
                requireContext(),
                Uri.parse(currentPlayingRingtone?.url?.let { createFullLink(it) })
            )
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);



            binding.layoutRingtoneActionsFragment.visibility = View.VISIBLE
        }
    }

    override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return !Settings.System.canWrite(requireContext())
    }

    private fun initializeRingtoneRecycler() {
        binding.rvRingtones.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ringtonesAdapter
        }
    }

    private fun collectUIState() {
        lifecycleScope.launch {
            viewModel.ringtonesState.collect() {
                refreshUi(it)
            }
        }
    }


    private fun initializeRingtonesAdapter() {
        ringtonesAdapter = RingtoneCategoryItemsAdapter({ playRingtone, position ->
            try {
                currentPlayingRingtone = playRingtone
//                when(playerState){
//                    STATE_IDLE -> {
//                        currentPlayingPosition = position
//                        setAudioControlTitles(playRingtone)
//                        setActionsLayoutTitle(playRingtone)
//                        mediaPlayer.reset()
//                        mediaPlayer.setDataSource(
//                            requireContext(),
//                            Uri.parse(createFullLink(playRingtone.url))
//                        )
//                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                        mediaPlayer.prepare()
//                    }
//                    STATE_PLAY -> {
//                        if (currentPlayingPosition == position){
//                            pauseRingtone()
//                        }
//                    }
//                    STATE_PAUSE -> {
//                        if (currentPlayingPosition == position){
//                            resumeRingtone(currentDuration)
//                        }else{
//                            currentPlayingPosition = position
//                            setAudioControlTitles(playRingtone)
//                            setActionsLayoutTitle(playRingtone)
//                            mediaPlayer.reset()
//                            mediaPlayer.setDataSource(
//                                requireContext(),
//                                Uri.parse(createFullLink(playRingtone.url))
//                            )
//                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                            mediaPlayer.prepare()
//                        }
//                    }
//                    STATE_STOP -> {
//                        currentPlayingPosition = position
//                        setAudioControlTitles(playRingtone)
//                        setActionsLayoutTitle(playRingtone)
//                        mediaPlayer.reset()
//                        mediaPlayer.setDataSource(
//                            requireContext(),
//                            Uri.parse(createFullLink(playRingtone.url))
//                        )
//                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                        mediaPlayer.prepare()
//                    }
//                }
                currentPlayingRingtone = playRingtone
                if (mediaPlayer.isPlaying && currentPlayingPosition == position) {
                    pauseRingtone()
                } else if (!mediaPlayer.isPlaying && currentPlayingPosition == position) {
                //    resumeRingtone(currentDuration)
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(
                        requireContext(),
                        Uri.parse(createFullLink(playRingtone.url))
                    )
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare()
                } else if (mediaPlayer.isPlaying && currentPlayingPosition != position) {
                    pauseRingtone()
                    currentPlayingPosition = position
                    setAudioControlTitles(playRingtone)
                    setActionsLayoutTitle(playRingtone)
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(
                        requireContext(),
                        Uri.parse(createFullLink(playRingtone.url))
                    )
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare()
                } else {
                    currentPlayingPosition = position
                    setAudioControlTitles(playRingtone)
                    setActionsLayoutTitle(playRingtone)
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(
                        requireContext(),
                        Uri.parse(createFullLink(playRingtone.url))
                    )
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare()
                }
            } catch (e: Exception) {
                mediaPlayer.release()
                stopRingtone()
                Toast.makeText(
                    requireContext().applicationContext,
                    "playback error, try again",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }, { actionRington, position ->

            currentPlayingRingtone = actionRington
            if (currentPlayingPosition != position){
                pauseRingtone()
                currentPlayingPosition = position
                setAudioControlTitles(actionRington)
                setActionsLayoutTitle(actionRington)
                mediaPlayer.reset()
                mediaPlayer.setDataSource(
                    requireContext(),
                    Uri.parse(createFullLink(actionRington.url))
                )
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            }
            actionLayoutShown = true
            binding.layoutRingtoneActionsFragment.visibility = View.VISIBLE
        })
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer()

        mediaPlayer.setOnPreparedListener {
            playRingtone()
        }

        mediaPlayer.setOnCompletionListener {
            stopRingtone()
        }
        mediaPlayer.setOnErrorListener { mediaPlayer, i, i2 ->
            Toast.makeText(requireContext(), "playback error", Toast.LENGTH_SHORT).show()
            return@setOnErrorListener true
        }
    }

    private fun setAudioControlTitles(playRingtone: Ringtone) {
        binding.tvRingtoneTitle1Control.text = playRingtone.title
        binding.tvRingtoneTitle2Control.text = playRingtone.url.split("/").last().split(".").last()
        binding.ivRingtonesCategoryControl.load(createFullLink(playRingtone.imageUrl))
        binding.ivRingtonesCategory.load(createFullLink(playRingtone.imageUrl))
    }

    private fun setActionsLayoutTitle(playRingtone: Ringtone){
        actionBinding.tvRingtoneTitleActions.text = playRingtone.title
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
        Log.i("RingtoneCat", "audioFullDuration $audioFullDuration")
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
                    actionBinding.tvRingtoneDurationActions.text = getDurationText(mediaPlayer.currentPosition)
                }
                count-=1000
            }
        }
    }

    private fun getDurationText(durationMillis: Int): String{
        Log.i("RingtoneCat", "durationMillis $durationMillis")
        var m = durationMillis / 60000
        var s = (durationMillis / 1000)
        Log.i("RingtoneCat", "seconds $s")
        return StringBuilder()
            .append(m.toTimeFormatZeros())
            .append(":")
            .append(s.toTimeFormatZeros())
            .toString()
    }

    private fun playRingtone(){
        playerState = STATE_PLAY
        mediaPlayer.start()
        audioFullDuration = mediaPlayer.duration
        currentDuration = 0
        binding.seekBarAudioControl.max = audioFullDuration/100
        binding.ibPlayRingtoneControls.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_pause_24, null))
        actionBinding.ibRingtonePlayActions.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_pause_ringtone, null))
        ringtonesAdapter.setItemPlayed(currentPlayingPosition)
        showAudioProgressFrom(0)
    }

    private fun resumeRingtone(duration: Int){
        playerState = STATE_PLAY
        Log.i("Ringtone", "resume dur $duration")
        mediaPlayer.seekTo(duration)
        showAudioProgressFrom(duration)
        mediaPlayer.start()
        binding.ibPlayRingtoneControls.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_pause_24, null))
        actionBinding.ibRingtonePlayActions.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_pause_ringtone, null))

    }

    private fun pauseRingtone(){
        playerState = STATE_PAUSE
        currentDuration = mediaPlayer.duration
        mediaPlayer.pause()
        audioTrackingJob?.cancel()
        ringtonesAdapter.setItemPaused(currentPlayingPosition)
        binding.ibPlayRingtoneControls.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_play_arrow_24, null))
        actionBinding.ibRingtonePlayActions.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_play_ringtone, null))
    }

    private fun stopRingtone(){
        playerState = STATE_STOP
        currentDuration = 0
        mediaPlayer.stop()
        audioTrackingJob?.cancel()
        binding.seekBarAudioControl.progress = 0
        binding.ibPlayRingtoneControls.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_play_arrow_24, null))
        actionBinding.ibRingtonePlayActions.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_play_ringtone, null))
    }

    private fun refreshUi(uiState: RingtonesUIState) {
        if(uiState.ringtones.isNotEmpty()){
            if (!actionLayoutShown) currentPlayingPosition = 0
            if (!actionLayoutShown) currentPlayingRingtone = uiState.ringtones.first()
            val ringtone = if(!actionLayoutShown) uiState.ringtones.first() else currentPlayingRingtone
            binding.ivRingtonesCategory.load(createFullLink(ringtone?.imageUrl!!))
            binding.ivRingtonesCategoryControl.load(createFullLink(ringtone.imageUrl))
            binding.tvRingtoneTitle1Control.text = ringtone.title
            binding.tvRingtoneTitle2Control.text = ringtone.url.split("/").last().split(".").last()
        }

        ringtonesAdapter.setList(createList(uiState.ringtones))
    }

    override fun onClick(view: View?) {
        when(view?.id){
            binding.ibPlayRingtoneControls.id -> {
                if (currentPlayingRingtone !=null){
                    if (mediaPlayer.isPlaying) {
                        pauseRingtone()
                    }else{
                        Log.i("RingtoneFragment", "currentPlayingRingtone $currentPlayingRingtone")
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(
                            requireContext(),
                            Uri.parse(createFullLink(currentPlayingRingtone?.url!!))
                        )
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.prepare()
                     //   playRingtone()
                    }
                }

            }
            binding.ibBackRingtones.id -> {
                activity?.onBackPressed()
            }
            actionBinding.ibRingtonePlayActions.id -> {

                if (mediaPlayer.isPlaying) {
                    pauseRingtone()
                }else {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(
                        requireContext(),
                        Uri.parse(createFullLink(currentPlayingRingtone?.url!!))
                    )
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare()
                 //   playRingtone()
                }
            }
            actionBinding.btnSetAsRingtone.id -> {
                if(systemPermissionsGranted()){
                    setAsSystemRingtone()
                }else{
                    requestPermissionLauncher.launch(REQUIRED_PERMISSIONS[0])
                }
            }
            actionBinding.btnSetContactRingtone.id -> {
                if(permissionGranted(REQUIRED_PERMISSIONS[1]) && permissionGranted(
                        REQUIRED_PERMISSIONS[2])){
                    openContactPicker()
                }else {
                    if (!permissionGranted(REQUIRED_PERMISSIONS[1]))
                        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS[1])
                    else
                        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS[2])
                }
            }
            actionBinding.btnSetAsNotificationSound.id -> {

                if (systemPermissionsGranted()) {
                    setAsSystemNotification()
                }else{
                    requestPermissionLauncher.launch(REQUIRED_PERMISSIONS[0])
                }
            }
            actionBinding.btnSetAsAlarm.id -> {

                if (systemPermissionsGranted()) {
                    setAsSystemAlarm()
                }else{
                    requestPermissionLauncher.launch(REQUIRED_PERMISSIONS[0])
                }
            }
            actionBinding.btnDownloadRingtone.id -> {
                downloadRingtone()
            }
        }
    }

    private fun openContactPicker() {
        startActivityForResult(Intent(Intent.ACTION_PICK,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI), 3333)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3333){
            if (resultCode == Activity.RESULT_OK){
                Log.i("RingtoneCont", "data ${data?.data}")
                setAsContactRingtone(data?.data.toString())
            }
        }
    }

    private fun setAsSystemRingtone() {

            lifecycleScope.launch {
                mediaLoadManager?.downloadRingtone(
                    currentPlayingRingtone!!,
                    name,
                    object : FileDownloaderListener {
                        override suspend fun success(path: String) {
                            withContext(Dispatchers.Main) {
                                setDownloadedRingtoneAsDefault(path)
                            }
                        }

                        override fun error(message: String) {

                        }
                    })
            }


    }

    private fun setAsSystemNotification() {

            lifecycleScope.launch {
                mediaLoadManager?.downloadRingtone(
                    currentPlayingRingtone!!,
                    name,
                    object : FileDownloaderListener {
                        override suspend fun success(path: String) {
                            withContext(Dispatchers.Main) {
                                setDownloadedRingtoneAsNotification(path)
                            }
                        }

                        override fun error(message: String) {

                        }
                    })
            }
    }
    private fun setAsSystemAlarm() {

            lifecycleScope.launch {
                mediaLoadManager?.downloadRingtone(
                    currentPlayingRingtone!!,
                    name,
                    object : FileDownloaderListener {
                        override suspend fun success(path: String) {
                            withContext(Dispatchers.Main) {
                                setDownloadedRingtoneAsAlarm(path)
                            }
                        }

                        override fun error(message: String) {

                        }
                    })
            }

    }
    private fun setAsContactRingtone(contUri: String) {

        lifecycleScope.launch {
            mediaLoadManager?.downloadRingtone(
                currentPlayingRingtone!!,
                name,
                object : FileDownloaderListener {
                    override suspend fun success(path: String) {
                        withContext(Dispatchers.Main) {
                            downloadedRingtoneUri = path
                            setDownloadedRingtoneAsContactRing(contUri)
                        }
                    }

                    override fun error(message: String) {

                    }
                })
        }

    }

    private fun setDownloadedRingtoneAsDefault(uri: String){
        RingtoneManager.setActualDefaultRingtoneUri(requireContext(), RingtoneManager.TYPE_RINGTONE, Uri.parse(uri))
        setSuccessButtonIcon(actionBinding.btnSetAsRingtone)
        handleSuccessSaving()
    }
    private fun setDownloadedRingtoneAsNotification(uri: String){
        RingtoneManager.setActualDefaultRingtoneUri(requireContext(), RingtoneManager.TYPE_NOTIFICATION, Uri.parse(uri))
        setSuccessButtonIcon(actionBinding.btnSetAsNotificationSound)
        handleSuccessSaving()
    }
    private fun setDownloadedRingtoneAsAlarm(uri: String){
        RingtoneManager.setActualDefaultRingtoneUri(requireContext(), RingtoneManager.TYPE_ALARM, Uri.parse(uri))
        setSuccessButtonIcon(actionBinding.btnSetAsAlarm)
        handleSuccessSaving()
    }
    private fun setDownloadedRingtoneAsContactRing(uri: String){
        val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY
        )
        val cursor = activity?.contentResolver?.query(Uri.parse(uri), PROJECTION, null, null, null)
        cursor.use { cursor ->
            cursor.use {
                it?.moveToFirst()
                val id = it?.getLong(0)!!
                val lookupKey = it?.getString(1)
                val contactUri = ContactsContract.Contacts.getLookupUri(id, lookupKey)
                Log.i("RingtonePermission", "contactUri $contactUri")
                if (contactUri != null){
                    val cv = ContentValues().apply {
                        put(ContactsContract.Contacts.CUSTOM_RINGTONE, downloadedRingtoneUri)
                    }
                    activity?.contentResolver?.update(contactUri, cv, null, null)
                }
            }
        }
        setSuccessButtonIcon(actionBinding.btnSetContactRingtone)
    }

    private fun downloadRingtone(){
        lifecycleScope.launch {
            mediaLoadManager?.downloadRingtone(
                currentPlayingRingtone!!,
                name,
                object : FileDownloaderListener {
                    override suspend fun success(path: String) {
                        val downloadedPath = "${Environment.DIRECTORY_RINGTONES}/${resources.getString(R.string.app_name)}/$name"
                        withContext(Dispatchers.Main) {
                            dialogLoadedFragment = RingtoneDownloadedDialog({
                                val intent = Intent().apply {
                                    setAction(android.content.Intent.ACTION_VIEW)
                                    setDataAndType(Uri.parse(path), "audio/*")
                                }
                                startActivity(intent)
                            }, currentPlayingRingtone!!, downloadedPath)

                            dialogLoadedFragment?.show(activity?.supportFragmentManager?.beginTransaction()!!, "downloaded_dialog")
                        }
                    }

                    override fun error(message: String) {

                    }
                })
        }
    }

    private fun setSuccessButtonIcon(button: MaterialButton){
        button.setIcon(ResourcesCompat.getDrawable(resources, R.drawable.ic_success_ringtone, null))
    }

    private fun handleSuccessSaving() {
     //   findNavController().popBackStack()
        try {
            findNavController().navigate(R.id.action_ringtoneCategoryItemsFragment_to_ringtoneSuccessFragment, Bundle().apply {
                putString(WallpaperPreviewSuccessFragment.ARG_SAVED_VALUE, resources.getString(R.string.ringtone))
            })
        }catch (e: Exception){

        }

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

    private fun systemPermissionsGranted() = Settings.System.canWrite(requireContext())

    private fun permissionGranted(permission: String):Boolean{
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onPause() {
        super.onPause()
        Log.i("RingtonePermission", "onPause")
        if (mediaPlayer.isPlaying){
            pauseRingtone()
        }
    }

    override fun onResume() {
        super.onResume()

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

fun Int.toTimeFormatZeros(): String{
    return if (this < 10)
        "0$this"
    else this.toString()
}
















