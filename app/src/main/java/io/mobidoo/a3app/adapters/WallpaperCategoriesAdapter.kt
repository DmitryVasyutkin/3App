package io.mobidoo.a3app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.mobidoo.a3app.databinding.ItemWallpaperCategoryBinding
import io.mobidoo.a3app.databinding.LayoutAdCollectionsBinding
import io.mobidoo.a3app.entity.startcollectionitem.SubCategoryRecyclerItem
import io.mobidoo.a3app.ui.startfragment.HorizontalLayoutManager
import java.util.*

class WallpaperCategoriesAdapter(private val onLinkClicked: (String, String) -> (Unit)) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = arrayListOf<SubCategoryRecyclerItem>()

    fun setList(newLIst: List<SubCategoryRecyclerItem>){
        list.clear()
        list.addAll(newLIst)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 0) CategoriesViewHolder(ItemWallpaperCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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

        fun onBind(item: SubCategoryRecyclerItem){
            val wallsAdapter = StartWallpaperCollectionsAdapter()
            binding.tvCategoryName.text = item.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
            binding.rvCategoryWallpapers.apply {
                layoutManager = HorizontalLayoutManager(itemView.context, 0.33f, 2)
                adapter = wallsAdapter
            }
            wallsAdapter.setList(item.array)
            binding.ibGetAllWalls.setOnClickListener {
                onLinkClicked(item.wallLink, item.name)
            }
        }
    }
    inner class AdvertisingViewHolder(binding: LayoutAdCollectionsBinding): RecyclerView.ViewHolder(binding.root)
}