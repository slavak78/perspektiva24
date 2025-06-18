package ru.crmkurgan.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.crmkurgan.main.R
import ru.crmkurgan.main.items.Notification

class NotificationAdapter(private val notificationList: ArrayList<Notification>, context: Context) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    private val context1 = context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.row_notification, parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notificationList[position]
        Glide.with(context1)
            .load(item.imageUrl)
            .error(R.drawable.ic_profile)
            .placeholder(R.drawable.ic_profile)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.mPhoto)
        holder.mNotification.text = item.notification
        holder.mAgo.text = item.date
        holder.mName.text = item.name
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mPhoto: ImageView = itemView.findViewById(R.id.photo)
        val mNotification: TextView = itemView.findViewById(R.id.notification)
        val mAgo: TextView = itemView.findViewById(R.id.ago)
        val mName: TextView = itemView.findViewById(R.id.from)
    }
}