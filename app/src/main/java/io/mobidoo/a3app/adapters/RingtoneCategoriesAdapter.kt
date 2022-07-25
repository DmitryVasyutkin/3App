package io.mobidoo.a3app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.gms.ads.nativead.NativeAd
import io.mobidoo.a3app.databinding.ItemRingtoneStartCollectionBinding
import io.mobidoo.a3app.databinding.ItemWallpaperCategoryBinding
import io.mobidoo.a3app.databinding.LayoutAdCollectionsBinding
import io.mobidoo.a3app.databinding.LayoutWallCategoryAdBinding
import io.mobidoo.a3app.entity.startcollectionitem.RingtoneCategoryRecyclerItem
import io.mobidoo.a3app.entity.startcollectionitem.SubCategoryRecyclerItem
import io.mobidoo.a3app.entity.startcollectionitem.WallpaperRecyclerItem
import io.mobidoo.a3app.ui.startfragment.HorizontalLayoutManager
import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import java.util.*

class RingtoneCategoriesAdapter (
    private val onLinkClicked: (String, String) -> (Unit),
    private val onItemClicked: (String, String, Ringtone, Int) -> (Unit)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = arrayListOf<RingtoneCategoryRecyclerItem>()

    fun setList(newLIst: List<RingtoneCategoryRecyclerItem>){
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

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 0) CategoriesViewHolder(
            ItemWallpaperCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        else AdvertisingViewHolder(LayoutWallCategoryAdBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategoriesViewHolder)
            holder.onBind(list[position])
        else if (holder is AdvertisingViewHolder) holder.onBind(list[position])
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
            val ringtoneAdapter = RingtoneItemsAdapter{r, p ->
                onItemClicked(item.ringLink, item.name, r, p)
            }
            binding.rvCategoryWallpapers.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = ringtoneAdapter
            }
            ringtoneAdapter.setList(item.array)
        }
    }
    inner class AdvertisingViewHolder(private val binding: LayoutWallCategoryAdBinding): RecyclerView.ViewHolder(binding.root){

        fun onBind(recyclerItem: RingtoneCategoryRecyclerItem){
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