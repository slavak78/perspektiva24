package ru.crmkurgan.main.utils

import androidx.recyclerview.widget.DiffUtil
import ru.crmkurgan.main.items.Notification

class NotificationsDiffUtilCallback(private val oldList: ArrayList<Notification>?, private val newList: ArrayList<Notification>?) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList!!.size
    }

    override fun getNewListSize(): Int {
        return newList!!.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldNotification: Notification = oldList!![oldItemPosition]
        val newNotification: Notification = newList!![newItemPosition]
        return oldNotification.id == newNotification.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldNotification: Notification = oldList!![oldItemPosition]
        val newNotification: Notification = newList!![newItemPosition]
        return (oldNotification.imageUrl == newNotification.imageUrl
                && oldNotification.notification == newNotification.notification
                && oldNotification.date == newNotification.date)
    }
}