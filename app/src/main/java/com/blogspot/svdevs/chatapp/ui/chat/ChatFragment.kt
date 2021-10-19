package com.blogspot.svdevs.chatapp.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewbinding.ViewBinding
import com.blogspot.svdevs.chatapp.databinding.FragmentChannelBinding
import com.blogspot.svdevs.chatapp.databinding.FragmentChatBinding
import com.blogspot.svdevs.chatapp.ui.BindingFragment
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory

@AndroidEntryPoint
class ChatFragment: BindingFragment<FragmentChatBinding>() {
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentChatBinding::inflate

    private val args: ChatFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = MessageListViewModelFactory(args.channelId)

        //ViewModel for header
        val msgListHeaderViewModel: MessageListHeaderViewModel by viewModels{ factory }

        //ViewModel for message list
        val msgListViewModel: MessageListViewModel by viewModels { factory }

        //ViewModel for input messages
        val msgInputViewModel: MessageInputViewModel by viewModels { factory }

        //binding
        msgListHeaderViewModel.bindView(binding.messageListHeaderView,viewLifecycleOwner)
        msgListViewModel.bindView(binding.messageListView,viewLifecycleOwner)
        msgInputViewModel.bindView(binding.messageInputView, viewLifecycleOwner)

        //Thread messages vs normal messages
        msgListViewModel.mode.observe(viewLifecycleOwner){mode ->
            when(mode){
                is MessageListViewModel.Mode.Thread -> {
                    msgListHeaderViewModel.setActiveThread(mode.parentMessage)
                    msgInputViewModel.setActiveThread(mode.parentMessage)
                }
                is MessageListViewModel.Mode.Normal -> {
                    msgListHeaderViewModel.resetThread()
                    msgInputViewModel.resetThread()
                }
            }
        }

        // Edit sent message and resend it again
        binding.messageListView.setMessageEditHandler(msgInputViewModel::postMessageToEdit)

        //navigate back functionality
        msgListViewModel.state.observe(viewLifecycleOwner){ state ->
            if(state is MessageListViewModel.State.NavigateUp){
                findNavController().navigateUp()
            }
        }
        val backHandler = {
            msgListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
        binding.messageListHeaderView.setBackButtonClickListener(backHandler)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            backHandler()
        }

    }


}