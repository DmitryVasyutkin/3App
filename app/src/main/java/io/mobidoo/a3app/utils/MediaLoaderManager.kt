package io.mobidoo.a3app.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import io.mobidoo.a3app.R
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.domain.entities.ringtone.Ringtone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*

class MediaLoadManager(
    private val contentResolver: ContentResolver,
    private val resources: Resources,
    private var listener: FileDownloaderListener?
) {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun downloadStaticWallpaper(url: String, subFolder: String){
        withContext(Dispatchers.IO){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val imageCollection = MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
                val relativeLocation = "${Environment.DIRECTORY_PICTURES}/${resources.getString(R.string.app_name)}/$subFolder"
                val contentDetails = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, url.getFileName())
                    put(MediaStore.Images.Media.RELATIVE_PATH, relativeLocation)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val contentUri = contentResolver.insert(imageCollection, contentDetails)
                contentUri?.let {
                    var input: InputStream? = null
                    var output: OutputStream? = null
                    var count = 0
                    try {
                        val url = URL(url)
                        val connection: URLConnection = url.openConnection()
                        input = BufferedInputStream(url.openStream())
                        connection.connect()
                        output = contentResolver.openOutputStream(it)
                        val data = ByteArray(1000000)
                        var total: Long = 0
                        while (input.read(data).also { count = it } !== -1) {
                            total += count
                            output?.write(data, 0, count)
                        }
                    }catch (e: Exception){
                        Log.i("MediaLoaderManager", "loading error $e")
                        e.message?.let { it1 -> listener?.error(it1) }
                    }
                    contentDetails.clear()
                    contentDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(it, contentDetails, null, null)
                    listener?.success(contentUri.toString())
                }
            }else{
                downloadStaticWallpaperOld(url, subFolder)
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun downloadStaticWallpaperOld(url: String, subFolder: String) {
        val extDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        val outDir = File(extDir + File.separator + resources.getString(R.string.app_name) + File.separator + subFolder)
        if (!outDir.exists()){
            outDir.mkdirs()
        }

        var newFile = File(outDir, url.getFileName())
        var inputStream: InputStream? = null
        var output: OutputStream? = null
        var count = 0
        try {
            val url = URL(url)
            val connection: URLConnection = url.openConnection()
            inputStream = BufferedInputStream(url.openStream())
            connection.connect()
            output = FileOutputStream(newFile)
            val data = ByteArray(1000000)
            var total: Long = 0
            while (inputStream.read(data).also { count = it } !== -1) {
                total += count
                output.write(data, 0, count)
            }

        } catch (e: Exception) {
            Log.e("MediaLoaderManager", "exc $e")
        } finally {
            listener?.success(newFile.absolutePath)
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(output)
        }
    }
    suspend fun downloadLiveWallpaper(url: String, subFolder: String, isFlashCall: Boolean){
        withContext(Dispatchers.IO){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val imageCollection = MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
                val relativeLocation = if (!isFlashCall)
                    "${Environment.DIRECTORY_PICTURES}/${resources.getString(R.string.app_name)}/${resources.getString(R.string.live_)}/$subFolder"
                else "${Environment.DIRECTORY_PICTURES}/${resources.getString(R.string.app_name)}/${resources.getString(R.string.flash_calls)}"
                val contentDetails = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, url.getFileName())
                    put(MediaStore.Video.Media.RELATIVE_PATH, relativeLocation)
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val contentUri = contentResolver.insert(imageCollection, contentDetails)
                contentUri?.let {
                    var input: InputStream? = null
                    var output: OutputStream? = null
                    var count = 0
                    try {
                        val url = URL(url)
                        val connection: URLConnection = url.openConnection()
                        input = BufferedInputStream(url.openStream())
                        connection.connect()
                        output = contentResolver.openOutputStream(it)
                        val data = ByteArray(1000000)
                        var total: Long = 0
                        while (input.read(data).also { count = it } !== -1) {
                            total += count
                            output?.write(data, 0, count)
                        }
                    }catch (e: Exception){
                        Log.i("MediaLoaderManager", "loading error $e")
                        e.message?.let { it1 -> listener?.error(it1) }
                    }
                    contentDetails.clear()
                    contentDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(it, contentDetails, null, null)
                    listener?.success(contentUri.toString())
                }
            }else{
                downloadLiveWallpaperOld(url, subFolder)
            }
        }
    }
    private suspend fun downloadLiveWallpaperOld(url: String, subFolder: String) {
        val extDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        val outDir = File(extDir + File.separator + resources.getString(R.string.app_name) + File.separator + resources.getString(R.string.live_)+ File.separator + subFolder)
        if (!outDir.exists()){
            outDir.mkdirs()
        }

        var newFile = File(outDir, url.getFileName())
        var inputStream: InputStream? = null
        var output: OutputStream? = null
        var count = 0
        try {
            val url = URL(url)
            val connection: URLConnection = url.openConnection()
            inputStream = BufferedInputStream(url.openStream())
            connection.connect()
            output = FileOutputStream(newFile)
            val data = ByteArray(1000000)
            var total: Long = 0
            while (inputStream.read(data).also { count = it } !== -1) {
                total += count
                output.write(data, 0, count)
            }

        } catch (e: Exception) {
            Log.e("MediaLoaderManager", "exc $e")
        } finally {
            listener?.success(newFile.absolutePath)
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(output)
        }
    }

    suspend fun downloadRingtone(ringtone: Ringtone, subFolder: String, fileDownloaderListener: FileDownloaderListener){
        listener = fileDownloaderListener
        withContext(Dispatchers.IO){
            val url = createFullLink(ringtone.url)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val imageCollection = MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
                val relativeLocation = "${Environment.DIRECTORY_RINGTONES}/${resources.getString(R.string.app_name)}/$subFolder"
                val contentDetails = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, ringtone.url.getFileName())
                    put(MediaStore.Audio.Media.RELATIVE_PATH, relativeLocation)
                    put(MediaStore.MediaColumns.TITLE, ringtone.title)
                    put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
                    put(MediaStore.Audio.Media.IS_RINGTONE, true)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val contentUri = contentResolver.insert(imageCollection, contentDetails)
                contentUri?.let {
                    var input: InputStream? = null
                    var output: OutputStream? = null
                    var count = 0
                    try {
                        val url = URL(url)
                        val connection: URLConnection = url.openConnection()
                        input = BufferedInputStream(url.openStream())
                        connection.connect()
                        output = contentResolver.openOutputStream(it)
                        val data = ByteArray(1000000)
                        var total: Long = 0
                        while (input.read(data).also { count = it } !== -1) {
                            total += count
                            output?.write(data, 0, count)
                        }
                    }catch (e: Exception){
                        Log.i("MediaLoaderManager", "loading error $e")
                        e.message?.let { it1 -> listener?.error(it1) }
                    }
                    contentDetails.clear()
                    contentDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(it, contentDetails, null, null)
                    listener?.success(contentUri.toString())
                }
            }else{
                downloadRingtoneOld(url, subFolder)
            }
        }
    }

    private suspend fun downloadRingtoneOld(url: String, subFolder: String){
        val extDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).absolutePath
        val outDir = File(extDir + File.separator + resources.getString(R.string.app_name) +  File.separator + subFolder)
        if (!outDir.exists()){
            outDir.mkdirs()
        }

        var newFile = File(outDir, url.getFileName())
        var inputStream: InputStream? = null
        var output: OutputStream? = null
        var count = 0
        try {
            val url = URL(url)
            val connection: URLConnection = url.openConnection()
            inputStream = BufferedInputStream(url.openStream())
            connection.connect()
            output = FileOutputStream(newFile)
            val data = ByteArray(1000000)
            var total: Long = 0
            while (inputStream.read(data).also { count = it } !== -1) {
                total += count
                output.write(data, 0, count)
            }

        } catch (e: Exception) {
            Log.e("MediaLoaderManager", "exc $e")
        } finally {
            listener?.success(newFile.absolutePath)
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(output)
        }
    }

    suspend fun extractAudio(context: Context, videoUri: String, action: (String) -> Unit){
        val destPath = ringFile(context).path
        try{

            val videoFd = contentResolver.openFileDescriptor(Uri.parse(videoUri), "w")?.fileDescriptor
            AudioUtils().genVideoUsingMuxer(videoFd, destPath, -1, -1, true, false)
        }catch (e: Exception){
            Log.e("MediaLoaderManager", "exc $e")
        }finally {
            withContext(Dispatchers.Main){
                destPath.let { action(it) }
            }

        }
    }
    private fun outputDir(context: Context) : File {

        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else context.filesDir
    }

    fun ringFile(context: Context) =
        File(outputDir(context),
            SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS", Locale.US
            ).format(System.currentTimeMillis()) + ".mp3"
        )

}
fun String.getFileName() : String{
    return this.split("/").last()
}
interface FileDownloaderListener{
    suspend fun success(path: String)
    fun error(message: String)
}