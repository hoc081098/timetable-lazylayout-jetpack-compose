package com.hoc081098.demo_lazylayout_compose.demo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import com.hoc081098.demo_lazylayout_compose.DateFormatter
import com.hoc081098.demo_lazylayout_compose.EventDay
import com.hoc081098.demo_lazylayout_compose.TimetableEventItem
import com.hoc081098.demo_lazylayout_compose.TimetableEventList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

object TimetableDefaults {
  @Stable
  val ColumnWidth = 200.dp

  @Stable
  val PerMinuteHeight = 4.dp

  @Stable
  val TimelineStrokeWidth = 1.dp

  @Stable
  val DaysRowHeight = 64.dp

  @Stable
  val HoursColumnWidth = 64.dp

  @ReadOnlyComposable
  @Composable
  fun headerBackgroundColor(): Color = MaterialTheme.colorScheme.surface

  @ReadOnlyComposable
  @Composable
  fun backgroundColor(): Color = MaterialTheme.colorScheme.surfaceVariant
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Timetable(
  timetableEventList: TimetableEventList,
  modifier: Modifier = Modifier,
  //
  perMinuteHeight: Dp = TimetableDefaults.PerMinuteHeight,
  columnWidth: Dp = TimetableDefaults.ColumnWidth,
  //
  hoursColumnWidth: Dp = TimetableDefaults.HoursColumnWidth,
  daysRowHeight: Dp = TimetableDefaults.DaysRowHeight,
  //
  headerBackgroundColor: Color = TimetableDefaults.headerBackgroundColor(),
  backgroundColor: Color = TimetableDefaults.backgroundColor(),
  //
  lineColor: Color = contentColorFor(backgroundColor),
  lineStrokeWidth: Dp = TimetableDefaults.TimelineStrokeWidth,
  content: @Composable (index: Int, item: TimetableEventItem) -> Unit,
) {
  val items = timetableEventList.items

  val itemProvider = remember(items) {
    itemProvider(
      itemCount = { items.size },
      itemContent = { content(it, items[it]) },
      key = { items[it].id.value }
    )
  }

  val density = LocalDensity.current
  val scope = rememberCoroutineScope()

  val scrollStates = rememberTimetableScrollStates()
  val screenState = remember(
    timetableEventList,
    density,
    scrollStates,
    columnWidth,
    perMinuteHeight
  ) {
    TimetableScreenState(
      timetableEventList = timetableEventList,
      density = density,
      scrollStates = scrollStates,
      columnWidth = columnWidth,
      perMinuteHeight = perMinuteHeight,
    )
  }

  val timelineStrokeWidthPx = density.run { lineStrokeWidth.toPx() }

  val flingBehavior = ScrollableDefaults.flingBehavior()

  val hoursScrollState = rememberScrollState()
  LaunchedEffect(screenState.offsetY) {
    hoursScrollState.scrollTo(screenState.offsetY.toInt())
  }

  val daysScrollState = rememberScrollState()
  LaunchedEffect(screenState.offsetX) {
    daysScrollState.scrollTo(screenState.offsetX.toInt())
  }

  Row(
    modifier = modifier
      .fillMaxSize(),
  ) {
    Column(
      modifier = Modifier.width(hoursColumnWidth)
    ) {
      Spacer(
        modifier = Modifier
          .height(daysRowHeight)
          .fillMaxWidth()
          .background(headerBackgroundColor)
      )

      HoursColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .background(headerBackgroundColor),
        perMinuteHeight = perMinuteHeight,
        state = hoursScrollState,
        flingBehavior = flingBehavior,
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      DaysRow(
        modifier = Modifier
          .height(daysRowHeight)
          .fillMaxWidth()
          .background(headerBackgroundColor),
        scrollState = daysScrollState,
        flingBehavior = flingBehavior,
        days = timetableEventList.days,
        columnWidth = columnWidth,
      )

      LazyLayout(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .focusGroup()
          .clipToBounds()
          .background(backgroundColor)
          .drawBehind {
            screenState.timelineHorizontalLines.value.fastForEach { y ->
              drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(screenState.totalWidthPx.toFloat(), y),
                strokeWidth = timelineStrokeWidthPx,
              )
            }
            screenState.dayVerticalLines.value.fastForEach { x ->
              drawLine(
                color = lineColor,
                start = Offset(x, 0f),
                end = Offset(x, screenState.totalHeightPx.toFloat()),
                strokeWidth = timelineStrokeWidthPx,
              )
            }
          }
          .pointerInput(Unit) {
            detectDragGestures(
              onDrag = { change, dragAmount ->
                if (change.positionChanged()) {
                  // Consume the gesture event, not passed to external
                  change.consume()
                }

                scope.launch {
                  screenState.scroll(change, -dragAmount)
                }
              },
              onDragEnd = {
                scope.launch {
                  screenState.fling()
                }
              },
              onDragCancel = {
                screenState.resetScrollTracking()
              }
            )
          },
        itemProvider = itemProvider,
      ) { constraints ->
        screenState.updateScreenConstraints(constraints)

        // measure visible items
        val placeableInfos = screenState.visibleTimetableEventItemLayoutInfos.value.fastMap { layoutInfo ->
          PlaceableInfo(
            placeable = measure(
              index = layoutInfo.index,
              constraints = Constraints.fixed(
                width = layoutInfo.widthPx,
                height = layoutInfo.heightPx
              )
            )[0],
            layoutInfo = layoutInfo,
          )
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
          // place
          placeableInfos.fastForEach { (placeable, layoutInfo) ->
            placeable.place(
              x = (layoutInfo.leftPx - screenState.offsetX).toInt(),
              y = (layoutInfo.topPx - screenState.offsetY).toInt(),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DaysRow(
  scrollState: ScrollState,
  flingBehavior: FlingBehavior,
  columnWidth: Dp,
  days: ImmutableList<EventDay>,
  modifier: Modifier = Modifier,
) {
  val dayWidth = remember(columnWidth) { columnWidth * 0.3f }

  Row(
    modifier = modifier
      .horizontalScroll(
        state = scrollState,
        enabled = false,
        flingBehavior = flingBehavior
      ),
  ) {
    days.forEachIndexed { index, day ->
      if (index == 0) {
        Spacer(modifier = Modifier.width(columnWidth / 2 - dayWidth / 2))
      }

      Box(
        modifier = Modifier
          .fillMaxHeight()
          .width(dayWidth),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = DateFormatter.format(day.asLocalDateTime()),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.titleMedium,
        )
      }

      Spacer(modifier = Modifier.width(columnWidth - dayWidth))
    }
  }
}

@Composable
private fun HoursColumn(
  perMinuteHeight: Dp,
  state: ScrollState,
  flingBehavior: FlingBehavior,
  modifier: Modifier = Modifier,
) {
  val perHourHeight = remember(perMinuteHeight) { perMinuteHeight * 60 }
  val hourHeight = remember(perHourHeight) { perHourHeight * 0.3f }

  Column(
    modifier = modifier
      .verticalScroll(
        state = state,
        enabled = false,
        flingBehavior = flingBehavior,
      ),
  ) {
    (1..23).forEach { hour ->
      if (hour == 1) {
        Spacer(modifier = Modifier.height(perHourHeight - hourHeight / 2))
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(hourHeight),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "${hour.toString().padStart(2, '0')}:00",
          style = MaterialTheme.typography.titleMedium,
        )
      }

      Spacer(modifier = Modifier.height(perHourHeight - hourHeight))
    }
  }
}
