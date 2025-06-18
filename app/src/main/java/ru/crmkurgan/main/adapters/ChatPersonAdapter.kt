package ru.crmkurgan.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.crmkurgan.main.items.Chat
import ru.crmkurgan.main.R

class ChatPersonAdapter(private val chatList: List<Chat>, context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val context1 = context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {
        3 -> ViewHolderDate(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_chat_date, parent,
                false
            )
        )
        2 -> ViewHolderFrom(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_chat_from, parent,
                false
            )
        )
        1 -> ViewHolderTo(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_chat_to, parent,
                false
            )
        )
        else -> throw IllegalArgumentException()
    }

    override fun getItemViewType(position: Int): Int {
        return chatList[position].type
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    private fun onBindTo(holder: RecyclerView.ViewHolder, chat: Chat) {
        val holder1 = holder as ViewHolderTo
        holder1.message.text = chat.message
        holder1.times.text = chat.time
        holder1.readied.setImageDrawable(context1.getDrawable(if (chat.readied==0) R.drawable.ic_check2 else if (chat.readied==1) R.drawable.ic_check2_all else R.drawable.ic_check2_all_readied))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder.itemViewType) {
            3 -> onBindDate(holder, chatList[position])
            2 -> onBindFrom(holder, chatList[position])
            1 -> onBindTo(holder, chatList[position])
            else -> throw IllegalArgumentException()
        }


    private fun onBindFrom(holder: RecyclerView.ViewHolder, chat: Chat) {
        val holder1 = holder as ViewHolderFrom
        holder1.message.text = chat.message
        holder1.times.text = chat.time
    }

    private fun onBindDate(holder: RecyclerView.ViewHolder, chat: Chat) {
        val holder1 = holder as ViewHolderDate
        holder1.date.text = chat.date
    }

    class ViewHolderFrom(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message: TextView = itemView.findViewById(R.id.tvMessage)
        val times: TextView = itemView.findViewById(R.id.tvTimeOfMessage)
    }

    class ViewHolderTo(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message: TextView = itemView.findViewById(R.id.tvMessage)
        val times: TextView = itemView.findViewById(R.id.tvTimeOfMessage)
        val readied: ImageView = itemView.findViewById(R.id.readied)
    }

    class ViewHolderDate(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tvDate)
    }
}
