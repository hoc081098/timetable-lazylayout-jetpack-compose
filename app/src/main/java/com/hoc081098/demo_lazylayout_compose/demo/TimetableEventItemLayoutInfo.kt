package com.hoc081098.demo_lazylayout_compose.demo

import androidx.annotation.Px
import androidx.compose.runtime.Stable
import androidx.compose.ui.layout.Placeable
import com.hoc081098.demo_lazylayout_compose.TimetableEventItem
import com.hoc081098.demo_lazylayout_compose.TimetableEventList
import java.time.temporal.ChronoUnit
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Contains [Placeable] info and [TimetableEventItemLayoutInfo] for each [TimetableEventItem].
 */
internal data class PlaceableInfo(
  val placeable: Placeable,
  val layoutInfo: TimetableEventItemLayoutInfo,
)

/**
 * Contains layout info for each [TimetableEventItem].
 */
@Stable
internal data class TimetableEventItemLayoutInfo(
  /**
   * The [TimetableEventItem] to be laid out.
   */
  internal val item: TimetableEventItem,
  /**
   * The index of [item] in [TimetableEventList.items].
   */
  internal val index: Int,
  /**
   * The height of each minute in pixels.
   */
  @Px private val perMinuteHeightPx: Int,
  /**
   * The index of the day of the [item] in [TimetableEventList.days].
   */
  private val dayIndex: Int,
  /**
   * The width in px for each column.
   */
  @Px
  private val columnWidthPx: Int,
) {
  @get:Px
  val heightPx by lazy(NONE) {
    ChronoUnit.MINUTES
      .between(item.startsAt, item.endsAt)
      .toInt() * perMinuteHeightPx
  }

  @get:Px
  val widthPx get() = columnWidthPx

  @Px
  val leftPx = dayIndex * widthPx

  @get:Px
  val topPx by lazy(NONE) {
    ChronoUnit.MINUTES
      .between(item.day.start, item.startsAt)
      .toInt() * perMinuteHeightPx
  }

  @get:Px
  val rightPx get() = leftPx + widthPx

  @get:Px
  val bottomPx get() = topPx + heightPx
}
