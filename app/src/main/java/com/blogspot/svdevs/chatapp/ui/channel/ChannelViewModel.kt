package com.blogspot.svdevs.chatapp.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val client: ChatClient
) : ViewModel(){

    private val _createChannelEvent = MutableSharedFlow<CreateChannelEvent>()
    val createChannelEvent = _createChannelEvent.asSharedFlow()

    fun createChannel(channelName: String){
        val trimmedChannelName = channelName.trim()
        viewModelScope.launch {
            if(trimmedChannelName.isEmpty()){
                _createChannelEvent.emit(CreateChannelEvent.Error("Invalid Channel Name"))
                return@launch
            }
            val res = client.channel(
                channelType = "messaging",
                channelId = UUID.randomUUID().toString()
            ).create(
                mapOf(
                    "name" to trimmedChannelName,
                )
            ).await()
            if(res.isError){
                _createChannelEvent.emit(CreateChannelEvent.Error(res.error().message?:"Unknown Error"))
                return@launch
            }
            _createChannelEvent.emit(CreateChannelEvent.Success)
        }
    }

    fun logout(){
        client.disconnect()
    }

    fun getUser(): User? {
        return client.getCurrentUser()
    }

    sealed class CreateChannelEvent {
        data class Error(val error: String) : CreateChannelEvent()
        object Success: CreateChannelEvent()
    }

}