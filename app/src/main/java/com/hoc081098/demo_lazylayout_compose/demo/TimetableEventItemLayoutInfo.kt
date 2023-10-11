package com.hoc081098.demo_lazylayout_compose.demo

import androidx.annotation.Px
import androidx.compose.runtime.Stable
import androidx.compose.ui.layout.Placeable
import com.hoc081098.demo_lazylayout_compose.TimetableEventItem
import com.hoc081098.demo_lazylayout_compose.TimetableEventList
import java.time.temporal.ChronoUnit

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
  private var cachedHeightPx: Int = -1
  private var cachedTopPx: Int = -1

  @get:Px
  val heightPx: Int
    get() = if (cachedHeightPx == -1) {
      (ChronoUnit.MINUTES
        .between(item.startsAt, item.endsAt)
        .toInt() * perMinuteHeightPx)
        .also { cachedHeightPx = it }
    } else {
      cachedHeightPx
    }

  @get:Px
  inline val widthPx get() = columnWidthPx

  @Px
  val leftPx = dayIndex * widthPx

  @get:Px
  val topPx: Int
    get() = if (cachedTopPx == -1) {
      (ChronoUnit.MINUTES
        .between(item.day.start, item.startsAt)
        .toInt() * perMinuteHeightPx)
        .also { cachedTopPx = it }
    } else {
      cachedTopPx
    }

  @get:Px
  inline val rightPx get() = leftPx + widthPx

  @get:Px
  inline val bottomPx get() = topPx + heightPx
}
