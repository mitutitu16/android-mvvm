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

package com.arthurivanets.sample.ui.events.list

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arthurivanets.adapster.listeners.OnItemClickListener
import com.arthurivanets.mvvm.events.ViewModelEvent
import com.arthurivanets.sample.BR
import com.arthurivanets.sample.R
import com.arthurivanets.sample.adapters.events.EventItem
import com.arthurivanets.sample.adapters.events.EventItemResources
import com.arthurivanets.sample.adapters.events.EventItemViewHolder
import com.arthurivanets.sample.adapters.events.EventItemsRecyclerViewAdapter
import com.arthurivanets.sample.databinding.FragmentEventsBinding
import com.arthurivanets.sample.domain.entities.Event
import com.arthurivanets.sample.ui.base.BaseFragment
import com.arthurivanets.sample.ui.events.info.EventInfoFragment
import com.arthurivanets.sample.ui.events.info.newBundle
import com.arthurivanets.sample.ui.util.extensions.sharedDescriptionTransitionName
import com.arthurivanets.sample.ui.util.extensions.sharedImageTransitionName
import com.arthurivanets.sample.ui.util.extensions.sharedTitleTransitionName
import com.arthurivanets.sample.ui.util.markers.CanScrollToTop
import kotlinx.android.synthetic.main.fragment_events.*
import kotlinx.android.synthetic.main.view_progress_bar_circular.*
import javax.inject.Inject

class EventsFragment : BaseFragment<FragmentEventsBinding, EventsViewModel>(), CanScrollToTop {


    @Inject
    lateinit var localViewModel : EventsViewModel

    @Inject
    lateinit var itemResources : EventItemResources

    private lateinit var adapter : EventItemsRecyclerViewAdapter


    override fun init(savedInstanceState : Bundle?) {
        initRecyclerView()
    }


    private fun initRecyclerView() {
        with(recyclerView) {
            layoutManager = initLayoutManager()
            adapter = initAdapter()
        }
    }


    private fun initLayoutManager() : RecyclerView.LayoutManager {
        return LinearLayoutManager(context)
    }


    private fun initAdapter() : EventItemsRecyclerViewAdapter {
        adapter = EventItemsRecyclerViewAdapter(
            context = context!!,
            items = localViewModel.items,
            resources = itemResources
        )
        adapter.onItemClickListener = OnItemClickListener { _, item, _ -> localViewModel.onEventClicked(item) }

        return adapter
    }
    
    
    override fun postInit() {
        super.postInit()
        
        onLoadingStateChanged(localViewModel.isLoading)
    }
    
    
    override fun scrollToTop(animate : Boolean) {
        if(animate) {
            recyclerView?.smoothScrollToPosition(0)
        } else {
            recyclerView?.scrollToPosition(0)
        }
    }
    
    
    override fun onRegisterObservables() {
        localViewModel.loadingStateHolder.register(::onLoadingStateChanged)
    }
    
    
    private fun onLoadingStateChanged(isLoading : Boolean) {
        progress_bar.isVisible = isLoading
    }


    override fun onViewModelEvent(event : ViewModelEvent<*>) {
        super.onViewModelEvent(event)

        when(event) {
            is EventsViewModelEvents.OpenEventInfoScreen -> event.data?.let(::onOpenEventInfoScreen)
        }
    }
    
    
    private fun onOpenEventInfoScreen(event : Event) {
        val viewHolder = (getItemViewHolder(event) ?: return)
        
        navigate(
            R.id.eventInfoFragmentAction,
            EventInfoFragment.newBundle(event),
            FragmentNavigatorExtras(
                viewHolder.imageIv to event.sharedImageTransitionName,
                viewHolder.titleTv to event.sharedTitleTransitionName,
                viewHolder.descriptionTv to event.sharedDescriptionTransitionName
            )
        )
    }
    
    
    private fun getItemViewHolder(event : Event) : EventItemViewHolder? {
        val index = adapter.indexOf(EventItem(event))
        
        return if(index != -1) {
            (recyclerView.findViewHolderForAdapterPosition(index) as EventItemViewHolder)
        } else {
            null
        }
    }


    override fun getLayoutId() : Int {
        return R.layout.fragment_events
    }


    override fun getBindingVariable() : Int {
        return BR.viewModel
    }


    override fun getViewModel() : EventsViewModel {
        return localViewModel
    }


}