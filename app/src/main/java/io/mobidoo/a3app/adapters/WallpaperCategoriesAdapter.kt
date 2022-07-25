package io.mobidoo.a3app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.gms.ads.nativead.NativeAd
import io.mobidoo.a3app.databinding.ItemWallpaperCategoryBinding
import io.mobidoo.a3app.databinding.LayoutAdCollectionsBinding
import io.mobidoo.a3app.databinding.LayoutWallCategoryAdBinding
import io.mobidoo.a3app.entity.startcollectionitem.SubCategoryRecyclerItem
import io.mobidoo.a3app.ui.startfragment.HorizontalLayoutManager
import io.mobidoo.a3app.ui.startfragment.StartCollectionFragment
import io.mobidoo.domain.common.Constants
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import java.util.*

class WallpaperCategoriesAdapter(
    private val onLinkClicked: (String, String) -> (Unit),
    private val onItemClicked: (Wallpaper) -> (Unit)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = arrayListOf<SubCategoryRecyclerItem>()
    private val adsList = arrayListOf<NativeAd>()
    fun setList(newLIst: List<SubCategoryRecyclerItem>){
        list.clear()
        list.addAll(newLIst)
        notifyDataSetChanged()
    }
    fun setNativeAd(nativeAd: NativeAd){
        list.forEachIndexed { i, item ->
            if (item.isAdvertising && item.nativeAd == null){
                item.nativeAd = nativeAd
                notifyItemChanged(i)
                return@forEachIndexed
            }
        }
        adsList.add(nativeAd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 0) CategoriesViewHolder(ItemWallpaperCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else AdvertisingViewHolder(LayoutWallCategoryAdBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategoriesViewHolder)
            holder.onBind(list[position])
        else if(holder is AdvertisingViewHolder)
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
            val wallsAdapter = StartWallpaperCollectionsAdapter{
                it.categoryName = item.name
                onItemClicked(it)
            }
            binding.tvCategoryName.text = item.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
            binding.rvCategoryWallpapers.apply {
                layoutManager = HorizontalLayoutManager(itemView.context, 0.33f, Constants.WALLS_HEIGHT_TO_WIDTH_DIMENSION)
                adapter = wallsAdapter
            }
            wallsAdapter.setList(item.array)
            binding.ibGetAllWalls.setOnClickListener {
                onLinkClicked(item.wallLink, item.name)
            }
        }
    }
    inner class AdvertisingViewHolder(private val binding: LayoutWallCategoryAdBinding): RecyclerView.ViewHolder(binding.root){

        fun onBind(recyclerItem: SubCategoryRecyclerItem){
            if (recyclerItem.nativeAd != null){
                val adBinding = LayoutAdCollectionsBinding.inflate((LayoutInflater.from(itemView.context)))
                recyclerItem.nativeAd?.let { populateNativeAdView(it, adBinding) }
                binding.frameLayoutAdWallCategory.removeAllViews()
                binding.frameLayoutAdWallCategory.addView(adBinding.root)
            }
        }
    }


    private fun populateNativeAdView(nativeAd: NativeAd, adViewBinding: LayoutAdCollectionsBinding){
        val nativeAdView = adViewBinding.root

        nativeAdView.bodyView = adViewBinding.adHeadlineCollections
        nativeAdView.iconView = adViewBinding.adAppIconCollections
        nativeAdView.callToActionView = adViewBinding.btnNativeAdActionCollections

        adViewBinding.adHeadlineCollections.text = nativeAd.body ?: nativeAd.headline
        if (nativeAd.mediaContent != null){
            adViewBinding.adAppIconCollections.load(nativeAd.mediaContent?.mainImage)
        }else{
            adViewBinding.adAppIconCollections.load(nativeAd.icon?.uri)
        }
        if(nativeAd.callToAction != null)
            adViewBinding.btnNativeAdActionCollections.text = nativeAd.callToAction

    }
}