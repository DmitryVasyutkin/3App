package io.mobidoo.a3app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.gms.ads.nativead.NativeAd
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.*
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import java.util.*

data class RingtoneApp(
    val url: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val nameCategory: String ="",
    val linkToFullCategory: String = "",
    val isAdvertising: Boolean = false,
    var isPlayedNow: Boolean = false,
    var nativeAd: NativeAd? = null
)

class RingtoneCategoryItemsAdapter(
    private val onPlayClick: (RingtoneApp, Int) -> (Unit),
    private val onActionClick: (RingtoneApp, Int) -> Unit
) :  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = arrayListOf<RingtoneApp>()

    fun setList(newList: List<RingtoneApp>) {
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
    }

    fun setItemPlayed(position: Int){
        list.forEachIndexed { index, ringtone ->
            if (index == position){
                ringtone.isPlayedNow = true
                notifyItemChanged(position)
                return@forEachIndexed
            }
        }
    }

    fun setItemPaused(position: Int){
        list.forEachIndexed { index, ringtone ->
            if (index == position){
                ringtone.isPlayedNow = false
                notifyItemChanged(position)
                return@forEachIndexed
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if(viewType == 0) RingtoneViewHolder(
            ItemCategoryRingtoneBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        ) else AdvertisingViewHolder(LayoutWallCategoryAdBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        ))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RingtoneViewHolder)
                holder.onBind(list[position], position)
        else if(holder is AdvertisingViewHolder) holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(!list[position].isAdvertising) 0 else 1
    }

    inner class RingtoneViewHolder(private val binding: ItemCategoryRingtoneBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: RingtoneApp, position: Int) {
            val resources = itemView.resources
            if (!item.isPlayedNow){
                binding.ibPlayRingtone.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_play_ringtone, null))
            }else{
                binding.ibPlayRingtone.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_pause_ringtone, null))
            }
            binding.ivRingtonesCategory.load(createFullLink(item.imageUrl))
            binding.tvRingtoneTitle1.text = item.title
            binding.tvRingtoneTitle2.text = item.url.split("/").last().split(".").last()
            binding.ibPlayRingtone.setOnClickListener {
                onPlayClick(item, position)
            }
            binding.ibRingtoneAction.setOnClickListener {
                onActionClick(item, position)
            }
        }
    }

    inner class AdvertisingViewHolder(private val binding: LayoutWallCategoryAdBinding): RecyclerView.ViewHolder(binding.root){

        fun onBind(item: RingtoneApp){
            if (item.isAdvertising){
                val adBinding = LayoutAdRingtonesBinding.inflate((LayoutInflater.from(itemView.context)))
                item.nativeAd?.let { populateNativeAdView(it, adBinding) }
                binding.frameLayoutAdWallCategory.removeAllViews()
                binding.frameLayoutAdWallCategory.addView(adBinding.root)
            }
        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adViewBinding: LayoutAdRingtonesBinding){
        val nativeAdView = adViewBinding.root

        nativeAdView.bodyView = adViewBinding.adHeadlineRingtone
        nativeAdView.iconView = adViewBinding.adAppIconRingtone
        nativeAdView.callToActionView = adViewBinding.btnNativeAdRingtoneAction

        adViewBinding.adHeadlineRingtone.text = nativeAd.body ?: nativeAd.headline
        if (nativeAd.mediaContent != null){
            adViewBinding.adAppIconRingtone.load(nativeAd.mediaContent?.mainImage)
        }else{
            adViewBinding.adAppIconRingtone.load(nativeAd.icon?.uri)
        }
        if(nativeAd.callToAction != null)
            adViewBinding.btnNativeAdRingtoneAction.text = nativeAd.callToAction

    }
}