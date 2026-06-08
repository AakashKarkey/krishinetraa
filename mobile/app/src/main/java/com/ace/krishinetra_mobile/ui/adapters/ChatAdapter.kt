package com.ace.krishinetra_mobile.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ace.krishinetra_mobile.R
import com.ace.krishinetra_mobile.data.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.MessageViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        private val container: LinearLayout = itemView.findViewById(R.id.messageContainer)

        fun bind(message: ChatMessage) {
            messageText.text = message.text

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            messageTime.text = timeFormat.format(Date(message.timestamp))

            val params = messageText.layoutParams as LinearLayout.LayoutParams

            if (message.isUser) {
                container.gravity = android.view.Gravity.END
                messageText.setBackgroundResource(R.drawable.bg_chat_bubble_user)
                messageText.setTextColor(
                    itemView.context.getColor(android.R.color.white)
                )
                params.marginStart = 80
                params.marginEnd = 0
            } else {
                container.gravity = android.view.Gravity.START
                messageText.setBackgroundResource(R.drawable.bg_chat_bubble_ai)
                messageText.setTextColor(
                    itemView.context.getColor(R.color.gray_800)
                )
                params.marginStart = 0
                params.marginEnd = 80
            }
            messageText.layoutParams = params

            messageTime.gravity =
                if (message.isUser) android.view.Gravity.END else android.view.Gravity.START
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean =
            oldItem == newItem
    }
}
