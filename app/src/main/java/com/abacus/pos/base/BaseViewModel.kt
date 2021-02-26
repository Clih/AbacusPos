package com.abacus.pos.base

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abacus.pos.event.SimpleViewEvent
import com.abacus.pos.event.ViewEvent
import com.abacus.pos.utils.LogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created by lin on 20-4-15.
 */
open class BaseViewModel : ViewModel() {


    private val _viewEvents = MutableLiveData<ViewEvent>()
    val viewEvents: LiveData<ViewEvent> get() = _viewEvents

    private val uiScope = viewModelScope

    fun launch(block: suspend CoroutineScope.() -> Unit) {
        uiScope.launch {
            block()
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    internal fun initEvent() {
        _viewEvents.value = null
    }

    fun <Event : ViewEvent> Event.publish() {
        LogUtils.d("ext","publish---------->$this")
        _viewEvents.value = this
    }

    fun Int.publish() {
        _viewEvents.value = SimpleViewEvent(this)
    }

}