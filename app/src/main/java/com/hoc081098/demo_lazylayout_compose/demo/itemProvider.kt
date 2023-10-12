package com.hoc081098.demo_lazylayout_compose.demo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable

@OptIn(ExperimentalFoundationApi::class)
internal fun itemProvider(
  key: ((index: Int) -> Any)? = null,
  itemCount: () -> Int,
  itemContent: @Composable (index: Int) -> Unit,
): LazyLayoutItemProvider = object : LazyLayoutItemProvider {
  @Composable
  override fun Item(index: Int, key: Any) = itemContent(index)
  override val itemCount: Int get() = itemCount()
  override fun getKey(index: Int): Any = key?.invoke(index) ?: super.getKey(index)
}