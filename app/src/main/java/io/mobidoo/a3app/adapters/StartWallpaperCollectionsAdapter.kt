package io.mobidoo.a3app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import io.mobidoo.a3app.databinding.ItemCollectionFlashcallBinding
import io.mobidoo.a3app.databinding.ItemCollectionWallpaperBinding
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.domain.common.Constants.TYPE_FLASH_CALL
import io.mobidoo.domain.entities.wallpaper.Wallpaper

class StartWallpaperCollectionsAdapter : RecyclerView.Adapter<StartWallpaperCollectionsAdapter.WallpapersViewHolder>() {

    private val list = arrayListOf<Wallpaper>()

    fun setList(newList: List<Wallpaper>){
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpapersViewHolder {
        return WallpapersViewHolder(ItemCollectionWallpaperBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    }

    override fun onBindViewHolder(holder: WallpapersViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class WallpapersViewHolder(private val binding: ItemCollectionWallpaperBinding) : RecyclerView.ViewHolder(binding.root){
        fun onBind(item: Wallpaper){
            Log.i("RvAdapter", "wall holder")
            binding.ivWallpaperPreview.load(createFullLink(item.previewUrl))
        }
    }
}

