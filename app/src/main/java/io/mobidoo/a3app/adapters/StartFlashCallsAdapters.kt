package io.mobidoo.a3app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import io.mobidoo.a3app.databinding.ItemCollectionFlashcallBinding
import io.mobidoo.a3app.databinding.ItemCollectionWallpaperBinding
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.domain.common.Constants
import io.mobidoo.domain.entities.wallpaper.Wallpaper

class StartFlashCallsAdapters(
    private val onItemClick: (String) -> (Unit)
) : RecyclerView.Adapter<StartFlashCallsAdapters.FlashCallViewHolder>() {

    private val list = arrayListOf<Wallpaper>()

    fun setList(newList: List<Wallpaper>){
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashCallViewHolder {
        return FlashCallViewHolder(ItemCollectionFlashcallBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FlashCallViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class FlashCallViewHolder(private val binding: ItemCollectionFlashcallBinding) : RecyclerView.ViewHolder(binding.root){
        fun onBind(item: Wallpaper){
            binding.ivFlashCallPreview.load(AppUtils.createFullLink(item.previewUrl))
            binding.cvFlashCallRecyclerItem.setOnClickListener {
                onItemClick(item.url)
            }
        }
    }
}