package com.hoc081098.demo_lazylayout_compose.demo

import androidx.annotation.Px
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import com.hoc081098.demo_lazylayout_compose.TimetableEventList
import com.hoc081098.demo_lazylayout_compose.fastFilter
import com.hoc081098.flowext.ThrottleConfiguration
import com.hoc081098.flowext.throttleTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Duration

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
  @Stable
  internal val totalHeightPx: Int

  /**
   * The total width of the timetable in pixels.
   */
  @Px
  @Stable
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
    val dayIndices = timetableEventList.days.associateWithIndex()

    // Avoid looping many times.
    var maxRightPx = -1
    var maxBottomPx = -1

    val timetableEventItemLayoutInfos = timetableEventList
      .items
      .mapIndexedTo(ArrayList()) { index, item ->
        TimetableEventItemLayoutInfo(
          item = item,
          index = index,
          dayIndex = dayIndices[item.day]!!,
          columnWidthPx = columnWidthPx,
          perMinuteHeightPx = perMinuteHeightPx,
        ).also {
          maxRightPx = maxOf(maxRightPx, it.rightPx)
          maxBottomPx = maxOf(maxBottomPx, it.bottomPx)
        }
      }

    totalWidthPx = maxRightPx
    totalHeightPx = maxBottomPx

    visibleTimetableEventItemLayoutInfos = buildVisibleTimetableEventItemLayoutInfos(timetableEventItemLayoutInfos)

    timelineHorizontalLines = buildTimelineHorizontalLines(perMinuteHeightPx)
    dayVerticalLines = derivedStateOf {
      val offsetX = scrollStates.offsetX
      List(timetableEventList.days.size) { columnWidthPx * it - offsetX }
    }
  }

  //region Private
  private fun buildTimelineHorizontalLines(perMinuteHeightPx: Int): State<List<Float>> {
    val perHourHeightPx = perMinuteHeightPx * Duration.ofHours(1).toMinutes()
    val hourRange = 1..<Duration.ofDays(1).toHours().toInt()

    return derivedStateOf {
      val offsetY = scrollStates.offsetY
      hourRange.map { perHourHeightPx * it - offsetY }
    }
  }

  private fun buildVisibleTimetableEventItemLayoutInfos(timetableEventItemLayoutInfos: ArrayList<TimetableEventItemLayoutInfo>): MutableState<List<TimetableEventItemLayoutInfo>> {
    val r = mutableStateOf(emptyList<TimetableEventItemLayoutInfo>())

    val throttleDispatcher = Dispatchers.IO.limitedParallelism(2)

    combine(
      snapshotFlow { screenSizeState.value },
      snapshotFlow { scrollStates.offsetX }
        .conflate()
        .flowOn(Dispatchers.Main.immediate)
        .throttleTime(200, ThrottleConfiguration.LEADING)
        .flowOn(throttleDispatcher),
      snapshotFlow { scrollStates.offsetY }
        .conflate()
        .flowOn(Dispatchers.Main.immediate)
        .throttleTime(200, ThrottleConfiguration.LEADING)
        .flowOn(throttleDispatcher),
    ) { size, offsetX,offsetY ->
      println("size=$size, offsetX=$offsetX, offsetY=$offsetY")

      // The visible items are the items that are inside the screen.
      // It will be calculated when any of the following changes:
      // - [offsetX]
      // - [offsetY]
      // - [screenSizeState]

      if (size == IntSize.Zero) {
        return@combine emptyList()
      }

      val screenRightX = offsetX + size.width
      val screenBottomY = offsetY + size.height

      // filter items that are visible (or partially visible) in the screen
      timetableEventItemLayoutInfos.fastFilter {
        (
            /** x is inside the screen */
            it.leftPx.toFloat() in offsetX..screenRightX ||
                it.rightPx.toFloat() in offsetX..screenRightX
            )
            && (
            /** y is inside the screen */
            it.topPx.toFloat() in offsetY..screenBottomY ||
                it.bottomPx.toFloat() in offsetY..screenBottomY
            )
      }
    }
      .onEach {
        r.value = it
      }
      .launchIn(CoroutineScope(Dispatchers.Main.immediate))

    return r
  }
  //endregion

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

  internal suspend fun scroll(change: PointerInputChange, dragAmount: Offset) = scrollStates.scroll(change, dragAmount)

  internal suspend fun fling() = scrollStates.fling()

  internal fun resetScrollTracking() = scrollStates.resetScrollTracking()
}

@Suppress("NOTHING_TO_INLINE")
private inline fun <T> List<T>.associateWithIndex(): Map<T, Int> =
  withIndex()
    .associateTo(hashMapOf()) { it.value to it.index }
