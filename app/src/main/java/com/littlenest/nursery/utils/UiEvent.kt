package com.littlenest.nursery.utils

open class UiEvent<out T>(private val content: T) {
    private var handled = false

    fun getContentIfNotHandled(): T? {
        return if (handled) null else {
            handled = true
            content
        }
    }
}