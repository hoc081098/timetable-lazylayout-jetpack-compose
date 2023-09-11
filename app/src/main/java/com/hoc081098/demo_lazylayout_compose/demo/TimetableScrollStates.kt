package com.hoc081098.demo_lazylayout_compose.demo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.splineBasedDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun rememberTimetableScrollStates(): TimetableScrollStates =
  rememberSaveable(saver = TimetableScrollStates.Saver) {
    TimetableScrollStates()
  }

@Stable
internal class TimetableScrollStates(
  initialOffsetX: Float = 0f,
  initialOffsetY: Float = 0f,
) {
  // This `Animatable` stores the horizontal offset for the element.
  private val offsetXState = Animatable(initialOffsetX)

  // This `Animatable` stores the vertical offset for the element.
  private val offsetYState = Animatable(initialOffsetY)

  internal val offsetX get() = offsetXState.value
  internal val offsetY get() = offsetYState.value

  // Prepare for drag events and record velocity of a fling.
  private val velocityTracker = VelocityTracker()
  private val decay = exponentialDecay<Float>()

  internal fun updateBounds(maxX: Float, maxY: Float) {
    offsetXState.updateBounds(
      lowerBound = 0f,
      upperBound = maxX
    )
    offsetYState.updateBounds(
      lowerBound = 0f,
      upperBound = maxY
    )
  }

  internal suspend fun scroll(change: PointerInputChange, dragAmount: Offset) {
    // Record the position after offset

    val horizontalDragOffset = offsetXState.value + dragAmount.x
    val maxX = offsetXState.upperBound
    val nextX = horizontalDragOffset.coerceIn(0f, maxX)

    val verticalDragOffset = offsetYState.value + dragAmount.y
    val maxY = offsetYState.upperBound
    val nextY = verticalDragOffset.coerceIn(0f, maxY)

    // Record the velocity of the drag.
    velocityTracker.addPosition(
      timeMillis = change.uptimeMillis,
      position = -change.position
    )

    // Overwrite the `Animatable` value while the element is dragged.
    coroutineScope {
      launch { offsetXState.snapTo(nextX) }
      launch { offsetYState.snapTo(nextY) }
    }
  }

  internal suspend fun fling() {
    // Dragging finished. Calculate the velocity of the fling.
    val velocity = velocityTracker.calculateVelocity()
    println("$this fling velocity=$velocity")

    coroutineScope {
      launch {
        offsetXState.animateDecay(
          initialVelocity = velocity.x,
          animationSpec = decay,
        )
      }

      launch {
        offsetYState.animateDecay(
          initialVelocity = velocity.y,
          animationSpec = decay,
        )
      }
    }
  }

  internal fun resetScrollTracking() {
    println("$this resetScrollTracking")
    velocityTracker.resetTracking()
  }

  companion object {
    val Saver: Saver<TimetableScrollStates, Any> = listSaver(
      save = { arrayListOf(it.offsetX, it.offsetY) },
      restore = {
        TimetableScrollStates(
          initialOffsetX = it[0],
          initialOffsetY = it[1],
        )
      }
    )
  }
}