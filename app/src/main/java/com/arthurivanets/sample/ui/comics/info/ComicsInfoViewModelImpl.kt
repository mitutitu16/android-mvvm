/*
 * Copyright 2018 Arthur Ivanets, arthur.ivanets.l@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arthurivanets.sample.ui.comics.info

import android.os.Bundle
import com.arthurivanets.adapster.databinding.ObservableTrackableArrayList
import com.arthurivanets.commons.data.rx.ktx.resultOrError
import com.arthurivanets.commons.ktx.extract
import com.arthurivanets.commons.rx.ktx.typicalBackgroundWorkSchedulers
import com.arthurivanets.sample.adapters.characters.CharacterItem
import com.arthurivanets.sample.adapters.characters.SmallCharacterItem
import com.arthurivanets.sample.domain.entities.Character
import com.arthurivanets.sample.domain.entities.Comics
import com.arthurivanets.sample.domain.repositories.comics.ComicsRepository
import com.arthurivanets.sample.ui.base.AbstractDataLoadingViewModel
import com.arthurivanets.sample.ui.comics.DEFAULT_COMICS_INFO_CHARACTER_LOADING_LIMIT

class ComicsInfoViewModelImpl(
    private val comicsRepository : ComicsRepository
) : AbstractDataLoadingViewModel(), ComicsInfoViewModel {
    
    
    private var comics = Comics()
    
    override val characterItems = ObservableTrackableArrayList<Long, CharacterItem>()
    
    
    override fun onStart() {
        super.onStart()
        
        loadInitialData()
    }
    
    
    override fun onRestoreState(bundle : Bundle) {
        super.onRestoreState(bundle)
        
        bundle.extract(stateExtractor).also {
            comics = it.comics
        }
    }
    
    
    override fun onSaveState(bundle : Bundle) {
        super.onSaveState(bundle)
        
        bundle.saveState(State(comics = comics))
    }
    
    
    override fun onCharacterClicked(item : CharacterItem) {
        dispatchEvent(ComicsInfoViewModelEvents.OpenCharacterInfoScreen(item.itemModel))
    }
    
    
    override fun setComics(comics : Comics) {
        this.comics = comics
    }
    
    
    override fun getComics() : Comics {
        return this.comics
    }
    
    
    private fun loadInitialData() {
        if(characterItems.isEmpty()) {
            loadCharacters()
        }
    }
    
    
    private fun loadCharacters() {
        if(isLoading) {
            return
        }
        
        isLoading = true
    
        comicsRepository.getComicsCharacters(
            comics = comics,
            offset = 0,
            limit = DEFAULT_COMICS_INFO_CHARACTER_LOADING_LIMIT
        )
        .resultOrError()
        .typicalBackgroundWorkSchedulers()
        .subscribe(::onCharactersLoadedSuccessfully, ::onCharacterLoadingFailed)
        .manageLongLivingDisposable()
    }
    
    
    private fun onCharactersLoadedSuccessfully(characters : List<Character>) {
        isLoading = false
        
        characters.forEach { characterItems.addOrUpdate(SmallCharacterItem(it)) }
    }
    
    
    private fun onCharacterLoadingFailed(throwable : Throwable) {
        isLoading = false
    
        // TODO the proper error handling should be done here
        throwable.printStackTrace()
    }
    
    
}