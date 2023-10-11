package com.hoc081098.demo_lazylayout_compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID
import kotlin.random.Random

@Immutable
data class TimetableEventItem(
  val id: Id,
  val title: String,
  val startsAt: Instant,
  val endsAt: Instant,
  val day: EventDay,
) {
  @Immutable
  @JvmInline
  value class Id(val value: String)
}

@Immutable
data class EventDay(
  val start: Instant,
  val end: Instant
) {
  fun asLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(start, CurrentZoneOffset)
}

@Immutable
data class TimetableEventList(
  val items: ImmutableList<TimetableEventItem>,
  val days: ImmutableList<EventDay>,
)

@Stable
val CurrentZoneOffset: ZoneOffset by lazy {
  OffsetDateTime.now().offset!!
}

@Stable
val DateFormatter by lazy {
  DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
}

fun getTimetableEventItemsList(): TimetableEventList {
  val days = (
      (1..29)
        .map { d ->
          EventDay(
            start = LocalDateTime
              .parse("2023-09-${d.toString().padStart(2, '0')}T00:00:00")
              .toInstant(CurrentZoneOffset),
            end = LocalDateTime
              .parse("2023-09-${(d + 1).toString().padStart(2, '0')}T00:00:00")
              .toInstant(CurrentZoneOffset),
          )
        } + (1..30)
        .map { d ->
          EventDay(
            start = LocalDateTime
              .parse("2023-10-${d.toString().padStart(2, '0')}T00:00:00")
              .toInstant(CurrentZoneOffset),
            end = LocalDateTime
              .parse("2023-10-${(d + 1).toString().padStart(2, '0')}T00:00:00")
              .toInstant(CurrentZoneOffset),
          )
        } + (1..29)
        .map { d ->
          EventDay(
            start = LocalDateTime
              .parse("2023-11-${d.toString().padStart(2, '0')}T00:00:00")
              .toInstant(CurrentZoneOffset),
            end = LocalDateTime
              .parse("2023-11-${(d + 1).toString().padStart(2, '0')}T00:00:00")
              .toInstant(CurrentZoneOffset),
          )
        }
      )
    .toImmutableList()


  val items = days
    .flatMap { day ->
      val formattedDay = DateFormatter.format(day.asLocalDateTime())
      var lastEnd = null as Instant?

      List(24) { index ->
        val delay: Duration = Duration.ofMinutes(Random.nextLong(10, 60))
        val startsAt: Instant = lastEnd?.plus(delay) ?: (day.start + delay)
        val endsAt: Instant = (startsAt + Duration.ofMinutes(Random.nextLong(30, 60)))
          .also { lastEnd = it }

        if (startsAt.isAfter(day.end) || endsAt.isAfter(day.end)) {
          return@List null
        }

        TimetableEventItem(
          id = TimetableEventItem.Id(value = UUID.randomUUID().toString()),
          title = "[Day ${formattedDay}] Event #$index",
          startsAt = startsAt,
          endsAt = endsAt,
          day = day,
        )
      }.filterNotNull()
    }
    .toImmutableList()

  return TimetableEventList(
    items = items,
    days = days,
  )
}