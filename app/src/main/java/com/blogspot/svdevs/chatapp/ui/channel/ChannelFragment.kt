package com.blogspot.svdevs.chatapp.ui.channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.blogspot.svdevs.chatapp.R
import com.blogspot.svdevs.chatapp.databinding.FragmentChannelBinding
import com.blogspot.svdevs.chatapp.ui.BindingFragment
import com.blogspot.svdevs.chatapp.utils.navigateSafe
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ChannelFragment: BindingFragment<FragmentChannelBinding>() {

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentChannelBinding::inflate

    private val viewModel: ChannelViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = viewModel.getUser()
        if (user == null){
            findNavController().popBackStack()
            return
        }

        val factory = ChannelListViewModelFactory(
            filter = Filters.and(
                //Filters.eq("id", "Valhalla"),// filtering by channel id
                Filters.eq("type","messaging"), // filtering by channel type
                //Filters.eq("members", listOf("")) // getting only specific members
            ),
            sort = ChannelListViewModel.DEFAULT_SORT,// sorting the channel's ordering
            limit = 30 // max number of channels that can be created/displayed
        )
        val channelListViewModel: ChannelListViewModel by viewModels() { factory }
        val channelHeaderViewModel: ChannelListHeaderViewModel by viewModels()
        channelListViewModel.bindView(binding.channelListView, viewLifecycleOwner)
        channelHeaderViewModel.bindView(binding.channelListHeaderView,viewLifecycleOwner)

        //adding functionality to the user image icon on the header
        binding.channelListHeaderView.setOnUserAvatarClickListener{
            viewModel.logout()
            findNavController().popBackStack()
        }

        //onClickListener for header view create new channel
        binding.channelListHeaderView.setOnActionButtonClickListener{
            findNavController().navigateSafe(
                R.id.action_channelFragment_to_createChannelDialog
            )
        }

        // onClickListeners to the channel items
        binding.channelListView.setChannelItemClickListener{ channel ->
            findNavController().navigateSafe(
                R.id.action_channelFragment_to_chatFragment,
                Bundle().apply { putString("channelId",channel.cid) }
            )
        }

        //Observe events when the channel is created
        lifecycleScope.launchWhenStarted {
            viewModel.createChannelEvent.collect { event ->
                when(event){
                    is ChannelViewModel.CreateChannelEvent.Error -> {
                       Toast.makeText(requireContext(),event.error,Toast.LENGTH_SHORT).show()
                    }
                    is ChannelViewModel.CreateChannelEvent.Success -> {
                        Toast.makeText(requireContext(), R.string.channel_created, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}