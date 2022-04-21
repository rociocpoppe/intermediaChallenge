package com.intermedia.challenge.ui.characters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intermedia.challenge.data.models.Character
import com.intermedia.challenge.data.models.NetResult
import com.intermedia.challenge.data.repositories.CharactersRepository
import kotlinx.coroutines.launch

class CharactersViewModel(private val charactersRepository: CharactersRepository) : ViewModel() {

    private val _characters = MutableLiveData<List<Character>>()
    val characters: LiveData<List<Character>> get() = _characters

    init {
        loadCharacters(0)
    }

    private fun loadCharacters(offset: Int) {
        viewModelScope.launch {
            when (val response = charactersRepository.getCharacters(offset)) {
                is NetResult.Success -> {
                    _characters.postValue(response.data.charactersList.characters)
                }
                is NetResult.Error -> {
                    // TODO complete
                }
            }
        }
    }

    fun loadMoreCharacters() {
        // TODO complete
    }
}