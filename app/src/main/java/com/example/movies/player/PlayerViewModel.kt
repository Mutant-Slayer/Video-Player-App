package com.example.movies.player

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerViewModel : ViewModel() {
    private val _playerUiModel = MutableStateFlow(PlayerUiModel())
    val playerUiModel = _playerUiModel.asStateFlow()

    fun updatePlayerUiModel(playerUiModel: PlayerUiModel) {
        _playerUiModel.value = playerUiModel
    }
}