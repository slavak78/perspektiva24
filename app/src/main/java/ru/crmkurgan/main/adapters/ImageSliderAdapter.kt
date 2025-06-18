package ru.crmkurgan.main.adapters

import android.content.Context
import android.view.*
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import player.Jzvd
import player.JzvdStd
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.items.Image


class ImageSliderAdapter(private val itemList: List<Image>, context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val context1 = context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {
        2 -> ViewHolderImage(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_header_banner, parent,
                false
            )
        )
        1 -> ViewHolderVideo(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_video, parent,
                false
            )
        )
        else -> throw IllegalArgumentException()
    }

    override fun getItemViewType(position: Int): Int {
        return itemList[position].type
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    private fun onBindVideo(holder: RecyclerView.ViewHolder, image: Image) {
        val holder1 = holder as ViewHolderVideo
        val video1 = holder1.video
        Jzvd.WIFI_TIP_DIALOG_SHOWED = true
        video1.setUp(image.imageUrl, "")
        Glide.with(context1)
            .load(image.thumbnail)
            .error(R.drawable.ic_logo)
            .placeholder(R.drawable.ic_logo)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(video1.posterImageView)
        video1.posterImageView.setOnClickListener {
            val fg = ""
            val j = fg
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder.itemViewType) {
            2 -> onBindImage(holder, itemList[position], context1)
            1 -> onBindVideo(holder, itemList[position])
            else -> throw IllegalArgumentException()
        }


    private fun onBindImage(holder: RecyclerView.ViewHolder, image: Image, context: Context) {
        val url = image.imageUrl
        val holder1 = holder as ViewHolderImage
        val photo = holder1.photo
        Glide.with(context)
            .load(url)
            .error(R.drawable.ic_logo)
            .placeholder(R.drawable.ic_logo)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(photo)
        photo.setOnClickListener {
            if (image.id != "") {
                val bundle = bundleOf(
                    Pair(Constants.ID, image.id),
                    Pair(Constants.TABLE, image.table)
                )
                it.findNavController().navigate(
                    R.id.action_homeFragment_to_propertyFragment,
                    bundle
                )
            }
        }
    }

    class ViewHolderImage(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.photo)
    }

    class ViewHolderVideo(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val video: JzvdStd = itemView.findViewById(R.id.video)
    }
}
