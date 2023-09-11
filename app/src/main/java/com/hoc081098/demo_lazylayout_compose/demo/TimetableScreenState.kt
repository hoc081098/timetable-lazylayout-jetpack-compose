package com.hoc081098.demo_lazylayout_compose.demo

import androidx.annotation.Px
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import com.hoc081098.demo_lazylayout_compose.TimetableEventList
import java.time.Duration

@OptIn(ExperimentalStdlibApi::class)
@Stable
internal class TimetableScreenState(
  private val timetableEventList: TimetableEventList,
  private val scrollStates: TimetableScrollStates,
  density: Density,
  private val columnWidth: Dp,
  private val perMinuteHeight: Dp,
) {
  private val screenSizeState = mutableStateOf(IntSize.Zero)

  /**
   * The total height of the timetable in pixels.
   */
  @Px
  internal val totalHeightPx: Int

  /**
   * The total width of the timetable in pixels.
   */
  @Px
  internal val totalWidthPx: Int

  /**
   * The visible items are the items that are inside the screen bounds.
   */
  internal val visibleTimetableEventItemLayoutInfos: State<List<TimetableEventItemLayoutInfo>>

  /**
   * List of y offset of horizontal lines in the timetable.
   */
  internal val timelineHorizontalLines: State<List<Float>>

  /**
   * List of x offset of vertical lines in the timetable.
   */
  internal val dayVerticalLines: State<List<Float>>

  internal val offsetX get() = scrollStates.offsetX
  internal val offsetY get() = scrollStates.offsetY

  init {
    val columnWidthPx = density.run { columnWidth.roundToPx() }
    val perMinuteHeightPx = density.run { perMinuteHeight.roundToPx() }

    val timetableEventItemLayoutInfos = timetableEventList
      .items
      .mapIndexed { index, item ->
        TimetableEventItemLayoutInfo(
          item = item,
          index = index,
          dayIndex = timetableEventList.days.indexOf(item.day),
          columnWidthPx = columnWidthPx,
          perMinuteHeightPx = perMinuteHeightPx,
        )
      }

    totalWidthPx = timetableEventItemLayoutInfos.maxOf { it.rightPx }
    totalHeightPx = timetableEventItemLayoutInfos.maxOf { it.bottomPx }

    visibleTimetableEventItemLayoutInfos = derivedStateOf {
      val size = screenSizeState.value
      val offsetX = scrollStates.offsetX
      val offsetY = scrollStates.offsetY

      if (size == IntSize.Zero) {
        return@derivedStateOf emptyList()
      }

      // The visible items are the items that are inside the screen.
      // It will be calculated when any of the following changes:
      // - [scrollX]
      // - [scrollY]
      // - [size]
      timetableEventItemLayoutInfos.filter {
        it.isVisible(
          offsetX = offsetX,
          offsetY = offsetY,
          screenWidth = size.width,
          screenHeight = size.height,
        )
      }
    }

    val perHourHeightPx = perMinuteHeightPx * Duration.ofHours(1).toMinutes()
    val hourRange = 1..<Duration.ofDays(1).toHours().toInt()
    timelineHorizontalLines = derivedStateOf {
      val offsetY = scrollStates.offsetY
      hourRange.map { perHourHeightPx * it - offsetY }
    }

    dayVerticalLines = derivedStateOf {
      val offsetX = scrollStates.offsetX
      List(timetableEventList.days.size) { columnWidthPx * it - offsetX }
    }
  }

  internal fun updateScreenConstraints(constraints: Constraints) {
    val size = IntSize(width = constraints.maxWidth, height = constraints.maxHeight)

    screenSizeState.value = size
    scrollStates.updateBounds(
      maxX = if (size.width > totalWidthPx) {
        0f
      } else {
        (totalWidthPx - size.width).toFloat()
      },
      maxY = if (size.height > totalHeightPx) {
        0f
      } else {
        (totalHeightPx - size.height).toFloat()
      },
    )
  }

  internal suspend fun scroll(change: PointerInputChange, dragAmount: Offset) {
    scrollStates.scroll(change, dragAmount)
  }

  internal suspend fun fling() {
    scrollStates.fling()
  }

  internal fun resetScrollTracking() {
    scrollStates.resetScrollTracking()
  }
}

private fun TimetableEventItemLayoutInfo.isVisible(
  @Px offsetX: Float,
  @Px offsetY: Float,
  @Px screenWidth: Int,
  @Px screenHeight: Int,
): Boolean {
  val screenX = offsetX..(offsetX + screenWidth)
  val screenY = offsetY..(offsetY + screenHeight)

  val xInside = leftPx.toFloat() in screenX ||
      rightPx.toFloat() in screenX
  val yInside = topPx.toFloat() in screenY ||
      bottomPx.toFloat() in screenY

  return xInside && yInside
}