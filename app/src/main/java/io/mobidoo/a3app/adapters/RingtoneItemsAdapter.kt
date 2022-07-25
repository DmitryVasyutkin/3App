package io.mobidoo.a3app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import io.mobidoo.a3app.databinding.ItemCollectionWallpaperBinding
import io.mobidoo.a3app.databinding.ItemRingtoneStartCollectionBinding
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.domain.entities.ringtone.Ringtone
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import java.util.*

class RingtoneItemsAdapter(
    private val onClick: (Ringtone, Int) -> (Unit)
) :  RecyclerView.Adapter<RingtoneItemsAdapter.RingtoneViewHolder>() {

    private val list = arrayListOf<Ringtone>()

    fun setList(newList: List<Ringtone>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RingtoneItemsAdapter.RingtoneViewHolder {
        return RingtoneViewHolder(
            ItemRingtoneStartCollectionBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: RingtoneItemsAdapter.RingtoneViewHolder, position: Int) {
        holder.onBind(list[position], position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class RingtoneViewHolder(private val binding: ItemRingtoneStartCollectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: Ringtone, position: Int) {

            binding.ivRingtoneStartItem.load(createFullLink(item.imageUrl))
            binding.tvRingtoneTitleStart.text = item.title.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
            binding.cvRingtoneStartItem.setOnClickListener{
                onClick(item, position)
            }
        }
    }
}