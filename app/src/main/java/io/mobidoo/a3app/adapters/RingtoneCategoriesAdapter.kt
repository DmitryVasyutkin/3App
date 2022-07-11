package io.mobidoo.a3app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.mobidoo.a3app.databinding.ItemRingtoneStartCollectionBinding
import io.mobidoo.a3app.databinding.ItemWallpaperCategoryBinding
import io.mobidoo.a3app.databinding.LayoutAdCollectionsBinding
import io.mobidoo.a3app.entity.startcollectionitem.RingtoneCategoryRecyclerItem
import io.mobidoo.a3app.entity.startcollectionitem.SubCategoryRecyclerItem
import io.mobidoo.a3app.ui.startfragment.HorizontalLayoutManager
import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import java.util.*

class RingtoneCategoriesAdapter (
    private val onLinkClicked: (String, String) -> (Unit),
    private val onItemClicked: (Ringtone) -> (Unit)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = arrayListOf<RingtoneCategoryRecyclerItem>()

    fun setList(newLIst: List<RingtoneCategoryRecyclerItem>){
        list.clear()
        list.addAll(newLIst)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 0) CategoriesViewHolder(
            ItemWallpaperCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        else AdvertisingViewHolder(LayoutAdCollectionsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategoriesViewHolder)
            holder.onBind(list[position])
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

    inner class CategoriesViewHolder(private val binding: ItemWallpaperCategoryBinding): RecyclerView.ViewHolder(binding.root){

        fun onBind(item: RingtoneCategoryRecyclerItem){
            binding.tvCategoryName.text = item.name
            binding.ibGetAllWalls.setOnClickListener {
                onLinkClicked(item.ringLink, item.name)
            }
            val ringtoneAdapter = RingtoneItemsAdapter(onItemClicked)
            binding.rvCategoryWallpapers.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = ringtoneAdapter
            }
            ringtoneAdapter.setList(item.array)
        }
    }
    inner class AdvertisingViewHolder(binding: LayoutAdCollectionsBinding): RecyclerView.ViewHolder(binding.root)
}