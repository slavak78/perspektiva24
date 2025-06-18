package ru.crmkurgan.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.crmkurgan.main.R
import ru.crmkurgan.main.items.Review

class ReviewAdapter(private val reviewList: ArrayList<Review>, context: Context) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {
    private val context1 = context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.row_review, parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = reviewList[position]
        Glide.with(context1)
            .load(item.imageUrl)
            .error(R.drawable.ic_profile)
            .placeholder(R.drawable.ic_profile)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.mPhoto)
        holder.mReview.text = item.review
        holder.mAgo.text = item.date
        holder.mName.text = item.name
        val rating1 = holder.rating
        try {
            rating1.rating = item.rating.toFloat()
        } catch (e: Exception) {
            rating1.rating = 0.0F
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mPhoto: ImageView = itemView.findViewById(R.id.photo)
        val mReview: TextView = itemView.findViewById(R.id.review)
        val mAgo: TextView = itemView.findViewById(R.id.ago)
        val mName: TextView = itemView.findViewById(R.id.from)
        val rating: RatingBar = itemView.findViewById(R.id.rating_package)
    }
}