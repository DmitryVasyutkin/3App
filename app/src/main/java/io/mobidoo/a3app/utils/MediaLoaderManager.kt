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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URL
import java.net.URLConnection

class MediaLoadManager(
    private val contentResolver: ContentResolver,
    private val resources: Resources,
    private val listener: FileDownloaderListener
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
                        e.message?.let { it1 -> listener.error(it1) }
                    }
                    contentDetails.clear()
                    contentDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(it, contentDetails, null, null)
                    listener.success(contentUri.toString())
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
            listener.success(newFile.absolutePath)
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(output)
        }
    }
    suspend fun downloadLiveWallpaper(url: String, subFolder: String){
        withContext(Dispatchers.IO){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val imageCollection = MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
                val relativeLocation = "${Environment.DIRECTORY_PICTURES}/${resources.getString(R.string.app_name)}/${resources.getString(R.string.live_)}/$subFolder"
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
                        e.message?.let { it1 -> listener.error(it1) }
                    }
                    contentDetails.clear()
                    contentDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(it, contentDetails, null, null)
                    listener.success(contentUri.toString())
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
            listener.success(newFile.absolutePath)
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(output)
        }
    }
}
fun String.getFileName() : String{
    return this.split("/").last()
}
interface FileDownloaderListener{
    suspend fun success(path: String)
    fun error(message: String)
}