package io.mobidoo.a3app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import io.mobidoo.a3app.R
import io.mobidoo.a3app.databinding.*
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import java.util.*

class RingtoneCategoryItemsAdapter(
    private val onPlayClick: (Ringtone, Int) -> (Unit),
    private val onActionClick: (Ringtone) -> Unit
) :  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = arrayListOf<Ringtone>()

    fun setList(newList: List<Ringtone>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
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
        ) else AdvertisingViewHolder(LayoutAdRingtonesBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        ))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RingtoneViewHolder)
                holder.onBind(list[position], position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(!list[position].isAdvertising) 0 else 1
    }

    inner class RingtoneViewHolder(private val binding: ItemCategoryRingtoneBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: Ringtone, position: Int) {
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
                onActionClick(item)
            }
        }
    }
    inner class AdvertisingViewHolder(binding: LayoutAdRingtonesBinding): RecyclerView.ViewHolder(binding.root)
}