package io.mobidoo.a3app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.mobidoo.a3app.databinding.ItemWallpaperRecyclerBinding
import io.mobidoo.a3app.databinding.LayoutAdCollectionsBinding
import io.mobidoo.a3app.entity.startcollectionitem.ResizedGridLayoutManager
import io.mobidoo.a3app.entity.startcollectionitem.SelectedCategoryWallpapersFragment
import io.mobidoo.a3app.entity.startcollectionitem.WallpaperRecyclerItem

class FlashCallRecyclerItemAdapter (private val onClick: (String) -> (Unit)) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = arrayListOf<WallpaperRecyclerItem>()

    fun setList(newList: List<WallpaperRecyclerItem>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 0) WallpapersViewHolder(
            ItemWallpaperRecyclerBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )else AdvertisingViewHolder(LayoutAdCollectionsBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is WallpapersViewHolder) holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(list[position].isAdvertising){
            false -> 0
            else -> 1
        }
    }

    inner class WallpapersViewHolder(private val binding: ItemWallpaperRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: WallpaperRecyclerItem) {
            val wallAdapter = StartFlashCallsAdapters{
                onClick(it)
            }
            binding.rvSelectedCategoryWallpapers.apply {
                layoutManager = ResizedGridLayoutManager(itemView.context, SelectedCategoryWallpapersFragment.widthScale, SelectedCategoryWallpapersFragment.heightToWidthScale, 3)
                adapter = wallAdapter
            }
            wallAdapter.setList(item.list)
        }

    }
    inner class AdvertisingViewHolder(binding: LayoutAdCollectionsBinding): RecyclerView.ViewHolder(binding.root)
}