package io.mobidoo.a3app.services

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class LiveWallpaperService : WallpaperService() {

    companion object{
        const val PREFERENCES_WAL_URI = "pref_wall_uri"
    }

    override fun onCreateEngine(): Engine {
        return LiveWallEngine()
    }

    override fun onDestroy() {
        Log.i("WallService", "onDestroy")
        super.onDestroy()
    }

    inner class LiveWallEngine(): Engine(){
        private var mediaPlayer: MediaPlayer? = null
        private var exoPlayer: ExoPlayer

        init {
            val sharedPreferences = baseContext.getSharedPreferences("APP_PREFS", MODE_PRIVATE)

            val uri = Uri.parse(sharedPreferences.getString(PREFERENCES_WAL_URI, ""))
//            mediaPlayer = MediaPlayer()
//            mediaPlayer?.setDataSource(baseContext, uri)
//            mediaPlayer?.prepare()
//            mediaPlayer?.isLooping = true
            exoPlayer = ExoPlayer.Builder(baseContext)
                .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                .build()
            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer.volume = 0f
            exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
            exoPlayer.prepare()

        }


        override fun onSurfaceCreated(holder: SurfaceHolder) {
            exoPlayer.setVideoSurface(holder.surface)
            exoPlayer.volume = 0f
            exoPlayer.play()
//            mediaPlayer?.setSurface(holder.surface)
//            mediaPlayer?.start()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
//            playheadTime = mediaPlayer?.currentPosition!!
//            mediaPlayer?.reset()
//            mediaPlayer?.release()
//            Log.i("WallService", "onSurfaceDestroyed")
            exoPlayer.release()

        }


    }
}