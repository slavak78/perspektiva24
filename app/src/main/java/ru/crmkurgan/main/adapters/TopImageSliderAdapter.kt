package ru.crmkurgan.main.adapters

import android.content.Context
import android.view.*
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.items.Image


class TopImageSliderAdapter(private val itemList: List<Image>, context: Context) :
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
                R.layout.row_video_image, parent,
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

    private fun onBindVideo(holder: RecyclerView.ViewHolder, image: Image, position: Int) {
        val holder1 = holder as ViewHolderVideo
        val poster1 = holder1.poster
        Glide.with(context1)
            .load(image.thumbnail)
            .error(R.drawable.ic_logo)
            .placeholder(R.drawable.ic_logo)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(poster1)
        holder1.video.setOnClickListener {
            val bundle = bundleOf(
                Pair(Constants.ID, image.id),
                Pair(Constants.TABLE, image.table),
                Pair(Constants.POSITION, position)
            )
            it.findNavController().navigate(
                R.id.action_propertyFragment_to_sliderFragment,
                bundle
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder.itemViewType) {
            2 -> onBindImage(holder, itemList[position], context1, position)
            1 -> onBindVideo(holder, itemList[position], position)
            else -> throw IllegalArgumentException()
        }


    private fun onBindImage(holder: RecyclerView.ViewHolder, image: Image, context: Context, position: Int) {
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
            val bundle = bundleOf(
                Pair(Constants.ID, image.id),
                Pair(Constants.TABLE, image.table),
                Pair(Constants.POSITION, position)
            )
            it.findNavController().navigate(
                R.id.action_propertyFragment_to_sliderFragment,
                bundle
            )
        }
    }

    class ViewHolderImage(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.photo)
    }

    class ViewHolderVideo(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val video: CardView = itemView.findViewById(R.id.video)
        val poster: ImageView = itemView.findViewById(R.id.poster)
    }
}
