package com.example.momentia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NotificationAdapter(private val notifications: MutableList<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {

    inner class NotifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostThumb: ImageView = itemView.findViewById(R.id.ivPostThumb)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val viewUnreadDot: View = itemView.findViewById(R.id.viewUnreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotifViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        val notif = notifications[position]
        holder.tvMessage.text = notif.message
        holder.tvTime.text = getRelativeTime(notif.createdAt)
        holder.tvType.text = when (notif.type) {
            "like" -> "Like"; else -> ""
        }
        if (notif.postImage.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(notif.postImage).centerCrop()
                .placeholder(android.R.color.darker_gray).into(holder.ivPostThumb)
        }
        if (notif.read) {
            holder.itemView.background =
                holder.itemView.context.getDrawable(R.drawable.bg_notif_unread)
            holder.viewUnreadDot.visibility = View.GONE
        } else {
            holder.itemView.background =
                holder.itemView.context.getDrawable(R.drawable.bg_notif_unread)
            holder.viewUnreadDot.visibility = View.VISIBLE
        }
    }


    override fun getItemCount() = notifications.size

    private fun getRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000
        return when {
            minutes < 1 -> "ahora"
            minutes < 60 -> "hace ${minutes}min"
            hours < 24 -> "hace ${hours}h"
            days == 1L -> "ayer"
            days < 7 -> "hace ${days}d"
            else -> "hace ${days / 7}sem"
        }
    }

}