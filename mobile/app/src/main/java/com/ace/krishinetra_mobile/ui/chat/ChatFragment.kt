package com.ace.krishinetra_mobile.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ace.krishinetra_mobile.databinding.FragmentChatBinding
import com.ace.krishinetra_mobile.ui.adapters.ChatAdapter
import com.ace.krishinetra_mobile.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private val chatAdapter = ChatAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeMessages()
        observeTyping()
    }

    private fun setupRecyclerView() {
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        binding.messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        binding.quickQ1.setOnClickListener {
            viewModel.sendQuickQuestion("How often should I water my plants?")
        }
        binding.quickQ2.setOnClickListener {
            viewModel.sendQuickQuestion("What are signs of overwatering?")
        }
        binding.quickQ3.setOnClickListener {
            viewModel.sendQuickQuestion("How to prevent plant diseases?")
        }
        binding.quickQ4.setOnClickListener {
            viewModel.sendQuickQuestion("Best fertilizer for houseplants?")
        }
    }

    private fun sendMessage() {
        val text = binding.messageInput.text.toString().trim()
        if (text.isNotEmpty()) {
            viewModel.sendMessage(text)
            binding.messageInput.setText("")
        }
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.observe(viewLifecycleOwner) { messages ->
                    chatAdapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }
    }

    private fun observeTyping() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isTyping.observe(viewLifecycleOwner) { typing ->
                    binding.quickQuestionsContainer.visibility =
                        if (typing) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
