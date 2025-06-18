package ru.crmkurgan.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.crmkurgan.main.Constants
import ru.crmkurgan.main.items.Chat
import ru.crmkurgan.main.R

class ChatAdapter(private val chatList: ArrayList<Chat>, context: Context) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private val context1 = context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.row_chat_list, parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfilePicture: ImageView = itemView.findViewById(R.id.ivProfilePicture)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvTimeOfMessage: TextView = itemView.findViewById(R.id.tvTimeOfMessage)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val toPersonChat: RelativeLayout = itemView.findViewById(R.id.toPersonChat)
        val readied: ImageView = itemView.findViewById(R.id.readied)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = chatList[position]
        Glide.with(context1)
            .load(item.avatar)
            .error(R.drawable.ic_profile)
            .placeholder(R.drawable.ic_profile)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.ivProfilePicture)
        holder.tvUserName.text = item.name
        holder.tvTimeOfMessage.text = item.date
        holder.tvMessage.text = item.message
        val readied1 = holder.readied
        if(item.last==0) {
            readied1.setImageDrawable(context1.getDrawable(if (item.readied==0) R.drawable.ic_check2 else if (item.readied==1) R.drawable.ic_check2_all else R.drawable.ic_check2_all_readied))
        } else {
            readied1.setImageDrawable(null)
        }
        holder.toPersonChat.setOnClickListener {
            val bundle = bundleOf(
                Pair(Constants.AGENT, item.user_id)
            )
            it.findNavController().navigate(
                R.id.goto_chat,
                bundle
            )
        }
    }
}
