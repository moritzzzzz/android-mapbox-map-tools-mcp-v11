package com.example.mcp_tools_demo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mcp_tools_demo.models.ChatMessage

/**
 * RecyclerView adapter for displaying chat messages.
 *
 * Shows user messages aligned right (green) and assistant messages aligned left (gray).
 */
class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {
    private val messages = mutableListOf<ChatMessage>()

    /**
     * Adds a message to the chat.
     */
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    /**
     * Clears all messages.
     */
    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userMessageContainer: LinearLayout = itemView.findViewById(R.id.userMessageContainer)
        private val userMessageText: TextView = itemView.findViewById(R.id.userMessageText)
        private val assistantMessageContainer: LinearLayout = itemView.findViewById(R.id.assistantMessageContainer)
        private val assistantMessageText: TextView = itemView.findViewById(R.id.assistantMessageText)

        fun bind(message: ChatMessage) {
            if (message.isUser) {
                // Show user message (right side, green)
                userMessageContainer.visibility = View.VISIBLE
                userMessageText.text = message.text
                assistantMessageContainer.visibility = View.GONE
            } else {
                // Show assistant message (left side, gray)
                assistantMessageContainer.visibility = View.VISIBLE
                assistantMessageText.text = message.text
                userMessageContainer.visibility = View.GONE
            }
        }
    }
}
