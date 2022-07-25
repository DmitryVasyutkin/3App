package io.mobidoo.a3app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.gms.ads.nativead.NativeAd
import io.mobidoo.a3app.databinding.ItemWallpaperRecyclerBinding
import io.mobidoo.a3app.databinding.LayoutAdCollectionsBinding
import io.mobidoo.a3app.databinding.LayoutWallCategoryAdBinding
import io.mobidoo.a3app.entity.startcollectionitem.ResizedGridLayoutManager
import io.mobidoo.a3app.entity.startcollectionitem.SelectedCategoryWallpapersFragment
import io.mobidoo.a3app.entity.startcollectionitem.WallpaperRecyclerItem

class WallpaperRecyclerItemAdapter(private val onClick: (String) -> (Unit)) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = arrayListOf<WallpaperRecyclerItem>()
    private val adsList = arrayListOf<NativeAd>()

    fun setList(newList: List<WallpaperRecyclerItem>) {
        list.clear()
        list.addAll(newList)
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
        return if(viewType == 0) WallpapersViewHolder(
            ItemWallpaperRecyclerBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )else AdvertisingViewHolder(LayoutWallCategoryAdBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is WallpapersViewHolder) holder.onBind(list[position])
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

    inner class WallpapersViewHolder(private val binding: ItemWallpaperRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: WallpaperRecyclerItem) {
            val wallAdapter = WallpaperAdapter(onClick)
            binding.rvSelectedCategoryWallpapers.apply {
                layoutManager = ResizedGridLayoutManager(itemView.context, SelectedCategoryWallpapersFragment.widthScale, SelectedCategoryWallpapersFragment.heightToWidthScale, 3)
                adapter = wallAdapter
            }
            wallAdapter.setList(item.list)
        }
    }
    inner class AdvertisingViewHolder(private val binding: LayoutWallCategoryAdBinding): RecyclerView.ViewHolder(binding.root){

        fun onBind(recyclerItem: WallpaperRecyclerItem){
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
        nativeAdView.imageView = adViewBinding.adAppIconCollections
        nativeAdView.callToActionView = adViewBinding.btnNativeAdActionCollections

        adViewBinding.adHeadlineCollections.text = nativeAd.body ?: nativeAd.headline
        if(nativeAd.mediaContent != null){
            adViewBinding.adAppIconCollections.load(nativeAd.mediaContent?.mainImage)
        }else{
            adViewBinding.adAppIconCollections.load(nativeAd.icon?.uri)
        }
        if(nativeAd.callToAction != null)
            adViewBinding.btnNativeAdActionCollections.text = nativeAd.callToAction

    }
}