package com.hoc081098.demo_lazylayout_compose

import androidx.compose.ui.util.fastForEach
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Returns a list containing the results of applying the given [transform] function
 * to each element in the original collection.
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastFilter(predicate: (T) -> Boolean): List<T> {
  contract { callsInPlace(predicate) }
  val target = ArrayList<T>()
  fastForEach {
    if (predicate(it)) target += it
  }
  return target
}