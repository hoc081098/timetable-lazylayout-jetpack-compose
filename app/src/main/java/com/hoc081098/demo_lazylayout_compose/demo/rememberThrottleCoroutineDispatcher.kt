package com.hoc081098.demo_lazylayout_compose.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

@Composable
internal fun rememberThrottleCoroutineDispatcher(): CoroutineDispatcher = remember {
  object : RememberObserver {
    val dispatcher = Executors.newScheduledThreadPool(
      /* corePoolSize = */ 2,
      /* threadFactory = */ object : ThreadFactory {
        val count = AtomicInteger()
        override fun newThread(r: Runnable?) = Thread(
          /* target = */ r,
          /* name = */ "TimetableScreenState-Thread-${count.getAndIncrement()}"
        )
      }
    ).asCoroutineDispatcher()

    override fun onAbandoned() = dispatcher.close()

    override fun onForgotten() = dispatcher.close()

    override fun onRemembered() {
      // Do nothing
    }
  }.dispatcher
}