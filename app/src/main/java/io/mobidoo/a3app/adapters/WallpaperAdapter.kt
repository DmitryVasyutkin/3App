package io.mobidoo.a3app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import io.mobidoo.a3app.databinding.ItemSelectedCatWallpapersBinding
import io.mobidoo.a3app.entity.startcollectionitem.WallpaperRecyclerItem
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import java.nio.file.Files.createLink

class WallpaperAdapter(private val onClick: (String) -> (Unit)) : RecyclerView.Adapter<WallpaperAdapter.WallpapersViewHolder>() {

    private val list = arrayListOf<Wallpaper>()

    fun setList(newList: List<Wallpaper>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpapersViewHolder {
        return WallpapersViewHolder(
            ItemSelectedCatWallpapersBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: WallpapersViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class WallpapersViewHolder(private val binding: ItemSelectedCatWallpapersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: Wallpaper) {
            binding.ivWallpaperPreviewSelectedCat.load(createFullLink(item.previewUrl))
            binding.cvWallpaperPreviewSelectedCat.setOnClickListener {
                onClick(item.url)
            }
        }
    }
}