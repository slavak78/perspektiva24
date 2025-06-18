package ru.crmkurgan.main.adapters

import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.R
import ru.crmkurgan.main.items.Image
import ru.crmkurgan.main.views.TouchImageView


class CenteredImageSliderAdapter(private val itemList: List<Image>) :
    PagerAdapter() {

    override fun getCount(): Int {
        return itemList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val context = container.context
        val inflater = LayoutInflater.from(context)
        val view: View
        val itemList1 = itemList
        if (itemList1[position].type == 2) {
            view = inflater.inflate(
                R.layout.row_centered_image, container,
                false
            )
            val photo = view.findViewById<TouchImageView>(R.id.photo)
            Glide.with(context)
                .load(itemList1[position].imageUrl)
                .error(R.drawable.ic_logo)
                .placeholder(R.drawable.ic_logo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(photo)
        } else {
            view = inflater.inflate(
                R.layout.row_centered_video, container,
                false
            )
            val poster = view.findViewById<ImageView>(R.id.poster)
            val video = view.findViewById<RelativeLayout>(R.id.video)
            Glide.with(context)
                .load(itemList1[position].thumbnail)
                .error(R.drawable.ic_logo)
                .placeholder(R.drawable.ic_logo)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(poster)
            video.setOnClickListener {
                val bundle = bundleOf(
                    Pair(Constants.VIDEO, itemList1[position].imageUrl),
                    Pair(Constants.THUMBNAIL, itemList1[position].thumbnail)
                )
                it.findNavController().navigate(
                    R.id.action_sliderFragment_to_videoFragment,
                    bundle
                )
            }
        }
        container.addView(view)
        return view
    }
}
